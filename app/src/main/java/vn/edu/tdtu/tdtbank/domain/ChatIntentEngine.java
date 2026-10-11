package vn.edu.tdtu.tdtbank.domain;

import vn.edu.tdtu.tdtbank.utils.TextNormalizer;

import java.util.LinkedHashMap;
import java.util.Map;

/** Nhận diện ý định tiếng Việt (không dấu, không phân biệt hoa thường) bằng từ khóa có trọng số. */
public final class ChatIntentEngine {

    public enum Intent {
        GREETING, BALANCE, TRANSFER, PAY_ELECTRICITY, PAY_WATER, PHONE_TOPUP, MONTH_TRANSACTIONS,
        FLIGHT, MOVIE, HOTEL, SHOPPING, NEAREST_BRANCH, SAVING_RATE, MORTGAGE, DEPOSIT, WITHDRAW,
        KYC, SECURITY_OTP, HELP, UNKNOWN
    }

    private static final Map<Intent, String[]> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put(Intent.MONTH_TRANSACTIONS, new String[]{"giao dich thang", "sao ke", "lich su", "chi tieu"});
        KEYWORDS.put(Intent.PAY_ELECTRICITY, new String[]{"tien dien", "hoa don dien", "evn"});
        KEYWORDS.put(Intent.PAY_WATER, new String[]{"tien nuoc", "hoa don nuoc", "cap nuoc"});
        KEYWORDS.put(Intent.PHONE_TOPUP, new String[]{"nap tien dien thoai", "nap dien thoai", "nap sim", "top up", "nap the", "dien thoai"});
        KEYWORDS.put(Intent.FLIGHT, new String[]{"ve may bay", "chuyen bay", "bay"});
        KEYWORDS.put(Intent.MOVIE, new String[]{"ve xem phim", "xem phim", "rap phim", "ve phim"});
        KEYWORDS.put(Intent.HOTEL, new String[]{"khach san", "dat phong", "phong khach san"});
        KEYWORDS.put(Intent.SHOPPING, new String[]{"mua sam", "thuong mai dien tu", "thanh toan don hang", "mua hang"});
        KEYWORDS.put(Intent.NEAREST_BRANCH, new String[]{"chi nhanh", "phong giao dich", "ban do", "duong di", "gan nhat", "atm"});
        KEYWORDS.put(Intent.SAVING_RATE, new String[]{"lai suat", "tiet kiem", "loi nhuan"});
        KEYWORDS.put(Intent.MORTGAGE, new String[]{"vay", "the chap", "tra no", "khoan vay"});
        KEYWORDS.put(Intent.TRANSFER, new String[]{"chuyen tien", "chuyen khoan", "gui tien cho"});
        KEYWORDS.put(Intent.DEPOSIT, new String[]{"nap tien", "gui tien", "nap vao"});
        KEYWORDS.put(Intent.WITHDRAW, new String[]{"rut tien", "rut"});
        KEYWORDS.put(Intent.BALANCE, new String[]{"so du", "tai khoan cua toi", "con bao nhieu"});
        KEYWORDS.put(Intent.KYC, new String[]{"ekyc", "xac minh", "khuon mat", "dinh danh"});
        KEYWORDS.put(Intent.SECURITY_OTP, new String[]{"otp", "bao mat", "xac thuc", "2fa"});
        KEYWORDS.put(Intent.GREETING, new String[]{"xin chao", "chao", "hello", "hi"});
        KEYWORDS.put(Intent.HELP, new String[]{"giup", "huong dan", "lam duoc gi", "chuc nang"});
    }

    private ChatIntentEngine() {
    }

    public static Intent detect(String message) {
        if (message == null) {
            return Intent.UNKNOWN;
        }
        String text = " " + TextNormalizer.normalize(message) + " ";
        Intent best = Intent.UNKNOWN;
        int bestScore = 0;
        for (Map.Entry<Intent, String[]> e : KEYWORDS.entrySet()) {
            int score = 0;
            for (String kw : e.getValue()) {
                if (text.contains(" " + kw + " ") || (kw.length() > 3 && text.contains(kw))) {
                    score += kw.split(" ").length; // cụm từ dài có trọng số cao hơn
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = e.getKey();
            }
        }
        return best;
    }
}
