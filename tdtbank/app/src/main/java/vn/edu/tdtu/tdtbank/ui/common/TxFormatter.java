package vn.edu.tdtu.tdtbank.ui.common;

import android.content.Context;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.model.BankTransaction;
import vn.edu.tdtu.tdtbank.model.TxType;
import vn.edu.tdtu.tdtbank.utils.MoneyFormat;

/** Quy tắc hiển thị giao dịch theo góc nhìn của người dùng. */
public final class TxFormatter {

    private TxFormatter() {
    }

    public static int titleRes(TxType type) {
        switch (type) {
            case DEPOSIT:
                return R.string.tx_deposit;
            case WITHDRAW:
                return R.string.tx_withdraw;
            case TRANSFER_INTERNAL:
                return R.string.tx_transfer_internal;
            case TRANSFER_INTERBANK:
                return R.string.tx_transfer_interbank;
            case UTILITY_PAYMENT:
                return R.string.tx_utility;
            default:
                return R.string.tx_mortgage_payment;
        }
    }

    /** Tiền vào tài khoản của người xem? (nạp tiền, hoặc nhận chuyển khoản từ người khác). */
    public static boolean isIncoming(BankTransaction t, String viewerUid) {
        return t.type == TxType.DEPOSIT || !viewerUid.equals(t.ownerUid);
    }

    /** Số tiền có dấu: +amount khi tiền vào, -(amount+fee) khi tiền ra. */
    public static String signedAmount(BankTransaction t, String viewerUid) {
        if (isIncoming(t, viewerUid)) {
            return "+" + MoneyFormat.vnd(t.amount);
        }
        return "-" + MoneyFormat.vnd(t.amount + t.fee);
    }

    public static String title(Context c, BankTransaction t) {
        return c.getString(titleRes(t.type));
    }
}
