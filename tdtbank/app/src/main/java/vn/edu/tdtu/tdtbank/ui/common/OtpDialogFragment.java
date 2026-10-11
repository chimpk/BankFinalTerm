package vn.edu.tdtu.tdtbank.ui.common;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.databinding.DialogOtpBinding;
import vn.edu.tdtu.tdtbank.domain.OtpManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Bước 2FA: nhập OTP 6 số. OTP được "gửi" bằng SMS MÔ PHỎNG hiển thị trong hộp thoại (xem docs/limitations.md).
 */
public class OtpDialogFragment extends DialogFragment {

    public static final String KEY_VERIFIED = "verified";
    private static final String ARG_REQUEST = "request";
    private static final String ARG_FINGERPRINT = "fingerprint";
    private static final String ARG_SUMMARY = "summary";

    private DialogOtpBinding binding;

    public static OtpDialogFragment newInstance(String requestKey, String fingerprint, String summary) {
        OtpDialogFragment f = new OtpDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_REQUEST, requestKey);
        b.putString(ARG_FINGERPRINT, fingerprint);
        b.putString(ARG_SUMMARY, summary);
        f.setArguments(b);
        f.setCancelable(false);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogOtpBinding.inflate(LayoutInflater.from(requireContext()));
        String fingerprint = requireArguments().getString(ARG_FINGERPRINT);
        binding.tvSummary.setText(requireArguments().getString(ARG_SUMMARY));
        if (savedInstanceState == null) {
            OtpSession.issue(fingerprint);
        }
        showSimulatedSms(false);
        binding.btnShowSms.setOnClickListener(v -> showSimulatedSms(true));
        binding.btnResend.setOnClickListener(v -> {
            OtpSession.issue(fingerprint);
            binding.tilOtp.setError(null);
            showSimulatedSms(true);
        });
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.otp_title)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.action_confirm, null)
                .setNegativeButton(R.string.action_cancel, (d, w) -> finish(false))
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> verify());
        }
    }

    private void showSimulatedSms(boolean show) {
        binding.cardSms.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnShowSms.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) {
            binding.tvSms.setText(getString(R.string.otp_sms_text, OtpSession.lastCode()));
        }
    }

    private void verify() {
        String code = binding.tilOtp.getEditText() == null ? "" : binding.tilOtp.getEditText().getText().toString();
        OtpManager.Result r = OtpSession.manager().verify(code,
                requireArguments().getString(ARG_FINGERPRINT), System.currentTimeMillis());
        switch (r) {
            case OK:
                finish(true);
                break;
            case WRONG_CODE:
                binding.tilOtp.setError(getString(R.string.otp_wrong, OtpSession.manager().remainingAttempts()));
                break;
            case EXPIRED:
                binding.tilOtp.setError(getString(R.string.otp_expired));
                break;
            case TOO_MANY_ATTEMPTS:
                binding.tilOtp.setError(getString(R.string.otp_locked));
                break;
            default:
                binding.tilOtp.setError(getString(R.string.otp_invalid_state));
                break;
        }
    }

    private void finish(boolean verified) {
        Bundle result = new Bundle();
        result.putBoolean(KEY_VERIFIED, verified);
        getParentFragmentManager().setFragmentResult(requireArguments().getString(ARG_REQUEST), result);
        dismissAllowingStateLoss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
