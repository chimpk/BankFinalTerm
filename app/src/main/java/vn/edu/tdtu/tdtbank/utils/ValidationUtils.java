package vn.edu.tdtu.tdtbank.utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final Pattern USERNAME = Pattern.compile("^[a-z][a-z0-9._-]{2,31}$");
    private static final Pattern PHONE = Pattern.compile("^0[0-9]{9}$");
    private static final Pattern NATIONAL_ID = Pattern.compile("^([0-9]{9}|[0-9]{12})$");
    private static final Pattern ACCOUNT_NUMBER = Pattern.compile("^[0-9]{10}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@ ]+@[^@ ]+[.][^@ ]+$");

    private ValidationUtils() {
        throw new AssertionError("Không được khởi tạo lớp tiện ích");
    }

    public static boolean isValidUsername(String v) {
        return v != null && USERNAME.matcher(v).matches();
    }

    public static boolean isValidPhone(String v) {
        return v != null && PHONE.matcher(v).matches();
    }

    public static boolean isValidNationalId(String v) {
        return v != null && NATIONAL_ID.matcher(v).matches();
    }

    public static boolean isValidAccountNumber(String v) {
        return v != null && ACCOUNT_NUMBER.matcher(v).matches();
    }

    /** Email liên hệ là tùy chọn: rỗng được xem là hợp lệ. */
    public static boolean isValidOptionalEmail(String v) {
        return v == null || v.isEmpty() || (v.length() <= 254 && EMAIL.matcher(v).matches());
    }

    /** Firebase Email/Password yêu cầu tối thiểu 6 ký tự. */
    public static boolean isValidPassword(String v) {
        return v != null && v.length() >= 6 && v.length() <= 64;
    }

    public static boolean isValidFullName(String v) {
        return v != null && v.trim().length() >= 2 && v.trim().length() <= 100;
    }

    /** Ngày sinh yyyy-MM-dd, đủ 18 tuổi trở lên. */
    public static boolean isAdultDateOfBirth(String iso, LocalDate today) {
        if (iso == null) {
            return false;
        }
        try {
            LocalDate dob = LocalDate.parse(iso);
            return !dob.isAfter(today) && Period.between(dob, today).getYears() >= 18;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
