package vn.edu.tdtu.tdtbank.ui.officer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.databinding.FragmentOpenAccountBinding;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.AccountType;
import vn.edu.tdtu.tdtbank.model.PaymentFrequency;
import vn.edu.tdtu.tdtbank.model.SavingRatePolicy;
import vn.edu.tdtu.tdtbank.model.User;
import vn.edu.tdtu.tdtbank.ui.common.DropdownField;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.utils.Args;
import vn.edu.tdtu.tdtbank.utils.MoneyFormat;
import vn.edu.tdtu.tdtbank.viewmodel.OfficerViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Mở sổ tiết kiệm (lãi suất lấy từ chính sách) hoặc khoản vay thế chấp cho khách hàng. */
public class OpenAccountFragment extends Fragment {

    private FragmentOpenAccountBinding binding;
    private OfficerViewModel vm;
    private User customer;
    private DropdownField<AccountType> typeField;
    private DropdownField<SavingRatePolicy> termField;
    private DropdownField<PaymentFrequency> freqField;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentOpenAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        String uid = requireArguments().getString(Args.CUSTOMER_UID);
        vm = new ViewModelProvider(requireActivity()).get(OfficerViewModel.class);

        typeField = new DropdownField<>(binding.actType);
        termField = new DropdownField<>(binding.actTerm);
        freqField = new DropdownField<>(binding.actFrequency);
        typeField.setItems(Arrays.asList(AccountType.SAVING, AccountType.MORTGAGE), t -> getString(
                t == AccountType.SAVING ? R.string.type_saving : R.string.type_mortgage));
        freqField.setItems(Arrays.asList(PaymentFrequency.MONTHLY, PaymentFrequency.BIWEEKLY), f -> getString(
                f == PaymentFrequency.MONTHLY ? R.string.freq_monthly : R.string.freq_biweekly));
        binding.actType.setOnItemClickListener((p, v, pos, id) -> updateVisibility());
        updateVisibility();

        vm.customer(uid).observe(getViewLifecycleOwner(), r -> {
            if (r.data != null) {
                customer = r.data;
                binding.tvCustomer.setText(getString(R.string.open_for_customer, customer.fullName));
            }
        });
        vm.policies().observe(getViewLifecycleOwner(), r -> {
            if (r.data != null) {
                List<SavingRatePolicy> list = new ArrayList<>(r.data);
                termField.setItems(list, p -> getString(R.string.term_rate_fmt, p.termMonths,
                        MoneyFormat.percent(p.annualRate)));
                binding.tvNoPolicy.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
        UiUtils.observeOperation(this, vm, () -> {
            if (isAdded()) {
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
        binding.btnOpen.setOnClickListener(v -> submit());
    }

    private boolean isSaving() {
        AccountType t = typeField.selected();
        return t == null || t == AccountType.SAVING;
    }

    private void updateVisibility() {
        boolean saving = isSaving();
        binding.tilTerm.setVisibility(saving ? View.VISIBLE : View.GONE);
        binding.tvNoPolicy.setVisibility(View.GONE);
        binding.groupMortgage.setVisibility(saving ? View.GONE : View.VISIBLE);
    }

    private void submit() {
        if (customer == null) {
            return;
        }
        Account a = new Account();
        a.ownerUid = customer.uid;
        a.ownerName = customer.fullName;
        if (isSaving()) {
            SavingRatePolicy p = termField.selected();
            binding.tilTerm.setError(p == null ? getString(R.string.err_no_policy) : null);
            if (p == null) {
                return;
            }
            a.type = AccountType.SAVING;
            a.termMonths = p.termMonths;
            a.interestRate = p.annualRate;
            a.balance = 0;
        } else {
            long principal = MoneyFormat.parse(UiUtils.text(binding.tilPrincipal));
            String rateText = UiUtils.text(binding.tilRate).replace(',', '.');
            int term = (int) MoneyFormat.parse(UiUtils.text(binding.tilTermMonths));
            double rate;
            try {
                rate = Double.parseDouble(rateText);
            } catch (NumberFormatException e) {
                rate = -1;
            }
            binding.tilPrincipal.setError(principal >= 1_000_000 ? null : getString(R.string.err_principal));
            binding.tilRate.setError(rate >= 0 && rate <= 30 ? null : getString(R.string.err_rate));
            binding.tilTermMonths.setError(term >= 1 && term <= 360 ? null : getString(R.string.err_term));
            if (principal < 1_000_000 || rate < 0 || rate > 30 || term < 1 || term > 360) {
                return;
            }
            PaymentFrequency f = freqField.selected();
            a.type = AccountType.MORTGAGE;
            a.principal = principal;
            a.balance = principal;
            a.interestRate = rate;
            a.termMonths = term;
            a.paymentFrequency = f == null ? PaymentFrequency.MONTHLY : f;
        }
        binding.btnOpen.setEnabled(false);
        vm.openAccount(a);
        binding.btnOpen.postDelayed(() -> {
            if (binding != null) {
                binding.btnOpen.setEnabled(true);
            }
        }, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
