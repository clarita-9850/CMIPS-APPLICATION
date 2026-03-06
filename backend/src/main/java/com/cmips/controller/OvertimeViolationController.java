package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.OvertimeViolationEntity;
import com.cmips.repository.OvertimeViolationRepository;
import com.cmips.service.OvertimeViolationLetterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Overtime Violation Letter Controller — DSD Section 23
 *
 * Adds letter download capability to existing violation records.
 * CRUD endpoints (create/list violations) are in ProviderManagementController.
 *
 * New endpoint:
 *   GET /api/providers/violations/{id}/letter
 *       ?type=VIOLATION_NOTICE|COUNTY_DISPUTE|SUPERVISOR_REVIEW|CDSS_REVIEW
 *       → returns SOC 2257 / 2258 / 2259 / 2260 / 2261 / 2262 / 2263 PDF
 */
@RestController
@RequestMapping("/api/providers")
@CrossOrigin(origins = "*")
public class OvertimeViolationController {

    private static final Logger log = LoggerFactory.getLogger(OvertimeViolationController.class);

    private final OvertimeViolationRepository violationRepository;
    private final OvertimeViolationLetterService letterService;

    public OvertimeViolationController(OvertimeViolationRepository violationRepository,
                                       OvertimeViolationLetterService letterService) {
        this.violationRepository = violationRepository;
        this.letterService = letterService;
    }

    /**
     * Download a SOC 2257–2263 violation letter as PDF.
     *
     * @param id    OvertimeViolationEntity id
     * @param type  VIOLATION_NOTICE (default), COUNTY_DISPUTE, SUPERVISOR_REVIEW, CDSS_REVIEW
     */
    @GetMapping("/violations/{id}/letter")
    @RequirePermission(resource = "Provider Resource", scope = "view")
    public ResponseEntity<byte[]> downloadLetter(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "VIOLATION_NOTICE") String type) {

        try {
            byte[] pdf = letterService.generateLetter(id, type);

            OvertimeViolationEntity v = violationRepository.findById(id).orElse(null);
            String socNum = resolveSocNumber(v, type);
            String filename = socNum + "-" + id + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception ex) {
            log.error("[OT-LETTER] Download failed for violation {}: {}", id, ex.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private String resolveSocNumber(OvertimeViolationEntity v, String type) {
        if ("COUNTY_DISPUTE".equalsIgnoreCase(type))   return "SOC-2261";
        if ("SUPERVISOR_REVIEW".equalsIgnoreCase(type)) return "SOC-2262";
        if ("CDSS_REVIEW".equalsIgnoreCase(type))      return "SOC-2263";
        if (v == null) return "SOC-2257";
        return switch (v.getViolationNumber() != null ? v.getViolationNumber() : 1) {
            case 2 -> "SOC-2258";
            case 3 -> "SOC-2259";
            case 4 -> "SOC-2260";
            default -> "SOC-2257";
        };
    }
}
