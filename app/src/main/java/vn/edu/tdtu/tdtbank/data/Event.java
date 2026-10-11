package vn.edu.tdtu.tdtbank.data;

/** Bọc một sự kiện chỉ được xử lý một lần (tránh hiện lại Snackbar khi xoay màn hình). */
public class Event<T> {

    private final T content;
    private boolean handled;

    public Event(T content) {
        this.content = content;
    }

    /** Trả về nội dung nếu chưa xử lý, ngược lại null. */
    public T consume() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }
}
