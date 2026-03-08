package com.cmips.baw.service;

import com.cmips.baw.BawFileMetadata;
import com.cmips.baw.BawFileService;
import com.cmips.baw.dto.WarrantPaidRecord;
import com.cmips.baw.filetype.*;
import com.cmips.entity.EVVRecord;
import com.cmips.entity.WarrantEntity;
import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.repository.FileRepository;
import com.cmips.repository.EVVRepository;
import com.cmips.repository.WarrantRepository;
import com.cmips.service.TpfEspAdapterService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DSD Section 10/24 — Inbound File Processing Service
 *
 * Orchestrates all 4 inbound file flows using the Integration Hub Framework's
 * FileRepository for type-safe fixed-width file parsing:
 *
 *   1. TPF (PRNR998)     — Paper timesheet batch files from Timesheet Processing Facility
 *   2. STO (PRDR110A)    — Warrant paid/voided/stale status from State Treasurer Office
 *   3. EDD (EDD_RESP)    — Payroll acknowledgment responses from Employment Development Dept
 *   4. DOJ (DOJ_BGC)     — Background check results from Department of Justice
 *
 * Each flow: check availability → fetch via SFTP → parse via FileRepository → process → acknowledge
 */
@Service
@Slf4j
public class InboundFileProcessingService {

    @Autowired private BawFileService bawFileService;
    @Autowired private Prnr998ParserService prnr998Parser;
    @Autowired private TpfEspAdapterService tpfAdapter;
    @Autowired private WarrantRepository warrantRepository;
    @Autowired private EVVRepository evvRepository;

    // FileRepositories for inbound file types
    private final FileRepository<WarrantPaidFileRecord> warrantRepo;
    private final FileRepository<EddResponseRecord> eddResponseRepo;
    private final FileRepository<DojBackgroundCheckRecord> dojBgcRepo;

    // Processing history
    private final Map<String, InboundProcessingResult> processingHistory = new ConcurrentHashMap<>();

    public InboundFileProcessingService() {
        this.warrantRepo = FileRepository.forType(WarrantPaidFileRecord.class);
        this.eddResponseRepo = FileRepository.forType(EddResponseRecord.class);
        this.dojBgcRepo = FileRepository.forType(DojBackgroundCheckRecord.class);
    }

    // ═══════════════════════════════════════════
    // FLOW 1: TPF — Paper Timesheet Batch (PRNR998)
    // ═══════════════════════════════════════════

    /**
     * Process a TPF batch file from a local path.
     * Delegates to Prnr998ParserService which uses FileRepository internally.
     */
    public InboundProcessingResult processTpfBatchFile(Path filePath) {
        log.info("[INBOUND-TPF] Processing batch file: {}", filePath);
        String processId = generateProcessId("TPF");
        InboundProcessingResult result = new InboundProcessingResult();
        result.setProcessId(processId);
        result.setSourceSystem("TPF");
        result.setFileType("PRNR998");
        result.setFileName(filePath.getFileName().toString());
        result.setStartTime(LocalDateTime.now());

        try {
            Prnr998ParserService.Prnr998ParseResult parseResult = prnr998Parser.parseFile(filePath);

            result.setRecordsParsed(parseResult.getDetailRecordsParsed());
            result.setRecordsProcessed(parseResult.getDetailRecordsProcessed());
            result.setRecordsCreated(parseResult.getTimesheetIdsCreated().size());
            result.setCreatedIds(parseResult.getTimesheetIdsCreated());
            result.setParseErrors(parseResult.getParseErrors());
            result.setValidationErrors(parseResult.getValidationErrors());
            result.setStatus(parseResult.hasErrors() ? "COMPLETED_WITH_ERRORS" : "SUCCESS");
            result.setBatchId(parseResult.getBatchId());

            log.info("[INBOUND-TPF] Complete: batch={}, parsed={}, created={}, errors={}",
                    parseResult.getBatchId(), parseResult.getDetailRecordsParsed(),
                    parseResult.getTimesheetIdsCreated().size(),
                    parseResult.getParseErrors().size() + parseResult.getValidationErrors().size());
        } catch (Exception e) {
            log.error("[INBOUND-TPF] Failed: {}", e.getMessage(), e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        processingHistory.put(processId, result);
        return result;
    }

    /**
     * Trigger TPF fetch from SFTP and process.
     */
    public InboundProcessingResult fetchAndProcessTpf() {
        log.info("[INBOUND-TPF] Fetching from SFTP...");
        String processId = generateProcessId("TPF");
        InboundProcessingResult result = new InboundProcessingResult();
        result.setProcessId(processId);
        result.setSourceSystem("TPF");
        result.setFileType("PRNR998");
        result.setStartTime(LocalDateTime.now());

        try {
            // Check availability
            boolean available = bawFileService.isFileAvailable("TPF", "TIMESHEET_BATCH");
            if (!available) {
                result.setStatus("NO_FILE");
                result.setErrorMessage("No TPF batch file available on SFTP");
                result.setEndTime(LocalDateTime.now());
                processingHistory.put(processId, result);
                return result;
            }

            // Get metadata
            BawFileMetadata metadata = bawFileService.getFileMetadata("TPF", "TIMESHEET_BATCH");
            result.setFileName(metadata.originalFileName());

            // The BawFileService downloads and we parse via Prnr998ParserService
            // For TPF, we use the local file path approach since the parser handles mixed record types
            Path localDir = Paths.get("./baw-inbound");
            Files.createDirectories(localDir);

            // Generate a test file path (in production, BawFileService downloads to this location)
            Path localFile = localDir.resolve(metadata.originalFileName() != null
                    ? metadata.originalFileName() : "PRNR998_" + LocalDate.now() + ".DAT");

            if (Files.exists(localFile)) {
                return processTpfBatchFile(localFile);
            } else {
                result.setStatus("NO_LOCAL_FILE");
                result.setErrorMessage("SFTP file not yet downloaded to local path");
            }
        } catch (Exception e) {
            log.error("[INBOUND-TPF] SFTP fetch failed: {}", e.getMessage(), e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        processingHistory.put(processId, result);
        return result;
    }

    // ═══════════════════════════════════════════
    // FLOW 2: STO — Warrant Paid (PRDR110A)
    // ═══════════════════════════════════════════

    /**
     * Process a warrant paid file from a local path using FileRepository.
     */
    public InboundProcessingResult processStoWarrantFile(Path filePath) {
        log.info("[INBOUND-STO] Processing warrant file: {}", filePath);
        String processId = generateProcessId("STO");
        InboundProcessingResult result = new InboundProcessingResult();
        result.setProcessId(processId);
        result.setSourceSystem("STO");
        result.setFileType("PRDR110A");
        result.setFileName(filePath.getFileName().toString());
        result.setStartTime(LocalDateTime.now());

        try {
            // Parse using manual substring extraction (framework read() has date format limitation)
            List<WarrantPaidFileRecord> records = parseStoWarrantLines(Files.readAllLines(filePath));

            log.info("[INBOUND-STO] Parsed {} warrant records", records.size());
            result.setRecordsParsed(records.size());

            int updated = 0;
            int inserted = 0;
            int skipped = 0;
            List<String> errors = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (WarrantPaidFileRecord record : records) {
                try {
                    var existing = warrantRepository.findByWarrantNumber(record.getWarrantNumber());
                    if (existing.isPresent()) {
                        WarrantEntity warrant = existing.get();
                        WarrantEntity.WarrantStatus newStatus = mapWarrantStatus(record.getStatusCode());
                        if (warrant.getStatus() != newStatus) {
                            warrant.setStatus(newStatus);
                            warrant.setPaidDate(record.getPaidDate());
                            warrant.setStatusUpdatedAt(now);
                            warrantRepository.save(warrant);
                            updated++;
                        } else {
                            skipped++;
                        }
                    } else {
                        WarrantEntity newWarrant = new WarrantEntity();
                        newWarrant.setWarrantNumber(record.getWarrantNumber());
                        newWarrant.setIssueDate(record.getIssueDate());
                        newWarrant.setPaidDate(record.getPaidDate());
                        newWarrant.setAmount(record.getAmount());
                        newWarrant.setCountyCode(record.getCountyCode());
                        newWarrant.setProviderId(record.getProviderId());
                        newWarrant.setCaseNumber(record.getCaseNumber());
                        newWarrant.setStatus(mapWarrantStatus(record.getStatusCode()));
                        newWarrant.setStatusUpdatedAt(now);
                        warrantRepository.save(newWarrant);
                        inserted++;
                    }
                } catch (Exception e) {
                    errors.add("Warrant " + record.getWarrantNumber() + ": " + e.getMessage());
                    skipped++;
                }
            }

            result.setRecordsProcessed(updated + inserted);
            result.setRecordsCreated(inserted);
            result.setRecordsUpdated(updated);
            result.setRecordsSkipped(skipped);
            result.setParseErrors(errors);
            result.setStatus(errors.isEmpty() ? "SUCCESS" : "COMPLETED_WITH_ERRORS");

            log.info("[INBOUND-STO] Complete: inserted={}, updated={}, skipped={}", inserted, updated, skipped);
        } catch (Exception e) {
            log.error("[INBOUND-STO] Failed: {}", e.getMessage(), e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        processingHistory.put(processId, result);
        return result;
    }

    /**
     * Trigger STO warrant file fetch from SFTP and process.
     */
    public InboundProcessingResult fetchAndProcessSto() {
        log.info("[INBOUND-STO] Fetching from SFTP...");
        String processId = generateProcessId("STO");
        InboundProcessingResult result = new InboundProcessingResult();
        result.setProcessId(processId);
        result.setSourceSystem("STO");
        result.setFileType("PRDR110A");
        result.setStartTime(LocalDateTime.now());

        try {
            boolean available = bawFileService.isFileAvailable("STO", "WARRANT_PAID");
            if (!available) {
                result.setStatus("NO_FILE");
                result.setErrorMessage("No STO warrant file available on SFTP");
                result.setEndTime(LocalDateTime.now());
                processingHistory.put(processId, result);
                return result;
            }

            // Fetch via BawFileService (downloads + parses)
            List<WarrantPaidRecord> dtos = bawFileService.fetchInboundFile(
                    "STO", "WARRANT_PAID", WarrantPaidRecord.class);

            result.setRecordsParsed(dtos.size());
            int updated = 0, inserted = 0, skipped = 0;
            LocalDateTime now = LocalDateTime.now();

            for (WarrantPaidRecord dto : dtos) {
                try {
                    var existing = warrantRepository.findByWarrantNumber(dto.warrantNumber());
                    if (existing.isPresent()) {
                        WarrantEntity warrant = existing.get();
                        WarrantEntity.WarrantStatus newStatus = mapDtoWarrantStatus(dto.status());
                        if (warrant.getStatus() != newStatus) {
                            warrant.setStatus(newStatus);
                            warrant.setPaidDate(dto.paidDate());
                            warrant.setStatusUpdatedAt(now);
                            warrantRepository.save(warrant);
                            updated++;
                        } else {
                            skipped++;
                        }
                    } else {
                        WarrantEntity newWarrant = new WarrantEntity();
                        newWarrant.setWarrantNumber(dto.warrantNumber());
                        newWarrant.setIssueDate(dto.issueDate());
                        newWarrant.setPaidDate(dto.paidDate());
                        newWarrant.setAmount(dto.amount());
                        newWarrant.setCountyCode(dto.countyCode());
                        newWarrant.setProviderId(dto.providerId());
                        newWarrant.setCaseNumber(dto.caseNumber());
                        newWarrant.setStatus(mapDtoWarrantStatus(dto.status()));
                        newWarrant.setStatusUpdatedAt(now);
                        warrantRepository.save(newWarrant);
                        inserted++;
                    }
                } catch (Exception e) {
                    skipped++;
                }
            }

            result.setRecordsProcessed(updated + inserted);
            result.setRecordsCreated(inserted);
            result.setRecordsUpdated(updated);
            result.setRecordsSkipped(skipped);
            result.setStatus("SUCCESS");

            // Acknowledge
            BawFileMetadata meta = bawFileService.getFileMetadata("STO", "WARRANT_PAID");
            if (meta.fileReference() != null) {
                bawFileService.acknowledgeFileProcessed("STO", "WARRANT_PAID", meta.fileReference());
            }
        } catch (Exception e) {
            log.error("[INBOUND-STO] SFTP fetch failed: {}", e.getMessage(), e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        processingHistory.put(processId, result);
        return result;
    }

    // ═══════════════════════════════════════════
    // FLOW 3: EDD — Payroll Acknowledgment Response
    // ═══════════════════════════════════════════

    /**
     * Process an EDD response file from a local path using FileRepository.
     */
    public InboundProcessingResult processEddResponseFile(Path filePath) {
        log.info("[INBOUND-EDD] Processing response file: {}", filePath);
        String processId = generateProcessId("EDD");
        InboundProcessingResult result = new InboundProcessingResult();
        result.setProcessId(processId);
        result.setSourceSystem("EDD");
        result.setFileType("EDD_RESPONSE");
        result.setFileName(filePath.getFileName().toString());
        result.setStartTime(LocalDateTime.now());

        try {
            List<EddResponseRecord> records = parseEddResponseLines(Files.readAllLines(filePath));

            log.info("[INBOUND-EDD] Parsed {} response records", records.size());
            result.setRecordsParsed(records.size());

            int accepted = 0, rejected = 0, partial = 0;
            List<String> errors = new ArrayList<>();

            for (EddResponseRecord record : records) {
                switch (record.getStatusCode()) {
                    case "A" -> accepted++;
                    case "R" -> {
                        rejected++;
                        errors.add("Provider " + record.getProviderId()
                                + " rejected: " + record.getErrorCode()
                                + " - " + (record.getErrorMessage() != null ? record.getErrorMessage().trim() : ""));
                    }
                    case "P" -> partial++;
                }
            }

            result.setRecordsProcessed(records.size());
            result.setParseErrors(errors);
            result.setStatus(rejected > 0 ? "COMPLETED_WITH_ERRORS" : "SUCCESS");

            // Store summary details
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("accepted", accepted);
            summary.put("rejected", rejected);
            summary.put("partial", partial);
            if (!records.isEmpty()) {
                summary.put("quarter", "Q" + records.get(0).getQuarter() + "/" + records.get(0).getYear());
            }
            result.setSummary(summary);

            log.info("[INBOUND-EDD] Complete: accepted={}, rejected={}, partial={}", accepted, rejected, partial);
        } catch (Exception e) {
            log.error("[INBOUND-EDD] Failed: {}", e.getMessage(), e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        processingHistory.put(processId, result);
        return result;
    }

    // ═══════════════════════════════════════════
    // FLOW 4: DOJ — Background Check Results
    // ═══════════════════════════════════════════

    /**
     * Process a DOJ background check response file from a local path.
     */
    public InboundProcessingResult processDojBackgroundCheckFile(Path filePath) {
        log.info("[INBOUND-DOJ] Processing background check file: {}", filePath);
        String processId = generateProcessId("DOJ");
        InboundProcessingResult result = new InboundProcessingResult();
        result.setProcessId(processId);
        result.setSourceSystem("DOJ");
        result.setFileType("DOJ_BGC");
        result.setFileName(filePath.getFileName().toString());
        result.setStartTime(LocalDateTime.now());

        try {
            List<DojBackgroundCheckRecord> records = parseDojBgcLines(Files.readAllLines(filePath));

            log.info("[INBOUND-DOJ] Parsed {} background check records", records.size());
            result.setRecordsParsed(records.size());

            int cleared = 0, flagged = 0, denied = 0, pending = 0;
            List<String> alerts = new ArrayList<>();

            for (DojBackgroundCheckRecord record : records) {
                switch (record.getResultCode()) {
                    case "C" -> cleared++;
                    case "F" -> {
                        flagged++;
                        alerts.add("Provider " + record.getProviderId()
                                + " FLAGGED: " + record.getFlagCategory()
                                + " - " + (record.getResultDescription() != null ? record.getResultDescription().trim() : ""));
                    }
                    case "D" -> {
                        denied++;
                        alerts.add("Provider " + record.getProviderId()
                                + " DENIED: " + (record.getResultDescription() != null ? record.getResultDescription().trim() : ""));
                    }
                    case "P" -> pending++;
                }
            }

            result.setRecordsProcessed(records.size());
            result.setParseErrors(alerts);
            result.setStatus((flagged > 0 || denied > 0) ? "COMPLETED_WITH_ALERTS" : "SUCCESS");

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("cleared", cleared);
            summary.put("flagged", flagged);
            summary.put("denied", denied);
            summary.put("pending", pending);
            result.setSummary(summary);

            log.info("[INBOUND-DOJ] Complete: cleared={}, flagged={}, denied={}, pending={}",
                    cleared, flagged, denied, pending);
        } catch (Exception e) {
            log.error("[INBOUND-DOJ] Failed: {}", e.getMessage(), e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        processingHistory.put(processId, result);
        return result;
    }

    // ═══════════════════════════════════════════
    // FLOW 3b: EDD — Fetch from SFTP
    // ═══════════════════════════════════════════

    /**
     * Trigger EDD response file fetch from SFTP and process.
     */
    public InboundProcessingResult fetchAndProcessEdd() {
        log.info("[INBOUND-EDD] Fetching from SFTP...");
        String processId = generateProcessId("EDD");
        InboundProcessingResult result = new InboundProcessingResult();
        result.setProcessId(processId);
        result.setSourceSystem("EDD");
        result.setFileType("EDD_RESPONSE");
        result.setStartTime(LocalDateTime.now());

        try {
            boolean available = bawFileService.isFileAvailable("EDD", "EDD_RESPONSE");
            if (!available) {
                result.setStatus("NO_FILE");
                result.setErrorMessage("No EDD response file available on SFTP");
                result.setEndTime(LocalDateTime.now());
                processingHistory.put(processId, result);
                return result;
            }

            BawFileMetadata metadata = bawFileService.getFileMetadata("EDD", "EDD_RESPONSE");
            result.setFileName(metadata.originalFileName());

            Path localDir = Paths.get("./baw-inbound/edd");
            Files.createDirectories(localDir);
            Path localFile = localDir.resolve(metadata.originalFileName() != null
                    ? metadata.originalFileName() : "EDD_RESP_" + LocalDate.now() + ".DAT");

            if (Files.exists(localFile)) {
                return processEddResponseFile(localFile);
            } else {
                result.setStatus("NO_LOCAL_FILE");
                result.setErrorMessage("SFTP file not yet downloaded to local path");
            }
        } catch (Exception e) {
            log.error("[INBOUND-EDD] SFTP fetch failed: {}", e.getMessage(), e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        processingHistory.put(processId, result);
        return result;
    }

    // ═══════════════════════════════════════════
    // FLOW 4b: DOJ — Fetch from SFTP
    // ═══════════════════════════════════════════

    /**
     * Trigger DOJ background check file fetch from SFTP and process.
     */
    public InboundProcessingResult fetchAndProcessDoj() {
        log.info("[INBOUND-DOJ] Fetching from SFTP...");
        String processId = generateProcessId("DOJ");
        InboundProcessingResult result = new InboundProcessingResult();
        result.setProcessId(processId);
        result.setSourceSystem("DOJ");
        result.setFileType("DOJ_BGC");
        result.setStartTime(LocalDateTime.now());

        try {
            boolean available = bawFileService.isFileAvailable("DOJ", "DOJ_BGC");
            if (!available) {
                result.setStatus("NO_FILE");
                result.setErrorMessage("No DOJ background check file available on SFTP");
                result.setEndTime(LocalDateTime.now());
                processingHistory.put(processId, result);
                return result;
            }

            BawFileMetadata metadata = bawFileService.getFileMetadata("DOJ", "DOJ_BGC");
            result.setFileName(metadata.originalFileName());

            Path localDir = Paths.get("./baw-inbound/doj");
            Files.createDirectories(localDir);
            Path localFile = localDir.resolve(metadata.originalFileName() != null
                    ? metadata.originalFileName() : "DOJ_BGC_" + LocalDate.now() + ".DAT");

            if (Files.exists(localFile)) {
                return processDojBackgroundCheckFile(localFile);
            } else {
                result.setStatus("NO_LOCAL_FILE");
                result.setErrorMessage("SFTP file not yet downloaded to local path");
            }
        } catch (Exception e) {
            log.error("[INBOUND-DOJ] SFTP fetch failed: {}", e.getMessage(), e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        processingHistory.put(processId, result);
        return result;
    }

    // ═══════════════════════════════════════════
    // FLOW 5: EVV — Daily Visit Verification (DSD Section 24 §1.1.4.10)
    // ═══════════════════════════════════════════

    /**
     * Process an EVV daily file from a local path.
     * Parses 150-byte EvvDailyRecord lines and imports into EVVRecord entities.
     */
    public InboundProcessingResult processEvvDailyFile(Path filePath) {
        log.info("[INBOUND-EVV] Processing daily file: {}", filePath);
        String processId = generateProcessId("EVV");
        InboundProcessingResult result = new InboundProcessingResult();
        result.setProcessId(processId);
        result.setSourceSystem("EVV");
        result.setFileType("EVV_DAILY");
        result.setFileName(filePath.getFileName().toString());
        result.setStartTime(LocalDateTime.now());

        try {
            List<EvvDailyRecord> records = parseEvvDailyLines(Files.readAllLines(filePath));

            log.info("[INBOUND-EVV] Parsed {} EVV records", records.size());
            result.setRecordsParsed(records.size());

            int imported = 0, matched = 0, unmatched = 0, skipped = 0;
            List<String> errors = new ArrayList<>();

            for (EvvDailyRecord evvRec : records) {
                try {
                    EVVRecord entity = new EVVRecord();
                    entity.setProviderId(evvRec.getProviderId());
                    entity.setRecipientId(evvRec.getRecipientId());
                    entity.setServiceType(evvRec.getServiceTypeName());

                    // Build check-in/out timestamps from serviceDate + time strings
                    LocalDate svcDate = evvRec.getServiceDate();
                    if (svcDate == null) svcDate = LocalDate.now();
                    if (evvRec.getCheckInTime() != null && !evvRec.getCheckInTime().isBlank()) {
                        entity.setCheckInTime(LocalDateTime.of(svcDate, parseTime(evvRec.getCheckInTime())));
                    } else {
                        entity.setCheckInTime(LocalDateTime.of(svcDate, LocalTime.MIDNIGHT));
                    }
                    if (evvRec.getCheckOutTime() != null && !evvRec.getCheckOutTime().isBlank()) {
                        entity.setCheckOutTime(LocalDateTime.of(svcDate, parseTime(evvRec.getCheckOutTime())));
                    }

                    // GPS coordinates (DB columns are NOT NULL, default to 0.0)
                    Double inLat = parseDoubleSafe(evvRec.getCheckInLatitude());
                    Double inLon = parseDoubleSafe(evvRec.getCheckInLongitude());
                    Double outLat = parseDoubleSafe(evvRec.getCheckOutLatitude());
                    Double outLon = parseDoubleSafe(evvRec.getCheckOutLongitude());
                    entity.setCheckInLatitude(inLat != null ? inLat : 0.0);
                    entity.setCheckInLongitude(inLon != null ? inLon : 0.0);
                    entity.setCheckOutLatitude(outLat);
                    entity.setCheckOutLongitude(outLon);

                    // Hours
                    entity.setHoursWorked(parseDoubleSafe(evvRec.getHoursWorked()));

                    // Status based on match
                    if (evvRec.isMatched()) {
                        entity.setStatus("VERIFIED");
                        matched++;
                    } else {
                        entity.setStatus("COMPLETED");
                        unmatched++;
                    }

                    // Link to timesheet if matched
                    if (evvRec.getTimesheetNumber() != null && !evvRec.getTimesheetNumber().isBlank()) {
                        try {
                            entity.setTimesheetId(Long.parseLong(evvRec.getTimesheetNumber().trim()));
                        } catch (NumberFormatException ignored) {}
                    }

                    evvRepository.save(entity);
                    imported++;

                } catch (Exception e) {
                    errors.add("Provider " + evvRec.getProviderId() + " / Recipient " + evvRec.getRecipientId()
                            + ": " + e.getMessage());
                    skipped++;
                }
            }

            result.setRecordsProcessed(imported);
            result.setRecordsCreated(imported);
            result.setRecordsSkipped(skipped);
            result.setParseErrors(errors);
            result.setStatus(errors.isEmpty() ? "SUCCESS" : "COMPLETED_WITH_ERRORS");

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("imported", imported);
            summary.put("matched", matched);
            summary.put("unmatched", unmatched);
            summary.put("skipped", skipped);
            result.setSummary(summary);

            log.info("[INBOUND-EVV] Complete: imported={}, matched={}, unmatched={}, skipped={}",
                    imported, matched, unmatched, skipped);
        } catch (Exception e) {
            log.error("[INBOUND-EVV] Failed: {}", e.getMessage(), e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        processingHistory.put(processId, result);
        return result;
    }

    /**
     * Trigger EVV daily file fetch from SFTP and process.
     */
    public InboundProcessingResult fetchAndProcessEvv() {
        log.info("[INBOUND-EVV] Fetching from SFTP...");
        String processId = generateProcessId("EVV");
        InboundProcessingResult result = new InboundProcessingResult();
        result.setProcessId(processId);
        result.setSourceSystem("EVV");
        result.setFileType("EVV_DAILY");
        result.setStartTime(LocalDateTime.now());

        try {
            boolean available = bawFileService.isFileAvailable("EVV", "EVV_DAILY");
            if (!available) {
                result.setStatus("NO_FILE");
                result.setErrorMessage("No EVV daily file available on SFTP");
                result.setEndTime(LocalDateTime.now());
                processingHistory.put(processId, result);
                return result;
            }

            BawFileMetadata metadata = bawFileService.getFileMetadata("EVV", "EVV_DAILY");
            result.setFileName(metadata.originalFileName());

            Path localDir = Paths.get("./baw-inbound/evv");
            Files.createDirectories(localDir);
            Path localFile = localDir.resolve(metadata.originalFileName() != null
                    ? metadata.originalFileName() : "EVV_DAILY_" + LocalDate.now().format(DATE_FMT) + ".DAT");

            if (Files.exists(localFile)) {
                return processEvvDailyFile(localFile);
            } else {
                result.setStatus("NO_LOCAL_FILE");
                result.setErrorMessage("SFTP file not yet downloaded to local path");
            }
        } catch (Exception e) {
            log.error("[INBOUND-EVV] SFTP fetch failed: {}", e.getMessage(), e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        result.setEndTime(LocalDateTime.now());
        processingHistory.put(processId, result);
        return result;
    }

    // ═══════════════════════════════════════════
    // STATUS & HISTORY
    // ═══════════════════════════════════════════

    /**
     * Get processing result by ID.
     */
    public InboundProcessingResult getProcessingResult(String processId) {
        return processingHistory.get(processId);
    }

    /**
     * Get all processing history, newest first.
     */
    public List<InboundProcessingResult> getProcessingHistory() {
        return processingHistory.values().stream()
                .sorted(Comparator.comparing(InboundProcessingResult::getStartTime).reversed())
                .toList();
    }

    /**
     * Get processing history for a specific source system.
     */
    public List<InboundProcessingResult> getProcessingHistory(String sourceSystem) {
        return processingHistory.values().stream()
                .filter(r -> sourceSystem.equalsIgnoreCase(r.getSourceSystem()))
                .sorted(Comparator.comparing(InboundProcessingResult::getStartTime).reversed())
                .toList();
    }

    /**
     * Check file availability for all inbound sources.
     */
    public Map<String, Object> checkAllInboundAvailability() {
        Map<String, Object> availability = new LinkedHashMap<>();
        for (String[] source : new String[][]{
                {"TPF", "TIMESHEET_BATCH"}, {"STO", "WARRANT_PAID"},
                {"EDD", "EDD_RESPONSE"}, {"DOJ", "DOJ_BGC"}, {"EVV", "EVV_DAILY"}}) {
            try {
                boolean avail = bawFileService.isFileAvailable(source[0], source[1]);
                availability.put(source[0], Map.of("available", avail, "fileType", source[1]));
            } catch (Exception e) {
                availability.put(source[0], Map.of("available", false, "error", e.getMessage()));
            }
        }
        return availability;
    }

    // ═══════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════

    private String generateProcessId(String system) {
        return system + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private WarrantEntity.WarrantStatus mapWarrantStatus(String code) {
        return switch (code) {
            case "P" -> WarrantEntity.WarrantStatus.PAID;
            case "V" -> WarrantEntity.WarrantStatus.VOIDED;
            case "S" -> WarrantEntity.WarrantStatus.STALE;
            default -> throw new IllegalArgumentException("Unknown warrant status: " + code);
        };
    }

    private WarrantEntity.WarrantStatus mapDtoWarrantStatus(WarrantPaidRecord.WarrantStatus status) {
        return switch (status) {
            case PAID -> WarrantEntity.WarrantStatus.PAID;
            case VOIDED -> WarrantEntity.WarrantStatus.VOIDED;
            case STALE -> WarrantEntity.WarrantStatus.STALE;
        };
    }

    // ═══════════════════════════════════════════
    // FIXED-WIDTH LINE PARSERS (manual substring)
    // Same approach as Prnr998ParserService — framework's FileRepository.read()
    // doesn't apply @FileColumn format for date parsing on read path.
    // ═══════════════════════════════════════════

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private LocalDate parseDateSafe(String s) {
        try {
            String t = s.trim();
            if (t.isEmpty() || "00000000".equals(t)) return null;
            return LocalDate.parse(t, DATE_FMT);
        } catch (Exception e) { return null; }
    }

    private String padLine(String line, int len) {
        return line.length() >= len ? line : line + " ".repeat(len - line.length());
    }

    /**
     * Parse STO PRDR110A warrant paid records (60 bytes each).
     * Layout: warrantNumber(10) + issueDate(8) + paidDate(8) + amount(12) + countyCode(2) + providerId(9) + caseNumber(10) + status(1)
     */
    private List<WarrantPaidFileRecord> parseStoWarrantLines(List<String> lines) {
        List<WarrantPaidFileRecord> records = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String padded = padLine(line, 60);
            records.add(WarrantPaidFileRecord.builder()
                    .warrantNumber(padded.substring(0, 10).trim())
                    .issueDate(parseDateSafe(padded.substring(10, 18)))
                    .paidDate(parseDateSafe(padded.substring(18, 26)))
                    .amount(new BigDecimal(padded.substring(26, 38).trim()).movePointLeft(2))
                    .countyCode(padded.substring(38, 40).trim())
                    .providerId(padded.substring(40, 49).trim())
                    .caseNumber(padded.substring(49, 59).trim())
                    .statusCode(padded.substring(59, 60).trim())
                    .build());
        }
        return records;
    }

    /**
     * Parse EDD response records (80 bytes each).
     * Layout: recordType(3) + providerId(10) + quarter(1) + year(4) + responseDate(8) + statusCode(1) + errorCode(2) + errorMessage(40) + filler(11)
     */
    private List<EddResponseRecord> parseEddResponseLines(List<String> lines) {
        List<EddResponseRecord> records = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String padded = padLine(line, 80);
            records.add(EddResponseRecord.builder()
                    .recordType(padded.substring(0, 3).trim())
                    .providerId(padded.substring(3, 13).trim())
                    .quarter(padded.substring(13, 14).trim())
                    .year(padded.substring(14, 18).trim())
                    .responseDate(parseDateSafe(padded.substring(18, 26)))
                    .statusCode(padded.substring(26, 27).trim())
                    .errorCode(padded.substring(27, 29).trim())
                    .errorMessage(padded.substring(29, 69).trim())
                    .filler(padded.substring(69, 80).trim())
                    .build());
        }
        return records;
    }

    /**
     * Parse EVV daily visit records (150 bytes each).
     * Layout per EvvDailyRecord @FileColumn annotations.
     */
    private List<EvvDailyRecord> parseEvvDailyLines(List<String> lines) {
        List<EvvDailyRecord> records = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String padded = padLine(line, 150);
            records.add(EvvDailyRecord.builder()
                    .recordType(padded.substring(0, 3).trim())
                    .providerId(padded.substring(3, 13).trim())
                    .recipientId(padded.substring(13, 23).trim())
                    .caseId(padded.substring(23, 33).trim())
                    .serviceDate(parseDateSafe(padded.substring(33, 41)))
                    .checkInTime(padded.substring(41, 45).trim())
                    .checkOutTime(padded.substring(45, 49).trim())
                    .hoursWorked(padded.substring(49, 57).trim())
                    .checkInLatitude(padded.substring(57, 67).trim())
                    .checkInLongitude(padded.substring(67, 78).trim())
                    .checkOutLatitude(padded.substring(78, 88).trim())
                    .checkOutLongitude(padded.substring(88, 99).trim())
                    .serviceTypeCode(padded.substring(99, 101).trim())
                    .verificationMethod(padded.substring(101, 102).trim())
                    .matchStatus(padded.substring(102, 103).trim())
                    .timesheetNumber(padded.substring(103, 113).trim())
                    .filler(padded.substring(113, 150).trim())
                    .build());
        }
        return records;
    }

    private LocalTime parseTime(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) return LocalTime.MIDNIGHT;
        String t = hhmm.trim();
        int h = Integer.parseInt(t.substring(0, 2));
        int m = Integer.parseInt(t.substring(2, 4));
        return LocalTime.of(h, m);
    }

    private Double parseDoubleSafe(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    /**
     * Parse DOJ background check response records (100 bytes each).
     * Layout: fileId(10) + recordType(4) + providerId(10) + checkDate(8) + resultCode(1) + flagCategory(2) + resultDescription(40) + expirationDate(8) + nextReviewDate(8) + filler(9)
     */
    private List<DojBackgroundCheckRecord> parseDojBgcLines(List<String> lines) {
        List<DojBackgroundCheckRecord> records = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String padded = padLine(line, 100);
            records.add(DojBackgroundCheckRecord.builder()
                    .fileId(padded.substring(0, 10).trim())
                    .recordType(padded.substring(10, 14).trim())
                    .providerId(padded.substring(14, 24).trim())
                    .checkDate(parseDateSafe(padded.substring(24, 32)))
                    .resultCode(padded.substring(32, 33).trim())
                    .flagCategory(padded.substring(33, 35).trim())
                    .resultDescription(padded.substring(35, 75).trim())
                    .expirationDate(parseDateSafe(padded.substring(75, 83)))
                    .nextReviewDate(parseDateSafe(padded.substring(83, 91)))
                    .filler(padded.substring(91, 100).trim())
                    .build());
        }
        return records;
    }

    // ═══════════════════════════════════════════
    // RESULT DTO
    // ═══════════════════════════════════════════

    @Data
    public static class InboundProcessingResult {
        private String processId;
        private String sourceSystem;
        private String fileType;
        private String fileName;
        private String batchId;
        private String status; // SUCCESS, COMPLETED_WITH_ERRORS, COMPLETED_WITH_ALERTS, FAILED, NO_FILE
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int recordsParsed;
        private int recordsProcessed;
        private int recordsCreated;
        private int recordsUpdated;
        private int recordsSkipped;
        private List<Long> createdIds = List.of();
        private List<String> parseErrors = List.of();
        private List<String> validationErrors = List.of();
        private Map<String, Object> summary = Map.of();

        public long getDurationMs() {
            if (startTime == null || endTime == null) return 0;
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
    }
}
