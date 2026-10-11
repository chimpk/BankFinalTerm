package vn.edu.tdtu.tdtbank.model;

import java.util.UUID;

/** Dữ liệu một giao dịch đang soạn, chưa được ghi nhận. txId sinh sẵn để thử lại không bị ghi trùng. */
public class TxDraft {
    public final String txId = UUID.randomUUID().toString();
    public TxType type;
    public Account from;
    public String toAccountNumber;
    public String toOwnerUid;
    public String counterpartyName;
    public String counterpartyBank;
    public long amount;
    public long principalPortion;
    public long toDebt;
    public String description;
    public UtilityCategory category;
    public String reference;
    public String gateway;

    /** Dấu vân tay gắn OTP với đúng giao dịch này. */
    public String fingerprint() {
        return txId + "|" + type + "|" + amount + "|" + toAccountNumber;
    }
}
