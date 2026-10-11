package vn.edu.tdtu.tdtbank.data;

public interface Callback<T> {
    void onSuccess(T result);

    void onError(String message);
}
