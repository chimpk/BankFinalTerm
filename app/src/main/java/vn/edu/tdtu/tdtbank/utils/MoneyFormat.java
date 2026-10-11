package vn.edu.tdtu.tdtbank.utils;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyFormat {

    private static final Locale VI = new Locale("vi", "VN");

    private MoneyFormat() {
    }

    public static String vnd(long amount) {
        return NumberFormat.getInstance(VI).format(amount) + " ₫";
    }

    public static String percent(double rate) {
        return String.format(VI, "%.2f%%", rate);
    }

    /** Lấy số nguyên từ chuỗi người dùng nhập (bỏ dấu chấm/phẩy/khoảng trắng); -1 nếu rỗng hoặc quá lớn. */
    public static long parse(CharSequence input) {
        if (input == null) {
            return -1;
        }
        String digits = input.toString().replaceAll("[^0-9]", "");
        if (digits.isEmpty() || digits.length() > 15) {
            return -1;
        }
        return Long.parseLong(digits);
    }
}
