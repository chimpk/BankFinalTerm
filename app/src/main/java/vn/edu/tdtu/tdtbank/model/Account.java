package vn.edu.tdtu.tdtbank.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

/**
 * Tài khoản ngân hàng (accounts/{accountNumber}).
 * CHECKING/SAVING: balance = số dư. MORTGAGE: balance = dư nợ gốc còn lại, principal = số tiền vay ban đầu.
 */
public class Account {
    public String accountNumber;
    public String ownerUid;
    public String ownerName;
    public AccountType type;
    public AccountStatus status;
    public long balance;
    /** Lãi suất %/năm (SAVING: lãi gửi, MORTGAGE: lãi vay). */
    public double interestRate;
    /** Kỳ hạn tính bằng tháng (SAVING, MORTGAGE). */
    public int termMonths;
    /** Số tiền vay ban đầu (MORTGAGE). */
    public long principal;
    public PaymentFrequency paymentFrequency;
    /** yyyy-MM-dd: ngày bắt đầu kỳ hạn / giải ngân. */
    public String startDate;
    public String lastTxId;
    @ServerTimestamp
    public Timestamp createdAt;

    public Account() {
    }
}
