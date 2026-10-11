package vn.edu.tdtu.tdtbank.ui.officer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.adapter.AccountAdapter;
import vn.edu.tdtu.tdtbank.adapter.CustomerAdapter;
import vn.edu.tdtu.tdtbank.databinding.FragmentCustomerDetailBinding;
import vn.edu.tdtu.tdtbank.model.KycRecord;
import vn.edu.tdtu.tdtbank.model.KycStatus;
import vn.edu.tdtu.tdtbank.model.Role;
import vn.edu.tdtu.tdtbank.model.User;
import vn.edu.tdtu.tdtbank.model.UserStatus;
import vn.edu.tdtu.tdtbank.ui.common.FaceCaptureDialogFragment;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.utils.Args;
import vn.edu.tdtu.tdtbank.utils.ValidationUtils;
import vn.edu.tdtu.tdtbank.viewmodel.OfficerViewModel;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;

/** Nhân viên xem và sửa dữ liệu khách hàng theo yêu cầu, khóa/mở khóa, mở tài khoản, hỗ trợ eKYC tại quầy. */
public class CustomerDetailFragment extends Fragment {

    private static final String KEY_FACE = "officer_face";

    private FragmentCustomerDetailBinding binding;
    private OfficerViewModel vm;
    private User customer;
    private String uid;
    private boolean filled;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentCustomerDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        uid = requireArguments().getString(Args.CUSTOMER_UID);
        vm = new ViewModelProvider(requireActivity()).get(OfficerViewModel.class);

        AccountAdapter accounts = new AccountAdapter(a -> { });
        binding.rvAccounts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAccounts.setAdapter(accounts);
        binding.rvAccounts.setNestedScrollingEnabled(false);

        vm.customer(uid).observe(getViewLifecycleOwner(), r -> {
            if (r.data != null) {
                customer = r.data;
                bind();
            }
        });
        vm.accountsOf(uid).observe(getViewLifecycleOwner(), r -> {
            if (r.data != null) {
                accounts.submit(r.data);
            }
        });
        UiUtils.observeOperation(this, vm, null);

        binding.btnSave.setOnClickListener(v -> save());
        binding.btnToggleLock.setOnClickListener(v -> toggleLock());
        binding.btnOpenAccount.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString(Args.CUSTOMER_UID, uid);
            NavHostFragment.findNavController(this).navigate(R.id.openAccountFragment, b);
        });
        binding.btnCaptureFace.setOnClickListener(v -> confirmCapture());
        getChildFragmentManager().setFragmentResultListener(KEY_FACE, getViewLifecycleOwner(), (key, b) -> {
            KycRecord rec = new KycRecord();
            rec.uid = uid;
            rec.fullName = customer.fullName;
            rec.faceImageBase64 = b.getString(FaceCaptureDialogFragment.KEY_IMAGE);
            rec.faceDetected = b.getBoolean(FaceCaptureDialogFragment.KEY_FACE_DETECTED);
            rec.livenessPassed = false; // nhân viên chụp trực tiếp, không có thử thách cười
            rec.status = KycStatus.VERIFIED;
            rec.capturedBy = vm.uid();
            rec.capturedByRole = Role.OFFICER;
            rec.reviewedBy = vm.uid();
            rec.note = getString(R.string.kyc_note_counter);
            vm.submitKyc(rec);
        });
    }

    private void bind() {
        binding.tvHeader.setText(customer.fullName);
        binding.tvSub.setText(getString(R.string.customer_sub, customer.username, customer.phone));
        binding.tvKyc.setText(getString(R.string.customer_kyc_line, getString(CustomerAdapter.kycLabel(customer.kycStatus))));
        boolean locked = customer.status == UserStatus.LOCKED;
        binding.tvStatus.setText(locked ? R.string.status_locked : R.string.status_active);
        binding.btnToggleLock.setText(locked ? R.string.action_unlock : R.string.action_lock);
        if (!filled) {
            UiUtils.setText(binding.tilFullName, customer.fullName);
            UiUtils.setText(binding.tilPhone, customer.phone);
            UiUtils.setText(binding.tilEmail, customer.contactEmail);
            UiUtils.setText(binding.tilAddress, customer.address);
            UiUtils.setText(binding.tilNationalId, customer.nationalId);
            UiUtils.setText(binding.tilDob, customer.dateOfBirth);
            filled = true;
        }
    }

    private void save() {
        String name = UiUtils.text(binding.tilFullName);
        String phone = UiUtils.text(binding.tilPhone);
        String email = UiUtils.text(binding.tilEmail);
        String address = UiUtils.text(binding.tilAddress);
        String nid = UiUtils.text(binding.tilNationalId);
        String dob = UiUtils.text(binding.tilDob);
        boolean ok = true;
        ok &= check(binding.tilFullName, ValidationUtils.isValidFullName(name), R.string.err_full_name);
        ok &= check(binding.tilPhone, ValidationUtils.isValidPhone(phone), R.string.err_phone);
        ok &= check(binding.tilEmail, ValidationUtils.isValidOptionalEmail(email), R.string.err_email);
        ok &= check(binding.tilNationalId, ValidationUtils.isValidNationalId(nid), R.string.err_national_id);
        ok &= check(binding.tilDob, ValidationUtils.isAdultDateOfBirth(dob, LocalDate.now()), R.string.err_dob);
        ok &= check(binding.tilAddress, address.length() >= 5, R.string.err_address);
        if (ok) {
            vm.updateCustomer(uid, name, phone, email, address, nid, dob);
        }
    }

    private boolean check(com.google.android.material.textfield.TextInputLayout til, boolean valid, int errorRes) {
        til.setError(valid ? null : getString(errorRes));
        return valid;
    }

    private void toggleLock() {
        boolean locked = customer.status == UserStatus.LOCKED;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(locked ? R.string.action_unlock : R.string.action_lock)
                .setMessage(locked ? R.string.confirm_unlock : R.string.confirm_lock)
                .setPositiveButton(R.string.action_confirm, (d, w) ->
                        vm.setStatus(uid, locked ? UserStatus.ACTIVE : UserStatus.LOCKED))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void confirmCapture() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.kyc_counter_title)
                .setMessage(R.string.kyc_counter_message)
                .setPositiveButton(R.string.action_continue, (d, w) ->
                        FaceCaptureDialogFragment.newInstance(KEY_FACE, false)
                                .show(getChildFragmentManager(), KEY_FACE))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        filled = false;
    }
}
