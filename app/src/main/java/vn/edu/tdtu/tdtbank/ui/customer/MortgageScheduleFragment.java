package vn.edu.tdtu.tdtbank.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.edu.tdtu.tdtbank.adapter.InstallmentAdapter;
import vn.edu.tdtu.tdtbank.databinding.FragmentMortgageScheduleBinding;
import vn.edu.tdtu.tdtbank.domain.MortgageCalculator;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.PaymentFrequency;
import vn.edu.tdtu.tdtbank.utils.Args;
import vn.edu.tdtu.tdtbank.viewmodel.AccountViewModel;

import java.time.LocalDate;
import java.util.List;

/** Lịch trả nợ vay thế chấp: số tiền phải trả mỗi tháng hoặc mỗi hai tuần. */
public class MortgageScheduleFragment extends Fragment {

    private FragmentMortgageScheduleBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentMortgageScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        InstallmentAdapter adapter = new InstallmentAdapter();
        binding.rvSchedule.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSchedule.setAdapter(adapter);
        AccountViewModel vm = new ViewModelProvider(this).get(AccountViewModel.class);
        vm.account(requireArguments().getString(Args.ACCOUNT_NUMBER)).observe(getViewLifecycleOwner(), r -> {
            Account a = r.data;
            if (a == null) {
                return;
            }
            PaymentFrequency f = a.paymentFrequency == null ? PaymentFrequency.MONTHLY : a.paymentFrequency;
            List<MortgageCalculator.Installment> s = MortgageCalculator.schedule(
                    a.principal, a.interestRate, a.termMonths, f, LocalDate.parse(a.startDate));
            int next = MortgageCalculator.nextInstallmentIndex(s, a.principal, a.balance);
            adapter.submit(s, next < 0 ? s.size() : next);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
