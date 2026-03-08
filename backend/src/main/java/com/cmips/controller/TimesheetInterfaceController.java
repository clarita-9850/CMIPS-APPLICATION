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
 * Provides two modes:
 *   1. Generate + Send: Writes @FileType records via FileRepository and sends to SFTP destination
 *   2. Legacy: Returns raw fixed-width strings for inspection/testing
 *
 * PRDS108A: Timesheet Processing Summary → SCO
 * PRDS943B: Payroll Detail → EDD (quarterly)
 * CMNR932A: Common Number Record → DOJ
 */
@RestController
@RequestMapping("/api/interfaces")
public class TimesheetInterfaceController {

    @Autowired private TimesheetInterfaceService interfaceService;

    // ==================== Generate + Send (new Integration Hub pipeline) ====================

    /**
     * PRDS108A — Generate and send Timesheet Processing Summary to SCO via SFTP.
     * POST body: { "timesheetIds": [1, 2, 3] }
     */
    @PostMapping("/prds108a/send")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<Map<String, Object>> generateAndSendPRDS108A(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) request.getOrDefault("timesheetIds", List.of());
        List<Long> tsIds = ids.stream().map(Number::longValue).toList();

        if (tsIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "timesheetIds is required and must not be empty"));
        }

        Map<String, Object> result = interfaceService.generateAndSendPRDS108A(tsIds);
        return ResponseEntity.ok(result);
    }

    /**
     * PRDS943B — Generate and send Payroll Detail to EDD via SFTP (quarterly).
     * POST body: { "quarter": 1, "year": 2026 }
     */
    @PostMapping("/prds943b/send")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<Map<String, Object>> generateAndSendPRDS943B(@RequestBody Map<String, Object> request) {
        int quarter = ((Number) request.getOrDefault("quarter", 0)).intValue();
        int year = ((Number) request.getOrDefault("year", 0)).intValue();

        if (quarter < 1 || quarter > 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "quarter must be 1-4"));
        }
        if (year < 2018 || year > 2100) {
            return ResponseEntity.badRequest().body(Map.of("error", "year out of range"));
        }

        Map<String, Object> result = interfaceService.generateAndSendPRDS943B(quarter, year);
        return ResponseEntity.ok(result);
    }

    /**
     * CMNR932A — Generate and send Common Number Record to DOJ via SFTP.
     * POST body: { "providerIds": [1, 2, 3] }
     */
    @PostMapping("/cmnr932a/send")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<Map<String, Object>> generateAndSendCMNR932A(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) request.getOrDefault("providerIds", List.of());
        List<Long> providerIds = ids.stream().map(Number::longValue).toList();

        if (providerIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "providerIds is required and must not be empty"));
        }

        Map<String, Object> result = interfaceService.generateAndSendCMNR932A(providerIds);
        return ResponseEntity.ok(result);
    }

    /**
     * PRDR120A — Generate and send Payment File to SCO via SFTP (on-demand).
     * Normally triggered by batch job; this endpoint allows manual/REST-driven send.
     * POST body: { "timesheetIds": [1, 2, 3] }
     */
    @PostMapping("/prdr120a/send")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<Map<String, Object>> generateAndSendPRDR120A(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) request.getOrDefault("timesheetIds", List.of());
        List<Long> tsIds = ids.stream().map(Number::longValue).toList();

        if (tsIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "timesheetIds is required and must not be empty"));
        }

        Map<String, Object> result = interfaceService.generateAndSendPRDR120A(tsIds);
        return ResponseEntity.ok(result);
    }

    // ==================== Legacy (raw string output for inspection) ====================

    /**
     * PRDS108A — Generate Timesheet Processing Summary as raw records.
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
     * PRDS943B — Generate Payroll Detail for EDD as raw records.
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
     * CMNR932A — Generate Common Number Record for DOJ as raw records.
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
     * List available interface types and their endpoints.
     */
    @GetMapping("/types")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<Map<String, String>>> listInterfaceTypes() {
        List<Map<String, String>> types = List.of(
            Map.of("type", "PRDS108A", "description", "Timesheet Processing Summary to SCO",
                    "method", "POST", "endpoint", "/api/interfaces/prds108a",
                    "sendEndpoint", "/api/interfaces/prds108a/send",
                    "destination", "SCO (State Controller's Office)"),
            Map.of("type", "PRDR120A", "description", "Payment File to SCO (on-demand)",
                    "method", "POST", "endpoint", "(batch only)",
                    "sendEndpoint", "/api/interfaces/prdr120a/send",
                    "destination", "SCO (State Controller's Office)"),
            Map.of("type", "PRDS943B", "description", "Payroll Detail to EDD (Quarterly)",
                    "method", "GET/POST", "endpoint", "/api/interfaces/prds943b",
                    "sendEndpoint", "/api/interfaces/prds943b/send",
                    "destination", "EDD (Employment Development Department)"),
            Map.of("type", "CMNR932A", "description", "Common Number Record to DOJ",
                    "method", "POST", "endpoint", "/api/interfaces/cmnr932a",
                    "sendEndpoint", "/api/interfaces/cmnr932a/send",
                    "destination", "DOJ (Department of Justice)")
        );
        return ResponseEntity.ok(types);
    }
}
