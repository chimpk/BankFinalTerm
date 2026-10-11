package vn.edu.tdtu.tdtbank.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;

public final class GeoUtils {

    private static final double EARTH_RADIUS_M = 6_371_000.0;

    private GeoUtils() {
    }

    public static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_M * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    /** Trả về tối đa k phần tử gần (lat0,lng0) nhất theo khoảng cách đường chim bay. */
    public static <T> List<T> nearest(List<T> items, ToDoubleFunction<T> lat, ToDoubleFunction<T> lng,
                                      double lat0, double lng0, int k) {
        List<T> copy = new ArrayList<>(items);
        Collections.sort(copy, Comparator.comparingDouble(
                (T t) -> haversineMeters(lat0, lng0, lat.applyAsDouble(t), lng.applyAsDouble(t))));
        return copy.size() > k ? new ArrayList<>(copy.subList(0, k)) : copy;
    }
}
