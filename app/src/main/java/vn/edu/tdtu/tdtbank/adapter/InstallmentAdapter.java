package vn.edu.tdtu.tdtbank.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.databinding.ItemInstallmentBinding;
import vn.edu.tdtu.tdtbank.domain.MortgageCalculator.Installment;
import vn.edu.tdtu.tdtbank.utils.DateUtils;
import vn.edu.tdtu.tdtbank.utils.MoneyFormat;

import java.util.ArrayList;
import java.util.List;

public class InstallmentAdapter extends RecyclerView.Adapter<InstallmentAdapter.Holder> {

    private final List<Installment> items = new ArrayList<>();
    private int paidCount;

    public void submit(List<Installment> list, int paidCount) {
        items.clear();
        items.addAll(list);
        this.paidCount = paidCount;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemInstallmentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Installment i = items.get(position);
        h.binding.tvTitle.setText(h.binding.getRoot().getContext()
                .getString(R.string.installment_title, i.number, DateUtils.formatDate(i.dueDate)));
        h.binding.tvPayment.setText(MoneyFormat.vnd(i.payment));
        h.binding.tvDetail.setText(h.binding.getRoot().getContext().getString(R.string.installment_detail,
                MoneyFormat.vnd(i.principal), MoneyFormat.vnd(i.interest), MoneyFormat.vnd(i.remaining)));
        h.binding.tvStatus.setText(position < paidCount ? R.string.installment_paid : R.string.installment_due);
        h.binding.getRoot().setAlpha(position < paidCount ? 0.55f : 1f);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemInstallmentBinding binding;

        Holder(ItemInstallmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
