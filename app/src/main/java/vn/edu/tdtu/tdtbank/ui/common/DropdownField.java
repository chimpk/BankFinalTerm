package vn.edu.tdtu.tdtbank.ui.common;

import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Bao ô chọn dạng dropdown (AutoCompleteTextView) để lấy phần tử đang chọn, kể cả sau khi xoay màn hình. */
public class DropdownField<T> {

    private final AutoCompleteTextView view;
    private final List<T> items = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private int selected = -1;

    public DropdownField(AutoCompleteTextView view) {
        this.view = view;
        view.setOnItemClickListener((parent, v, position, id) -> selected = position);
    }

    public void setItems(List<T> newItems, Function<T, String> label) {
        items.clear();
        labels.clear();
        items.addAll(newItems);
        for (T t : newItems) {
            labels.add(label.apply(t));
        }
        UiUtils.setDropdown(view, labels);
        if (selected >= items.size()) {
            selected = -1;
        }
        if (selected < 0 && !items.isEmpty()) {
            select(0);
        } else if (selected >= 0) {
            view.setText(labels.get(selected), false);
        } else {
            view.setText("", false);
        }
    }

    public void select(int index) {
        if (index >= 0 && index < items.size()) {
            selected = index;
            view.setText(labels.get(index), false);
        }
    }

    public T selected() {
        if (selected >= 0 && selected < items.size()) {
            return items.get(selected);
        }
        String shown = view.getText() == null ? "" : view.getText().toString();
        int idx = labels.indexOf(shown);
        return idx >= 0 ? items.get(idx) : null;
    }

    public AutoCompleteTextView view() {
        return view;
    }
}
