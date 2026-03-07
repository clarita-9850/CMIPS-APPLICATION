package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.service.TimesheetInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * DSD Section 24 — Internal Interface REST Endpoints
 *
 * PRDS108A: Timesheet Processing Summary to SCO
 * PRDS943B: Payroll Detail to EDD (quarterly)
 * CMNR932A: Common Number Record to DOJ
 *
 * These endpoints expose the flat-file generation as REST for operational use,
 * batch scheduling, and integration testing.
 */
@RestController
@RequestMapping("/api/interfaces")
public class TimesheetInterfaceController {

    @Autowired private TimesheetInterfaceService interfaceService;

    /**
     * PRDS108A — Generate Timesheet Processing Summary for SCO.
     * POST body: { "timesheetIds": [1, 2, 3] }
     */
    @PostMapping("/prds108a")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<Map<String, Object>> generatePRDS108A(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) request.getOrDefault("timesheetIds", List.of());
        List<Long> tsIds = ids.stream().map(Number::longValue).toList();

        if (tsIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "timesheetIds is required and must not be empty"));
        }

        List<String> records = interfaceService.generatePRDS108A(tsIds);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("interfaceType", "PRDS108A");
        result.put("description", "Timesheet Processing Summary to SCO");
        result.put("recordCount", records.size());
        result.put("records", records);
        return ResponseEntity.ok(result);
    }

    /**
     * PRDS943B — Generate Payroll Detail for EDD (quarterly wage reporting).
     * GET /api/interfaces/prds943b?quarter=1&year=2026
     */
    @GetMapping("/prds943b")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> generatePRDS943B(
            @RequestParam int quarter,
            @RequestParam int year) {
        if (quarter < 1 || quarter > 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "quarter must be 1-4"));
        }
        if (year < 2018 || year > 2100) {
            return ResponseEntity.badRequest().body(Map.of("error", "year out of range"));
        }

        List<String> records = interfaceService.generatePRDS943B(quarter, year);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("interfaceType", "PRDS943B");
        result.put("description", "Payroll Detail to EDD - Q" + quarter + "/" + year);
        result.put("recordCount", records.size());
        result.put("records", records);
        return ResponseEntity.ok(result);
    }

    /**
     * CMNR932A — Generate Common Number Record for DOJ.
     * POST body: { "providerIds": [1, 2, 3] }
     */
    @PostMapping("/cmnr932a")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<Map<String, Object>> generateCMNR932A(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) request.getOrDefault("providerIds", List.of());
        List<Long> providerIds = ids.stream().map(Number::longValue).toList();

        if (providerIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "providerIds is required and must not be empty"));
        }

        List<String> records = interfaceService.generateCMNR932A(providerIds);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("interfaceType", "CMNR932A");
        result.put("description", "Common Number Record to DOJ");
        result.put("recordCount", records.size());
        result.put("records", records);
        return ResponseEntity.ok(result);
    }

    /**
     * List available interface types and their descriptions.
     */
    @GetMapping("/types")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<Map<String, String>>> listInterfaceTypes() {
        List<Map<String, String>> types = List.of(
            Map.of("type", "PRDS108A", "description", "Timesheet Processing Summary to SCO (State Controller's Office)",
                    "method", "POST", "endpoint", "/api/interfaces/prds108a"),
            Map.of("type", "PRDS943B", "description", "Payroll Detail to EDD (Employment Development Department) - Quarterly",
                    "method", "GET", "endpoint", "/api/interfaces/prds943b?quarter=N&year=YYYY"),
            Map.of("type", "CMNR932A", "description", "Common Number Record to DOJ (Department of Justice)",
                    "method", "POST", "endpoint", "/api/interfaces/cmnr932a"),
            Map.of("type", "PRDS108A-SLC", "description", "Sick Leave Data to Payroll (via SickLeaveClaimController send-to-payroll)",
                    "method", "POST", "endpoint", "/api/sick-leave-claims/{claimNumber}/send-to-payroll")
        );
        return ResponseEntity.ok(types);
    }
}
