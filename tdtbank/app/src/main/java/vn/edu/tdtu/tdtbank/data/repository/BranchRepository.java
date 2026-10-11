package vn.edu.tdtu.tdtbank.data.repository;

import androidx.lifecycle.LiveData;

import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.firebase.FirebaseClient;
import vn.edu.tdtu.tdtbank.data.firebase.QueryLiveData;
import vn.edu.tdtu.tdtbank.model.Branch;
import vn.edu.tdtu.tdtbank.utils.Constants;

import java.util.List;

public class BranchRepository {

    public LiveData<Resource<List<Branch>>> observeBranches() {
        return new QueryLiveData<>(FirebaseClient.db().collection(Constants.COLLECTION_BRANCHES), Branch.class,
                (a, b) -> a.name.compareTo(b.name));
    }
}
