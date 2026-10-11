package vn.edu.tdtu.tdtbank.data.repository;

import androidx.lifecycle.LiveData;

import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.firebase.FirebaseClient;
import vn.edu.tdtu.tdtbank.data.firebase.QueryLiveData;
import vn.edu.tdtu.tdtbank.model.Account;
import vn.edu.tdtu.tdtbank.model.AccountType;
import vn.edu.tdtu.tdtbank.model.SavingRatePolicy;
import vn.edu.tdtu.tdtbank.utils.Constants;
import vn.edu.tdtu.tdtbank.utils.ErrorMapper;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RateRepository {

    /** Bảng lãi suất mẫu để khởi tạo lần đầu (số liệu minh họa, không phải lãi suất của ngân hàng thật). */
    private static final int[] SAMPLE_TERMS = {1, 3, 6, 12, 24};
    private static final double[] SAMPLE_RATES = {2.0, 3.0, 4.5, 5.5, 5.8};

    private final FirebaseFirestore db = FirebaseClient.db();

    public LiveData<Resource<List<SavingRatePolicy>>> observePolicies() {
        return new QueryLiveData<>(db.collection(Constants.COLLECTION_RATES), SavingRatePolicy.class,
                (a, b) -> Integer.compare(a.termMonths, b.termMonths));
    }

    public void createSamplePolicies(String officerUid, Callback<Void> cb) {
        WriteBatch b = db.batch();
        for (int i = 0; i < SAMPLE_TERMS.length; i++) {
            b.set(db.collection(Constants.COLLECTION_RATES).document(String.valueOf(SAMPLE_TERMS[i])),
                    policy(SAMPLE_TERMS[i], SAMPLE_RATES[i], officerUid));
        }
        b.commit().addOnSuccessListener(x -> cb.onSuccess(null))
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }

    private Map<String, Object> policy(int term, double rate, String officerUid) {
        Map<String, Object> m = new HashMap<>();
        m.put("termMonths", term);
        m.put("annualRate", rate);
        m.put("updatedBy", officerUid);
        m.put("updatedAt", FieldValue.serverTimestamp());
        return m;
    }

    /**
     * Cập nhật lãi suất theo chính sách của ngân hàng. Nếu applyToExisting, đồng thời cập nhật lãi suất
     * của các sổ tiết kiệm đang mở cùng kỳ hạn.
     */
    public void updateRate(int term, double rate, String officerUid, boolean applyToExisting, Callback<Integer> cb) {
        db.collection(Constants.COLLECTION_RATES).document(String.valueOf(term)).set(policy(term, rate, officerUid))
                .addOnSuccessListener(x -> {
                    if (!applyToExisting) {
                        cb.onSuccess(0);
                        return;
                    }
                    db.collection(Constants.COLLECTION_ACCOUNTS)
                            .whereEqualTo("type", AccountType.SAVING.name()).whereEqualTo("termMonths", term).get()
                            .addOnSuccessListener(q -> {
                                WriteBatch b = db.batch();
                                int n = 0;
                                for (Account a : q.toObjects(Account.class)) {
                                    b.update(db.collection(Constants.COLLECTION_ACCOUNTS).document(a.accountNumber),
                                            "interestRate", rate);
                                    n++;
                                }
                                final int count = n;
                                b.commit().addOnSuccessListener(y -> cb.onSuccess(count))
                                        .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
                            })
                            .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
                })
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }
}
