package vn.edu.tdtu.tdtbank.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

/** Hồ sơ eKYC (kyc/{uid}). Ảnh mặt được nén JPEG, mã hóa Base64 (gói Spark không có Storage). */
public class KycRecord {
    public String uid;
    public String fullName;
    public String faceImageBase64;
    public KycStatus status;
    /** uid người chụp (khách hàng hoặc nhân viên hỗ trợ). */
    public String capturedBy;
    public Role capturedByRole;
    /** ML Kit phát hiện đúng 1 khuôn mặt. */
    public boolean faceDetected;
    /** Vượt thử thách cười (liveness mức cơ bản). */
    public boolean livenessPassed;
    public String reviewedBy;
    public String note;
    @ServerTimestamp
    public Timestamp updatedAt;

    public KycRecord() {
    }
}
