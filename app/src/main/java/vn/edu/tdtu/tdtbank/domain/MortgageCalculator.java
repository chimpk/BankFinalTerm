package vn.edu.tdtu.tdtbank.domain;

import vn.edu.tdtu.tdtbank.model.PaymentFrequency;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Lịch trả nợ vay thế chấp theo phương pháp niên kim (số tiền trả mỗi kỳ cố định). */
public final class MortgageCalculator {

    public static final class Installment {
        public final int number;
        public final LocalDate dueDate;
        public final long payment;
        public final long interest;
        public final long principal;
        /** Dư nợ gốc sau khi trả kỳ này. */
        public final long remaining;

        Installment(int number, LocalDate dueDate, long payment, long interest, long principal, long remaining) {
            this.number = number;
            this.dueDate = dueDate;
            this.payment = payment;
            this.interest = interest;
            this.principal = principal;
            this.remaining = remaining;
        }
    }

    private MortgageCalculator() {
    }

    public static int periodsPerYear(PaymentFrequency f) {
        return f == PaymentFrequency.BIWEEKLY ? 26 : 12;
    }

    public static int numberOfPeriods(int termMonths, PaymentFrequency f) {
        if (f == PaymentFrequency.BIWEEKLY) {
            return (int) Math.max(1, Math.round(termMonths / 12.0 * 26));
        }
        return Math.max(1, termMonths);
    }

    /** Số tiền phải trả mỗi kỳ (đã gồm lãi), làm tròn đến đồng. */
    public static long periodicPayment(long principal, double annualRatePercent, int termMonths, PaymentFrequency f) {
        int n = numberOfPeriods(termMonths, f);
        double r = annualRatePercent / 100.0 / periodsPerYear(f);
        if (r == 0) {
            return Math.round((double) principal / n);
        }
        double pmt = principal * r / (1 - Math.pow(1 + r, -n));
        return Math.round(pmt);
    }

    public static List<Installment> schedule(long principal, double annualRatePercent, int termMonths,
                                             PaymentFrequency f, LocalDate start) {
        int n = numberOfPeriods(termMonths, f);
        double r = annualRatePercent / 100.0 / periodsPerYear(f);
        long pmt = periodicPayment(principal, annualRatePercent, termMonths, f);
        List<Installment> list = new ArrayList<>(n);
        long remaining = principal;
        for (int i = 1; i <= n; i++) {
            long interest = Math.round(remaining * r);
            long principalPart = pmt - interest;
            long payment = pmt;
            if (i == n || principalPart > remaining) { // kỳ cuối tất toán phần gốc còn lại
                principalPart = remaining;
                payment = principalPart + interest;
            }
            remaining -= principalPart;
            LocalDate due = f == PaymentFrequency.BIWEEKLY ? start.plusWeeks(2L * i) : start.plusMonths(i);
            list.add(new Installment(i, due, payment, interest, principalPart, remaining));
        }
        return list;
    }

    /** Chỉ số (0-based) của kỳ phải trả tiếp theo dựa vào dư nợ hiện tại; -1 nếu đã tất toán. */
    public static int nextInstallmentIndex(List<Installment> schedule, long principal, long outstanding) {
        if (outstanding <= 0) {
            return -1;
        }
        if (outstanding >= principal) {
            return 0;
        }
        for (int i = 0; i < schedule.size(); i++) {
            if (schedule.get(i).remaining <= outstanding) {
                return i + 1 < schedule.size() ? i + 1 : -1;
            }
        }
        return -1;
    }
}
