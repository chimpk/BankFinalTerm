package vn.edu.tdtu.tdtbank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class VnPayUrlBuilderTest {

    @Test
    public void hmacSha512_matchesRfc4231TestCase1() {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            key.append((char) 0x0b);
        }
        assertEquals("87aa7cdea5ef619d4ff0b4241a1d6cb02379f4e2ce4ec2787ad0b30545e17cde"
                        + "daa833b7d6b8a702038b274eaea3f4e4be9d914eeb61f1702e696c203a126854",
                VnPayUrlBuilder.hmacSha512(key.toString(), "Hi There"));
    }

    @Test
    public void builtUrl_isSignedAndRoundTripVerifies() throws Exception {
        VnPayUrlBuilder b = new VnPayUrlBuilder("TESTCODE", "SECRETKEY", VnPayUrlBuilder.SANDBOX_URL,
                "https://tdtbank.app/vnpay-return");
        assertTrue(b.isConfigured());
        String url = b.buildPaymentUrl("TX123", 150_000, "Nap tien TX123", "127.0.0.1", new Date(1767225600000L)); // 2026-01-01T00:00:00Z
        assertTrue(url.startsWith(VnPayUrlBuilder.SANDBOX_URL + "?"));
        Map<String, String> q = new HashMap<>();
        for (String kv : url.substring(url.indexOf('?') + 1).split("&")) {
            String[] p = kv.split("=", 2);
            q.put(p[0], URLDecoder.decode(p[1], "UTF-8"));
        }
        assertEquals("15000000", q.get("vnp_Amount"));
        assertEquals("20260101070000", q.get("vnp_CreateDate")); // GMT+7
        assertTrue(b.verifyReturn(q));
        q.put("vnp_Amount", "1");
        assertFalse(b.verifyReturn(q)); // bị sửa -> chữ ký sai
    }

    @Test
    public void unconfiguredBuilder_reportsNotConfigured() {
        assertFalse(new VnPayUrlBuilder("", "", VnPayUrlBuilder.SANDBOX_URL, "x").isConfigured());
    }

    @Test
    public void successRequiresBothCodesToBeZeroZero() {
        Map<String, String> m = new HashMap<>();
        m.put("vnp_ResponseCode", "00");
        m.put("vnp_TransactionStatus", "00");
        assertTrue(VnPayUrlBuilder.isSuccess(m));
        m.put("vnp_TransactionStatus", "02");
        assertFalse(VnPayUrlBuilder.isSuccess(m));
    }
}
