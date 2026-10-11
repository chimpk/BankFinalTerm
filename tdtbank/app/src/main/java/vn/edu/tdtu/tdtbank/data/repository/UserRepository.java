package vn.edu.tdtu.tdtbank.data.repository;

import androidx.lifecycle.LiveData;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.TdtBankApp;
import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.firebase.DocumentLiveData;
import vn.edu.tdtu.tdtbank.data.firebase.FirebaseClient;
import vn.edu.tdtu.tdtbank.data.firebase.QueryLiveData;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.AccountDirectoryEntry;
import vn.edu.tdtu.tdtbank.model.KycStatus;
import vn.edu.tdtu.tdtbank.model.Role;
import vn.edu.tdtu.tdtbank.model.User;
import vn.edu.tdtu.tdtbank.model.UserStatus;
import vn.edu.tdtu.tdtbank.utils.Constants;
import vn.edu.tdtu.tdtbank.utils.ErrorMapper;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.Collator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserRepository {

    private final FirebaseFirestore db = FirebaseClient.db();
    private final AuthRepository authRepository = new AuthRepository();
    private final AccountRepository accountRepository = new AccountRepository();

    public LiveData<Resource<User>> observeUser(String uid) {
        return new DocumentLiveData<>(db.collection(Constants.COLLECTION_USERS).document(uid), User.class);
    }

    public LiveData<Resource<List<User>>> observeCustomers() {
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        return new QueryLiveData<>(
                db.collection(Constants.COLLECTION_USERS).whereEqualTo("role", Role.CUSTOMER.name()), User.class,
                (a, b) -> collator.compare(a.fullName == null ? "" : a.fullName, b.fullName == null ? "" : b.fullName));
    }

    /**
     * Nhân viên tạo khách hàng: (1) kiểm tra username chưa dùng, (2) tạo tài khoản Auth bằng phiên phụ,
     * (3) ghi hồ sơ + tài khoản thanh toán mặc định trong cùng một batch.
     * Nếu bước 3 lỗi sau bước 2, tài khoản Auth sẽ mồ côi (xem docs/limitations.md).
     */
    public void createCustomer(User draft, String password, String officerUid, Callback<String> cb) {
        db.collection(Constants.COLLECTION_USERS).whereEqualTo("username", draft.username).limit(1).get()
                .addOnSuccessListener(q -> {
                    if (!q.isEmpty()) {
                        cb.onError(TdtBankApp.get().getString(R.string.err_username_taken));
                        return;
                    }
                    authRepository.createLoginAccount(draft.username, password, new Callback<String>() {
                        @Override
                        public void onSuccess(String uid) {
                            accountRepository.generateUniqueNumber(new Callback<String>() {
                                @Override
                                public void onSuccess(String number) {
                                    draft.uid = uid;
                                    draft.role = Role.CUSTOMER;
                                    draft.status = UserStatus.ACTIVE;
                                    draft.kycStatus = KycStatus.NONE;
                                    draft.createdBy = officerUid;
                                    Account checking = accountRepository.newCheckingAccount(number, uid, draft.fullName);
                                    WriteBatch b = db.batch();
                                    b.set(db.collection(Constants.COLLECTION_USERS).document(uid), draft);
                                    b.set(db.collection(Constants.COLLECTION_ACCOUNTS).document(number), checking);
                                    b.set(db.collection(Constants.COLLECTION_DIRECTORY).document(number),
                                            new AccountDirectoryEntry(number, uid, draft.fullName, checking.type));
                                    b.commit().addOnSuccessListener(x -> cb.onSuccess(uid))
                                            .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
                                }

                                @Override
                                public void onError(String message) {
                                    cb.onError(message);
                                }
                            });
                        }

                        @Override
                        public void onError(String message) {
                            cb.onError(message);
                        }
                    });
                })
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }

    /** Nhân viên sửa dữ liệu khách hàng theo yêu cầu của khách. */
    public void officerUpdateCustomer(String uid, String fullName, String phone, String email, String address,
                                      String nationalId, String dateOfBirth, Callback<Void> cb) {
        Map<String, Object> f = new HashMap<>();
        f.put("fullName", fullName);
        f.put("phone", phone);
        f.put("contactEmail", email);
        f.put("address", address);
        f.put("nationalId", nationalId);
        f.put("dateOfBirth", dateOfBirth);
        update(uid, f, cb);
    }

    public void setStatus(String uid, UserStatus status, Callback<Void> cb) {
        Map<String, Object> f = new HashMap<>();
        f.put("status", status.name());
        update(uid, f, cb);
    }

    /** Khách hàng tự sửa thông tin liên hệ của mình. */
    public void updateOwnContact(String uid, String phone, String email, String address, Callback<Void> cb) {
        Map<String, Object> f = new HashMap<>();
        f.put("phone", phone);
        f.put("contactEmail", email);
        f.put("address", address);
        update(uid, f, cb);
    }

    private void update(String uid, Map<String, Object> fields, Callback<Void> cb) {
        db.collection(Constants.COLLECTION_USERS).document(uid).update(fields)
                .addOnSuccessListener(x -> cb.onSuccess(null))
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }
}
