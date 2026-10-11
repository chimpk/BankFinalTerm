package vn.edu.tdtu.tdtbank.data.firebase;

import androidx.lifecycle.LiveData;

import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.utils.ErrorMapper;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** LiveData thời gian thực cho một truy vấn; tự đăng ký/hủy listener theo vòng đời quan sát. */
public class QueryLiveData<T> extends LiveData<Resource<List<T>>> {

    private final Query query;
    private final Class<T> type;
    private final Comparator<T> sorter;
    private ListenerRegistration registration;

    public QueryLiveData(Query query, Class<T> type, Comparator<T> sorter) {
        this.query = query;
        this.type = type;
        this.sorter = sorter;
    }

    @Override
    protected void onActive() {
        setValue(Resource.loading());
        registration = query.addSnapshotListener((snap, e) -> {
            if (e != null) {
                setValue(Resource.error(ErrorMapper.map(e)));
                return;
            }
            List<T> list = new ArrayList<>();
            if (snap != null) {
                for (DocumentSnapshot d : snap.getDocuments()) {
                    T obj = d.toObject(type);
                    if (obj != null) {
                        list.add(obj);
                    }
                }
            }
            if (sorter != null) {
                Collections.sort(list, sorter);
            }
            setValue(Resource.success(list));
        });
    }

    @Override
    protected void onInactive() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
