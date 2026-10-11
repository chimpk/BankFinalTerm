package vn.edu.tdtu.tdtbank.ui.customer;

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
import vn.edu.tdtu.tdtbank.adapter.AccountAdapter;
import vn.edu.tdtu.tdtbank.adapter.UtilityAdapter;
import vn.edu.tdtu.tdtbank.data.remote.UtilityCatalog;
import vn.edu.tdtu.tdtbank.databinding.FragmentUtilityPayBinding;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.AccountStatus;
import vn.edu.tdtu.tdtbank.model.AccountType;
import vn.edu.tdtu.tdtbank.model.BankTransaction;
import vn.edu.tdtu.tdtbank.model.TxDraft;
import vn.edu.tdtu.tdtbank.model.TxType;
import vn.edu.tdtu.tdtbank.model.UtilityCategory;
import vn.edu.tdtu.tdtbank.ui.common.DropdownField;
import vn.edu.tdtu.tdtbank.ui.common.TransactionFlow;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.utils.Args;
import vn.edu.tdtu.tdtbank.utils.Constants;
import vn.edu.tdtu.tdtbank.utils.MoneyFormat;
import vn.edu.tdtu.tdtbank.utils.ValidationUtils;
import vn.edu.tdtu.tdtbank.viewmodel.AccountViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Thanh toán tiện ích. Hai nhóm: (a) hóa đơn điện/nước và nạp điện thoại - người dùng nhập mã/số và số tiền;
 * (b) vé máy bay, vé phim, khách sạn, đơn hàng TMĐT - chọn từ danh mục MẪU (không có đối tác thật).
 */
public class UtilityPayFragment extends Fragment {

    private FragmentUtilityPayBinding binding;
    private TransactionFlow flow;
    private UtilityCategory category;
    private UtilityCatalog catalog;
    private DropdownField<Account> accountField;
    private DropdownField<String> providerField;
    private DropdownField<UtilityCatalog.Item> itemField;
    private DropdownField<Long> topupField;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentUtilityPayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        category = UtilityCategory.valueOf(requireArguments().getString(Args.UTILITY_CATEGORY,
                UtilityCategory.ELECTRICITY.name()));
        flow = new TransactionFlow(this);
        binding.tvHeading.setText(UtilityAdapter.labelRes(category));
        binding.ivIcon.setImageResource(UtilityAdapter.iconRes(category));

        try {
            catalog = UtilityCatalog.load(requireContext());
        } catch (Exception e) {
            UiUtils.snack(binding.getRoot(), getString(R.string.err_catalog));
            binding.btnPay.setEnabled(false);
            return;
        }

        accountField = new DropdownField<>(binding.actAccount);
        providerField = new DropdownField<>(binding.actProvider);
        itemField = new DropdownField<>(binding.actItem);
        topupField = new DropdownField<>(binding.actTopup);
        setupFormForCategory();

        new ViewModelProvider(this).get(AccountViewModel.class).accounts().observe(getViewLifecycleOwner(), r -> {
            if (r.data == null) {
                return;
            }
            List<Account> usable = new ArrayList<>();
            for (Account a : r.data) {
                if (a.type == AccountType.CHECKING && a.status == AccountStatus.ACTIVE) {
                    usable.add(a);
                }
            }
            accountField.setItems(usable, a -> getString(R.string.account_pick_label,
                    getString(AccountAdapter.typeLabel(a.type)), a.accountNumber, MoneyFormat.vnd(a.balance)));
        });
        binding.btnPay.setOnClickListener(v -> pay());
    }

    private boolean isBill() {
        return category == UtilityCategory.ELECTRICITY || category == UtilityCategory.WATER;
    }

    private boolean isTopup() {
        return category == UtilityCategory.PHONE_TOPUP;
    }

    private void setupFormForCategory() {
        boolean provider = isBill() || isTopup();
        binding.tilProvider.setVisibility(provider ? View.VISIBLE : View.GONE);
        binding.tilReference.setVisibility(provider ? View.VISIBLE : View.GONE);
        binding.tilAmount.setVisibility(isBill() ? View.VISIBLE : View.GONE);
        binding.tilTopup.setVisibility(isTopup() ? View.VISIBLE : View.GONE);
        binding.tilItem.setVisibility(provider ? View.GONE : View.VISIBLE);
        binding.tilNote.setVisibility(provider ? View.GONE : View.VISIBLE);

        if (provider) {
            providerField.setItems(catalog.providers(category), s -> s);
            binding.tilReference.setHint(isTopup() ? R.string.hint_phone_number : R.string.hint_customer_code);
            if (isTopup()) {
                topupField.setItems(catalog.topupAmounts(), MoneyFormat::vnd);
            }
        } else {
            itemField.setItems(catalog.items(category), i -> i.name + " • " + MoneyFormat.vnd(i.price));
        }
        binding.tvSampleNote.setVisibility(View.VISIBLE);
    }

    private void pay() {
        Account account = accountField.selected();
        binding.tilAccount.setError(account == null ? getString(R.string.err_no_checking) : null);
        if (account == null) {
            return;
        }
        TxDraft d = new TxDraft();
        d.type = TxType.UTILITY_PAYMENT;
        d.from = account;
        d.category = category;
        d.toAccountNumber = Constants.UTILITY;
        String categoryName = getString(UtilityAdapter.labelRes(category));

        if (isBill() || isTopup()) {
            String provider = providerField.selected();
            String reference = UiUtils.text(binding.tilReference);
            boolean refOk = isTopup() ? ValidationUtils.isValidPhone(reference) : reference.length() >= 4;
            binding.tilReference.setError(refOk ? null : getString(
                    isTopup() ? R.string.err_phone : R.string.err_customer_code));
            long amount = isTopup() ? (topupField.selected() == null ? -1 : topupField.selected())
                    : MoneyFormat.parse(UiUtils.text(binding.tilAmount));
            if (!isTopup()) {
                binding.tilAmount.setError(amount > 0 ? null : getString(R.string.err_required));
            }
            if (!refOk || provider == null || amount <= 0) {
                return;
            }
            d.amount = amount;
            d.reference = reference;
            d.counterpartyName = provider;
            d.description = categoryName + " • " + provider + " • " + reference;
        } else {
            UtilityCatalog.Item item = itemField.selected();
            if (item == null) {
                binding.tilItem.setError(getString(R.string.err_required));
                return;
            }
            binding.tilItem.setError(null);
            d.amount = item.price;
            d.counterpartyName = item.name;
            String note = UiUtils.text(binding.tilNote);
            d.reference = note.isEmpty() ? null : note;
            d.description = categoryName + " • " + item.name;
        }

        flow.start(d, new TransactionFlow.Listener() {
            @Override
            public void onProgress(boolean inProgress) {
                if (binding != null) {
                    binding.btnPay.setEnabled(!inProgress);
                    binding.progress.setVisibility(inProgress ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onSuccess(BankTransaction tx) {
                TransactionFlow.showReceipt(requireContext(), tx, () -> {
                    if (isAdded()) {
                        NavHostFragment.findNavController(UtilityPayFragment.this).popBackStack();
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                if (binding != null) {
                    UiUtils.snack(binding.getRoot(), message);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
