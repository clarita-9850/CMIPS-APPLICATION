package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.CountyContractorInvoiceEntity;
import com.cmips.service.CaseMaintenanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * County Contractor Controller — DSD Section 25, CI-67732
 * Manages contractor invoices and SOC 432 (Claim for Reimbursement) generation.
 */
@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class CountyContractorController {

    private static final Logger log = LoggerFactory.getLogger(CountyContractorController.class);

    private final CaseMaintenanceService maintenanceService;

    public CountyContractorController(CaseMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("/{caseId}/contractor-invoices")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<CountyContractorInvoiceEntity>> getInvoices(@PathVariable Long caseId) {
        return ResponseEntity.ok(maintenanceService.getContractorInvoices(caseId));
    }

    @PostMapping("/{caseId}/contractor-invoices")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> createInvoice(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(maintenanceService.createContractorInvoice(caseId, request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/contractor-invoices/{id}/authorize")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> authorizeInvoice(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            CountyContractorInvoiceEntity authorized = maintenanceService.authorizeContractorInvoice(
                    id, request != null ? request : Map.of(), userId);
            return ResponseEntity.ok(Map.of(
                    "invoice", authorized,
                    "soc432Url", "/api/cases/contractor-invoices/" + id + "/soc432",
                    "message", "Invoice authorized. SOC 432 available for download."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/contractor-invoices/{id}/reject")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> rejectInvoice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            String reason = request.get("rejectionReason") != null
                    ? (String) request.get("rejectionReason") : null;
            return ResponseEntity.ok(maintenanceService.rejectContractorInvoice(id, reason, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/contractor-invoices/{id}/soc432")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> downloadSoc432(@PathVariable Long id) {
        // Mock response — in production: generate SOC 432 PDF
        return ResponseEntity.ok(Map.of(
                "invoiceId", id,
                "formType", "SOC_432",
                "message", "SOC 432 (Claim for Reimbursement) — PDF generation endpoint."
        ));
    }
}
