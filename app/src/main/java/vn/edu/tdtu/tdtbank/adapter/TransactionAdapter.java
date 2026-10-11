package vn.edu.tdtu.tdtbank.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.databinding.ItemTransactionBinding;
import vn.edu.tdtu.tdtbank.model.BankTransaction;
import vn.edu.tdtu.tdtbank.ui.common.TxFormatter;
import vn.edu.tdtu.tdtbank.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.Holder> {

    private final List<BankTransaction> items = new ArrayList<>();
    private final String viewerUid;

    public TransactionAdapter(String viewerUid) {
        this.viewerUid = viewerUid;
    }

    public void submit(List<BankTransaction> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        BankTransaction t = items.get(position);
        boolean in = TxFormatter.isIncoming(t, viewerUid);
        h.binding.tvTitle.setText(TxFormatter.titleRes(t.type));
        String desc = t.description == null || t.description.isEmpty() ? "" : t.description;
        h.binding.tvDesc.setText(desc);
        h.binding.tvDate.setText(DateUtils.formatDateTime(t.createdAt));
        h.binding.tvAmount.setText(TxFormatter.signedAmount(t, viewerUid));
        h.binding.tvAmount.setTextColor(ContextCompat.getColor(h.binding.getRoot().getContext(),
                in ? R.color.positive : R.color.negative));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemTransactionBinding binding;

        Holder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
