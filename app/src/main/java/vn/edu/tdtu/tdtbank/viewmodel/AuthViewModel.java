package vn.edu.tdtu.tdtbank.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.repository.AuthRepository;
import vn.edu.tdtu.tdtbank.model.User;

public class AuthViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();
    private final MutableLiveData<Resource<User>> loginState = new MutableLiveData<>();

    public LiveData<Resource<User>> loginState() {
        return loginState;
    }

    public void login(String identifier, String password) {
        loginState.setValue(Resource.loading());
        repository.login(identifier, password, callback());
    }

    /** Duy trì phiên: nếu Firebase còn phiên đăng nhập thì đi thẳng vào màn hình theo vai trò. */
    public void restoreSession() {
        loginState.setValue(Resource.loading());
        repository.restoreSession(new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                loginState.setValue(Resource.success(user));
            }

            @Override
            public void onError(String message) {
                // message == null: chưa đăng nhập - không hiển thị lỗi
                loginState.setValue(message == null ? null : Resource.error(message));
            }
        });
    }

    private Callback<User> callback() {
        return new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                loginState.setValue(Resource.success(user));
            }

            @Override
            public void onError(String message) {
                loginState.setValue(Resource.error(message));
            }
        };
    }
}
