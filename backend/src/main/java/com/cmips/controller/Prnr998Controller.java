package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.baw.service.Prnr998ParserService;
import com.cmips.baw.service.Prnr998ParserService.Prnr998ParseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * DSD Section 10 — PRNR998 File Ingest REST Endpoint
 *
 * Accepts PRNR998A/C/D fixed-width batch files from TPF via:
 *   1. File upload (multipart POST)
 *   2. Local file path (for Integration Hub / SFTP-downloaded files)
 *
 * Parses the file, validates batch integrity, and delegates to TpfEspAdapterService
 * for timesheet creation.
 */
@RestController
@RequestMapping("/api/tpf-esp")
public class Prnr998Controller {

    @Autowired
    private Prnr998ParserService parserService;

    /**
     * Upload a PRNR998 batch file for parsing and ingestion.
     * POST /api/tpf-esp/prnr998/upload (multipart/form-data)
     */
    @PostMapping("/prnr998/upload")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<Map<String, Object>> uploadPrnr998File(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try (InputStream is = file.getInputStream()) {
            Prnr998ParseResult result = parserService.parseStream(is);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("batchId", result.getBatchId());
            response.put("batchDate", result.getBatchDate());
            response.put("countyCode", result.getCountyCode());
            response.put("totalLinesRead", result.getTotalLinesRead());
            response.put("detailRecordsParsed", result.getDetailRecordsParsed());
            response.put("detailRecordsProcessed", result.getDetailRecordsProcessed());
            response.put("timesheetsCreated", result.getTimesheetIdsCreated().size());
            response.put("timesheetIds", result.getTimesheetIdsCreated());
            response.put("hasErrors", result.hasErrors());
            response.put("parseErrors", result.getParseErrors());
            response.put("validationErrors", result.getValidationErrors());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Failed to parse PRNR998 file: " + e.getMessage()));
        }
    }

    /**
     * Parse a PRNR998 file from a local path (Integration Hub / SFTP download).
     * POST /api/tpf-esp/prnr998/parse-local
     * Body: { "filePath": "/baw-inbound/PRNR998_20260308_001.DAT" }
     */
    @PostMapping("/prnr998/parse-local")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<Map<String, Object>> parseLocalPrnr998File(
            @RequestBody Map<String, String> request) {

        String filePath = request.get("filePath");
        if (filePath == null || filePath.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "filePath is required"));
        }

        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            return ResponseEntity.badRequest().body(Map.of("error", "File not found: " + filePath));
        }

        Prnr998ParseResult result = parserService.parseFile(path);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("filePath", filePath);
        response.put("batchId", result.getBatchId());
        response.put("batchDate", result.getBatchDate());
        response.put("countyCode", result.getCountyCode());
        response.put("totalLinesRead", result.getTotalLinesRead());
        response.put("detailRecordsParsed", result.getDetailRecordsParsed());
        response.put("detailRecordsProcessed", result.getDetailRecordsProcessed());
        response.put("timesheetsCreated", result.getTimesheetIdsCreated().size());
        response.put("timesheetIds", result.getTimesheetIdsCreated());
        response.put("hasErrors", result.hasErrors());
        response.put("parseErrors", result.getParseErrors());
        response.put("validationErrors", result.getValidationErrors());

        return ResponseEntity.ok(response);
    }
}
