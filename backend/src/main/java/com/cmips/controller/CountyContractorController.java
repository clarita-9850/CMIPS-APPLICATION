package com.cmips.controller;

import com.cmips.entity.CCInvoiceDetailsEntity;
import com.cmips.entity.CountyContractorEntity;
import com.cmips.entity.CountyContractorInvoiceEntity;
import com.cmips.service.CountyContractorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * County Contractor REST Controller
 * Provides endpoints for Rate Management (CRUD) and Invoice Processing.
 */
@RestController
@RequestMapping("/api/county-contractors")
@CrossOrigin(origins = "*")
public class CountyContractorController {

    private static final Logger log = LoggerFactory.getLogger(CountyContractorController.class);

    private final CountyContractorService service;

    public CountyContractorController(CountyContractorService service) {
        this.service = service;
    }

    // ==================== RATE ENDPOINTS ====================

    /**
     * GET /rates?countyCode=XX — List rates by county
     */
    @GetMapping("/rates")
    public ResponseEntity<List<CountyContractorEntity>> getRatesByCounty(
            @RequestParam(required = false) String countyCode) {
        if (countyCode == null || countyCode.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        List<CountyContractorEntity> rates = service.getRatesByCounty(countyCode);
        return ResponseEntity.ok(rates);
    }

    /**
     * GET /rates/{id} — View single rate
     */
    @GetMapping("/rates/{id}")
    public ResponseEntity<CountyContractorEntity> getRateById(@PathVariable Long id) {
        CountyContractorEntity rate = service.getRateById(id);
        return ResponseEntity.ok(rate);
    }

    /**
     * POST /rates — Create rate
     */
    @PostMapping("/rates")
    public ResponseEntity<?> createRate(
            @RequestBody CountyContractorEntity entity,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "system") String userId) {
        try {
            CountyContractorEntity created = service.createRate(entity, userId);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /rates/{id} — Modify rate
     */
    @PutMapping("/rates/{id}")
    public ResponseEntity<?> modifyRate(
            @PathVariable Long id,
            @RequestBody CountyContractorEntity entity,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "system") String userId) {
        try {
            CountyContractorEntity updated = service.modifyRate(id, entity, userId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== INVOICE ENDPOINTS ====================

    /**
     * GET /invoices?countyContractorId=XX — List invoices for contractor
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<CountyContractorInvoiceEntity>> getInvoicesByContractor(
            @RequestParam Long countyContractorId) {
        List<CountyContractorInvoiceEntity> invoices = service.getInvoicesByContractor(countyContractorId);
        return ResponseEntity.ok(invoices);
    }

    /**
     * GET /invoices/{id} — View invoice with details
     */
    @GetMapping("/invoices/{id}")
    public ResponseEntity<Map<String, Object>> getInvoiceById(@PathVariable Long id) {
        CountyContractorInvoiceEntity invoice = service.getInvoiceById(id);
        List<CCInvoiceDetailsEntity> details = service.getInvoiceDetails(id);
        Map<String, Object> response = new HashMap<>();
        response.put("invoice", invoice);
        response.put("details", details);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /invoices/{id} — Modify invoice (warrant/paid date)
     */
    @PutMapping("/invoices/{id}")
    public ResponseEntity<?> modifyInvoice(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "system") String userId) {
        try {
            String warrantNumber = body.get("warrantNumber");
            String paidDateStr = body.get("paidDate");
            LocalDate paidDate = (paidDateStr != null && !paidDateStr.isBlank()) ? LocalDate.parse(paidDateStr) : null;

            CountyContractorInvoiceEntity updated = service.modifyInvoice(id, warrantNumber, paidDate, userId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /invoices/{id}/details — Invoice detail rows
     */
    @GetMapping("/invoices/{id}/details")
    public ResponseEntity<List<CCInvoiceDetailsEntity>> getInvoiceDetails(@PathVariable Long id) {
        List<CCInvoiceDetailsEntity> details = service.getInvoiceDetails(id);
        return ResponseEntity.ok(details);
    }

    /**
     * GET /invoices/{id}/soc432 — Generate SOC 432 data (Contract Expenditure Report)
     */
    @GetMapping("/invoices/{id}/soc432")
    public ResponseEntity<?> getSoc432(@PathVariable Long id) {
        try {
            CountyContractorInvoiceEntity invoice = service.validateAndGetSoc432(id);
            List<CCInvoiceDetailsEntity> details = service.getInvoiceDetails(id);
            Map<String, Object> response = new HashMap<>();
            response.put("invoice", invoice);
            response.put("details", details);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
