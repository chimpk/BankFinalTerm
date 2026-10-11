package vn.edu.tdtu.tdtbank.ui.common;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.domain.TransactionRules;
import vn.edu.tdtu.tdtbank.model.BankTransaction;
import vn.edu.tdtu.tdtbank.model.KycStatus;
import vn.edu.tdtu.tdtbank.model.TxDraft;
import vn.edu.tdtu.tdtbank.model.User;
import vn.edu.tdtu.tdtbank.utils.MoneyFormat;
import vn.edu.tdtu.tdtbank.viewmodel.SessionViewModel;
import vn.edu.tdtu.tdtbank.viewmodel.TransactionViewModel;

import androidx.lifecycle.ViewModelProvider;

/**
 * Luồng giao dịch bảo mật dùng chung cho mọi chức năng:
 * kiểm tra nghiệp vụ -> (xác minh khuôn mặt nếu giá trị cao) -> OTP (2FA) -> ghi nhận nguyên tử.
 */
public class TransactionFlow {

    public interface Listener {
        void onProgress(boolean inProgress);

        void onSuccess(BankTransaction tx);

        void onFailure(String message);
    }

    private static final String KEY_FACE = "tx_face";
    private static final String KEY_OTP = "tx_otp";

    private final Fragment host;
    private final TransactionViewModel vm;
    private final SessionViewModel session;
    private Listener pending;

    public TransactionFlow(Fragment host) {
        this.host = host;
        this.vm = new ViewModelProvider(host).get(TransactionViewModel.class);
        this.session = new ViewModelProvider(host.requireActivity()).get(SessionViewModel.class);
        vm.resetCommit();
        vm.commitState().observe(host.getViewLifecycleOwner(), this::onCommitState);
    }

    public void start(TxDraft draft, Listener listener) {
        this.pending = listener;
        listener.onProgress(true);
        vm.validate(draft, new Callback<TransactionRules.Error>() {
            @Override
            public void onSuccess(TransactionRules.Error error) {
                if (error != TransactionRules.Error.NONE) {
                    fail(message(error));
                } else {
                    afterValidation(draft);
                }
            }

            @Override
            public void onError(String message) {
                fail(message);
            }
        });
    }

    private void afterValidation(TxDraft d) {
        if (!TransactionRules.requiresFaceVerification(d.amount)) {
            askOtp(d, false);
            return;
        }
        Resource<User> r = session.currentUser().getValue();
        User u = r == null ? null : r.data;
        if (u == null || u.kycStatus != KycStatus.VERIFIED) {
            fail(host.getString(R.string.err_kyc_required, MoneyFormat.vnd(TransactionRules.HIGH_VALUE_THRESHOLD)));
            return;
        }
        FragmentManager fm = host.getChildFragmentManager();
        fm.setFragmentResultListener(KEY_FACE, host.getViewLifecycleOwner(), (key, b) -> {
            if (b.getBoolean(FaceCaptureDialogFragment.KEY_LIVENESS)) {
                askOtp(d, true);
            } else {
                fail(host.getString(R.string.err_face_failed));
            }
        });
        FaceCaptureDialogFragment.newInstance(KEY_FACE, true).show(fm, KEY_FACE);
    }

    private void askOtp(TxDraft d, boolean faceVerified) {
        FragmentManager fm = host.getChildFragmentManager();
        fm.setFragmentResultListener(KEY_OTP, host.getViewLifecycleOwner(), (key, b) -> {
            if (b.getBoolean(OtpDialogFragment.KEY_VERIFIED)) {
                vm.commit(d, true, faceVerified);
            } else {
                fail(host.getString(R.string.err_otp_cancelled));
            }
        });
        String summary = host.getString(R.string.otp_summary, MoneyFormat.vnd(d.amount),
                d.counterpartyName == null ? "" : d.counterpartyName);
        OtpDialogFragment.newInstance(KEY_OTP, d.fingerprint(), summary).show(fm, KEY_OTP);
    }

    private void onCommitState(Resource<BankTransaction> r) {
        if (r == null || pending == null) {
            return;
        }
        if (r.status == Resource.Status.SUCCESS) {
            Listener l = pending;
            pending = null;
            vm.resetCommit();
            l.onProgress(false);
            l.onSuccess(r.data);
        } else if (r.status == Resource.Status.ERROR) {
            vm.resetCommit();
            fail(r.message);
        }
    }

    private void fail(String message) {
        Listener l = pending;
        pending = null;
        if (l != null) {
            l.onProgress(false);
            l.onFailure(message);
        }
    }

    private String message(TransactionRules.Error e) {
        switch (e) {
            case AMOUNT_TOO_SMALL:
                return host.getString(R.string.err_amount_small, MoneyFormat.vnd(TransactionRules.MIN_AMOUNT));
            case AMOUNT_TOO_LARGE:
                return host.getString(R.string.err_amount_large, MoneyFormat.vnd(TransactionRules.MAX_AMOUNT));
            case INSUFFICIENT_FUNDS:
                return host.getString(R.string.err_insufficient);
            case DAILY_LIMIT_EXCEEDED:
                return host.getString(R.string.err_daily_limit, MoneyFormat.vnd(TransactionRules.DAILY_LIMIT));
            case SAME_ACCOUNT:
                return host.getString(R.string.err_same_account);
            case INVALID_DESTINATION:
                return host.getString(R.string.err_invalid_destination);
            case EXCEEDS_DEBT:
                return host.getString(R.string.err_exceeds_debt);
            default:
                return host.getString(R.string.err_source_not_allowed);
        }
    }

    /** Hộp thoại biên lai sau khi giao dịch thành công. */
    public static void showReceipt(Context c, BankTransaction tx, Runnable onClose) {
        String body = c.getString(R.string.receipt_body, c.getString(TxFormatter.titleRes(tx.type)),
                MoneyFormat.vnd(tx.amount), MoneyFormat.vnd(tx.fee), tx.id == null ? "" : tx.id.substring(0, 8).toUpperCase());
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(c)
                .setTitle(R.string.receipt_title)
                .setMessage(body)
                .setCancelable(false)
                .setPositiveButton(R.string.action_close, (d, w) -> {
                    if (onClose != null) {
                        onClose.run();
                    }
                })
                .show();
    }
}
