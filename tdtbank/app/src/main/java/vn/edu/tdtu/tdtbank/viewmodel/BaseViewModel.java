package vn.edu.tdtu.tdtbank.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import vn.edu.tdtu.tdtbank.TdtBankApp;
import vn.edu.tdtu.tdtbank.data.Callback;
import vn.edu.tdtu.tdtbank.data.Event;
import vn.edu.tdtu.tdtbank.data.Resource;

/** Lớp cơ sở: phát kết quả của các thao tác ghi (thành công/thất bại) dưới dạng sự kiện một lần. */
public abstract class BaseViewModel extends ViewModel {

    private final MutableLiveData<Event<Resource<String>>> operation = new MutableLiveData<>();

    public LiveData<Event<Resource<String>>> operation() {
        return operation;
    }

    protected <T> Callback<T> opCallback(int successMessageRes) {
        return new Callback<T>() {
            @Override
            public void onSuccess(T result) {
                operation.setValue(new Event<>(Resource.success(TdtBankApp.get().getString(successMessageRes))));
            }

            @Override
            public void onError(String message) {
                operation.setValue(new Event<>(Resource.error(message)));
            }
        };
    }

    protected void postError(String message) {
        operation.setValue(new Event<>(Resource.error(message)));
    }
}
