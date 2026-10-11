package vn.edu.tdtu.tdtbank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import vn.edu.tdtu.tdtbank.domain.OtpManager.Result;

import org.junit.Test;

public class OtpManagerTest {

    @Test
    public void issuedCode_hasSixDigits_andVerifiesOnce() {
        OtpManager m = new OtpManager();
        String code = m.issue("tx1|1000", 0);
        assertTrue(code.matches("[0-9]{6}"));
        assertEquals(Result.OK, m.verify(code, "tx1|1000", 1000));
        assertEquals(Result.NO_CHALLENGE, m.verify(code, "tx1|1000", 1001)); // dùng một lần
    }

    @Test
    public void wrongCode_isRejected_andLocksAfterThreeAttempts() {
        OtpManager m = new OtpManager();
        String code = m.issue("fp", 0);
        String wrong = code.equals("000000") ? "111111" : "000000";
        assertEquals(Result.WRONG_CODE, m.verify(wrong, "fp", 10));
        assertEquals(Result.WRONG_CODE, m.verify(wrong, "fp", 20));
        assertEquals(Result.TOO_MANY_ATTEMPTS, m.verify(wrong, "fp", 30));
        assertEquals(Result.TOO_MANY_ATTEMPTS, m.verify(code, "fp", 40)); // đúng cũng bị khóa
    }

    @Test
    public void expiredCode_isRejected() {
        OtpManager m = new OtpManager();
        String code = m.issue("fp", 0);
        assertEquals(Result.EXPIRED, m.verify(code, "fp", OtpManager.TTL_MS + 1));
    }

    @Test
    public void codeIsBoundToTransactionFingerprint() {
        OtpManager m = new OtpManager();
        String code = m.issue("txA|5000000", 0);
        assertEquals(Result.FINGERPRINT_MISMATCH, m.verify(code, "txB|5000000", 5));
        assertEquals(Result.OK, m.verify(code, "txA|5000000", 6));
    }

    @Test
    public void verifyWithoutIssue_failsSafely() {
        assertEquals(Result.NO_CHALLENGE, new OtpManager().verify("123456", "fp", 0));
    }
}
