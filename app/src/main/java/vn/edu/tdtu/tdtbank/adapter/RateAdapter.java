package vn.edu.tdtu.tdtbank.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.databinding.ItemRateBinding;
import vn.edu.tdtu.tdtbank.model.SavingRatePolicy;
import vn.edu.tdtu.tdtbank.utils.MoneyFormat;

import java.util.ArrayList;
import java.util.List;

public class RateAdapter extends RecyclerView.Adapter<RateAdapter.Holder> {

    public interface OnClick {
        void onClick(SavingRatePolicy policy);
    }

    private final List<SavingRatePolicy> items = new ArrayList<>();
    private final OnClick listener;

    public RateAdapter(OnClick listener) {
        this.listener = listener;
    }

    public void submit(List<SavingRatePolicy> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemRateBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        SavingRatePolicy p = items.get(position);
        h.binding.tvTerm.setText(h.binding.getRoot().getContext().getString(R.string.months_fmt, p.termMonths));
        h.binding.tvRate.setText(MoneyFormat.percent(p.annualRate));
        h.binding.getRoot().setOnClickListener(v -> listener.onClick(p));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemRateBinding binding;

        Holder(ItemRateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
