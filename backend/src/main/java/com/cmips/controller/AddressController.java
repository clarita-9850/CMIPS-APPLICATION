package com.cmips.controller;

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
 * In production this would call the USPS CASS web service.
 * For MVP: simulates 90% match, 10% correction suggestion.
 */
@RestController
@RequestMapping("/api/address")
@CrossOrigin(origins = "*")
public class AddressController {

    private static final Logger log = LoggerFactory.getLogger(AddressController.class);

    /**
     * Verify an address against CASS (mock).
     * Body: { streetNumber, streetName, unitType, unitNumber, city, state, zip }
     * Returns: { cassMatch, cassUpdates, cassFailed, streetNumber, streetName,
     *             unitType, unitNumber, city, state, zip }
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyAddress(
            @RequestBody Map<String, String> req) {

        String streetName = req.getOrDefault("streetName", "");
        String city       = req.getOrDefault("city", "");
        String zip        = req.getOrDefault("zip", "");

        // Mock CASS: verify basic required fields are present
        if (streetName.isBlank()) {
            return ResponseEntity.ok(java.util.Map.of(
                "cassMatch",   false,
                "cassFailed",  true,
                "cassUpdates", "EM-24: Street name is required",
                "streetNumber", req.getOrDefault("streetNumber", ""),
                "streetName",  streetName,
                "unitType",    req.getOrDefault("unitType", ""),
                "unitNumber",  req.getOrDefault("unitNumber", ""),
                "city",        city,
                "state",       req.getOrDefault("state", "CA"),
                "zip",         zip
            ));
        }

        if (zip.isBlank() || !zip.matches("\\d{5}(-\\d{4})?")) {
            return ResponseEntity.ok(java.util.Map.of(
                "cassMatch",   false,
                "cassFailed",  true,
                "cassUpdates", "EM-25: ZIP code must be 5 or 9 digits",
                "streetNumber", req.getOrDefault("streetNumber", ""),
                "streetName",  streetName,
                "unitType",    req.getOrDefault("unitType", ""),
                "unitNumber",  req.getOrDefault("unitNumber", ""),
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
        String unitNumber = req.getOrDefault("unitNumber", "");
        if (!unitNumber.isBlank() && req.getOrDefault("unitType", "").isBlank()) {
            hasCassMatch = false;
            cassUpdates = "EM-178: Unit type not specified. CASS suggests adding unit type (APT, STE, UNIT).";
        }

        log.info("[CASS mock] verify address: {}, {}, {} -> cassMatch={}", streetName, city, zip, hasCassMatch);

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("cassMatch",   hasCassMatch);
        response.put("cassFailed",  !hasCassMatch && cassUpdates == null);
        response.put("cassUpdates", cassUpdates);
        response.put("streetNumber", req.getOrDefault("streetNumber", ""));
        response.put("streetName",  streetName);
        response.put("unitType",    req.getOrDefault("unitType", ""));
        response.put("unitNumber",  unitNumber);
        response.put("city",        city);
        response.put("state",       req.getOrDefault("state", "CA"));
        response.put("zip",         zip);

        return ResponseEntity.ok(response);
    }
}
