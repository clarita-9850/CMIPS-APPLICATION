package com.cmips.controller;

import com.cmips.integration.DojBackgroundCheckService;
import com.cmips.integration.MediCalSuspendedListService;
import com.cmips.integration.SsaVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * External Integration Controller
 *
 * Exposes mock external system integrations for testing and validation.
 * These endpoints simulate the payloads that would be sent to/from:
 * - SSA (Social Security Administration) SSN Verification
 * - DOJ (Department of Justice) Background Check / CORI
 * - DHCS Medi-Cal Suspended/Ineligible Provider List
 */
@RestController
@RequestMapping("/api/integration")
@CrossOrigin(origins = "*")
public class ExternalIntegrationController {

    private static final Logger log = LoggerFactory.getLogger(ExternalIntegrationController.class);

    private final SsaVerificationService ssaService;
    private final DojBackgroundCheckService dojService;
    private final MediCalSuspendedListService mediCalService;

    public ExternalIntegrationController(
            SsaVerificationService ssaService,
            DojBackgroundCheckService dojService,
            MediCalSuspendedListService mediCalService) {
        this.ssaService = ssaService;
        this.dojService = dojService;
        this.mediCalService = mediCalService;
    }

    // ==================== SSA SSN VERIFICATION ====================

    /**
     * Submit SSN verification request to SSA (mock)
     * Returns both the request payload (what would be sent) and the response
     */
    @PostMapping("/ssa/verify-ssn")
    public ResponseEntity<Map<String, Object>> verifySsn(@RequestBody SsnVerificationRequest request) {
        log.info("SSA SSN Verification - Provider: {}", request.providerNumber);

        // Build the request payload
        SsaVerificationService.SsnVerificationRequest ssaRequest = ssaService.buildVerificationRequest(
                request.ssn,
                request.lastName,
                request.firstName,
                request.middleName,
                request.dateOfBirth,
                request.gender,
                request.providerNumber
        );

        // Get the fixed-width file format (exact payload to SSA)
        String fixedWidthPayload = ssaService.formatRequestAsFixedWidth(ssaRequest);

        // Submit and get response
        SsaVerificationService.SsnVerificationResponse response = ssaService.verifySSN(ssaRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("requestPayload", ssaRequest);
        result.put("requestPayloadFixedWidth", fixedWidthPayload);
        result.put("response", response);
        result.put("verificationPassed", "1".equals(response.getVerificationCode()));

        return ResponseEntity.ok(result);
    }

    /**
     * Get SSA verification return codes reference
     */
    @GetMapping("/ssa/return-codes")
    public ResponseEntity<Map<String, String>> getSsaReturnCodes() {
        Map<String, String> codes = new HashMap<>();
        for (SsaVerificationService.VerificationCode code : SsaVerificationService.VerificationCode.values()) {
            codes.put(code.getCode(), code.getDescription());
        }
        return ResponseEntity.ok(codes);
    }

    // ==================== DOJ BACKGROUND CHECK ====================

    /**
     * Submit DOJ background check request (mock)
     * Returns both the request payload (XML format) and the response
     */
    @PostMapping("/doj/background-check")
    public ResponseEntity<Map<String, Object>> submitBackgroundCheck(@RequestBody BackgroundCheckRequest request) {
        log.info("DOJ Background Check - Provider: {}", request.providerNumber);

        // Build the request payload
        DojBackgroundCheckService.BackgroundCheckRequest dojRequest = dojService.buildBackgroundCheckRequest(
                request.providerNumber,
                request.ssn,
                request.lastName,
                request.firstName,
                request.middleName,
                request.dateOfBirth,
                request.gender,
                request.streetAddress,
                request.city,
                request.state,
                request.zipCode
        );

        // Get the XML payload (exact format to DOJ APPS)
        String xmlPayload = dojService.formatRequestAsXml(dojRequest);

        // Submit and get response
        DojBackgroundCheckService.BackgroundCheckResponse response = dojService.submitBackgroundCheck(dojRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("requestPayload", dojRequest);
        result.put("requestPayloadXml", xmlPayload);
        result.put("response", response);
        result.put("providerEligible", response.isProviderEligible());
        result.put("waiverAvailable", response.isWaiverAvailable());

        return ResponseEntity.ok(result);
    }

    /**
     * Get CORI tier classifications reference
     */
    @GetMapping("/doj/cori-tiers")
    public ResponseEntity<Map<String, Object>> getCoriTiers() {
        Map<String, Object> tiers = new HashMap<>();
        for (DojBackgroundCheckService.CoriTier tier : DojBackgroundCheckService.CoriTier.values()) {
            Map<String, String> tierInfo = new HashMap<>();
            tierInfo.put("description", tier.getDescription());
            tierInfo.put("details", tier.getDetails());
            tiers.put(tier.getCode(), tierInfo);
        }
        return ResponseEntity.ok(tiers);
    }

    // ==================== MEDI-CAL SUSPENDED LIST ====================

    /**
     * Check provider against Medi-Cal suspended/ineligible list (mock)
     */
    @PostMapping("/medicallist/check")
    public ResponseEntity<Map<String, Object>> checkMediCalSuspendedList(@RequestBody MediCalCheckRequest request) {
        log.info("Medi-Cal Suspended List Check - Provider: {}", request.providerNumber);

        // Build the lookup request
        MediCalSuspendedListService.MediCalLookupRequest lookupRequest = mediCalService.buildLookupRequest(
                request.providerNumber,
                request.ssn,
                request.lastName,
                request.firstName,
                request.dateOfBirth
        );

        // Check the list
        MediCalSuspendedListService.MediCalLookupResponse response = mediCalService.checkSuspendedList(lookupRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("lookupRequest", lookupRequest);
        result.put("response", response);
        result.put("onSuspendedList", response.isMatchFound());
        result.put("providerEligible", !response.isMatchFound());

        return ResponseEntity.ok(result);
    }

    /**
     * Get sample batch file format (what CMIPS receives from DHCS)
     */
    @GetMapping("/medicallist/sample-batch-file")
    public ResponseEntity<Map<String, Object>> getSampleBatchFile() {
        String batchFile = mediCalService.generateSampleBatchFile();

        Map<String, Object> result = new HashMap<>();
        result.put("format", "Fixed-width text file (100 bytes per record)");
        result.put("source", "DHCS (Department of Health Care Services)");
        result.put("frequency", "Weekly batch");
        result.put("sampleFile", batchFile);

        return ResponseEntity.ok(result);
    }

    /**
     * Get suspension type codes reference
     */
    @GetMapping("/medicallist/suspension-types")
    public ResponseEntity<Map<String, String>> getSuspensionTypes() {
        Map<String, String> types = new HashMap<>();
        for (MediCalSuspendedListService.SuspensionType type : MediCalSuspendedListService.SuspensionType.values()) {
            types.put(type.getCode(), type.getDescription());
        }
        return ResponseEntity.ok(types);
    }

    // ==================== COMBINED VERIFICATION ====================

    /**
     * Run all external verifications for a provider (mock)
     * This simulates the complete enrollment verification workflow
     */
    @PostMapping("/verify-all")
    public ResponseEntity<Map<String, Object>> runAllVerifications(@RequestBody ProviderVerificationRequest request) {
        log.info("Running all verifications for Provider: {}", request.providerNumber);

        Map<String, Object> results = new HashMap<>();
        boolean allPassed = true;

        // 1. SSA SSN Verification
        SsaVerificationService.SsnVerificationRequest ssaRequest = ssaService.buildVerificationRequest(
                request.ssn, request.lastName, request.firstName, request.middleName,
                request.dateOfBirth, request.gender, request.providerNumber
        );
        SsaVerificationService.SsnVerificationResponse ssaResponse = ssaService.verifySSN(ssaRequest);
        boolean ssaPassed = "1".equals(ssaResponse.getVerificationCode());
        results.put("ssaVerification", Map.of(
                "passed", ssaPassed,
                "code", ssaResponse.getVerificationCode(),
                "message", ssaResponse.getVerificationCodeDesc()
        ));
        if (!ssaPassed) allPassed = false;

        // 2. DOJ Background Check
        DojBackgroundCheckService.BackgroundCheckRequest dojRequest = dojService.buildBackgroundCheckRequest(
                request.providerNumber, request.ssn, request.lastName, request.firstName,
                request.middleName, request.dateOfBirth, request.gender,
                request.streetAddress, request.city, request.state, request.zipCode
        );
        DojBackgroundCheckService.BackgroundCheckResponse dojResponse = dojService.submitBackgroundCheck(dojRequest);
        results.put("dojBackgroundCheck", Map.of(
                "passed", dojResponse.isProviderEligible(),
                "coriTier", dojResponse.getCoriTier() != null ? dojResponse.getCoriTier() : "N/A",
                "waiverAvailable", dojResponse.isWaiverAvailable(),
                "recordsFound", dojResponse.getCriminalRecords() != null ? dojResponse.getCriminalRecords().size() : 0
        ));
        if (!dojResponse.isProviderEligible() && !dojResponse.isWaiverAvailable()) allPassed = false;

        // 3. Medi-Cal Suspended List Check
        MediCalSuspendedListService.MediCalLookupRequest mcRequest = mediCalService.buildLookupRequest(
                request.providerNumber, request.ssn, request.lastName, request.firstName, request.dateOfBirth
        );
        MediCalSuspendedListService.MediCalLookupResponse mcResponse = mediCalService.checkSuspendedList(mcRequest);
        results.put("mediCalSuspendedList", Map.of(
                "passed", !mcResponse.isMatchFound(),
                "onList", mcResponse.isMatchFound(),
                "status", mcResponse.getStatus() != null ? mcResponse.getStatus() : "N/A"
        ));
        if (mcResponse.isMatchFound()) allPassed = false;

        // Overall result
        results.put("overallResult", Map.of(
                "allVerificationsPassed", allPassed,
                "providerEligibleForEnrollment", allPassed,
                "requiresWaiver", dojResponse.isWaiverAvailable() && !dojResponse.isProviderEligible()
        ));

        return ResponseEntity.ok(results);
    }

    // ==================== REQUEST DTOs ====================

    @lombok.Data
    public static class SsnVerificationRequest {
        private String providerNumber;
        private String ssn;
        private String lastName;
        private String firstName;
        private String middleName;
        private LocalDate dateOfBirth;
        private String gender;
    }

    @lombok.Data
    public static class BackgroundCheckRequest {
        private String providerNumber;
        private String ssn;
        private String lastName;
        private String firstName;
        private String middleName;
        private LocalDate dateOfBirth;
        private String gender;
        private String streetAddress;
        private String city;
        private String state;
        private String zipCode;
    }

    @lombok.Data
    public static class MediCalCheckRequest {
        private String providerNumber;
        private String ssn;
        private String lastName;
        private String firstName;
        private LocalDate dateOfBirth;
    }

    @lombok.Data
    public static class ProviderVerificationRequest {
        private String providerNumber;
        private String ssn;
        private String lastName;
        private String firstName;
        private String middleName;
        private LocalDate dateOfBirth;
        private String gender;
        private String streetAddress;
        private String city;
        private String state;
        private String zipCode;
    }
}
