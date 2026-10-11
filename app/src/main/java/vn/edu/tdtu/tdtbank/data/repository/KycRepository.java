package vn.edu.tdtu.tdtbank.data.repository;

import androidx.lifecycle.LiveData;

import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.firebase.DocumentLiveData;
import vn.edu.tdtu.tdtbank.data.firebase.FirebaseClient;
import vn.edu.tdtu.tdtbank.data.firebase.QueryLiveData;
import vn.edu.tdtu.tdtbank.model.KycRecord;
import vn.edu.tdtu.tdtbank.model.KycStatus;
import vn.edu.tdtu.tdtbank.utils.Constants;
import vn.edu.tdtu.tdtbank.utils.ErrorMapper;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class KycRepository {

    private final FirebaseFirestore db = FirebaseClient.db();

    public LiveData<Resource<KycRecord>> observe(String uid) {
        return new DocumentLiveData<>(db.collection(Constants.COLLECTION_KYC).document(uid), KycRecord.class);
    }

    public LiveData<Resource<List<KycRecord>>> observePending() {
        return new QueryLiveData<>(
                db.collection(Constants.COLLECTION_KYC).whereEqualTo("status", KycStatus.PENDING.name()),
                KycRecord.class, null);
    }

    /**
     * Lưu hồ sơ eKYC và cập nhật kycStatus của người dùng trong cùng một batch.
     * record.status = PENDING (khách tự chụp, chờ nhân viên duyệt) hoặc VERIFIED (nhân viên chụp trực tiếp
     * sau khi đối chiếu giấy tờ với người thật tại quầy).
     */
    public void submit(KycRecord record, Callback<Void> cb) {
        WriteBatch b = db.batch();
        b.set(db.collection(Constants.COLLECTION_KYC).document(record.uid), record);
        b.update(db.collection(Constants.COLLECTION_USERS).document(record.uid), "kycStatus", record.status.name());
        b.commit().addOnSuccessListener(x -> cb.onSuccess(null))
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }

    public void review(String uid, boolean approve, String officerUid, String note, Callback<Void> cb) {
        KycStatus status = approve ? KycStatus.VERIFIED : KycStatus.REJECTED;
        WriteBatch b = db.batch();
        b.update(db.collection(Constants.COLLECTION_KYC).document(uid),
                "status", status.name(), "reviewedBy", officerUid, "note", note,
                "updatedAt", FieldValue.serverTimestamp());
        b.update(db.collection(Constants.COLLECTION_USERS).document(uid), "kycStatus", status.name());
        b.commit().addOnSuccessListener(x -> cb.onSuccess(null))
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }
}
