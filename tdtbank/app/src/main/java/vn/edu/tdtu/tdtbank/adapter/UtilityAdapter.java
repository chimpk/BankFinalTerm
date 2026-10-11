package vn.edu.tdtu.tdtbank.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.databinding.ItemUtilityBinding;
import vn.edu.tdtu.tdtbank.model.UtilityCategory;

public class UtilityAdapter extends RecyclerView.Adapter<UtilityAdapter.Holder> {

    public interface OnClick {
        void onClick(UtilityCategory category);
    }

    private static final UtilityCategory[] ORDER = UtilityCategory.values();
    private final OnClick listener;

    public UtilityAdapter(OnClick listener) {
        this.listener = listener;
    }

    public static int labelRes(UtilityCategory c) {
        switch (c) {
            case ELECTRICITY:
                return R.string.util_electricity;
            case WATER:
                return R.string.util_water;
            case PHONE_TOPUP:
                return R.string.util_phone;
            case FLIGHT:
                return R.string.util_flight;
            case MOVIE:
                return R.string.util_movie;
            case HOTEL:
                return R.string.util_hotel;
            default:
                return R.string.util_ecommerce;
        }
    }

    public static int iconRes(UtilityCategory c) {
        switch (c) {
            case ELECTRICITY:
                return R.drawable.ic_bolt;
            case WATER:
                return R.drawable.ic_water;
            case PHONE_TOPUP:
                return R.drawable.ic_phone;
            case FLIGHT:
                return R.drawable.ic_flight;
            case MOVIE:
                return R.drawable.ic_movie;
            case HOTEL:
                return R.drawable.ic_hotel;
            default:
                return R.drawable.ic_cart;
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemUtilityBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        UtilityCategory c = ORDER[position];
        h.binding.tvLabel.setText(labelRes(c));
        h.binding.ivIcon.setImageResource(iconRes(c));
        h.binding.getRoot().setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() {
        return ORDER.length;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemUtilityBinding binding;

        Holder(ItemUtilityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
