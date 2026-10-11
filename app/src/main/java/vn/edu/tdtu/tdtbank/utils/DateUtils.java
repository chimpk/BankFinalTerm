package vn.edu.tdtu.tdtbank.utils;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtils {

    public static final TimeZone VN_TZ = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DateUtils() {
    }

    public static String formatDateTime(Timestamp ts) {
        if (ts == null) {
            return "—";
        }
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN"));
        f.setTimeZone(VN_TZ);
        return f.format(ts.toDate());
    }

    public static String formatDate(LocalDate d) {
        return d.format(DISPLAY_DATE);
    }

    public static String todayIso() {
        return LocalDate.now(VN_TZ.toZoneId()).toString();
    }

    public static Date startOfToday() {
        Calendar c = Calendar.getInstance(VN_TZ);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /** Khoảng [đầu tháng, đầu tháng sau) cho month 1..12. */
    public static Date[] monthRange(int year, int month) {
        Calendar c = Calendar.getInstance(VN_TZ);
        c.clear();
        c.set(year, month - 1, 1, 0, 0, 0);
        Date start = c.getTime();
        c.add(Calendar.MONTH, 1);
        return new Date[]{start, c.getTime()};
    }
}
