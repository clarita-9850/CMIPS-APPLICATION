package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.ElectronicFormEntity;
import com.cmips.service.CaseMaintenanceService;
import com.cmips.service.SocFormPdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Electronic Form Controller — DSD Section 25, CI-71055/67718/67782/67898/507512/67891/507511
 * Manages electronic form requests with BVI format options and print method selection.
 */
@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class ElectronicFormController {

    private static final Logger log = LoggerFactory.getLogger(ElectronicFormController.class);

    private final CaseMaintenanceService maintenanceService;
    private final SocFormPdfService socFormPdfService;

    public ElectronicFormController(CaseMaintenanceService maintenanceService,
                                    SocFormPdfService socFormPdfService) {
        this.maintenanceService = maintenanceService;
        this.socFormPdfService = socFormPdfService;
    }

    @GetMapping("/{caseId}/forms")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<ElectronicFormEntity>> getForms(@PathVariable Long caseId) {
        return ResponseEntity.ok(maintenanceService.getElectronicForms(caseId));
    }

    @PostMapping("/{caseId}/forms")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> requestForm(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(maintenanceService.requestForm(caseId, request, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/forms/{id}/download")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> downloadForm(@PathVariable Long id) {
        try {
            byte[] pdf = socFormPdfService.generateFormPdf(id);
            ElectronicFormEntity form = maintenanceService.getElectronicForm(id);
            String filename = "SOC-" + form.getFormType().name().replace("SOC_", "").replace("_", "-")
                    + "-" + id + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (RuntimeException e) {
            log.error("Failed to generate PDF for form {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/forms/{id}/inactivate")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> inactivateForm(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(maintenanceService.inactivateForm(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/forms/{id}/suppress")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> suppressForm(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(maintenanceService.suppressForm(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/forms/{id}/mark-printed")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> markPrinted(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(maintenanceService.markFormPrinted(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
