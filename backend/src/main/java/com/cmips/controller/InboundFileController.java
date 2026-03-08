package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.baw.service.InboundFileProcessingService;
import com.cmips.baw.service.InboundFileProcessingService.InboundProcessingResult;
import com.cmips.baw.service.Prnr998ParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * DSD Section 10/24 — Inbound File Processing REST Endpoints
 *
 * Provides endpoints to:
 *   - Trigger inbound file processing for each external system (TPF, STO, EDD, DOJ)
 *   - Upload files manually for processing (for testing or manual recovery)
 *   - Check file availability across all SFTP sources
 *   - View processing history and results
 *
 * All flows use Integration Hub Framework's FileRepository for type-safe parsing.
 */
@RestController
@RequestMapping("/api/inbound")
public class InboundFileController {

    @Autowired private InboundFileProcessingService inboundService;
    @Autowired private Prnr998ParserService prnr998Parser;

    // ==================== Availability ====================

    /**
     * Check file availability across all inbound SFTP sources.
     * GET /api/inbound/availability
     */
    @GetMapping("/availability")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> checkAvailability() {
        Map<String, Object> availability = inboundService.checkAllInboundAvailability();
        return ResponseEntity.ok(availability);
    }

    // ==================== TPF — Paper Timesheet Batch ====================

    /**
     * Trigger TPF batch file fetch from SFTP and process.
     * POST /api/inbound/tpf/fetch
     */
    @PostMapping("/tpf/fetch")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<InboundProcessingResult> fetchTpf() {
        InboundProcessingResult result = inboundService.fetchAndProcessTpf();
        return ResponseEntity.ok(result);
    }

    /**
     * Upload a PRNR998 batch file for processing.
     * POST /api/inbound/tpf/upload (multipart/form-data)
     */
    @PostMapping("/tpf/upload")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<InboundProcessingResult> uploadTpf(@RequestParam("file") MultipartFile file) {
        try {
            Path tempFile = saveUploadedFile(file, "tpf");
            InboundProcessingResult result = inboundService.processTpfBatchFile(tempFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            InboundProcessingResult error = new InboundProcessingResult();
            error.setSourceSystem("TPF");
            error.setStatus("FAILED");
            error.setErrorMessage("Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Parse a PRNR998 file via direct upload and return parse results (no DB writes).
     * POST /api/inbound/tpf/parse (multipart/form-data)
     */
    @PostMapping("/tpf/parse")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<Prnr998ParserService.Prnr998ParseResult> parseTpf(@RequestParam("file") MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            Prnr998ParserService.Prnr998ParseResult result = prnr998Parser.parseStream(is);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== STO — Warrant Paid ====================

    /**
     * Trigger STO warrant file fetch from SFTP and process.
     * POST /api/inbound/sto/fetch
     */
    @PostMapping("/sto/fetch")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<InboundProcessingResult> fetchSto() {
        InboundProcessingResult result = inboundService.fetchAndProcessSto();
        return ResponseEntity.ok(result);
    }

    /**
     * Upload a PRDR110A warrant file for processing.
     * POST /api/inbound/sto/upload (multipart/form-data)
     */
    @PostMapping("/sto/upload")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<InboundProcessingResult> uploadSto(@RequestParam("file") MultipartFile file) {
        try {
            Path tempFile = saveUploadedFile(file, "sto");
            InboundProcessingResult result = inboundService.processStoWarrantFile(tempFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            InboundProcessingResult error = new InboundProcessingResult();
            error.setSourceSystem("STO");
            error.setStatus("FAILED");
            error.setErrorMessage("Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== EDD — Payroll Acknowledgment ====================

    /**
     * Trigger EDD response file fetch from SFTP and process.
     * POST /api/inbound/edd/fetch
     */
    @PostMapping("/edd/fetch")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<InboundProcessingResult> fetchEdd() {
        InboundProcessingResult result = inboundService.fetchAndProcessEdd();
        return ResponseEntity.ok(result);
    }

    /**
     * Upload an EDD response file for processing.
     * POST /api/inbound/edd/upload (multipart/form-data)
     */
    @PostMapping("/edd/upload")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<InboundProcessingResult> uploadEdd(@RequestParam("file") MultipartFile file) {
        try {
            Path tempFile = saveUploadedFile(file, "edd");
            InboundProcessingResult result = inboundService.processEddResponseFile(tempFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            InboundProcessingResult error = new InboundProcessingResult();
            error.setSourceSystem("EDD");
            error.setStatus("FAILED");
            error.setErrorMessage("Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== DOJ — Background Check ====================

    /**
     * Trigger DOJ background check file fetch from SFTP and process.
     * POST /api/inbound/doj/fetch
     */
    @PostMapping("/doj/fetch")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<InboundProcessingResult> fetchDoj() {
        InboundProcessingResult result = inboundService.fetchAndProcessDoj();
        return ResponseEntity.ok(result);
    }

    /**
     * Upload a DOJ background check response file for processing.
     * POST /api/inbound/doj/upload (multipart/form-data)
     */
    @PostMapping("/doj/upload")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<InboundProcessingResult> uploadDoj(@RequestParam("file") MultipartFile file) {
        try {
            Path tempFile = saveUploadedFile(file, "doj");
            InboundProcessingResult result = inboundService.processDojBackgroundCheckFile(tempFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            InboundProcessingResult error = new InboundProcessingResult();
            error.setSourceSystem("DOJ");
            error.setStatus("FAILED");
            error.setErrorMessage("Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== EVV — Electronic Visit Verification ====================

    /**
     * Trigger EVV daily file fetch from SFTP and process.
     * POST /api/inbound/evv/fetch
     */
    @PostMapping("/evv/fetch")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<InboundProcessingResult> fetchEvv() {
        InboundProcessingResult result = inboundService.fetchAndProcessEvv();
        return ResponseEntity.ok(result);
    }

    /**
     * Upload an EVV daily file for processing.
     * POST /api/inbound/evv/upload (multipart/form-data)
     */
    @PostMapping("/evv/upload")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<InboundProcessingResult> uploadEvv(@RequestParam("file") MultipartFile file) {
        try {
            Path tempFile = saveUploadedFile(file, "evv");
            InboundProcessingResult result = inboundService.processEvvDailyFile(tempFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            InboundProcessingResult error = new InboundProcessingResult();
            error.setSourceSystem("EVV");
            error.setStatus("FAILED");
            error.setErrorMessage("Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ==================== Processing History ====================

    /**
     * Get all inbound processing history.
     * GET /api/inbound/history
     */
    @GetMapping("/history")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<InboundProcessingResult>> getHistory(
            @RequestParam(required = false) String source) {
        List<InboundProcessingResult> history = (source != null && !source.isBlank())
                ? inboundService.getProcessingHistory(source)
                : inboundService.getProcessingHistory();
        return ResponseEntity.ok(history);
    }

    /**
     * Get a specific processing result by ID.
     * GET /api/inbound/history/{processId}
     */
    @GetMapping("/history/{processId}")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<InboundProcessingResult> getProcessingResult(@PathVariable String processId) {
        InboundProcessingResult result = inboundService.getProcessingResult(processId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * List all inbound source types and their endpoints.
     * GET /api/inbound/types
     */
    @GetMapping("/types")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<Map<String, String>>> listInboundTypes() {
        List<Map<String, String>> types = List.of(
            Map.of("source", "TPF", "fileType", "PRNR998",
                    "description", "Paper Timesheet Batch from Timesheet Processing Facility",
                    "fetchEndpoint", "/api/inbound/tpf/fetch",
                    "uploadEndpoint", "/api/inbound/tpf/upload",
                    "parseEndpoint", "/api/inbound/tpf/parse"),
            Map.of("source", "STO", "fileType", "PRDR110A",
                    "description", "Warrant Paid/Voided/Stale from State Treasurer Office",
                    "fetchEndpoint", "/api/inbound/sto/fetch",
                    "uploadEndpoint", "/api/inbound/sto/upload"),
            Map.of("source", "EDD", "fileType", "EDD_RESPONSE",
                    "description", "Payroll Acknowledgment Response from Employment Development Dept",
                    "fetchEndpoint", "/api/inbound/edd/fetch",
                    "uploadEndpoint", "/api/inbound/edd/upload"),
            Map.of("source", "DOJ", "fileType", "DOJ_BGC",
                    "description", "Background Check Results from Department of Justice",
                    "fetchEndpoint", "/api/inbound/doj/fetch",
                    "uploadEndpoint", "/api/inbound/doj/upload"),
            Map.of("source", "EVV", "fileType", "EVV_DAILY",
                    "description", "EVV Daily Visit Verification from EVV Vendor (DSD Section 24)",
                    "fetchEndpoint", "/api/inbound/evv/fetch",
                    "uploadEndpoint", "/api/inbound/evv/upload")
        );
        return ResponseEntity.ok(types);
    }

    // ==================== Helper ====================

    private Path saveUploadedFile(MultipartFile file, String subdir) throws Exception {
        Path uploadDir = Paths.get(System.getProperty("java.io.tmpdir"), "baw-inbound", subdir);
        Files.createDirectories(uploadDir);
        String fileName = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "upload_" + System.currentTimeMillis() + ".DAT";
        Path dest = uploadDir.resolve(fileName);
        try (var is = file.getInputStream()) {
            Files.copy(is, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return dest;
    }
}
