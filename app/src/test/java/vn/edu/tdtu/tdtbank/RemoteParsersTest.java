package vn.edu.tdtu.tdtbank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import vn.edu.tdtu.tdtbank.data.remote.RoutingClient;
import vn.edu.tdtu.tdtbank.data.remote.UtilityCatalog;
import vn.edu.tdtu.tdtbank.model.UtilityCategory;

import org.junit.Test;

public class RemoteParsersTest {

    @Test
    public void osrmResponse_isParsedIntoRoute() throws Exception {
        String json = "{\"code\":\"Ok\",\"routes\":[{\"distance\":1234.5,\"duration\":900.0,"
                + "\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[106.7,10.73],[106.71,10.74]]}}]}";
        RoutingClient.Route r = RoutingClient.parse(json);
        assertNotNull(r);
        assertEquals(1234.5, r.distanceMeters, 1e-9);
        assertEquals(900.0, r.durationSeconds, 1e-9);
        assertEquals(2, r.points.size());
        assertEquals(10.73, r.points.get(0)[0], 1e-9); // {lat, lon}: GeoJSON là {lon, lat} nên phải đảo
        assertEquals(106.7, r.points.get(0)[1], 1e-9);
    }

    @Test
    public void osrmNoRoute_returnsNull() throws Exception {
        assertNull(RoutingClient.parse("{\"code\":\"NoRoute\",\"routes\":[]}"));
        assertNull(RoutingClient.parse("{\"code\":\"Ok\",\"routes\":[]}"));
    }

    @Test
    public void utilityCatalog_parsesProvidersItemsAndTopups() throws Exception {
        String json = "{\"providers\":{\"ELECTRICITY\":[\"A\",\"B\"]},\"topupAmounts\":[10000,20000],"
                + "\"items\":{\"FLIGHT\":[{\"name\":\"F1\",\"price\":1500000}]}}";
        UtilityCatalog c = UtilityCatalog.parse(json);
        assertEquals(2, c.providers(UtilityCategory.ELECTRICITY).size());
        assertEquals(0, c.providers(UtilityCategory.WATER).size());
        assertEquals(1, c.items(UtilityCategory.FLIGHT).size());
        assertEquals(1_500_000L, c.items(UtilityCategory.FLIGHT).get(0).price);
        assertTrue(c.topupAmounts().contains(20000L));
    }

    @Test
    public void bundledCatalogAsset_isValid() throws Exception {
        String json = new String(java.nio.file.Files.readAllBytes(
                java.nio.file.Paths.get("src/main/assets/utility_catalog.json")), "UTF-8");
        UtilityCatalog c = UtilityCatalog.parse(json);
        for (UtilityCategory cat : new UtilityCategory[]{UtilityCategory.FLIGHT, UtilityCategory.MOVIE,
                UtilityCategory.HOTEL, UtilityCategory.ECOMMERCE}) {
            assertTrue(cat.name(), c.items(cat).size() >= 3);
        }
        assertTrue(c.providers(UtilityCategory.PHONE_TOPUP).size() >= 3);
        assertTrue(c.topupAmounts().size() >= 5);
    }
}
