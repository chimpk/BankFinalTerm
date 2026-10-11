package vn.edu.tdtu.tdtbank.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.data.remote.RoutingClient;
import vn.edu.tdtu.tdtbank.data.remote.SampleBranches;
import vn.edu.tdtu.tdtbank.data.repository.BranchRepository;
import vn.edu.tdtu.tdtbank.domain.GeoUtils;
import vn.edu.tdtu.tdtbank.model.Branch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapViewModel extends ViewModel {

    /** Kết quả tìm đường tới chi nhánh gần nhất. */
    public static class NearestResult {
        public final Branch branch;
        /** null khi không gọi được dịch vụ định tuyến -> dùng đường chim bay. */
        public final RoutingClient.Route route;
        public final double straightLineMeters;

        NearestResult(Branch branch, RoutingClient.Route route, double straightLineMeters) {
            this.branch = branch;
            this.route = route;
            this.straightLineMeters = straightLineMeters;
        }
    }

    private static final int CANDIDATES = 3;

    private final BranchRepository repository = new BranchRepository();
    private final RoutingClient routing = new RoutingClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Resource<NearestResult>> nearest = new MutableLiveData<>();
    private final MediatorLiveData<BranchList> branches = new MediatorLiveData<>();
    private LiveData<Resource<List<Branch>>> source;

    /** Danh sách chi nhánh kèm cờ cho biết đang dùng dữ liệu mẫu. */
    public static class BranchList {
        public final List<Branch> items;
        public final boolean sample;

        BranchList(List<Branch> items, boolean sample) {
            this.items = items;
            this.sample = sample;
        }
    }

    public LiveData<BranchList> branches() {
        if (source == null) {
            source = repository.observeBranches();
            branches.addSource(source, r -> {
                if (r == null || r.status == Resource.Status.LOADING) {
                    return;
                }
                boolean empty = r.status == Resource.Status.ERROR || r.data == null || r.data.isEmpty();
                branches.setValue(empty ? new BranchList(SampleBranches.all(), true) : new BranchList(r.data, false));
            });
        }
        return branches;
    }

    public LiveData<Resource<NearestResult>> nearest() {
        return nearest;
    }

    /** Chọn tối đa 3 chi nhánh gần nhất (chim bay), tìm đường đi bộ cho từng nơi và lấy tuyến ngắn nhất. */
    public void findNearest(double lat, double lon, List<Branch> all) {
        nearest.setValue(Resource.loading());
        executor.execute(() -> {
            List<Branch> candidates = GeoUtils.nearest(all, b -> b.lat, b -> b.lng, lat, lon, CANDIDATES);
            if (candidates.isEmpty()) {
                nearest.postValue(Resource.error(""));
                return;
            }
            NearestResult best = null;
            List<NearestResult> failed = new ArrayList<>();
            for (Branch b : candidates) {
                double straight = GeoUtils.haversineMeters(lat, lon, b.lat, b.lng);
                try {
                    RoutingClient.Route r = routing.fetchWalkingRoute(lat, lon, b.lat, b.lng);
                    if (r == null) {
                        failed.add(new NearestResult(b, null, straight));
                    } else if (best == null || r.distanceMeters < best.route.distanceMeters) {
                        best = new NearestResult(b, r, straight);
                    }
                } catch (Exception e) {
                    failed.add(new NearestResult(b, null, straight));
                }
            }
            nearest.postValue(Resource.success(best != null ? best : failed.get(0)));
        });
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}
