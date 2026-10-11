package vn.edu.tdtu.tdtbank.domain;

import java.security.SecureRandom;

/** Sinh số tài khoản 10 chữ số, chữ số đầu khác 0. Việc kiểm tra trùng do repository đảm nhiệm. */
public final class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private AccountNumberGenerator() {
    }

    public static String generate() {
        return generate(RANDOM);
    }

    public static String generate(java.util.Random random) {
        long n = 1_000_000_000L + (long) (random.nextDouble() * 9_000_000_000L);
        return Long.toString(n);
    }
}
