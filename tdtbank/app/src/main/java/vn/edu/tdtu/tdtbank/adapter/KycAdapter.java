package vn.edu.tdtu.tdtbank.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.databinding.ItemKycBinding;
import vn.edu.tdtu.tdtbank.model.KycRecord;
import vn.edu.tdtu.tdtbank.model.Role;
import vn.edu.tdtu.tdtbank.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class KycAdapter extends RecyclerView.Adapter<KycAdapter.Holder> {

    public interface Actions {
        void onApprove(KycRecord r);

        void onReject(KycRecord r);
    }

    private final List<KycRecord> items = new ArrayList<>();
    private final Actions actions;

    public KycAdapter(Actions actions) {
        this.actions = actions;
    }

    public void submit(List<KycRecord> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemKycBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        KycRecord r = items.get(position);
        h.binding.tvName.setText(r.fullName == null ? r.uid : r.fullName);
        h.binding.tvMeta.setText(h.binding.getRoot().getContext().getString(R.string.kyc_item_meta,
                r.capturedByRole == Role.OFFICER ? "nhân viên" : "khách hàng",
                r.faceDetected ? "✓" : "✗", r.livenessPassed ? "✓" : "✗"));
        Bitmap bmp = ImageUtils.fromBase64(r.faceImageBase64);
        if (bmp != null) {
            h.binding.ivFace.setImageBitmap(bmp);
        }
        h.binding.btnApprove.setOnClickListener(v -> actions.onApprove(r));
        h.binding.btnReject.setOnClickListener(v -> actions.onReject(r));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemKycBinding binding;

        Holder(ItemKycBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
