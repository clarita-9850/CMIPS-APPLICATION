package com.cmips.baw.service;

import com.cmips.baw.filetype.Prnr998AHeaderRecord;
import com.cmips.baw.filetype.Prnr998CDetailRecord;
import com.cmips.baw.filetype.Prnr998DTrailerRecord;
import com.cmips.integration.framework.baw.format.FileFormat;
import com.cmips.integration.framework.baw.repository.FileRepository;
import com.cmips.service.TpfEspAdapterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * DSD Section 10 — PRNR998 Fixed-Width File Parser
 *
 * Parses TPF batch files in PRNR998A/C/D format:
 *   998A = Header record (1 per batch)
 *   998C = Detail records (1 per timesheet)
 *   998D = Trailer record (1 per batch, integrity check)
 *
 * After parsing, converts detail records to the JSON map format expected by
 * TpfEspAdapterService.receiveTpfBatch() and delegates timesheet creation.
 */
@Service
@Slf4j
public class Prnr998ParserService {

    private final FileRepository<Prnr998AHeaderRecord> headerRepo;
    private final FileRepository<Prnr998CDetailRecord> detailRepo;
    private final FileRepository<Prnr998DTrailerRecord> trailerRepo;

    @Autowired
    private TpfEspAdapterService tpfAdapter;

    public Prnr998ParserService() {
        this.headerRepo = FileRepository.forType(Prnr998AHeaderRecord.class);
        this.detailRepo = FileRepository.forType(Prnr998CDetailRecord.class);
        this.trailerRepo = FileRepository.forType(Prnr998DTrailerRecord.class);
    }

    /**
     * Parse a PRNR998 batch file from a local path.
     * The file contains mixed record types (HDR/DTL/TRL) identified by the first 3 chars.
     *
     * @param filePath local path to the PRNR998 file
     * @return parsing result with batch metadata and created timesheet IDs
     */
    public Prnr998ParseResult parseFile(Path filePath) {
        log.info("[PRNR998] Parsing file: {}", filePath);

        try {
            List<String> lines = Files.readAllLines(filePath);
            return parseLines(lines);
        } catch (Exception e) {
            log.error("[PRNR998] Failed to parse file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse PRNR998 file: " + filePath, e);
        }
    }

    /**
     * Parse PRNR998 content from an InputStream (for REST upload or SFTP download).
     */
    public Prnr998ParseResult parseStream(InputStream inputStream) {
        log.info("[PRNR998] Parsing from input stream");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return parseLines(lines);
        } catch (Exception e) {
            log.error("[PRNR998] Failed to parse stream: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse PRNR998 stream", e);
        }
    }

    /**
     * Core parsing logic — processes lines by record type prefix.
     */
    private Prnr998ParseResult parseLines(List<String> lines) {
        Prnr998AHeaderRecord header = null;
        List<Prnr998CDetailRecord> details = new ArrayList<>();
        Prnr998DTrailerRecord trailer = null;
        List<String> parseErrors = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.length() < 3) {
                parseErrors.add("Line " + (i + 1) + ": too short (" + line.length() + " chars)");
                continue;
            }

            String recType = line.substring(0, 3);
            try {
                switch (recType) {
                    case "HDR" -> header = parseHeader(line);
                    case "DTL" -> details.add(parseDetail(line));
                    case "TRL" -> trailer = parseTrailer(line);
                    default -> parseErrors.add("Line " + (i + 1) + ": unknown record type '" + recType + "'");
                }
            } catch (Exception e) {
                parseErrors.add("Line " + (i + 1) + ": " + e.getMessage());
            }
        }

        // Validate batch integrity
        List<String> validationErrors = validateBatch(header, details, trailer);

        // Convert detail records to TPF adapter format and create timesheets
        List<Map<String, Object>> tpfRecords = new ArrayList<>();
        for (Prnr998CDetailRecord detail : details) {
            if (!detail.hasError()) {
                tpfRecords.add(convertDetailToTpfRecord(detail));
            } else {
                parseErrors.add("Skipped detail " + detail.getTimesheetNumber()
                        + ": verification error " + detail.getErrorCode());
            }
        }

        List<Long> createdIds = List.of();
        if (!tpfRecords.isEmpty() && validationErrors.isEmpty()) {
            createdIds = tpfAdapter.receiveTpfBatch(tpfRecords);
        }

        Prnr998ParseResult result = new Prnr998ParseResult();
        result.setBatchId(header != null ? header.getBatchId() : "UNKNOWN");
        result.setBatchDate(header != null ? header.getBatchDate() : null);
        result.setCountyCode(header != null ? header.getCountyCode() : null);
        result.setTotalLinesRead(lines.size());
        result.setDetailRecordsParsed(details.size());
        result.setDetailRecordsProcessed(tpfRecords.size());
        result.setTimesheetIdsCreated(createdIds);
        result.setParseErrors(parseErrors);
        result.setValidationErrors(validationErrors);

        log.info("[PRNR998] Parse complete: batch={}, details={}, created={}, errors={}",
                result.getBatchId(), details.size(), createdIds.size(),
                parseErrors.size() + validationErrors.size());

        return result;
    }

    /**
     * Parse a header line (80 chars) into Prnr998AHeaderRecord.
     */
    private Prnr998AHeaderRecord parseHeader(String line) {
        String padded = padToLength(line, 80);
        return Prnr998AHeaderRecord.builder()
                .recordType(padded.substring(0, 3).trim())
                .batchId(padded.substring(3, 11).trim())
                .batchDate(parseDate(padded.substring(11, 19)))
                .recordCount(parseIntSafe(padded.substring(19, 24)))
                .countyCode(padded.substring(24, 26).trim())
                .tpfSiteId(padded.substring(26, 34).trim())
                .operatorId(padded.substring(34, 42).trim())
                .scanDate(parseDateSafe(padded.substring(42, 50)))
                .filler(padded.substring(50, 80).trim())
                .build();
    }

    /**
     * Parse a detail line (200 chars) into Prnr998CDetailRecord.
     */
    private Prnr998CDetailRecord parseDetail(String line) {
        String padded = padToLength(line, 200);
        return Prnr998CDetailRecord.builder()
                .recordType(padded.substring(0, 3).trim())
                .batchId(padded.substring(3, 11).trim())
                .timesheetNumber(padded.substring(11, 21).trim())
                .providerId(padded.substring(21, 30).trim())
                .recipientId(padded.substring(30, 39).trim())
                .caseNumber(padded.substring(39, 49).trim())
                .payPeriodStart(parseDate(padded.substring(49, 57)))
                .payPeriodEnd(parseDate(padded.substring(57, 65)))
                .programType(padded.substring(65, 69).trim())
                .totalHoursClaimed(parseBigDecimalSafe(padded.substring(69, 74)))
                .dailyHoursBlock(padded.substring(74, 134))
                .providerSignature(padded.substring(134, 135).trim())
                .recipientSignature(padded.substring(135, 136).trim())
                .dateReceived(parseDateSafe(padded.substring(136, 144)))
                .imageId(padded.substring(144, 152).trim())
                .verificationStatus(padded.substring(152, 153).trim())
                .errorCode(padded.substring(153, 155).trim())
                .filler(padded.substring(155, 200).trim())
                .build();
    }

    /**
     * Parse a trailer line (80 chars) into Prnr998DTrailerRecord.
     */
    private Prnr998DTrailerRecord parseTrailer(String line) {
        String padded = padToLength(line, 80);
        return Prnr998DTrailerRecord.builder()
                .recordType(padded.substring(0, 3).trim())
                .batchId(padded.substring(3, 11).trim())
                .batchDate(parseDate(padded.substring(11, 19)))
                .totalDetailRecords(parseIntSafe(padded.substring(19, 24)))
                .totalHours(parseBigDecimalSafe(padded.substring(24, 31)))
                .errorCount(parseIntSafe(padded.substring(31, 36)))
                .verifiedCount(parseIntSafe(padded.substring(36, 41)))
                .filler(padded.substring(41, 80).trim())
                .build();
    }

    /**
     * Validate batch integrity: header/trailer counts match, batch IDs consistent.
     */
    private List<String> validateBatch(Prnr998AHeaderRecord header,
                                        List<Prnr998CDetailRecord> details,
                                        Prnr998DTrailerRecord trailer) {
        List<String> errors = new ArrayList<>();

        if (header == null) {
            errors.add("Missing HDR (header) record");
            return errors;
        }
        if (trailer == null) {
            errors.add("Missing TRL (trailer) record");
            return errors;
        }

        if (!header.getBatchId().equals(trailer.getBatchId())) {
            errors.add("Batch ID mismatch: header=" + header.getBatchId()
                    + ", trailer=" + trailer.getBatchId());
        }

        if (header.getRecordCount() != null && header.getRecordCount() != details.size()) {
            errors.add("Header record count (" + header.getRecordCount()
                    + ") does not match detail count (" + details.size() + ")");
        }

        if (trailer.getTotalDetailRecords() != null && trailer.getTotalDetailRecords() != details.size()) {
            errors.add("Trailer record count (" + trailer.getTotalDetailRecords()
                    + ") does not match detail count (" + details.size() + ")");
        }

        return errors;
    }

    /**
     * Convert a PRNR998C detail record to the JSON map format for TpfEspAdapterService.
     */
    private Map<String, Object> convertDetailToTpfRecord(Prnr998CDetailRecord detail) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("timesheetNumber", detail.getTimesheetNumber());
        record.put("providerId", Long.parseLong(detail.getProviderId()));
        record.put("recipientId", Long.parseLong(detail.getRecipientId()));
        record.put("caseId", Long.parseLong(detail.getCaseNumber()));
        record.put("payPeriodStart", detail.getPayPeriodStart().toString());
        record.put("payPeriodEnd", detail.getPayPeriodEnd().toString());
        record.put("programType", detail.getProgramType());
        record.put("providerSignature", detail.isProviderSigned());
        record.put("recipientSignature", detail.isRecipientSigned());

        // Parse daily hours block (15 days × 4 chars each)
        Map<String, Number> dailyHours = parseDailyHoursBlock(
                detail.getDailyHoursBlock(), detail.getPayPeriodStart());
        record.put("dailyHours", dailyHours);

        return record;
    }

    /**
     * Parse the 60-char daily hours block into a date→hours map.
     * Each 4-char slot = hours for one day (format: 99.9), from payPeriodStart.
     */
    private Map<String, Number> parseDailyHoursBlock(String block, LocalDate startDate) {
        Map<String, Number> daily = new LinkedHashMap<>();
        if (block == null || block.isBlank()) return daily;

        for (int day = 0; day < 15; day++) {
            int pos = day * 4;
            if (pos + 4 > block.length()) break;

            String hourStr = block.substring(pos, pos + 4).trim();
            if (!hourStr.isEmpty()) {
                try {
                    double hours = Double.parseDouble(hourStr);
                    if (hours > 0) {
                        LocalDate date = startDate.plusDays(day);
                        daily.put(date.toString(), hours);
                    }
                } catch (NumberFormatException e) {
                    // Skip unparseable day
                }
            }
        }
        return daily;
    }

    // --- Parsing helpers ---

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private LocalDate parseDate(String s) {
        return LocalDate.parse(s.trim(), DATE_FMT);
    }

    private LocalDate parseDateSafe(String s) {
        try {
            String trimmed = s.trim();
            if (trimmed.isEmpty() || "00000000".equals(trimmed)) return null;
            return LocalDate.parse(trimmed, DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private BigDecimal parseBigDecimalSafe(String s) {
        try {
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String padToLength(String line, int length) {
        if (line.length() >= length) return line;
        return line + " ".repeat(length - line.length());
    }

    // --- Result DTO ---

    @lombok.Data
    public static class Prnr998ParseResult {
        private String batchId;
        private LocalDate batchDate;
        private String countyCode;
        private int totalLinesRead;
        private int detailRecordsParsed;
        private int detailRecordsProcessed;
        private List<Long> timesheetIdsCreated = List.of();
        private List<String> parseErrors = List.of();
        private List<String> validationErrors = List.of();

        public boolean hasErrors() {
            return !parseErrors.isEmpty() || !validationErrors.isEmpty();
        }
    }
}
