package vn.edu.tdtu.tdtbank.domain;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Tạo URL thanh toán VNPay (phiên bản 2.1.0) và xác minh chữ ký URL trả về (HMAC-SHA512).
 *
 * CẢNH BÁO BẢO MẬT: HashSecret nằm trong ứng dụng client chỉ chấp nhận được cho sandbox/demo.
 * Khi chạy thật, chữ ký và xác nhận IPN phải thực hiện ở máy chủ.
 * Lớp này CHƯA được kiểm thử với cổng VNPay thật vì nhóm chưa có tài khoản sandbox.
 */
public class VnPayUrlBuilder {

    public static final String SANDBOX_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    private final String tmnCode;
    private final String hashSecret;
    private final String baseUrl;
    private final String returnUrl;

    public VnPayUrlBuilder(String tmnCode, String hashSecret, String baseUrl, String returnUrl) {
        this.tmnCode = tmnCode;
        this.hashSecret = hashSecret;
        this.baseUrl = baseUrl;
        this.returnUrl = returnUrl;
    }

    public boolean isConfigured() {
        return tmnCode != null && !tmnCode.isEmpty() && hashSecret != null && !hashSecret.isEmpty();
    }

    public String buildPaymentUrl(String txnRef, long amountVnd, String orderInfo, String ipAddr, Date now) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", Long.toString(amountVnd * 100)); // VNPay yêu cầu nhân 100
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_CreateDate", fmt.format(now));
        params.put("vnp_ExpireDate", fmt.format(new Date(now.getTime() + 15 * 60_000L)));
        String data = canonical(params);
        return baseUrl + "?" + data + "&vnp_SecureHash=" + hmacSha512(hashSecret, data);
    }

    /** Xác minh chữ ký của các tham số mà VNPay gắn vào ReturnUrl. */
    public boolean verifyReturn(Map<String, String> returned) {
        String given = returned.get("vnp_SecureHash");
        if (given == null) {
            return false;
        }
        Map<String, String> params = new TreeMap<>();
        for (Map.Entry<String, String> e : returned.entrySet()) {
            String k = e.getKey();
            if (k.startsWith("vnp_") && !k.equals("vnp_SecureHash") && !k.equals("vnp_SecureHashType")
                    && e.getValue() != null && !e.getValue().isEmpty()) {
                params.put(k, e.getValue());
            }
        }
        String expected = hmacSha512(hashSecret, canonical(params));
        return java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), given.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isSuccess(Map<String, String> returned) {
        return "00".equals(returned.get("vnp_ResponseCode")) && "00".equals(returned.get("vnp_TransactionStatus"));
    }

    private static String canonical(Map<String, String> sortedParams) {
        StringBuilder sb = new StringBuilder();
        for (String name : new ArrayList<>(sortedParams.keySet())) {
            String value = sortedParams.get(name);
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            try {
                sb.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }
        return sb.toString();
    }

    static String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
