package com.cmips.service;

import com.cmips.baw.destination.DojSftpDestination;
import com.cmips.baw.destination.EddSftpDestination;
import com.cmips.baw.destination.ScoSftpDestination;
import com.cmips.baw.filetype.Cmnr932ARecord;
import com.cmips.baw.filetype.PaymentFileRecord;
import com.cmips.baw.filetype.Prds108ARecord;
import com.cmips.baw.filetype.Prds943BRecord;
import com.cmips.entity.TimesheetEntity;
import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.repository.FileRepository;
import com.cmips.repository.IhssTimesheetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DSD Section 24 — Internal Interfaces for Timesheet Processing
 *
 * Uses Integration Hub Framework's FileRepository + @FileType records
 * to generate properly formatted fixed-width files and transmit them
 * via SendBuilder to external SFTP destinations.
 *
 * PRDS108A: Timesheet Processing Summary → SCO (State Controller's Office)
 * PRDS943B: Payroll Detail Record → EDD (Employment Development Department)
 * CMNR932A: Common Number Record → DOJ (Department of Justice)
 */
@Service
public class TimesheetInterfaceService {

    private static final Logger log = LoggerFactory.getLogger(TimesheetInterfaceService.class);
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Autowired private IhssTimesheetRepository tsRepo;

    // Integration Hub FileRepositories — type-safe file I/O
    private final FileRepository<Prds108ARecord> prds108aRepo;
    private final FileRepository<Prds943BRecord> prds943bRepo;
    private final FileRepository<Cmnr932ARecord> cmnr932aRepo;

    public TimesheetInterfaceService() {
        this.prds108aRepo = FileRepository.forType(Prds108ARecord.class);
        this.prds943bRepo = FileRepository.forType(Prds943BRecord.class);
        this.cmnr932aRepo = FileRepository.forType(Cmnr932ARecord.class);
    }

    /**
     * PRDS108A — Generate Timesheet Processing Summary for SCO.
     *
     * Builds typed Prds108ARecord objects from processed timesheets,
     * writes to fixed-width file via FileRepository, and sends to SCO SFTP.
     *
     * @param processedTimesheetIds list of timesheet IDs in this batch
     * @return result map with file path, record count, and send status
     */
    public Map<String, Object> generateAndSendPRDS108A(List<Long> processedTimesheetIds) {
        log.info("[PRDS108A] Generating for {} timesheets", processedTimesheetIds.size());

        // Build typed records from timesheet entities
        List<Prds108ARecord> records = new ArrayList<>();
        for (Long tsId : processedTimesheetIds) {
            tsRepo.findById(tsId).ifPresent(ts -> {
                if (ts.getStatus() == TimesheetEntity.TimesheetStatus.PROCESSED
                        || ts.getStatus() == TimesheetEntity.TimesheetStatus.SENT_TO_PAYROLL) {
                    records.add(Prds108ARecord.fromTimesheet(
                            ts.getTimesheetNumber(),
                            ts.getProviderId(),
                            ts.getRecipientId(),
                            ts.getCaseId(),
                            ts.getPayPeriodStart(),
                            ts.getPayPeriodEnd(),
                            ts.getTotalHoursApproved() != null ? ts.getTotalHoursApproved() : 0.0,
                            ts.getRegularHours() != null ? ts.getRegularHours() : 0.0,
                            ts.getOvertimeHours() != null ? ts.getOvertimeHours() : 0.0,
                            ts.getSocAmount() != null ? ts.getSocAmount() : 0.0,
                            ts.getProgramType() != null ? ts.getProgramType().name() : "IHSS",
                            ts.getCountyCode()
                    ));
                }
            });
        }

        if (records.isEmpty()) {
            log.warn("[PRDS108A] No eligible timesheets found");
            return Map.of("status", "EMPTY", "recordCount", 0);
        }

        // Write to local file via FileRepository
        String fileName = "PRDS108A_" + LocalDateTime.now().format(TIMESTAMP_FMT) + ".DAT";
        Path localFile = Paths.get("./baw-outbound").resolve(fileName);

        try {
            Files.createDirectories(localFile.getParent());
            prds108aRepo.write(records, localFile, FileFormat.fixedWidth().build());
            log.info("[PRDS108A] Written {} records to {}", records.size(), localFile);

            // Send to SCO via SendBuilder
            var sendResult = prds108aRepo.send(records)
                    .as(FileFormat.fixedWidth().build())
                    .to(ScoSftpDestination.class)
                    .withFilename(fileName)
                    .execute();

            log.info("[PRDS108A] Send result: success={}, records={}, duration={}ms",
                    sendResult.isSuccess(), sendResult.getRecordCount(), sendResult.getByteCount());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("interfaceType", "PRDS108A");
            result.put("destination", "SCO");
            result.put("fileName", fileName);
            result.put("recordCount", records.size());
            result.put("sendSuccess", sendResult.isSuccess());
            result.put("sendByteCount", sendResult.getByteCount());
            return result;

        } catch (Exception e) {
            log.error("[PRDS108A] Failed: {}", e.getMessage(), e);
            // Fallback: return records as formatted strings for backward compatibility
            return Map.of(
                    "interfaceType", "PRDS108A",
                    "status", "WRITE_ONLY",
                    "recordCount", records.size(),
                    "localFile", localFile.toString(),
                    "error", e.getMessage()
            );
        }
    }

    /**
     * PRDS943B — Generate Payroll Detail for EDD (quarterly).
     *
     * Aggregates provider hours across the quarter, writes typed records,
     * and sends to EDD SFTP.
     */
    public Map<String, Object> generateAndSendPRDS943B(int quarter, int year) {
        log.info("[PRDS943B] Generating for Q{}/{}", quarter, year);

        // Determine quarter date range
        LocalDate qStart = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
        LocalDate qEnd = qStart.plusMonths(3).minusDays(1);

        // Get processed timesheets in the quarter
        List<TimesheetEntity> processed = tsRepo.findAll().stream()
                .filter(ts -> ts.getStatus() == TimesheetEntity.TimesheetStatus.PROCESSED)
                .filter(ts -> !ts.getPayPeriodStart().isBefore(qStart) && !ts.getPayPeriodEnd().isAfter(qEnd))
                .collect(Collectors.toList());

        // Group by provider and aggregate
        Map<Long, List<TimesheetEntity>> byProvider = processed.stream()
                .collect(Collectors.groupingBy(TimesheetEntity::getProviderId));

        List<Prds943BRecord> records = new ArrayList<>();
        for (Map.Entry<Long, List<TimesheetEntity>> entry : byProvider.entrySet()) {
            double totalHours = entry.getValue().stream()
                    .mapToDouble(ts -> ts.getTotalHoursApproved() != null ? ts.getTotalHoursApproved() : 0).sum();
            double totalOT = entry.getValue().stream()
                    .mapToDouble(ts -> ts.getOvertimeHours() != null ? ts.getOvertimeHours() : 0).sum();

            records.add(Prds943BRecord.fromProviderAggregation(
                    entry.getKey(), totalHours, totalOT, quarter, year));
        }

        if (records.isEmpty()) {
            log.warn("[PRDS943B] No provider records for Q{}/{}", quarter, year);
            return Map.of("status", "EMPTY", "recordCount", 0);
        }

        String fileName = "PRDS943B_Q" + quarter + year + "_" + LocalDateTime.now().format(TIMESTAMP_FMT) + ".DAT";
        Path localFile = Paths.get("./baw-outbound").resolve(fileName);

        try {
            Files.createDirectories(localFile.getParent());
            prds943bRepo.write(records, localFile, FileFormat.fixedWidth().build());
            log.info("[PRDS943B] Written {} records to {}", records.size(), localFile);

            var sendResult = prds943bRepo.send(records)
                    .as(FileFormat.fixedWidth().build())
                    .to(EddSftpDestination.class)
                    .withFilename(fileName)
                    .execute();

            log.info("[PRDS943B] Send result: success={}, records={}", sendResult.isSuccess(), sendResult.getRecordCount());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("interfaceType", "PRDS943B");
            result.put("destination", "EDD");
            result.put("quarter", "Q" + quarter + "/" + year);
            result.put("fileName", fileName);
            result.put("providerCount", records.size());
            result.put("sendSuccess", sendResult.isSuccess());
            result.put("sendByteCount", sendResult.getByteCount());
            return result;

        } catch (Exception e) {
            log.error("[PRDS943B] Failed: {}", e.getMessage(), e);
            return Map.of(
                    "interfaceType", "PRDS943B",
                    "status", "WRITE_ONLY",
                    "recordCount", records.size(),
                    "localFile", localFile.toString(),
                    "error", e.getMessage()
            );
        }
    }

    /**
     * CMNR932A — Generate Common Number Record for DOJ.
     *
     * One record per provider, sent to DOJ for background check matching.
     */
    public Map<String, Object> generateAndSendCMNR932A(List<Long> providerIds) {
        log.info("[CMNR932A] Generating for {} providers", providerIds.size());

        List<Cmnr932ARecord> records = providerIds.stream()
                .map(id -> Cmnr932ARecord.fromProvider(id, "ACTIVE"))
                .collect(Collectors.toList());

        if (records.isEmpty()) {
            return Map.of("status", "EMPTY", "recordCount", 0);
        }

        String fileName = "CMNR932A_" + LocalDateTime.now().format(TIMESTAMP_FMT) + ".DAT";
        Path localFile = Paths.get("./baw-outbound").resolve(fileName);

        try {
            Files.createDirectories(localFile.getParent());
            cmnr932aRepo.write(records, localFile, FileFormat.fixedWidth().build());
            log.info("[CMNR932A] Written {} records to {}", records.size(), localFile);

            var sendResult = cmnr932aRepo.send(records)
                    .as(FileFormat.fixedWidth().build())
                    .to(DojSftpDestination.class)
                    .withFilename(fileName)
                    .execute();

            log.info("[CMNR932A] Send result: success={}, records={}", sendResult.isSuccess(), sendResult.getRecordCount());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("interfaceType", "CMNR932A");
            result.put("destination", "DOJ");
            result.put("fileName", fileName);
            result.put("providerCount", records.size());
            result.put("sendSuccess", sendResult.isSuccess());
            result.put("sendByteCount", sendResult.getByteCount());
            return result;

        } catch (Exception e) {
            log.error("[CMNR932A] Failed: {}", e.getMessage(), e);
            return Map.of(
                    "interfaceType", "CMNR932A",
                    "status", "WRITE_ONLY",
                    "recordCount", records.size(),
                    "localFile", localFile.toString(),
                    "error", e.getMessage()
            );
        }
    }

    // --- Shared helpers for sick leave / travel claim / PRDR120A SFTP send ---

    /**
     * Send a single PRDS108A record to SCO via SFTP.
     * Used by SickLeaveClaimController and IhssTimesheetController (travel claims)
     * to transmit individual payroll records to SCO.
     *
     * @param record the PRDS108A record to send
     * @param filePrefix filename prefix (e.g. "PRDS108A-SLC", "PRDS108A-TC")
     * @return result map with send status
     */
    public Map<String, Object> sendSingleRecordToSco(Prds108ARecord record, String filePrefix) {
        log.info("[{}] Sending single record to SCO: {}", filePrefix, record.getTimesheetNumber());
        List<Prds108ARecord> records = List.of(record);

        String fileName = filePrefix + "_" + LocalDateTime.now().format(TIMESTAMP_FMT) + ".DAT";
        try {
            var sendResult = prds108aRepo.send(records)
                    .as(FileFormat.fixedWidth().build())
                    .to(ScoSftpDestination.class)
                    .withFilename(fileName)
                    .execute();

            log.info("[{}] Send result: success={}, bytes={}",
                    filePrefix, sendResult.isSuccess(), sendResult.getByteCount());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("interfaceType", filePrefix);
            result.put("destination", "SCO");
            result.put("fileName", fileName);
            result.put("sendSuccess", sendResult.isSuccess());
            result.put("sendByteCount", sendResult.getByteCount());
            return result;
        } catch (Exception e) {
            log.error("[{}] SFTP send failed: {}", filePrefix, e.getMessage(), e);
            return Map.of(
                    "interfaceType", filePrefix,
                    "destination", "SCO",
                    "sendSuccess", false,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * PRDR120A — Send payment file records to SCO on demand.
     * Normally triggered by batch job, but this allows manual/REST-driven send.
     */
    public Map<String, Object> generateAndSendPRDR120A(List<Long> timesheetIds) {
        log.info("[PRDR120A] Generating payment file for {} timesheets", timesheetIds.size());
        FileRepository<PaymentFileRecord> paymentRepo = FileRepository.forType(PaymentFileRecord.class);

        List<PaymentFileRecord> records = new ArrayList<>();
        for (Long tsId : timesheetIds) {
            tsRepo.findById(tsId).ifPresent(ts -> {
                if (ts.getStatus() == TimesheetEntity.TimesheetStatus.PROCESSED
                        || ts.getStatus() == TimesheetEntity.TimesheetStatus.SENT_TO_PAYROLL) {
                    records.add(PaymentFileRecord.builder()
                            .paymentRequestId("PAY-" + ts.getTimesheetNumber())
                            .providerId(ts.getProviderId() != null ? String.valueOf(ts.getProviderId()) : "")
                            .providerName("")
                            .caseNumber(ts.getCaseId() != null ? String.valueOf(ts.getCaseId()) : "")
                            .countyCode(ts.getCountyCode() != null ? ts.getCountyCode() : "")
                            .payPeriodStart(ts.getPayPeriodStart())
                            .payPeriodEnd(ts.getPayPeriodEnd())
                            .regularHours(ts.getRegularHours() != null
                                    ? BigDecimal.valueOf(ts.getRegularHours()) : BigDecimal.ZERO)
                            .overtimeHours(ts.getOvertimeHours() != null
                                    ? BigDecimal.valueOf(ts.getOvertimeHours()) : BigDecimal.ZERO)
                            .totalHours(ts.getTotalHoursApproved() != null
                                    ? BigDecimal.valueOf(ts.getTotalHoursApproved()) : BigDecimal.ZERO)
                            .paymentAmount(BigDecimal.ZERO)
                            .timesheetId(ts.getId())
                            .paymentTypeCode("R")
                            .build());
                }
            });
        }

        if (records.isEmpty()) {
            return Map.of("status", "EMPTY", "recordCount", 0);
        }

        String fileName = "PRDR120A_" + LocalDateTime.now().format(TIMESTAMP_FMT) + ".DAT";
        try {
            var sendResult = paymentRepo.send(records)
                    .as(FileFormat.fixedWidth().build())
                    .to(ScoSftpDestination.class)
                    .withFilename(fileName)
                    .execute();

            log.info("[PRDR120A] Send result: success={}, records={}", sendResult.isSuccess(), sendResult.getRecordCount());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("interfaceType", "PRDR120A");
            result.put("destination", "SCO");
            result.put("fileName", fileName);
            result.put("recordCount", records.size());
            result.put("sendSuccess", sendResult.isSuccess());
            result.put("sendByteCount", sendResult.getByteCount());
            return result;
        } catch (Exception e) {
            log.error("[PRDR120A] Failed: {}", e.getMessage(), e);
            return Map.of("interfaceType", "PRDR120A", "status", "FAILED", "error", e.getMessage());
        }
    }

    // --- Legacy API compatibility ---
    // These methods return List<String> for backward compatibility with the existing controller

    /**
     * Legacy: Generate PRDS108A as raw record strings.
     */
    public List<String> generatePRDS108A(List<Long> processedTimesheetIds) {
        log.info("[INTERFACE] Generating PRDS108A for {} timesheets", processedTimesheetIds.size());
        List<Prds108ARecord> records = new ArrayList<>();

        for (Long tsId : processedTimesheetIds) {
            tsRepo.findById(tsId).ifPresent(ts -> {
                if (ts.getStatus() == TimesheetEntity.TimesheetStatus.PROCESSED
                        || ts.getStatus() == TimesheetEntity.TimesheetStatus.SENT_TO_PAYROLL) {
                    records.add(Prds108ARecord.fromTimesheet(
                            ts.getTimesheetNumber(), ts.getProviderId(), ts.getRecipientId(),
                            ts.getCaseId(), ts.getPayPeriodStart(), ts.getPayPeriodEnd(),
                            ts.getTotalHoursApproved() != null ? ts.getTotalHoursApproved() : 0.0,
                            ts.getRegularHours() != null ? ts.getRegularHours() : 0.0,
                            ts.getOvertimeHours() != null ? ts.getOvertimeHours() : 0.0,
                            ts.getSocAmount() != null ? ts.getSocAmount() : 0.0,
                            ts.getProgramType() != null ? ts.getProgramType().name() : "IHSS",
                            ts.getCountyCode()
                    ));
                }
            });
        }

        // Use FileRepository to serialize to fixed-width bytes, then convert to strings
        try {
            byte[] bytes = prds108aRepo.writeToBytes(records, FileFormat.fixedWidth().build());
            return List.of(new String(bytes).split(System.lineSeparator()));
        } catch (Exception e) {
            log.error("[PRDS108A] FileRepository serialization failed, returning empty: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Legacy: Generate PRDS943B as raw record strings.
     */
    public List<String> generatePRDS943B(int quarter, int year) {
        log.info("[INTERFACE] Generating PRDS943B for Q{}/{}", quarter, year);

        LocalDate qStart = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
        LocalDate qEnd = qStart.plusMonths(3).minusDays(1);

        List<TimesheetEntity> processed = tsRepo.findAll().stream()
                .filter(ts -> ts.getStatus() == TimesheetEntity.TimesheetStatus.PROCESSED)
                .filter(ts -> !ts.getPayPeriodStart().isBefore(qStart) && !ts.getPayPeriodEnd().isAfter(qEnd))
                .collect(Collectors.toList());

        Map<Long, List<TimesheetEntity>> byProvider = processed.stream()
                .collect(Collectors.groupingBy(TimesheetEntity::getProviderId));

        List<Prds943BRecord> records = new ArrayList<>();
        for (Map.Entry<Long, List<TimesheetEntity>> entry : byProvider.entrySet()) {
            double totalHours = entry.getValue().stream()
                    .mapToDouble(ts -> ts.getTotalHoursApproved() != null ? ts.getTotalHoursApproved() : 0).sum();
            double totalOT = entry.getValue().stream()
                    .mapToDouble(ts -> ts.getOvertimeHours() != null ? ts.getOvertimeHours() : 0).sum();
            records.add(Prds943BRecord.fromProviderAggregation(entry.getKey(), totalHours, totalOT, quarter, year));
        }

        try {
            byte[] bytes = prds943bRepo.writeToBytes(records, FileFormat.fixedWidth().build());
            return List.of(new String(bytes).split(System.lineSeparator()));
        } catch (Exception e) {
            log.error("[PRDS943B] FileRepository serialization failed: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Legacy: Generate CMNR932A as raw record strings.
     */
    public List<String> generateCMNR932A(List<Long> providerIds) {
        log.info("[INTERFACE] Generating CMNR932A for {} providers", providerIds.size());

        List<Cmnr932ARecord> records = providerIds.stream()
                .map(id -> Cmnr932ARecord.fromProvider(id, "ACTIVE"))
                .collect(Collectors.toList());

        try {
            byte[] bytes = cmnr932aRepo.writeToBytes(records, FileFormat.fixedWidth().build());
            return List.of(new String(bytes).split(System.lineSeparator()));
        } catch (Exception e) {
            log.error("[CMNR932A] FileRepository serialization failed: {}", e.getMessage());
            return List.of();
        }
    }
}
