package vn.edu.tdtu.tdtbank.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Sinh và xác minh OTP một lần cho 2FA. OTP gắn với "dấu vân tay" của giao dịch (txId + số tiền + đích)
 * nên không thể dùng lại cho giao dịch khác. Chỉ lưu băm SHA-256 có muối, không lưu OTP thô.
 *
 * LƯU Ý: OTP chỉ tồn tại trong bộ nhớ thiết bị và được hiển thị bằng "SMS mô phỏng".
 * Gửi OTP thật qua SMS/email cần hạ tầng phía máy chủ (xem docs/limitations.md).
 */
public class OtpManager {

    public static final int OTP_LENGTH = 6;
    public static final long TTL_MS = 120_000L;
    public static final int MAX_ATTEMPTS = 3;

    public enum Result { OK, WRONG_CODE, EXPIRED, TOO_MANY_ATTEMPTS, NO_CHALLENGE, FINGERPRINT_MISMATCH }

    private final SecureRandom random;
    private String fingerprint;
    private String salt;
    private String hash;
    private long expiresAt;
    private int attempts;

    public OtpManager() {
        this(new SecureRandom());
    }

    public OtpManager(SecureRandom random) {
        this.random = random;
    }

    /** Tạo OTP mới cho một giao dịch; trả về mã thô để "gửi" cho người dùng. */
    public synchronized String issue(String txFingerprint, long nowMs) {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        String code = String.format("%0" + OTP_LENGTH + "d", random.nextInt(bound));
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        this.salt = toHex(saltBytes);
        this.fingerprint = txFingerprint;
        this.hash = digest(salt, code, txFingerprint);
        this.expiresAt = nowMs + TTL_MS;
        this.attempts = 0;
        return code;
    }

    public synchronized Result verify(String code, String txFingerprint, long nowMs) {
        if (hash == null) {
            return Result.NO_CHALLENGE;
        }
        if (!fingerprint.equals(txFingerprint)) {
            return Result.FINGERPRINT_MISMATCH;
        }
        if (attempts >= MAX_ATTEMPTS) {
            return Result.TOO_MANY_ATTEMPTS;
        }
        if (nowMs > expiresAt) {
            clear();
            return Result.EXPIRED;
        }
        attempts++;
        String candidate = digest(salt, code == null ? "" : code.trim(), txFingerprint);
        boolean ok = MessageDigest.isEqual(
                candidate.getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8));
        if (ok) {
            clear(); // dùng một lần
            return Result.OK;
        }
        return attempts >= MAX_ATTEMPTS ? Result.TOO_MANY_ATTEMPTS : Result.WRONG_CODE;
    }

    public synchronized int remainingAttempts() {
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }

    public synchronized void clear() {
        hash = null;
        salt = null;
        fingerprint = null;
        expiresAt = 0;
        attempts = 0;
    }

    private static String digest(String salt, String code, String fp) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return toHex(md.digest((salt + ":" + code + ":" + fp).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
