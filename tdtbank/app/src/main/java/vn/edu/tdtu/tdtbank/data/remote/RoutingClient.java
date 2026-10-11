package vn.edu.tdtu.tdtbank.data.remote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Định tuyến đi bộ qua máy chủ OSRM công cộng của OpenStreetMap (FOSSGIS, profile "foot").
 * Máy chủ công cộng có giới hạn sử dụng; khi bảo trì hoặc mất mạng, UI chuyển sang ước lượng đường chim bay.
 */
public class RoutingClient {

    private static final String BASE = "https://routing.openstreetmap.de/routed-foot/route/v1/foot/";

    public static class Route {
        public final double distanceMeters;
        public final double durationSeconds;
        /** Danh sách {lat, lon}. */
        public final List<double[]> points;

        public Route(double distanceMeters, double durationSeconds, List<double[]> points) {
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
            this.points = points;
        }
    }

    /** Trả về null nếu OSRM không tìm thấy tuyến. */
    public static Route parse(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        if (!"Ok".equals(root.optString("code"))) {
            return null;
        }
        JSONArray routes = root.getJSONArray("routes");
        if (routes.length() == 0) {
            return null;
        }
        JSONObject r = routes.getJSONObject(0);
        JSONArray coords = r.getJSONObject("geometry").getJSONArray("coordinates");
        List<double[]> pts = new ArrayList<>(coords.length());
        for (int i = 0; i < coords.length(); i++) {
            JSONArray c = coords.getJSONArray(i);
            pts.add(new double[]{c.getDouble(1), c.getDouble(0)});
        }
        return new Route(r.getDouble("distance"), r.getDouble("duration"), pts);
    }

    /** Gọi mạng - phải chạy trên luồng nền. */
    public Route fetchWalkingRoute(double lat1, double lon1, double lat2, double lon2)
            throws IOException, JSONException {
        String url = String.format(Locale.US, "%s%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
                BASE, lon1, lat1, lon2, lat2);
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(10000);
        c.setRequestProperty("User-Agent", "vn.edu.tdtu.tdtbank (student project)");
        try {
            if (c.getResponseCode() != 200) {
                throw new IOException("HTTP " + c.getResponseCode());
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            return parse(sb.toString());
        } finally {
            c.disconnect();
        }
    }
}
