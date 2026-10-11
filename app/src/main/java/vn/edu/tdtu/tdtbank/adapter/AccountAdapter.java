package vn.edu.tdtu.tdtbank.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.databinding.ItemAccountBinding;
import vn.edu.tdtu.tdtbank.domain.MortgageCalculator;
import vn.edu.tdtu.tdtbank.domain.SavingsCalculator;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.AccountType;
import vn.edu.tdtu.tdtbank.model.PaymentFrequency;
import vn.edu.tdtu.tdtbank.utils.MoneyFormat;

import java.util.ArrayList;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.Holder> {

    public interface OnClick {
        void onAccountClick(Account a);
    }

    private final List<Account> items = new ArrayList<>();
    private final OnClick listener;

    public AccountAdapter(OnClick listener) {
        this.listener = listener;
    }

    public void submit(List<Account> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    /** Nhãn loại tài khoản dùng chung. */
    public static int typeLabel(AccountType t) {
        switch (t) {
            case SAVING:
                return R.string.type_saving;
            case MORTGAGE:
                return R.string.type_mortgage;
            default:
                return R.string.type_checking;
        }
    }

    /** Dòng thông tin riêng cho từng loại tài khoản (khác nhau theo đề bài). */
    public static String extraInfo(Context c, Account a) {
        switch (a.type) {
            case SAVING:
                return c.getString(R.string.saving_extra, MoneyFormat.percent(a.interestRate),
                        MoneyFormat.vnd(SavingsCalculator.monthlyProfit(a.balance, a.interestRate)));
            case MORTGAGE:
                long pmt = MortgageCalculator.periodicPayment(a.principal, a.interestRate, a.termMonths,
                        a.paymentFrequency == null ? PaymentFrequency.MONTHLY : a.paymentFrequency);
                int per = a.paymentFrequency == PaymentFrequency.BIWEEKLY
                        ? R.string.per_two_weeks : R.string.per_month;
                return c.getString(R.string.mortgage_extra, MoneyFormat.vnd(pmt), c.getString(per));
            default:
                return c.getString(R.string.checking_extra);
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemAccountBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Account a = items.get(position);
        Context c = h.binding.getRoot().getContext();
        h.binding.tvType.setText(typeLabel(a.type));
        h.binding.tvNumber.setText(a.accountNumber);
        h.binding.tvBalanceLabel.setText(a.type == AccountType.MORTGAGE ? R.string.label_debt : R.string.label_balance);
        h.binding.tvBalance.setText(MoneyFormat.vnd(a.balance));
        h.binding.tvExtra.setText(extraInfo(c, a));
        h.binding.getRoot().setOnClickListener(v -> listener.onAccountClick(a));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemAccountBinding binding;

        Holder(ItemAccountBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
