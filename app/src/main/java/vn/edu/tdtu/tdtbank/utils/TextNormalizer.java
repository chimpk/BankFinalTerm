package vn.edu.tdtu.tdtbank.utils;

import java.text.Normalizer;
import java.util.Locale;

public final class TextNormalizer {

    private TextNormalizer() {
    }

    /** Hạ chữ thường, bỏ dấu tiếng Việt, đ -> d, gom khoảng trắng và bỏ dấu câu. */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String s = input.toLowerCase(Locale.ROOT).replace('đ', 'd');
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        s = s.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        return s;
    }
}
