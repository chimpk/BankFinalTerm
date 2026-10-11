package vn.edu.tdtu.tdtbank.data.remote;

import vn.edu.tdtu.tdtbank.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Trợ lý AI tùy chọn qua Gemini REST. Chỉ bật khi local.properties có gemini.apiKey.
 * Khóa nằm trong APK nên chỉ phù hợp demo; môi trường thật phải gọi qua máy chủ trung gian.
 * Chưa được kiểm thử với API thật (nhóm chưa có khóa).
 */
public class GeminiClient {

    private static final String SYSTEM_PROMPT =
            "Bạn là trợ lý của ứng dụng ngân hàng di động TDT Bank. Trả lời ngắn gọn bằng tiếng Việt. "
                    + "Chỉ giải thích tính năng ngân hàng thông thường; không bao giờ yêu cầu mật khẩu hoặc OTP; "
                    + "không đưa lời khuyên đầu tư; không bịa số dư hay thông tin tài khoản.";

    public static boolean isConfigured() {
        return !BuildConfig.GEMINI_API_KEY.isEmpty();
    }

    /** Gọi mạng - phải chạy trên luồng nền. */
    public String ask(String userMessage) throws IOException, JSONException {
        URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/"
                + BuildConfig.GEMINI_MODEL + ":generateContent");
        JSONObject body = new JSONObject()
                .put("systemInstruction", new JSONObject().put("parts",
                        new JSONArray().put(new JSONObject().put("text", SYSTEM_PROMPT))))
                .put("contents", new JSONArray().put(new JSONObject().put("role", "user").put("parts",
                        new JSONArray().put(new JSONObject().put("text", userMessage)))));
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(8000);
        c.setReadTimeout(20000);
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        c.setRequestProperty("x-goog-api-key", BuildConfig.GEMINI_API_KEY);
        try (OutputStream os = c.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        try {
            if (c.getResponseCode() != 200) {
                throw new IOException("HTTP " + c.getResponseCode());
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            return parseText(sb.toString());
        } finally {
            c.disconnect();
        }
    }

    public static String parseText(String json) throws JSONException {
        JSONArray candidates = new JSONObject(json).getJSONArray("candidates");
        return candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
                .getJSONObject(0).getString("text").trim();
    }
}
