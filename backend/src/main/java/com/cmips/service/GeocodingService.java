package com.cmips.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Geocoding Service — BR OS 67
 *
 * Resolves a US address to geographic coordinates (latitude/longitude).
 *
 * In production this would call a real geocoding provider (Google Maps API,
 * HERE Geocoding, or the USPS Address Matching API).
 * For MVP: returns deterministic mock coordinates based on ZIP code,
 * approximating California county centroids.
 */
@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);

    /**
     * Geocode a US address into latitude/longitude coordinates.
     *
     * @param streetAddress  Street number + name (e.g. "123 MAIN ST")
     * @param city           City name
     * @param state          State abbreviation (e.g. "CA")
     * @param zip            5-digit ZIP code
     * @return Coordinates (never null — falls back to Sacramento centroid)
     */
    public Coordinates geocode(String streetAddress, String city, String state, String zip) {
        if (zip == null || zip.length() < 5) {
            log.warn("[Geocoding] Invalid ZIP '{}' — returning Sacramento centroid", zip);
            return SACRAMENTO_CENTROID;
        }

        String zip5 = zip.substring(0, 5);
        Coordinates coords = ZIP_CENTROIDS.getOrDefault(zip5, approximateFromZip(zip5));
        log.info("[Geocoding] {} {}, {} {} -> lat={}, lon={}", streetAddress, city, state, zip5,
                coords.getLatitude(), coords.getLongitude());
        return coords;
    }

    /**
     * Approximate coordinates from the ZIP numeric range.
     * California ZIPs run 90001–96162. This approximation is used
     * when the specific ZIP is not in the lookup table.
     */
    private Coordinates approximateFromZip(String zip5) {
        try {
            int zipNum = Integer.parseInt(zip5);
            // California: 90001-96162 spans roughly lat 32.5–42.0, lon -124.5 to -114.1
            if (zipNum >= 90001 && zipNum <= 96162) {
                double fraction = (double)(zipNum - 90001) / (96162 - 90001);
                double lat = 32.5 + fraction * (42.0 - 32.5);
                double lon = -124.5 + fraction * (-114.1 - (-124.5));
                return new Coordinates(Math.round(lat * 10000.0) / 10000.0,
                                       Math.round(lon * 10000.0) / 10000.0);
            }
        } catch (NumberFormatException ignored) {
            // Fall through to default
        }
        return SACRAMENTO_CENTROID;
    }

    // ==================== Common California county centroids by representative ZIP ====================

    private static final Coordinates SACRAMENTO_CENTROID = new Coordinates(38.5816, -121.4944);

    @SuppressWarnings("serial")
    private static final java.util.Map<String, Coordinates> ZIP_CENTROIDS =
            java.util.Collections.unmodifiableMap(new java.util.HashMap<>() {{
                // Los Angeles County
                put("90001", new Coordinates(33.9731, -118.2479));
                put("90210", new Coordinates(34.0901, -118.4065));
                put("90012", new Coordinates(34.0564, -118.2385));
                // San Diego County
                put("92101", new Coordinates(32.7157, -117.1611));
                put("92037", new Coordinates(32.8422, -117.2742));
                // Orange County
                put("92701", new Coordinates(33.7455, -117.8678));
                put("92868", new Coordinates(33.7879, -117.8531));
                // San Francisco County
                put("94102", new Coordinates(37.7801, -122.4152));
                put("94103", new Coordinates(37.7726, -122.4099));
                put("94110", new Coordinates(37.7484, -122.4156));
                // Alameda County
                put("94601", new Coordinates(37.7697, -122.2182));
                put("94612", new Coordinates(37.8044, -122.2712));
                // Sacramento County
                put("95814", new Coordinates(38.5816, -121.4944));
                put("95825", new Coordinates(38.5907, -121.3908));
                // Contra Costa County
                put("94520", new Coordinates(37.9735, -122.0311));
                // Fresno County
                put("93721", new Coordinates(36.7468, -119.7726));
                // Kern County
                put("93301", new Coordinates(35.3733, -119.0187));
                // San Bernardino County
                put("92401", new Coordinates(34.1083, -117.2898));
                // Riverside County
                put("92501", new Coordinates(33.9533, -117.3961));
                // Santa Clara County
                put("95110", new Coordinates(37.3382, -121.8863));
                put("94040", new Coordinates(37.3861, -122.0839));
                // Ventura County
                put("93001", new Coordinates(34.2805, -119.2945));
                // Monterey County
                put("93940", new Coordinates(36.6002, -121.8947));
                // Shasta County
                put("96001", new Coordinates(40.5865, -122.3917));
                // Humboldt County
                put("95501", new Coordinates(40.8021, -124.1637));
            }});

    // ==================== Coordinates DTO ====================

    public static class Coordinates {
        private final double latitude;
        private final double longitude;

        public Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude()  { return latitude; }
        public double getLongitude() { return longitude; }
    }
}
