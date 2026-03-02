package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.service.SCIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SCIController – REST endpoints for Statewide Client Index (SCI) interactions.
 *
 * Implements the multi-step CIN Clearance flow from DSD Section 20:
 *
 *  1. GET  /api/sci/search            — OI transaction (BR 32/33): search for CIN candidates
 *  2. GET  /api/sci/meds-eligibility  — EL/OM transaction: fetch Medi-Cal eligibility for a CIN
 *  3. GET  /api/sci/check-cin         — Scenario 6 / EM-202: check if CIN is already in CMIPS
 *
 * All endpoints require authentication and the "Application Resource / edit" permission
 * since CIN clearance is part of the Create Case workflow.
 */
@RestController
@RequestMapping("/api/sci")
@CrossOrigin(origins = "*")
public class SCIController {

    private static final Logger log = LoggerFactory.getLogger(SCIController.class);

    private final SCIService sciService;

    public SCIController(SCIService sciService) {
        this.sciService = sciService;
    }

    // ============================================================
    // OI TRANSACTION: CIN Search (BR 32 / BR 33)
    // ============================================================

    /**
     * Initiates the SCI OI (CIN Search) transaction.
     *
     * Query params:
     *   lastName      – applicant last name
     *   firstName     – applicant first name
     *   dob           – date of birth (yyyy-MM-dd)
     *   gender        – Male / Female / Other
     *   cin           – CIN already on case (BR 32); omit if none (BR 33)
     *   ssn           – SSN (omitted if mediCalPseudo=true)
     *   mediCalPseudo – true = exclude SSN from SCI request
     *
     * Response scenarios:
     *   NO_MATCH      – "CIN does not exist for the applicant"
     *   MATCHES_FOUND – list of CIN records + EM-186 informational message
     */
    @GetMapping("/search")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> sciSearch(
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String dob,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String cin,
            @RequestParam(required = false) String ssn,
            @RequestParam(defaultValue = "false") boolean mediCalPseudo) {

        try {
            log.info("SCI OI search: lastName={}, firstName={}, cin={}", lastName, firstName, cin);
            Map<String, Object> result = sciService.sciSearch(
                    lastName, firstName, dob, gender, cin, ssn, mediCalPseudo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("SCI search error", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // EL/OM TRANSACTION: Medi-Cal Eligibility
    // ============================================================

    /**
     * Initiates the SCI EL transaction and returns the OM (Medi-Cal Eligibility Return) data.
     *
     * Query params:
     *   cin – the CIN whose eligibility is being requested
     *
     * Response scenarios:
     *   SUCCESS – eligibility data from OM response
     *   FAILED  – Scenario 3: EL/OM transaction not successful (error shown on CIN search screen)
     */
    @GetMapping("/meds-eligibility")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> getMediCalEligibility(@RequestParam String cin) {
        try {
            log.info("SCI EL transaction for CIN: {}", cin);
            Map<String, Object> result = sciService.getMediCalEligibility(cin);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("SCI EL/OM error for CIN: {}", cin, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // CIN AVAILABILITY CHECK (Scenario 6 / EM-202)
    // ============================================================

    /**
     * Checks whether a CIN is already assigned to a different person in CMIPS.
     *
     * Query params:
     *   cin           – the CIN to check
     *   applicationId – the application performing the check (excluded from conflict detection)
     *
     * Response:
     *   { available: true }
     *   { available: false, errorCode: "EM-202", message: "..." }
     */
    @GetMapping("/check-cin")
    @RequirePermission(resource = "Application Resource", scope = "edit")
    public ResponseEntity<?> checkCinAvailability(
            @RequestParam String cin,
            @RequestParam(required = false, defaultValue = "") String applicationId) {
        try {
            Map<String, Object> result = sciService.checkCinAvailability(cin, applicationId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("CIN availability check error", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
