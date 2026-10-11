package vn.edu.tdtu.tdtbank.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.adapter.AccountAdapter;
import vn.edu.tdtu.tdtbank.adapter.TransactionAdapter;
import vn.edu.tdtu.tdtbank.databinding.FragmentAccountDetailBinding;
import vn.edu.tdtu.tdtbank.domain.MortgageCalculator;
import vn.edu.tdtu.tdtbank.domain.SavingsCalculator;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.AccountStatus;
import vn.edu.tdtu.tdtbank.model.AccountType;
import vn.edu.tdtu.tdtbank.model.PaymentFrequency;
import vn.edu.tdtu.tdtbank.model.TxDraft;
import vn.edu.tdtu.tdtbank.model.TxType;
import vn.edu.tdtu.tdtbank.ui.common.TransactionFlow;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.utils.Args;
import vn.edu.tdtu.tdtbank.utils.DateUtils;
import vn.edu.tdtu.tdtbank.utils.MoneyFormat;
import vn.edu.tdtu.tdtbank.viewmodel.AccountViewModel;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Mỗi loại tài khoản có một cách hiển thị khác nhau (checking / saving / mortgage) theo đề bài. */
public class AccountDetailFragment extends Fragment {

    private FragmentAccountDetailBinding binding;
    private AccountViewModel vm;
    private TransactionFlow flow;
    private Account current;
    private final List<Account> allAccounts = new ArrayList<>();
    private String number;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentAccountDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        number = requireArguments().getString(Args.ACCOUNT_NUMBER);
        vm = new ViewModelProvider(this).get(AccountViewModel.class);
        flow = new TransactionFlow(this);

        TransactionAdapter txAdapter = new TransactionAdapter(vm.uid());
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTransactions.setAdapter(txAdapter);
        binding.rvTransactions.setNestedScrollingEnabled(false);

        vm.accounts().observe(getViewLifecycleOwner(), r -> {
            if (r.data != null) {
                allAccounts.clear();
                allAccounts.addAll(r.data);
            }
        });
        vm.account(number).observe(getViewLifecycleOwner(), r -> {
            if (r.data != null) {
                current = r.data;
                bind(r.data);
            }
        });
        vm.accountTransactions(number).observe(getViewLifecycleOwner(), r -> {
            if (r.data != null) {
                txAdapter.submit(r.data);
                binding.tvNoTx.setVisibility(r.data.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        binding.btnDeposit.setOnClickListener(v -> openCash(TxType.DEPOSIT));
        binding.btnWithdraw.setOnClickListener(v -> openCash(TxType.WITHDRAW));
        binding.btnTransfer.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.transferFragment));
        binding.btnSchedule.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString(Args.ACCOUNT_NUMBER, number);
            NavHostFragment.findNavController(this).navigate(R.id.mortgageScheduleFragment, b);
        });
        binding.btnPayInstallment.setOnClickListener(v -> payInstallment());
    }

    private void openCash(TxType mode) {
        Bundle b = new Bundle();
        b.putString(Args.CASH_MODE, mode.name());
        b.putString(Args.ACCOUNT_NUMBER, number);
        NavHostFragment.findNavController(this).navigate(R.id.cashFragment, b);
    }

    private void bind(Account a) {
        binding.tvType.setText(AccountAdapter.typeLabel(a.type));
        binding.tvNumber.setText(a.accountNumber);
        binding.tvBalanceLabel.setText(a.type == AccountType.MORTGAGE ? R.string.label_debt : R.string.label_balance);
        binding.tvBalance.setText(MoneyFormat.vnd(a.balance));
        binding.infoContainer.removeAllViews();

        boolean cash = a.type != AccountType.MORTGAGE;
        binding.btnDeposit.setVisibility(cash ? View.VISIBLE : View.GONE);
        binding.btnWithdraw.setVisibility(cash ? View.VISIBLE : View.GONE);
        binding.btnTransfer.setVisibility(cash ? View.VISIBLE : View.GONE);
        binding.btnSchedule.setVisibility(cash ? View.GONE : View.VISIBLE);
        binding.btnPayInstallment.setVisibility(cash ? View.GONE : View.VISIBLE);

        switch (a.type) {
            case SAVING:
                row(R.string.row_term, getString(R.string.months_fmt, a.termMonths));
                row(R.string.row_rate, MoneyFormat.percent(a.interestRate));
                row(R.string.row_monthly_profit, MoneyFormat.vnd(SavingsCalculator.monthlyProfit(a.balance, a.interestRate)));
                row(R.string.row_maturity_profit, MoneyFormat.vnd(
                        SavingsCalculator.profitAtMaturity(a.balance, a.interestRate, a.termMonths)));
                row(R.string.row_start_date, formatIso(a.startDate));
                break;
            case MORTGAGE:
                PaymentFrequency f = a.paymentFrequency == null ? PaymentFrequency.MONTHLY : a.paymentFrequency;
                row(R.string.row_principal, MoneyFormat.vnd(a.principal));
                row(R.string.row_rate, MoneyFormat.percent(a.interestRate));
                row(R.string.row_term, getString(R.string.months_fmt, a.termMonths));
                row(R.string.row_frequency, getString(f == PaymentFrequency.BIWEEKLY
                        ? R.string.freq_biweekly : R.string.freq_monthly));
                long pmt = MortgageCalculator.periodicPayment(a.principal, a.interestRate, a.termMonths, f);
                row(R.string.row_installment, MoneyFormat.vnd(pmt));
                MortgageCalculator.Installment next = nextInstallment(a);
                row(R.string.row_next_due, next == null ? getString(R.string.paid_off)
                        : DateUtils.formatDate(next.dueDate) + " • " + MoneyFormat.vnd(next.payment));
                binding.btnPayInstallment.setEnabled(next != null);
                break;
            default:
                row(R.string.row_status, getString(a.status == AccountStatus.ACTIVE
                        ? R.string.status_active : R.string.status_closed));
                row(R.string.row_start_date, formatIso(a.startDate));
                break;
        }
    }

    private static String formatIso(String iso) {
        try {
            return DateUtils.formatDate(LocalDate.parse(iso));
        } catch (Exception e) {
            return iso == null ? "—" : iso;
        }
    }

    private void row(int labelRes, String value) {
        TextView tv = new TextView(requireContext());
        tv.setText(getString(R.string.row_format, getString(labelRes), value));
        tv.setPadding(0, 4, 0, 4);
        binding.infoContainer.addView(tv);
    }

    private MortgageCalculator.Installment nextInstallment(Account a) {
        PaymentFrequency f = a.paymentFrequency == null ? PaymentFrequency.MONTHLY : a.paymentFrequency;
        List<MortgageCalculator.Installment> s = MortgageCalculator.schedule(a.principal, a.interestRate,
                a.termMonths, f, LocalDate.parse(a.startDate));
        int idx = MortgageCalculator.nextInstallmentIndex(s, a.principal, a.balance);
        return idx < 0 ? null : s.get(idx);
    }

    private void payInstallment() {
        if (current == null) {
            return;
        }
        MortgageCalculator.Installment next = nextInstallment(current);
        if (next == null) {
            return;
        }
        List<Account> sources = new ArrayList<>();
        for (Account a : allAccounts) {
            if (a.type == AccountType.CHECKING && a.status == AccountStatus.ACTIVE) {
                sources.add(a);
            }
        }
        if (sources.isEmpty()) {
            UiUtils.snack(binding.getRoot(), getString(R.string.err_no_checking));
            return;
        }
        if (sources.size() == 1) {
            startPayment(sources.get(0), next);
            return;
        }
        String[] labels = new String[sources.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = sources.get(i).accountNumber + " • " + MoneyFormat.vnd(sources.get(i).balance);
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.pick_source_account)
                .setItems(labels, (d, which) -> startPayment(sources.get(which), next))
                .show();
    }

    private void startPayment(Account source, MortgageCalculator.Installment next) {
        TxDraft d = new TxDraft();
        d.type = TxType.MORTGAGE_PAYMENT;
        d.from = source;
        d.toAccountNumber = current.accountNumber;
        d.toOwnerUid = vm.uid();
        d.amount = next.payment;
        d.principalPortion = next.principal;
        d.toDebt = current.balance;
        d.counterpartyName = getString(R.string.type_mortgage) + " " + current.accountNumber;
        d.description = getString(R.string.installment_title, next.number, DateUtils.formatDate(next.dueDate));
        flow.start(d, new TransactionFlow.Listener() {
            @Override
            public void onProgress(boolean inProgress) {
                if (binding != null) {
                    binding.btnPayInstallment.setEnabled(!inProgress);
                }
            }

            @Override
            public void onSuccess(vn.edu.tdtu.tdtbank.model.BankTransaction tx) {
                TransactionFlow.showReceipt(requireContext(), tx, null);
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
