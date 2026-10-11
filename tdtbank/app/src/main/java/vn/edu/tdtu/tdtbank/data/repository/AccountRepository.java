package vn.edu.tdtu.tdtbank.data.repository;

import androidx.lifecycle.LiveData;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.TdtBankApp;
import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.firebase.DocumentLiveData;
import vn.edu.tdtu.tdtbank.data.firebase.FirebaseClient;
import vn.edu.tdtu.tdtbank.data.firebase.QueryLiveData;
import vn.edu.tdtu.tdtbank.domain.AccountNumberGenerator;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.AccountDirectoryEntry;
import vn.edu.tdtu.tdtbank.model.AccountStatus;
import vn.edu.tdtu.tdtbank.model.AccountType;
import vn.edu.tdtu.tdtbank.utils.Constants;
import vn.edu.tdtu.tdtbank.utils.DateUtils;
import vn.edu.tdtu.tdtbank.utils.ErrorMapper;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class AccountRepository {

    private static final int MAX_NUMBER_ATTEMPTS = 8;

    private final FirebaseFirestore db = FirebaseClient.db();

    private CollectionReference accounts() {
        return db.collection(Constants.COLLECTION_ACCOUNTS);
    }

    public LiveData<Resource<List<Account>>> observeAccounts(String ownerUid) {
        return new QueryLiveData<>(accounts().whereEqualTo("ownerUid", ownerUid), Account.class,
                (a, b) -> a.type != b.type ? a.type.compareTo(b.type) : a.accountNumber.compareTo(b.accountNumber));
    }

    public void getAccountsOnce(String ownerUid, Callback<List<Account>> cb) {
        accounts().whereEqualTo("ownerUid", ownerUid).get()
                .addOnSuccessListener(q -> cb.onSuccess(q.toObjects(Account.class)))
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }

    public LiveData<Resource<Account>> observeAccount(String accountNumber) {
        return new DocumentLiveData<>(accounts().document(accountNumber), Account.class);
    }

    /** Tra tên chủ tài khoản nội bộ. onSuccess(null) khi không tồn tại. */
    public void lookupDirectory(String accountNumber, Callback<AccountDirectoryEntry> cb) {
        db.collection(Constants.COLLECTION_DIRECTORY).document(accountNumber).get()
                .addOnSuccessListener(s -> cb.onSuccess(s.exists() ? s.toObject(AccountDirectoryEntry.class) : null))
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }

    /** Sinh số tài khoản chưa tồn tại trong danh bạ. */
    public void generateUniqueNumber(Callback<String> cb) {
        tryGenerate(0, cb);
    }

    private void tryGenerate(int attempt, Callback<String> cb) {
        if (attempt >= MAX_NUMBER_ATTEMPTS) {
            cb.onError(TdtBankApp.get().getString(R.string.err_generic));
            return;
        }
        String candidate = AccountNumberGenerator.generate();
        db.collection(Constants.COLLECTION_DIRECTORY).document(candidate).get()
                .addOnSuccessListener(s -> {
                    if (s.exists()) {
                        tryGenerate(attempt + 1, cb);
                    } else {
                        cb.onSuccess(candidate);
                    }
                })
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }

    /** Nhân viên mở tài khoản mới (tiết kiệm/vay thế chấp) cho khách hàng. */
    public void openAccount(Account template, Callback<Account> cb) {
        generateUniqueNumber(new Callback<String>() {
            @Override
            public void onSuccess(String number) {
                template.accountNumber = number;
                template.status = AccountStatus.ACTIVE;
                template.startDate = DateUtils.todayIso();
                WriteBatch b = db.batch();
                b.set(accounts().document(number), template);
                b.set(db.collection(Constants.COLLECTION_DIRECTORY).document(number),
                        new AccountDirectoryEntry(number, template.ownerUid, template.ownerName, template.type));
                b.commit().addOnSuccessListener(x -> cb.onSuccess(template))
                        .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
            }

            @Override
            public void onError(String message) {
                cb.onError(message);
            }
        });
    }

    /** Tạo tài khoản thanh toán mặc định (dùng khi tạo khách hàng; ghi vào batch do UserRepository cung cấp). */
    public Account newCheckingAccount(String number, String ownerUid, String ownerName) {
        Account a = new Account();
        a.accountNumber = number;
        a.ownerUid = ownerUid;
        a.ownerName = ownerName;
        a.type = AccountType.CHECKING;
        a.status = AccountStatus.ACTIVE;
        a.balance = 0;
        a.startDate = DateUtils.todayIso();
        return a;
    }
}
