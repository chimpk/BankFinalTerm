package vn.edu.tdtu.tdtbank.data.firebase;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/** Điểm truy cập dùng chung tới Firebase. */
public final class FirebaseClient {

    private static final String SECONDARY_APP = "secondary";

    private FirebaseClient() {
    }

    /** false khi chưa đặt app/google-services.json (app báo lỗi cấu hình thay vì crash). */
    public static boolean isConfigured(Context context) {
        return !FirebaseApp.getApps(context).isEmpty();
    }

    public static FirebaseAuth auth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseFirestore db() {
        return FirebaseFirestore.getInstance();
    }

    /**
     * FirebaseAuth thứ hai để nhân viên tạo tài khoản đăng nhập cho khách hàng
     * mà không làm đăng xuất phiên của chính nhân viên.
     */
    public static FirebaseAuth secondaryAuth(Context context) {
        FirebaseApp secondary;
        try {
            secondary = FirebaseApp.getInstance(SECONDARY_APP);
        } catch (IllegalStateException e) {
            secondary = FirebaseApp.initializeApp(context, FirebaseApp.getInstance().getOptions(), SECONDARY_APP);
        }
        return FirebaseAuth.getInstance(secondary);
    }
}
