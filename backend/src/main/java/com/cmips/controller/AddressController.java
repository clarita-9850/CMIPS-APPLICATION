package com.cmips.controller;

import com.cmips.service.GeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Address Verification Controller — Mock CASS (Coding Accuracy Support System).
 *
 * Per DSD CI-116197: Address verification is informational and non-blocking.
 * Workers may proceed with an unverified address.
 *
 * BR OS 67: Geocoding (lat/lon) added to CASS response.
 * BR OS 70: Special character validation rejects < > " ' & % ; ! \ / in address fields.
 *
 * In production this would call the USPS CASS web service.
 * For MVP: simulates 90% match, 10% correction suggestion.
 */
@RestController
@RequestMapping("/api/address")
@CrossOrigin(origins = "*")
public class AddressController {

    private static final Logger log = LoggerFactory.getLogger(AddressController.class);

    private final GeocodingService geocodingService;

    public AddressController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    /**
     * Verify an address against CASS (mock).
     * Body: { streetNumber, streetName, unitType, unitNumber, city, state, zip }
     * Returns: { cassMatch, cassUpdates, cassFailed, streetNumber, streetName,
     *             unitType, unitNumber, city, state, zip }
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyAddress(
            @RequestBody Map<String, String> req) {

        String streetName  = req.getOrDefault("streetName", "");
        String streetNumber = req.getOrDefault("streetNumber", "");
        String city        = req.getOrDefault("city", "");
        String zip         = req.getOrDefault("zip", "");
        String unitType    = req.getOrDefault("unitType", "");
        String unitNumber  = req.getOrDefault("unitNumber", "");

        // BR OS 70: Reject special characters in address fields (EM OS 070)
        // Characters disallowed: < > " ' & % ; ! \ /
        java.util.regex.Pattern SPECIAL_CHARS = java.util.regex.Pattern.compile("[<>\"'&%;!\\\\/]");
        for (Map.Entry<String, String> field : java.util.Map.of(
                "streetName", streetName,
                "streetNumber", streetNumber,
                "city", city,
                "unitType", unitType,
                "unitNumber", unitNumber).entrySet()) {
            if (!field.getValue().isBlank() && SPECIAL_CHARS.matcher(field.getValue()).find()) {
                log.warn("[CASS mock] BR OS 070: Special character found in field '{}': {}", field.getKey(), field.getValue());
                java.util.Map<String, Object> errResp = new java.util.LinkedHashMap<>();
                errResp.put("cassMatch", false);
                errResp.put("cassFailed", true);
                errResp.put("cassUpdates", "EM OS 070: Special characters are not allowed in address fields. Remove angle brackets, quotes, ampersands, semicolons, and similar characters, then resubmit.");
                errResp.put("streetNumber", streetNumber);
                errResp.put("streetName", streetName);
                errResp.put("unitType", unitType);
                errResp.put("unitNumber", unitNumber);
                errResp.put("city", city);
                errResp.put("state", req.getOrDefault("state", "CA"));
                errResp.put("zip", zip);
                errResp.put("latitude", null);
                errResp.put("longitude", null);
                return ResponseEntity.ok(errResp);
            }
        }

        // Mock CASS: verify basic required fields are present
        if (streetName.isBlank()) {
            return ResponseEntity.ok(java.util.Map.of(
                "cassMatch",   false,
                "cassFailed",  true,
                "cassUpdates", "EM OS 024: Street name is required",
                "streetNumber", streetNumber,
                "streetName",  streetName,
                "unitType",    unitType,
                "unitNumber",  unitNumber,
                "city",        city,
                "state",       req.getOrDefault("state", "CA"),
                "zip",         zip
            ));
        }

        if (zip.isBlank() || !zip.matches("\\d{5}(-\\d{4})?")) {
            return ResponseEntity.ok(java.util.Map.of(
                "cassMatch",   false,
                "cassFailed",  true,
                "cassUpdates", "EM OS 025: ZIP code must be 5 or 9 digits",
                "streetNumber", streetNumber,
                "streetName",  streetName,
                "unitType",    unitType,
                "unitNumber",  unitNumber,
                "city",        city,
                "state",       req.getOrDefault("state", "CA"),
                "zip",         zip
            ));
        }

        // Mock: simulate CASS match for well-formed addresses
        // In production: call USPS CASS API here
        boolean hasCassMatch = !streetName.isBlank() && !city.isBlank() && zip.matches("\\d{5}(-\\d{4})?");
        String cassUpdates = null;

        // Mock: 10% of addresses get an informational correction (unit type normalized)
        if (!unitNumber.isBlank() && unitType.isBlank()) {
            hasCassMatch = false;
            cassUpdates = "EM OS 178: Unit type not specified. CASS suggests adding unit type (APT, STE, UNIT).";
        }

        // BR OS 67: Geocoding — resolve lat/lon from address (mock implementation)
        GeocodingService.Coordinates coords = hasCassMatch
                ? geocodingService.geocode(streetNumber + " " + streetName, city, req.getOrDefault("state", "CA"), zip)
                : null;

        log.info("[CASS mock] verify address: {}, {}, {} -> cassMatch={}, lat={}, lon={}",
                streetName, city, zip, hasCassMatch,
                coords != null ? coords.getLatitude() : "N/A",
                coords != null ? coords.getLongitude() : "N/A");

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("cassMatch",   hasCassMatch);
        response.put("cassFailed",  !hasCassMatch && cassUpdates == null);
        response.put("cassUpdates", cassUpdates);
        response.put("streetNumber", streetNumber);
        response.put("streetName",  streetName);
        response.put("unitType",    unitType);
        response.put("unitNumber",  unitNumber);
        response.put("city",        city);
        response.put("state",       req.getOrDefault("state", "CA"));
        response.put("zip",         zip);
        response.put("latitude",    coords != null ? coords.getLatitude() : null);
        response.put("longitude",   coords != null ? coords.getLongitude() : null);

        return ResponseEntity.ok(response);
    }
}
