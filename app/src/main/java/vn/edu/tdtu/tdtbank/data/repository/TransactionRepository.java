package vn.edu.tdtu.tdtbank.data.repository;

import androidx.lifecycle.LiveData;

import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.firebase.FirebaseClient;
import vn.edu.tdtu.tdtbank.data.firebase.QueryLiveData;
import vn.edu.tdtu.tdtbank.domain.TransactionRules;
import vn.edu.tdtu.tdtbank.model.BankTransaction;
import vn.edu.tdtu.tdtbank.model.TxDraft;
import vn.edu.tdtu.tdtbank.model.TxType;
import vn.edu.tdtu.tdtbank.utils.Constants;
import vn.edu.tdtu.tdtbank.utils.DateUtils;
import vn.edu.tdtu.tdtbank.utils.ErrorMapper;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TransactionRepository {

    private final FirebaseFirestore db = FirebaseClient.db();

    private CollectionReference transactions() {
        return db.collection(Constants.COLLECTION_TRANSACTIONS);
    }

    private DocumentReference account(String number) {
        return db.collection(Constants.COLLECTION_ACCOUNTS).document(number);
    }

    private static int byNewest(BankTransaction a, BankTransaction b) {
        long ta = a.createdAt == null ? Long.MAX_VALUE : a.createdAt.toDate().getTime();
        long tb = b.createdAt == null ? Long.MAX_VALUE : b.createdAt.toDate().getTime();
        return Long.compare(tb, ta);
    }

    public LiveData<Resource<List<BankTransaction>>> observeByUser(String uid) {
        return new QueryLiveData<>(transactions().whereArrayContains("participantUids", uid),
                BankTransaction.class, TransactionRepository::byNewest);
    }

    public LiveData<Resource<List<BankTransaction>>> observeByAccount(String accountNumber) {
        return new QueryLiveData<>(transactions().whereArrayContains("accountIds", accountNumber),
                BankTransaction.class, TransactionRepository::byNewest);
    }

    /** Tổng tiền đã chi trong ngày hôm nay (để kiểm tra hạn mức ngày). */
    public void getTodayOutgoing(String uid, Callback<Long> cb) {
        Date start = DateUtils.startOfToday();
        transactions().whereArrayContains("participantUids", uid).get()
                .addOnSuccessListener(q -> {
                    long sum = 0;
                    for (BankTransaction t : q.toObjects(BankTransaction.class)) {
                        if (uid.equals(t.ownerUid) && TransactionRules.isOutgoing(t.type)
                                && t.createdAt != null && !t.createdAt.toDate().before(start)) {
                            sum += t.amount;
                        }
                    }
                    cb.onSuccess(sum);
                })
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }

    public void getOnce(String uid, Callback<List<BankTransaction>> cb) {
        transactions().whereArrayContains("participantUids", uid).get()
                .addOnSuccessListener(q -> {
                    List<BankTransaction> list = new ArrayList<>(q.toObjects(BankTransaction.class));
                    java.util.Collections.sort(list, TransactionRepository::byNewest);
                    cb.onSuccess(list);
                })
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }

    /**
     * Ghi nhận giao dịch NGUYÊN TỬ bằng một WriteBatch: bản ghi giao dịch + cập nhật số dư (FieldValue.increment).
     * Firestore Rules kiểm tra lại số dư đủ, chủ sở hữu, hạn mức và khớp số tiền giữa giao dịch và số dư
     * (xem firestore.rules). txId sinh sẵn nên thử lại sau khi đã thành công sẽ bị từ chối -> không trừ tiền hai lần.
     */
    public void commit(TxDraft d, String uid, boolean otpVerified, boolean faceVerified, Callback<BankTransaction> cb) {
        long fee = TransactionRules.feeFor(d.type);
        BankTransaction tx = new BankTransaction();
        tx.type = d.type;
        tx.ownerUid = uid;
        tx.amount = d.amount;
        tx.fee = fee;
        tx.principalPortion = d.principalPortion;
        tx.description = d.description == null ? "" : d.description;
        tx.category = d.category;
        tx.reference = d.reference;
        tx.counterpartyBank = d.counterpartyBank;
        tx.counterpartyName = d.counterpartyName;
        tx.gateway = d.gateway;
        tx.status = "COMPLETED";
        tx.otpVerified = otpVerified;
        tx.faceVerified = faceVerified;

        List<String> participants = new ArrayList<>();
        participants.add(uid);
        List<String> accountIds = new ArrayList<>();

        WriteBatch b = db.batch();
        switch (d.type) {
            case DEPOSIT:
                tx.fromAccountId = Constants.EXTERNAL;
                tx.toAccountId = d.toAccountNumber;
                accountIds.add(d.toAccountNumber);
                break;
            case WITHDRAW:
                tx.fromAccountId = d.from.accountNumber;
                tx.toAccountId = Constants.CASH;
                accountIds.add(d.from.accountNumber);
                break;
            case TRANSFER_INTERBANK:
                tx.fromAccountId = d.from.accountNumber;
                tx.toAccountId = Constants.INTERBANK;
                accountIds.add(d.from.accountNumber);
                tx.reference = d.toAccountNumber;
                break;
            case UTILITY_PAYMENT:
                tx.fromAccountId = d.from.accountNumber;
                tx.toAccountId = Constants.UTILITY;
                accountIds.add(d.from.accountNumber);
                break;
            case TRANSFER_INTERNAL:
            case MORTGAGE_PAYMENT:
            default:
                tx.fromAccountId = d.from.accountNumber;
                tx.toAccountId = d.toAccountNumber;
                accountIds.addAll(Arrays.asList(d.from.accountNumber, d.toAccountNumber));
                if (d.toOwnerUid != null && !d.toOwnerUid.equals(uid)) {
                    participants.add(d.toOwnerUid);
                }
                break;
        }
        tx.participantUids = participants;
        tx.accountIds = accountIds;

        b.set(transactions().document(d.txId), tx);
        if (d.type != TxType.DEPOSIT) {
            b.update(account(d.from.accountNumber),
                    "balance", FieldValue.increment(-(d.amount + fee)), "lastTxId", d.txId);
        }
        if (d.type == TxType.DEPOSIT || d.type == TxType.TRANSFER_INTERNAL) {
            b.update(account(d.toAccountNumber), "balance", FieldValue.increment(d.amount), "lastTxId", d.txId);
        } else if (d.type == TxType.MORTGAGE_PAYMENT) {
            b.update(account(d.toAccountNumber),
                    "balance", FieldValue.increment(-d.principalPortion), "lastTxId", d.txId);
        }

        b.commit().addOnSuccessListener(x -> {
            tx.id = d.txId;
            tx.createdAt = Timestamp.now();
            cb.onSuccess(tx);
        }).addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }
}
