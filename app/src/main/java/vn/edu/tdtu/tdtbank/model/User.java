package vn.edu.tdtu.tdtbank.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

/** Hồ sơ người dùng (users/{uid}). Mật khẩu KHÔNG được lưu ở đây - do Firebase Authentication quản lý. */
public class User {
    public String uid;
    public String username;
    public Role role;
    public String fullName;
    public String phone;
    public String contactEmail;
    public String nationalId;
    public String address;
    /** yyyy-MM-dd */
    public String dateOfBirth;
    public UserStatus status;
    public KycStatus kycStatus;
    @ServerTimestamp
    public Timestamp createdAt;
    public String createdBy;

    public User() {
    }
}
