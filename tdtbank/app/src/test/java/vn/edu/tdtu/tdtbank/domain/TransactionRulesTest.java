package vn.edu.tdtu.tdtbank.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import vn.edu.tdtu.tdtbank.domain.TransactionRules.Error;
import vn.edu.tdtu.tdtbank.model.AccountType;
import vn.edu.tdtu.tdtbank.model.TxType;

import org.junit.Test;

public class TransactionRulesTest {

    private static Error internal(long amount, long balance, String from, String to, long today) {
        return TransactionRules.validate(TxType.TRANSFER_INTERNAL, amount, balance, true,
                AccountType.CHECKING, from, to, today, 0, 0);
    }

    @Test
    public void validInternalTransfer_passes() {
        assertEquals(Error.NONE, internal(1_000_000, 5_000_000, "1000000001", "1000000002", 0));
    }

    @Test
    public void amountBelowMinimumOrAboveMaximum_isRejected() {
        assertEquals(Error.AMOUNT_TOO_SMALL, internal(9_999, 5_000_000, "1000000001", "1000000002", 0));
        assertEquals(Error.AMOUNT_TOO_LARGE, internal(200_000_001, 900_000_000, "1000000001", "1000000002", 0));
    }

    @Test
    public void insufficientFunds_isRejected_andExactBalanceIsAccepted() {
        assertEquals(Error.INSUFFICIENT_FUNDS, internal(1_000_001, 1_000_000, "1000000001", "1000000002", 0));
        assertEquals(Error.NONE, internal(1_000_000, 1_000_000, "1000000001", "1000000002", 0));
    }

    @Test
    public void interbankFee_isIncludedInBalanceCheck() {
        long amount = 1_000_000;
        assertEquals(Error.INSUFFICIENT_FUNDS, TransactionRules.validate(TxType.TRANSFER_INTERBANK, amount,
                amount, true, AccountType.CHECKING, "1000000001", "123456789", 0, 0, 0));
        assertEquals(Error.NONE, TransactionRules.validate(TxType.TRANSFER_INTERBANK, amount,
                amount + TransactionRules.INTERBANK_FEE, true, AccountType.CHECKING, "1000000001", "123456789", 0, 0, 0));
    }

    @Test
    public void sameAccountAndBadNumbers_areRejected() {
        assertEquals(Error.SAME_ACCOUNT, internal(100_000, 5_000_000, "1000000001", "1000000001", 0));
        assertEquals(Error.INVALID_DESTINATION, internal(100_000, 5_000_000, "1000000001", "12345", 0));
        assertEquals(Error.INVALID_DESTINATION, internal(100_000, 5_000_000, "1000000001", null, 0));
    }

    @Test
    public void dailyLimit_isEnforced() {
        assertEquals(Error.DAILY_LIMIT_EXCEEDED, internal(100_000, 900_000_000, "1000000001", "1000000002", 499_950_000));
        assertEquals(Error.NONE, internal(100_000, 900_000_000, "1000000001", "1000000002", 499_900_000));
    }

    @Test
    public void mortgageAccount_cannotBeUsedAsSource() {
        assertEquals(Error.SOURCE_NOT_ALLOWED, TransactionRules.validate(TxType.WITHDRAW, 100_000, 9_000_000,
                true, AccountType.MORTGAGE, "1000000003", "CASH", 0, 0, 0));
        assertEquals(Error.SOURCE_NOT_ALLOWED, TransactionRules.validate(TxType.WITHDRAW, 100_000, 9_000_000,
                false, AccountType.CHECKING, "1000000001", "CASH", 0, 0, 0));
    }

    @Test
    public void deposit_ignoresSourceBalance() {
        assertEquals(Error.NONE, TransactionRules.validate(TxType.DEPOSIT, 50_000, 0, true,
                AccountType.CHECKING, "EXTERNAL", "1000000001", 0, 0, 0));
    }

    @Test
    public void mortgagePayment_principalCannotExceedDebtOrPayment() {
        assertEquals(Error.NONE, TransactionRules.validate(TxType.MORTGAGE_PAYMENT, 5_000_000, 6_000_000, true,
                AccountType.CHECKING, "1000000001", "1000000003", 0, 100_000_000, 4_000_000));
        assertEquals(Error.EXCEEDS_DEBT, TransactionRules.validate(TxType.MORTGAGE_PAYMENT, 5_000_000, 6_000_000, true,
                AccountType.CHECKING, "1000000001", "1000000003", 0, 3_000_000, 4_000_000));
        assertEquals(Error.EXCEEDS_DEBT, TransactionRules.validate(TxType.MORTGAGE_PAYMENT, 5_000_000, 6_000_000, true,
                AccountType.CHECKING, "1000000001", "1000000003", 0, 100_000_000, 5_000_001));
    }

    @Test
    public void faceVerificationThreshold() {
        assertFalse(TransactionRules.requiresFaceVerification(9_999_999));
        assertTrue(TransactionRules.requiresFaceVerification(10_000_000));
        assertEquals(0L, TransactionRules.feeFor(TxType.TRANSFER_INTERNAL));
        assertEquals(7_700L, TransactionRules.feeFor(TxType.TRANSFER_INTERBANK));
    }
}
