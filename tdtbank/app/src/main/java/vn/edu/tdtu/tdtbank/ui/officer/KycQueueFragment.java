package vn.edu.tdtu.tdtbank.ui.officer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.adapter.KycAdapter;
import vn.edu.tdtu.tdtbank.databinding.FragmentKycQueueBinding;
import vn.edu.tdtu.tdtbank.model.KycRecord;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.viewmodel.OfficerViewModel;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** Hàng đợi duyệt eKYC do khách hàng tự gửi: nhân viên đối chiếu ảnh với giấy tờ rồi duyệt/từ chối. */
public class KycQueueFragment extends Fragment {

    private FragmentKycQueueBinding binding;
    private OfficerViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentKycQueueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(requireActivity()).get(OfficerViewModel.class);
        KycAdapter adapter = new KycAdapter(new KycAdapter.Actions() {
            @Override
            public void onApprove(KycRecord r) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.kyc_approve_title)
                        .setMessage(R.string.kyc_approve_message)
                        .setPositiveButton(R.string.action_approve, (d, w) -> vm.reviewKyc(r.uid, true, ""))
                        .setNegativeButton(R.string.action_cancel, null)
                        .show();
            }

            @Override
            public void onReject(KycRecord r) {
                askRejectReason(r);
            }
        });
        binding.rvKyc.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvKyc.setAdapter(adapter);
        vm.pendingKyc().observe(getViewLifecycleOwner(), r -> {
            if (r.data != null) {
                adapter.submit(r.data);
                binding.tvEmpty.setVisibility(r.data.isEmpty() ? View.VISIBLE : View.GONE);
            } else if (r.message != null) {
                UiUtils.snack(binding.getRoot(), r.message);
            }
        });
        UiUtils.observeOperation(this, vm, null);
    }

    private void askRejectReason(KycRecord r) {
        TextInputLayout til = new TextInputLayout(requireContext());
        til.setHint(getString(R.string.kyc_reject_reason));
        TextInputEditText et = new TextInputEditText(til.getContext());
        til.addView(et);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        til.setPadding(pad, pad / 2, pad, 0);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.kyc_reject_title)
                .setView(til)
                .setPositiveButton(R.string.action_reject, (d, w) -> {
                    String note = et.getText() == null ? "" : et.getText().toString().trim();
                    vm.reviewKyc(r.uid, false, note);
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
