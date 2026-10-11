package vn.edu.tdtu.tdtbank.domain;

import vn.edu.tdtu.tdtbank.model.AccountType;
import vn.edu.tdtu.tdtbank.model.TxType;

import java.util.regex.Pattern;

/**
 * Quy tắc nghiệp vụ kiểm tra giao dịch phía ứng dụng. Các hằng số giới hạn được phản chiếu
 * trong firestore.rules (lớp thực thi cuối cùng). Lớp này thuần Java nên có thể kiểm thử bằng JUnit.
 */
public final class TransactionRules {

    public static final long MIN_AMOUNT = 10_000L;
    public static final long MAX_AMOUNT = 200_000_000L;
    /** Từ ngưỡng này trở lên bắt buộc xác minh khuôn mặt (đề: giao dịch giá trị cao). */
    public static final long HIGH_VALUE_THRESHOLD = 10_000_000L;
    public static final long DAILY_LIMIT = 500_000_000L;
    public static final long INTERBANK_FEE = 7_700L;

    private static final Pattern INTERNAL_ACCOUNT = Pattern.compile("^[0-9]{10}$");
    private static final Pattern EXTERNAL_ACCOUNT = Pattern.compile("^[0-9]{6,16}$");

    public enum Error {
        NONE,
        AMOUNT_TOO_SMALL,
        AMOUNT_TOO_LARGE,
        SOURCE_NOT_ALLOWED,
        INSUFFICIENT_FUNDS,
        DAILY_LIMIT_EXCEEDED,
        SAME_ACCOUNT,
        INVALID_DESTINATION,
        EXCEEDS_DEBT
    }

    private TransactionRules() {
    }

    public static long feeFor(TxType type) {
        return type == TxType.TRANSFER_INTERBANK ? INTERBANK_FEE : 0L;
    }

    public static boolean requiresFaceVerification(long amount) {
        return amount >= HIGH_VALUE_THRESHOLD;
    }

    public static boolean isOutgoing(TxType type) {
        return type != TxType.DEPOSIT;
    }

    /**
     * @param sourceBalance   số dư tài khoản nguồn (bỏ qua với DEPOSIT)
     * @param sourceActive    tài khoản nguồn còn hoạt động
     * @param sourceType      loại tài khoản nguồn (bỏ qua với DEPOSIT)
     * @param todayOutgoing   tổng tiền đã chi trong ngày (không gồm giao dịch này)
     * @param destDebt        dư nợ của tài khoản vay (chỉ MORTGAGE_PAYMENT), nếu không thì 0
     * @param principalPortion phần gốc trả trong kỳ (chỉ MORTGAGE_PAYMENT)
     */
    public static Error validate(TxType type, long amount, long sourceBalance, boolean sourceActive,
                                 AccountType sourceType, String fromNo, String toNo,
                                 long todayOutgoing, long destDebt, long principalPortion) {
        if (amount < MIN_AMOUNT) {
            return Error.AMOUNT_TOO_SMALL;
        }
        if (amount > MAX_AMOUNT) {
            return Error.AMOUNT_TOO_LARGE;
        }
        if (isOutgoing(type)) {
            if (!sourceActive || sourceType == AccountType.MORTGAGE) {
                return Error.SOURCE_NOT_ALLOWED;
            }
            if (sourceBalance < amount + feeFor(type)) {
                return Error.INSUFFICIENT_FUNDS;
            }
            if (todayOutgoing + amount > DAILY_LIMIT) {
                return Error.DAILY_LIMIT_EXCEEDED;
            }
        }
        switch (type) {
            case TRANSFER_INTERNAL:
                if (toNo == null || !INTERNAL_ACCOUNT.matcher(toNo).matches()) {
                    return Error.INVALID_DESTINATION;
                }
                if (toNo.equals(fromNo)) {
                    return Error.SAME_ACCOUNT;
                }
                break;
            case TRANSFER_INTERBANK:
                if (toNo == null || !EXTERNAL_ACCOUNT.matcher(toNo).matches()) {
                    return Error.INVALID_DESTINATION;
                }
                break;
            case MORTGAGE_PAYMENT:
                if (sourceType != AccountType.CHECKING) {
                    return Error.SOURCE_NOT_ALLOWED;
                }
                if (principalPortion < 0 || principalPortion > amount || principalPortion > destDebt) {
                    return Error.EXCEEDS_DEBT;
                }
                break;
            default:
                break;
        }
        return Error.NONE;
    }
}
