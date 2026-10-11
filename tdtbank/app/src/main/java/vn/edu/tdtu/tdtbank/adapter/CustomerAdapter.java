package vn.edu.tdtu.tdtbank.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.databinding.ItemCustomerBinding;
import vn.edu.tdtu.tdtbank.model.KycStatus;
import vn.edu.tdtu.tdtbank.model.User;
import vn.edu.tdtu.tdtbank.model.UserStatus;
import vn.edu.tdtu.tdtbank.utils.TextNormalizer;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.Holder> {

    public interface OnClick {
        void onClick(User user);
    }

    private final List<User> all = new ArrayList<>();
    private final List<User> shown = new ArrayList<>();
    private final OnClick listener;
    private String query = "";

    public CustomerAdapter(OnClick listener) {
        this.listener = listener;
    }

    public void submit(List<User> list) {
        all.clear();
        all.addAll(list);
        applyFilter();
    }

    public void filter(String q) {
        query = TextNormalizer.normalize(q);
        applyFilter();
    }

    private void applyFilter() {
        shown.clear();
        for (User u : all) {
            String hay = TextNormalizer.normalize(u.fullName + " " + u.username + " " + u.phone);
            if (query.isEmpty() || hay.contains(query)) {
                shown.add(u);
            }
        }
        notifyDataSetChanged();
    }

    public int shownCount() {
        return shown.size();
    }

    public static int kycLabel(KycStatus s) {
        if (s == null) {
            return R.string.kyc_none;
        }
        switch (s) {
            case VERIFIED:
                return R.string.kyc_verified;
            case PENDING:
                return R.string.kyc_pending;
            case REJECTED:
                return R.string.kyc_rejected;
            default:
                return R.string.kyc_none;
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemCustomerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        User u = shown.get(position);
        h.binding.tvName.setText(u.fullName);
        h.binding.tvSub.setText(h.binding.getRoot().getContext()
                .getString(R.string.customer_sub, u.username, u.phone == null ? "—" : u.phone));
        h.binding.tvKyc.setText(kycLabel(u.kycStatus));
        h.binding.tvLocked.setVisibility(u.status == UserStatus.LOCKED ? android.view.View.VISIBLE : android.view.View.GONE);
        h.binding.getRoot().setOnClickListener(v -> listener.onClick(u));
    }

    @Override
    public int getItemCount() {
        return shown.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemCustomerBinding binding;

        Holder(ItemCustomerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
