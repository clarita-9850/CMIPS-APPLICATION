package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.service.MergeDuplicateSsnService;
import com.cmips.service.MergeDuplicateSsnService.SaveResult;
import com.cmips.service.MergeDuplicateSsnService.VerifyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Merge Duplicate SSN Controller — DSD CI-446456
 *
 * Two endpoints: Verify (validates inputs) and Save (performs the merge).
 * Both require CaseMgmtProvMgmt / CaseMgmtProvMgmtPayroll / CDSSProgramMgmt roles
 * via Keycloak permission evaluation.
 */
@RestController
@RequestMapping("/api/merge-duplicate-ssn")
@CrossOrigin(origins = "*")
public class MergeDuplicateSsnController {

    private static final Logger log = LoggerFactory.getLogger(MergeDuplicateSsnController.class);

    private final MergeDuplicateSsnService mergeService;

    public MergeDuplicateSsnController(MergeDuplicateSsnService mergeService) {
        this.mergeService = mergeService;
    }

    /**
     * Verify merge inputs — validates SSN, master, and duplicate records.
     * Returns verification results with record details or error messages.
     */
    @PostMapping("/verify")
    @RequirePermission(resource = "Recipient Resource", scope = "merge")
    public ResponseEntity<?> verify(
            @RequestBody MergeRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("[MergeDuplicateSSN] Verify requested by {} — SSN ending {}, master={}, duplicates={}",
                userId,
                request.ssn != null && request.ssn.length() >= 4
                        ? "****" + request.ssn.substring(request.ssn.length() - 4) : "N/A",
                request.masterCin,
                request.duplicateCins != null ? request.duplicateCins.size() : 0);

        VerifyResult result = mergeService.verify(
                request.ssn, request.masterCin, request.duplicateCins, request.makeMaster);

        if (!result.success) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "errors", result.errors
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "masterRecord", result.masterRecord,
                "duplicateRecords", result.duplicateRecords,
                "effectiveMasterCin", result.effectiveMasterCin
        ));
    }

    /**
     * Save/execute the merge — marks duplicates as DUPLICATE_SSN, clears their SSN,
     * and creates AlternativeId audit records.
     */
    @PostMapping("/save")
    @RequirePermission(resource = "Recipient Resource", scope = "merge")
    public ResponseEntity<?> save(
            @RequestBody MergeRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("[MergeDuplicateSSN] Save requested by {} — master={}", userId, request.masterCin);

        SaveResult result = mergeService.save(
                request.ssn, request.masterCin, request.duplicateCins, request.makeMaster, userId);

        if (!result.success) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "errors", result.errors
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result.message,
                "mergedCount", result.mergedCount
        ));
    }

    // ── Request DTO ──

    public static class MergeRequest {
        public String ssn;
        public String masterCin;
        public List<String> duplicateCins;
        public boolean makeMaster;

        // Jackson needs these
        public String getSsn() { return ssn; }
        public void setSsn(String ssn) { this.ssn = ssn; }
        public String getMasterCin() { return masterCin; }
        public void setMasterCin(String masterCin) { this.masterCin = masterCin; }
        public List<String> getDuplicateCins() { return duplicateCins; }
        public void setDuplicateCins(List<String> duplicateCins) { this.duplicateCins = duplicateCins; }
        public boolean isMakeMaster() { return makeMaster; }
        public void setMakeMaster(boolean makeMaster) { this.makeMaster = makeMaster; }
    }
}
