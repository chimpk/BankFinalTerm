package vn.edu.tdtu.tdtbank.ui.profile;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.databinding.FragmentProfileBinding;
import vn.edu.tdtu.tdtbank.model.KycRecord;
import vn.edu.tdtu.tdtbank.model.KycStatus;
import vn.edu.tdtu.tdtbank.model.Role;
import vn.edu.tdtu.tdtbank.model.User;
import vn.edu.tdtu.tdtbank.ui.common.FaceCaptureDialogFragment;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.utils.ImageUtils;
import vn.edu.tdtu.tdtbank.utils.ValidationUtils;
import vn.edu.tdtu.tdtbank.viewmodel.ProfileViewModel;
import vn.edu.tdtu.tdtbank.viewmodel.SessionViewModel;

/** Hồ sơ cá nhân (dùng chung): xem/sửa thông tin liên hệ, eKYC (khách hàng), đổi mật khẩu. */
public class ProfileFragment extends Fragment {

    private static final String KEY_FACE = "profile_face";

    private FragmentProfileBinding binding;
    private ProfileViewModel vm;
    private User user;
    private boolean fieldsFilled;
    private boolean kycObserved;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(ProfileViewModel.class);
        SessionViewModel session = new ViewModelProvider(requireActivity()).get(SessionViewModel.class);

        session.currentUser().observe(getViewLifecycleOwner(), r -> {
            if (r != null && r.data != null) {
                user = r.data;
                bindUser();
            }
        });
        UiUtils.observeOperation(this, vm, () -> {
            if (binding != null) {
                UiUtils.setText(binding.tilOldPassword, "");
                UiUtils.setText(binding.tilNewPassword, "");
            }
        });

        binding.btnSave.setOnClickListener(v -> saveContact());
        binding.btnKyc.setOnClickListener(v -> startKyc());
        binding.btnChangePassword.setOnClickListener(v -> changePassword());
        getChildFragmentManager().setFragmentResultListener(KEY_FACE, getViewLifecycleOwner(), (key, b) -> {
            KycRecord rec = new KycRecord();
            rec.uid = user.uid;
            rec.fullName = user.fullName;
            rec.faceImageBase64 = b.getString(FaceCaptureDialogFragment.KEY_IMAGE);
            rec.faceDetected = b.getBoolean(FaceCaptureDialogFragment.KEY_FACE_DETECTED);
            rec.livenessPassed = b.getBoolean(FaceCaptureDialogFragment.KEY_LIVENESS);
            rec.status = KycStatus.PENDING;
            rec.capturedBy = user.uid;
            rec.capturedByRole = Role.CUSTOMER;
            vm.submitKyc(rec);
        });
    }

    private void bindUser() {
        binding.tvName.setText(user.fullName);
        binding.tvUsername.setText(getString(R.string.profile_username, user.username));
        binding.tvRole.setText(user.role == Role.OFFICER ? R.string.role_officer : R.string.role_customer);
        binding.tvNationalId.setText(getString(R.string.profile_national_id, user.nationalId == null ? "—" : user.nationalId));
        binding.tvDob.setText(getString(R.string.profile_dob, user.dateOfBirth == null ? "—" : user.dateOfBirth));
        if (!fieldsFilled) {
            UiUtils.setText(binding.tilPhone, user.phone);
            UiUtils.setText(binding.tilEmail, user.contactEmail);
            UiUtils.setText(binding.tilAddress, user.address);
            fieldsFilled = true;
        }
        boolean customer = user.role == Role.CUSTOMER;
        binding.cardKyc.setVisibility(customer ? View.VISIBLE : View.GONE);
        if (customer) {
            int label;
            switch (user.kycStatus == null ? KycStatus.NONE : user.kycStatus) {
                case VERIFIED:
                    label = R.string.kyc_verified;
                    break;
                case PENDING:
                    label = R.string.kyc_pending;
                    break;
                case REJECTED:
                    label = R.string.kyc_rejected;
                    break;
                default:
                    label = R.string.kyc_none;
                    break;
            }
            binding.tvKycStatus.setText(label);
            boolean canSubmit = user.kycStatus == null || user.kycStatus == KycStatus.NONE
                    || user.kycStatus == KycStatus.REJECTED;
            binding.btnKyc.setEnabled(canSubmit);
            if (kycObserved) {
                return;
            }
            kycObserved = true;
            vm.kycRecord(user.uid).observe(getViewLifecycleOwner(), r -> {
                if (binding == null || r.data == null) {
                    return;
                }
                Bitmap bmp = ImageUtils.fromBase64(r.data.faceImageBase64);
                if (bmp != null) {
                    binding.ivFace.setImageBitmap(bmp);
                    binding.ivFace.setVisibility(View.VISIBLE);
                }
                if (r.data.status == KycStatus.REJECTED && r.data.note != null && !r.data.note.isEmpty()) {
                    binding.tvKycNote.setText(getString(R.string.kyc_reject_note, r.data.note));
                    binding.tvKycNote.setVisibility(View.VISIBLE);
                } else {
                    binding.tvKycNote.setVisibility(View.GONE);
                }
            });
        }
    }

    private void startKyc() {
        FaceCaptureDialogFragment.newInstance(KEY_FACE, true).show(getChildFragmentManager(), KEY_FACE);
    }

    private void saveContact() {
        String phone = UiUtils.text(binding.tilPhone);
        String email = UiUtils.text(binding.tilEmail);
        String address = UiUtils.text(binding.tilAddress);
        boolean phoneOk = ValidationUtils.isValidPhone(phone);
        boolean emailOk = ValidationUtils.isValidOptionalEmail(email);
        binding.tilPhone.setError(phoneOk ? null : getString(R.string.err_phone));
        binding.tilEmail.setError(emailOk ? null : getString(R.string.err_email));
        if (phoneOk && emailOk) {
            vm.updateContact(phone, email, address);
        }
    }

    private void changePassword() {
        String oldPw = UiUtils.text(binding.tilOldPassword);
        String newPw = UiUtils.text(binding.tilNewPassword);
        binding.tilOldPassword.setError(oldPw.isEmpty() ? getString(R.string.err_required) : null);
        boolean ok = ValidationUtils.isValidPassword(newPw);
        binding.tilNewPassword.setError(ok ? null : getString(R.string.err_password_short));
        if (!oldPw.isEmpty() && ok) {
            vm.changePassword(oldPw, newPw);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        fieldsFilled = false;
        kycObserved = false;
    }
}
