package vn.edu.tdtu.tdtbank.data.firebase;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.edu.tdtu.tdtbank.BuildConfig;

/** Điểm truy cập dùng chung tới Firebase. */
public final class FirebaseClient {

    private static final String SECONDARY_APP = "secondary";

    /** Khớp firebase.json và docker-compose.yml. Project "demo-*" của Emulator không cần project thật. */
    public static final String EMULATOR_PROJECT_ID = "demo-tdtbank";
    private static final int EMULATOR_FIRESTORE_PORT = 8080;
    private static final int EMULATOR_AUTH_PORT = 9099;

    private FirebaseClient() {
    }

    /** Bản debug có đặt firebase.emulatorHost trong local.properties → dùng Firebase Local Emulator Suite. */
    public static boolean usesEmulator() {
        return !BuildConfig.FIREBASE_EMULATOR_HOST.isEmpty();
    }

    /**
     * Gọi một lần trong Application.onCreate, trước mọi truy cập Firestore/Auth.
     * Khi dùng Emulator mà chưa có google-services.json, tự khởi tạo FirebaseApp bằng cấu hình giả.
     */
    public static void init(Context context) {
        if (!usesEmulator()) {
            return;
        }
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context, new FirebaseOptions.Builder()
                    .setProjectId(EMULATOR_PROJECT_ID)
                    .setApplicationId("1:000000000000:android:0000000000000000")
                    .setApiKey("emulator-api-key")
                    .build());
        }
        String host = BuildConfig.FIREBASE_EMULATOR_HOST;
        FirebaseFirestore.getInstance().useEmulator(host, EMULATOR_FIRESTORE_PORT);
        FirebaseAuth.getInstance().useEmulator(host, EMULATOR_AUTH_PORT);
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
            if (usesEmulator()) {
                FirebaseAuth.getInstance(secondary).useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, EMULATOR_AUTH_PORT);
            }
        }
        return FirebaseAuth.getInstance(secondary);
    }
}
