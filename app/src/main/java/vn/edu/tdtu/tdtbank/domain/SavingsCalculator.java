package vn.edu.tdtu.tdtbank.domain;

/** Tính lãi tiết kiệm (lãi đơn, không nhập gốc) từ lãi suất %/năm. */
public final class SavingsCalculator {

    private SavingsCalculator() {
    }

    /** Lợi nhuận mỗi tháng = số dư * lãi suất / 12. */
    public static long monthlyProfit(long balance, double annualRatePercent) {
        return Math.round(balance * annualRatePercent / 100.0 / 12.0);
    }

    /** Tổng lãi khi đáo hạn sau termMonths tháng. */
    public static long profitAtMaturity(long balance, double annualRatePercent, int termMonths) {
        return Math.round(balance * annualRatePercent / 100.0 * termMonths / 12.0);
    }
}
