package vn.edu.tdtu.tdtbank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import vn.edu.tdtu.tdtbank.model.PaymentFrequency;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

public class CalculatorsTest {

    @Test
    public void savingsMonthlyProfit() {
        assertEquals(50_000L, SavingsCalculator.monthlyProfit(10_000_000, 6.0));
        assertEquals(600_000L, SavingsCalculator.profitAtMaturity(10_000_000, 6.0, 12));
        assertEquals(0L, SavingsCalculator.monthlyProfit(0, 6.0));
    }

    @Test
    public void mortgageMonthlyPayment_matchesAnnuityFormula() {
        // 100.000.000 vay 12 tháng, 12%/năm -> PMT = 8.884.879
        assertEquals(8_884_879L, MortgageCalculator.periodicPayment(100_000_000, 12.0, 12, PaymentFrequency.MONTHLY));
    }

    @Test
    public void mortgageSchedule_endsAtZero_andPrincipalSumsToLoan() {
        for (PaymentFrequency f : PaymentFrequency.values()) {
            List<MortgageCalculator.Installment> s = MortgageCalculator.schedule(
                    250_000_000, 9.5, 60, f, LocalDate.of(2026, 1, 1));
            assertEquals(MortgageCalculator.numberOfPeriods(60, f), s.size());
            assertEquals(0L, s.get(s.size() - 1).remaining);
            long sum = 0;
            for (MortgageCalculator.Installment i : s) {
                sum += i.principal;
                assertEquals(i.payment, i.interest + i.principal);
            }
            assertEquals(250_000_000L, sum);
        }
    }

    @Test
    public void biweeklySchedule_hasFourteenDaySpacing_and26PeriodsPerYear() {
        List<MortgageCalculator.Installment> s = MortgageCalculator.schedule(
                50_000_000, 8.0, 12, PaymentFrequency.BIWEEKLY, LocalDate.of(2026, 1, 1));
        assertEquals(26, s.size());
        assertEquals(LocalDate.of(2026, 1, 15), s.get(0).dueDate);
        assertEquals(LocalDate.of(2026, 1, 29), s.get(1).dueDate);
    }

    @Test
    public void zeroInterestMortgage_splitsEvenly() {
        assertEquals(1_000_000L, MortgageCalculator.periodicPayment(12_000_000, 0, 12, PaymentFrequency.MONTHLY));
    }

    @Test
    public void nextInstallmentIndex_followsOutstandingDebt() {
        List<MortgageCalculator.Installment> s = MortgageCalculator.schedule(
                100_000_000, 12.0, 12, PaymentFrequency.MONTHLY, LocalDate.of(2026, 1, 1));
        assertEquals(0, MortgageCalculator.nextInstallmentIndex(s, 100_000_000, 100_000_000));
        assertEquals(1, MortgageCalculator.nextInstallmentIndex(s, 100_000_000, s.get(0).remaining));
        assertEquals(-1, MortgageCalculator.nextInstallmentIndex(s, 100_000_000, 0));
        assertEquals(11, MortgageCalculator.nextInstallmentIndex(s, 100_000_000, s.get(10).remaining));
    }

    @Test
    public void accountNumbers_haveTenDigitsAndNoLeadingZero() {
        for (int i = 0; i < 1000; i++) {
            String n = AccountNumberGenerator.generate();
            assertTrue(n, n.matches("[1-9][0-9]{9}"));
        }
    }

    @Test
    public void haversine_andNearest() {
        // Quận 7 -> Quận 1 khoảng 5-6 km
        double d = GeoUtils.haversineMeters(10.7325, 106.6993, 10.7769, 106.7009);
        assertTrue(d > 4_500 && d < 5_500);
        List<double[]> pts = java.util.Arrays.asList(
                new double[]{10.80, 106.70}, new double[]{10.7330, 106.6990}, new double[]{10.70, 106.65});
        List<double[]> near = GeoUtils.nearest(pts, p -> p[0], p -> p[1], 10.7325, 106.6993, 2);
        assertEquals(2, near.size());
        assertEquals(10.7330, near.get(0)[0], 1e-9);
    }
}
