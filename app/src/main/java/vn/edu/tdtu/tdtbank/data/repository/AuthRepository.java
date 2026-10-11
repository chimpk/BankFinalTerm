package vn.edu.tdtu.tdtbank.data.repository;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.TdtBankApp;
import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.data.firebase.FirebaseClient;
import vn.edu.tdtu.tdtbank.model.User;
import vn.edu.tdtu.tdtbank.model.UserStatus;
import vn.edu.tdtu.tdtbank.utils.Constants;
import vn.edu.tdtu.tdtbank.utils.ErrorMapper;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

/** Đăng nhập/đăng xuất/phiên và tạo tài khoản đăng nhập. Mật khẩu chỉ do Firebase Authentication quản lý. */
public class AuthRepository {

    public static String toEmail(String identifier) {
        String id = identifier == null ? "" : identifier.trim().toLowerCase(Locale.ROOT);
        return id.contains("@") ? id : id + Constants.EMAIL_DOMAIN;
    }

    public void login(String identifier, String password, Callback<User> cb) {
        FirebaseClient.auth().signInWithEmailAndPassword(toEmail(identifier), password)
                .addOnSuccessListener(r -> loadProfile(r.getUser().getUid(), cb))
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }

    /** Khôi phục phiên đang lưu; onError(null) nghĩa là chưa đăng nhập. */
    public void restoreSession(Callback<User> cb) {
        FirebaseUser u = FirebaseClient.auth().getCurrentUser();
        if (u == null) {
            cb.onError(null);
            return;
        }
        loadProfile(u.getUid(), cb);
    }

    private void loadProfile(String uid, Callback<User> cb) {
        FirebaseClient.db().collection(Constants.COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(snap -> {
                    User user = snap.exists() ? snap.toObject(User.class) : null;
                    if (user == null) {
                        logout();
                        cb.onError(TdtBankApp.get().getString(R.string.err_profile_missing));
                    } else if (user.status == UserStatus.LOCKED) {
                        logout();
                        cb.onError(TdtBankApp.get().getString(R.string.err_account_locked));
                    } else {
                        cb.onSuccess(user);
                    }
                })
                .addOnFailureListener(e -> {
                    logout();
                    cb.onError(ErrorMapper.map(e));
                });
    }

    public void logout() {
        FirebaseClient.auth().signOut();
    }

    public String currentUid() {
        FirebaseUser u = FirebaseClient.auth().getCurrentUser();
        return u == null ? null : u.getUid();
    }

    public void changePassword(String oldPassword, String newPassword, Callback<Void> cb) {
        FirebaseUser u = FirebaseClient.auth().getCurrentUser();
        if (u == null || u.getEmail() == null) {
            cb.onError(TdtBankApp.get().getString(R.string.err_not_logged_in));
            return;
        }
        AuthCredential cred = EmailAuthProvider.getCredential(u.getEmail(), oldPassword);
        u.reauthenticate(cred)
                .addOnSuccessListener(x -> u.updatePassword(newPassword)
                        .addOnSuccessListener(y -> cb.onSuccess(null))
                        .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e))))
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }

    /** Tạo tài khoản Auth cho khách hàng bằng phiên Auth phụ để không đăng xuất nhân viên. */
    public void createLoginAccount(String username, String password, Callback<String> cb) {
        FirebaseAuth secondary = FirebaseClient.secondaryAuth(TdtBankApp.get());
        secondary.createUserWithEmailAndPassword(toEmail(username), password)
                .addOnSuccessListener(r -> {
                    String uid = r.getUser().getUid();
                    secondary.signOut();
                    cb.onSuccess(uid);
                })
                .addOnFailureListener(e -> cb.onError(ErrorMapper.map(e)));
    }
}
