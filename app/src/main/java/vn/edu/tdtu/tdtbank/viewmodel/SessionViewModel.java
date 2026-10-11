package vn.edu.tdtu.tdtbank.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.repository.AuthRepository;
import vn.edu.tdtu.tdtbank.data.repository.UserRepository;
import vn.edu.tdtu.tdtbank.model.User;

/** Phạm vi Activity: hồ sơ người dùng đang đăng nhập, quan sát thời gian thực (để bắt khóa tài khoản). */
public class SessionViewModel extends ViewModel {

    private final AuthRepository auth = new AuthRepository();
    private final UserRepository users = new UserRepository();
    private LiveData<Resource<User>> currentUser;

    public String uid() {
        return auth.currentUid();
    }

    public LiveData<Resource<User>> currentUser() {
        if (currentUser == null) {
            currentUser = users.observeUser(auth.currentUid());
        }
        return currentUser;
    }

    public void logout() {
        auth.logout();
    }
}
