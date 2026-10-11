package vn.edu.tdtu.tdtbank.data.firebase;

import androidx.lifecycle.LiveData;

import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.utils.ErrorMapper;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ListenerRegistration;

/** LiveData thời gian thực cho một document. Document không tồn tại -> SUCCESS với data == null. */
public class DocumentLiveData<T> extends LiveData<Resource<T>> {

    private final DocumentReference ref;
    private final Class<T> type;
    private ListenerRegistration registration;

    public DocumentLiveData(DocumentReference ref, Class<T> type) {
        this.ref = ref;
        this.type = type;
    }

    @Override
    protected void onActive() {
        setValue(Resource.loading());
        registration = ref.addSnapshotListener((snap, e) -> {
            if (e != null) {
                setValue(Resource.error(ErrorMapper.map(e)));
                return;
            }
            setValue(Resource.success(snap != null && snap.exists() ? snap.toObject(type) : null));
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
