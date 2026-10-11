package vn.edu.tdtu.tdtbank.utils;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.TdtBankApp;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestoreException;

/** Chuyển ngoại lệ Firebase thành thông báo tiếng Việt dễ hiểu. */
public final class ErrorMapper {

    private ErrorMapper() {
    }

    public static String map(Throwable t) {
        if (t instanceof FirebaseNetworkException) {
            return s(R.string.err_network);
        }
        if (t instanceof FirebaseAuthInvalidUserException || t instanceof FirebaseAuthInvalidCredentialsException) {
            return s(R.string.err_invalid_credentials);
        }
        if (t instanceof FirebaseAuthUserCollisionException) {
            return s(R.string.err_username_taken);
        }
        if (t instanceof FirebaseAuthException) {
            return s(R.string.err_auth_generic);
        }
        if (t instanceof FirebaseFirestoreException) {
            switch (((FirebaseFirestoreException) t).getCode()) {
                case PERMISSION_DENIED:
                    return s(R.string.err_permission_denied);
                case UNAVAILABLE:
                case DEADLINE_EXCEEDED:
                    return s(R.string.err_network);
                case ALREADY_EXISTS:
                    return s(R.string.err_already_exists);
                case NOT_FOUND:
                    return s(R.string.err_not_found);
                default:
                    return s(R.string.err_generic);
            }
        }
        return s(R.string.err_generic);
    }

    private static String s(int res) {
        return TdtBankApp.get().getString(res);
    }
}
