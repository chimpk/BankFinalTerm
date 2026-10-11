package vn.edu.tdtu.tdtbank.data.remote;

import android.content.Context;

import vn.edu.tdtu.tdtbank.model.UtilityCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Danh mục dịch vụ MẪU đọc từ assets/utility_catalog.json (nhà cung cấp, mệnh giá, vé, phòng, sản phẩm).
 * Không kết nối nhà cung cấp thật: tiền thật của tài khoản trong app vẫn bị trừ theo giao dịch.
 */
public class UtilityCatalog {

    public static class Item {
        public final String name;
        public final long price;

        public Item(String name, long price) {
            this.name = name;
            this.price = price;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Map<UtilityCategory, List<String>> providers = new EnumMap<>(UtilityCategory.class);
    private final Map<UtilityCategory, List<Item>> items = new EnumMap<>(UtilityCategory.class);
    private final List<Long> topupAmounts = new ArrayList<>();

    public static UtilityCatalog load(Context context) throws IOException, JSONException {
        try (InputStream in = context.getAssets().open("utility_catalog.json")) {
            byte[] buf = new byte[in.available()];
            int n = in.read(buf);
            return parse(new String(buf, 0, Math.max(n, 0), StandardCharsets.UTF_8));
        }
    }

    public static UtilityCatalog parse(String json) throws JSONException {
        UtilityCatalog c = new UtilityCatalog();
        JSONObject root = new JSONObject(json);
        JSONObject prov = root.getJSONObject("providers");
        for (UtilityCategory cat : UtilityCategory.values()) {
            if (prov.has(cat.name())) {
                List<String> list = new ArrayList<>();
                JSONArray a = prov.getJSONArray(cat.name());
                for (int i = 0; i < a.length(); i++) {
                    list.add(a.getString(i));
                }
                c.providers.put(cat, list);
            }
        }
        JSONObject its = root.getJSONObject("items");
        for (UtilityCategory cat : UtilityCategory.values()) {
            if (its.has(cat.name())) {
                List<Item> list = new ArrayList<>();
                JSONArray a = its.getJSONArray(cat.name());
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    list.add(new Item(o.getString("name"), o.getLong("price")));
                }
                c.items.put(cat, list);
            }
        }
        JSONArray amounts = root.getJSONArray("topupAmounts");
        for (int i = 0; i < amounts.length(); i++) {
            c.topupAmounts.add(amounts.getLong(i));
        }
        return c;
    }

    public List<String> providers(UtilityCategory cat) {
        List<String> l = providers.get(cat);
        return l == null ? new ArrayList<>() : l;
    }

    public List<Item> items(UtilityCategory cat) {
        List<Item> l = items.get(cat);
        return l == null ? new ArrayList<>() : l;
    }

    public List<Long> topupAmounts() {
        return topupAmounts;
    }
}
