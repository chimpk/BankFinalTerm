package vn.edu.tdtu.tdtbank.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import vn.edu.tdtu.tdtbank.R;
import vn.edu.tdtu.tdtbank.data.Resource;
import vn.edu.tdtu.tdtbank.databinding.FragmentBranchMapBinding;
import vn.edu.tdtu.tdtbank.model.Branch;
import vn.edu.tdtu.tdtbank.ui.common.UiUtils;
import vn.edu.tdtu.tdtbank.viewmodel.MapViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Bản đồ OpenStreetMap: vị trí người dùng, các chi nhánh và đường đi bộ ngắn nhất tới chi nhánh gần nhất.
 */
public class BranchMapFragment extends Fragment {

    private FragmentBranchMapBinding binding;
    private MapViewModel vm;
    private FusedLocationProviderClient fused;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private List<Branch> branches = new ArrayList<>();
    private boolean sampleData;
    private GeoPoint userPoint;
    private MapViewModel.NearestResult result;
    private boolean wantRouteAfterLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                (Map<String, Boolean> grants) -> {
                    boolean granted = Boolean.TRUE.equals(grants.get(Manifest.permission.ACCESS_FINE_LOCATION))
                            || Boolean.TRUE.equals(grants.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (granted) {
                        fetchLocation();
                    } else if (binding != null) {
                        UiUtils.snack(binding.getRoot(), getString(R.string.err_location_denied));
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentBranchMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(this).get(MapViewModel.class);
        fused = LocationServices.getFusedLocationProviderClient(requireContext());

        binding.map.setTileSource(TileSourceFactory.MAPNIK);
        binding.map.setMultiTouchControls(true);
        binding.map.getController().setZoom(13.0);
        binding.map.getController().setCenter(new GeoPoint(10.7326, 106.6996));

        vm.branches().observe(getViewLifecycleOwner(), list -> {
            branches = list.items;
            sampleData = list.sample;
            binding.tvSampleBanner.setVisibility(sampleData ? View.VISIBLE : View.GONE);
            redraw(false);
        });
        vm.nearest().observe(getViewLifecycleOwner(), r -> {
            binding.progress.setVisibility(r.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);
            if (r.status == Resource.Status.SUCCESS) {
                result = r.data;
                showResultInfo();
                redraw(true);
            } else if (r.status == Resource.Status.ERROR) {
                UiUtils.snack(binding.getRoot(), getString(R.string.err_no_branch));
            }
        });

        binding.btnMyLocation.setOnClickListener(v -> {
            wantRouteAfterLocation = false;
            ensureLocation();
        });
        binding.btnNearest.setOnClickListener(v -> {
            wantRouteAfterLocation = true;
            if (userPoint != null) {
                findNearest();
            } else {
                ensureLocation();
            }
        });
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void ensureLocation() {
        if (hasPermission()) {
            fetchLocation();
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLocation() {
        binding.progress.setVisibility(View.VISIBLE);
        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(loc -> {
                    if (binding == null) {
                        return;
                    }
                    binding.progress.setVisibility(View.GONE);
                    if (loc == null) {
                        UiUtils.snack(binding.getRoot(), getString(R.string.err_location_unavailable));
                        return;
                    }
                    userPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                    redraw(false);
                    if (wantRouteAfterLocation) {
                        findNearest();
                    } else {
                        binding.map.getController().animateTo(userPoint);
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding != null) {
                        binding.progress.setVisibility(View.GONE);
                        UiUtils.snack(binding.getRoot(), getString(R.string.err_location_unavailable));
                    }
                });
    }

    private void findNearest() {
        result = null;
        vm.findNearest(userPoint.getLatitude(), userPoint.getLongitude(), branches);
    }

    private void showResultInfo() {
        if (result == null) {
            return;
        }
        binding.cardInfo.setVisibility(View.VISIBLE);
        if (result.route != null) {
            long minutes = Math.round(result.route.durationSeconds / 60.0);
            binding.tvInfo.setText(getString(R.string.route_info, result.branch.name,
                    formatDistance(result.route.distanceMeters), minutes));
        } else {
            binding.tvInfo.setText(getString(R.string.route_info_straight, result.branch.name,
                    formatDistance(result.straightLineMeters)));
        }
    }

    private static String formatDistance(double meters) {
        return meters >= 1000 ? String.format(Locale.US, "%.1f km", meters / 1000.0)
                : String.format(Locale.US, "%d m", Math.round(meters));
    }

    private void redraw(boolean fitBounds) {
        if (binding == null) {
            return;
        }
        binding.map.getOverlays().clear();
        for (Branch b : branches) {
            Marker m = new Marker(binding.map);
            m.setPosition(new GeoPoint(b.lat, b.lng));
            m.setTitle(b.name);
            m.setSnippet(b.address);
            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            binding.map.getOverlays().add(m);
        }
        if (userPoint != null) {
            Marker me = new Marker(binding.map);
            me.setPosition(userPoint);
            me.setTitle(getString(R.string.you_are_here));
            me.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            binding.map.getOverlays().add(me);
        }
        if (result != null && userPoint != null) {
            List<GeoPoint> pts = new ArrayList<>();
            if (result.route != null) {
                for (double[] p : result.route.points) {
                    pts.add(new GeoPoint(p[0], p[1]));
                }
            } else {
                pts.add(userPoint);
                pts.add(new GeoPoint(result.branch.lat, result.branch.lng));
            }
            Polyline line = new Polyline();
            line.setPoints(pts);
            line.getOutlinePaint().setColor(ContextCompat.getColor(requireContext(), R.color.brand_primary));
            line.getOutlinePaint().setStrokeWidth(12f);
            binding.map.getOverlays().add(line);
            if (fitBounds && pts.size() >= 2) {
                binding.map.post(() -> {
                    if (binding != null) {
                        binding.map.zoomToBoundingBox(BoundingBox.fromGeoPoints(pts), true, 120);
                    }
                });
            }
        }
        binding.map.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) {
            binding.map.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null) {
            binding.map.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        if (binding != null) {
            binding.map.onDetach();
        }
        super.onDestroyView();
        binding = null;
    }
}
