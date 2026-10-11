package vn.edu.tdtu.tdtbank.ui.common;

import vn.edu.tdtu.tdtbank.domain.OtpManager;

/** Phiên OTP duy nhất trong tiến trình ứng dụng (OTP chỉ tồn tại trong RAM, không lưu đĩa/đám mây). */
public final class OtpSession {

    private static final OtpManager MANAGER = new OtpManager();
    private static String lastSimulatedMessage;

    private OtpSession() {
    }

    public static OtpManager manager() {
        return MANAGER;
    }

    public static String lastCode() {
        return lastSimulatedMessage;
    }

    public static String issue(String fingerprint) {
        lastSimulatedMessage = MANAGER.issue(fingerprint, System.currentTimeMillis());
        return lastSimulatedMessage;
    }
}
