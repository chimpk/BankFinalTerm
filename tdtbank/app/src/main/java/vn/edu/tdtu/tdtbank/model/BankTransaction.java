package vn.edu.tdtu.tdtbank.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.List;

/** Giao dịch bất biến (transactions/{txId}); chỉ được tạo, không sửa/xóa. */
public class BankTransaction {
    @DocumentId
    public String id;
    public TxType type;
    public String ownerUid;
    /** Số tài khoản nguồn, hoặc "EXTERNAL" (nạp tiền từ cổng thanh toán). */
    public String fromAccountId;
    /** Số tài khoản đích, hoặc "CASH", "INTERBANK", "UTILITY". */
    public String toAccountId;
    public List<String> participantUids;
    public List<String> accountIds;
    public long amount;
    public long fee;
    /** Phần nợ gốc giảm đi (chỉ MORTGAGE_PAYMENT). */
    public long principalPortion;
    public String description;
    public UtilityCategory category;
    public String reference;
    public String counterpartyBank;
    public String counterpartyName;
    /** SANDBOX hoặc VNPAY (chỉ DEPOSIT). */
    public String gateway;
    public String status;
    public boolean otpVerified;
    public boolean faceVerified;
    @ServerTimestamp
    public Timestamp createdAt;

    public BankTransaction() {
    }
}
