package vn.edu.tdtu.tdtbank.ui.common;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.viewmodel.BaseViewModel;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public final class UiUtils {

    private UiUtils() {
    }

    public static void snack(View anchor, String message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).show();
    }

    public static void toast(Context c, String message) {
        Toast.makeText(c, message, Toast.LENGTH_LONG).show();
    }

    /** Hiển thị kết quả thao tác ghi của ViewModel: lỗi -> Snackbar, thành công -> Toast rồi chạy onSuccess. */
    public static void observeOperation(Fragment f, BaseViewModel vm, Runnable onSuccess) {
        vm.operation().observe(f.getViewLifecycleOwner(), ev -> {
            Resource<String> r = ev == null ? null : ev.consume();
            if (r == null) {
                return;
            }
            if (r.status == Resource.Status.SUCCESS) {
                toast(f.requireContext(), r.message == null ? r.data : r.message);
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else if (r.status == Resource.Status.ERROR) {
                snack(f.requireView(), r.message);
            }
        });
    }

    public static void setError(TextInputLayout layout, String message) {
        layout.setError(message);
    }

    /** Gắn danh sách chuỗi vào ô chọn dạng dropdown. */
    public static void setDropdown(AutoCompleteTextView view, List<String> items) {
        view.setAdapter(new ArrayAdapter<>(view.getContext(), android.R.layout.simple_dropdown_item_1line, items));
    }

    public static String text(TextInputLayout layout) {
        return layout.getEditText() == null ? "" : layout.getEditText().getText().toString().trim();
    }

    public static void setText(TextInputLayout layout, String value) {
        if (layout.getEditText() != null) {
            layout.getEditText().setText(value == null ? "" : value);
        }
    }
}
