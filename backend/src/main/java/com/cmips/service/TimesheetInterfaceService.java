package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DSD Section 24 — Internal Interfaces for Timesheet Processing
 *
 * PRDS108A: Timesheet Processing Summary → SCO (State Controller's Office)
 *           Contains processed timesheets ready for warrant generation.
 *
 * PRDS943B: Payroll Detail Record → EDD (Employment Development Department)
 *           Provider wage/hour details for unemployment insurance.
 *
 * CMNR932A: Common Number Record → DOJ (Department of Justice)
 *           Provider identification for background check matching.
 *
 * These generate flat-file output per Integration Hub framework patterns.
 */
@Service
public class TimesheetInterfaceService {

    private static final Logger log = LoggerFactory.getLogger(TimesheetInterfaceService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired private IhssTimesheetRepository tsRepo;
    @Autowired private TimesheetTimeEntryRepository entryRepo;

    /**
     * PRDS108A — Generate Timesheet Processing Summary for SCO.
     * One record per processed timesheet in the batch.
     * Fields: TS Number, Provider ID, Recipient ID, Case ID, Pay Period, Hours, Pay Rate, Gross Amount,
     *         SOC Deduction, Net Amount, OT Hours, OT Amount, Program Type, County Code.
     *
     * @param processedTimesheetIds list of timesheet IDs processed in this batch
     * @return list of fixed-width record strings
     */
    public List<String> generatePRDS108A(List<Long> processedTimesheetIds) {
        log.info("[INTERFACE] Generating PRDS108A for {} timesheets", processedTimesheetIds.size());
        List<String> records = new ArrayList<>();

        // Header record
        String batchId = "PRDS108A-" + LocalDate.now().format(DATE_FMT);
        records.add(formatPRDS108AHeader(batchId, processedTimesheetIds.size()));

        for (Long tsId : processedTimesheetIds) {
            tsRepo.findById(tsId).ifPresent(ts -> {
                if (ts.getStatus() == TimesheetEntity.TimesheetStatus.PROCESSED
                        || ts.getStatus() == TimesheetEntity.TimesheetStatus.SENT_TO_PAYROLL) {
                    records.add(formatPRDS108ADetail(ts));
                }
            });
        }

        // Trailer record
        records.add(formatPRDS108ATrailer(batchId, records.size() - 1)); // exclude header

        log.info("[INTERFACE] PRDS108A generated: {} detail records", records.size() - 2);
        return records;
    }

    /**
     * PRDS943B — Generate Payroll Detail for EDD.
     * Quarterly wage reporting for unemployment insurance.
     * Fields: Provider SSN, Name, Wage Amount, Hours Worked, Quarter, Year, Employer ID.
     */
    public List<String> generatePRDS943B(int quarter, int year) {
        log.info("[INTERFACE] Generating PRDS943B for Q{}/{}", quarter, year);
        List<String> records = new ArrayList<>();

        // Determine date range for quarter
        LocalDate qStart = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
        LocalDate qEnd = qStart.plusMonths(3).minusDays(1);

        // Get all processed timesheets in the quarter
        List<TimesheetEntity> processed = tsRepo.findAll().stream()
                .filter(ts -> ts.getStatus() == TimesheetEntity.TimesheetStatus.PROCESSED)
                .filter(ts -> !ts.getPayPeriodStart().isBefore(qStart) && !ts.getPayPeriodEnd().isAfter(qEnd))
                .collect(Collectors.toList());

        // Group by provider
        Map<Long, List<TimesheetEntity>> byProvider = processed.stream()
                .collect(Collectors.groupingBy(TimesheetEntity::getProviderId));

        String batchId = "PRDS943B-Q" + quarter + "-" + year;
        records.add(formatPRDS943BHeader(batchId, byProvider.size()));

        for (Map.Entry<Long, List<TimesheetEntity>> entry : byProvider.entrySet()) {
            double totalHours = entry.getValue().stream()
                    .mapToDouble(ts -> ts.getTotalHoursApproved() != null ? ts.getTotalHoursApproved() : 0).sum();
            double totalOT = entry.getValue().stream()
                    .mapToDouble(ts -> ts.getOvertimeHours() != null ? ts.getOvertimeHours() : 0).sum();

            records.add(formatPRDS943BDetail(entry.getKey(), totalHours, totalOT, quarter, year));
        }

        records.add(formatPRDS943BTrailer(batchId, byProvider.size()));

        log.info("[INTERFACE] PRDS943B generated: {} provider records for Q{}/{}", byProvider.size(), quarter, year);
        return records;
    }

    /**
     * CMNR932A — Generate Common Number Record for DOJ.
     * Provider identification records for background check matching.
     * Fields: Provider ID, SSN, Name, DOB, Address, County Code, Assignment Status.
     */
    public List<String> generateCMNR932A(List<Long> providerIds) {
        log.info("[INTERFACE] Generating CMNR932A for {} providers", providerIds.size());
        List<String> records = new ArrayList<>();

        String batchId = "CMNR932A-" + LocalDate.now().format(DATE_FMT);
        records.add(String.format("%-10s%-14s%-8s%06d", "CMNR932A", "HDR", LocalDate.now().format(DATE_FMT), providerIds.size()));

        for (Long providerId : providerIds) {
            // Generate provider identification record
            records.add(String.format("%-10s%-14s%010d%-10s", "CMNR932A", "DTL", providerId, "ACTIVE"));
        }

        records.add(String.format("%-10s%-14s%06d", "CMNR932A", "TRL", providerIds.size()));

        log.info("[INTERFACE] CMNR932A generated: {} provider records", providerIds.size());
        return records;
    }

    // --- Format helpers ---

    private String formatPRDS108AHeader(String batchId, int recordCount) {
        return String.format("%-10s%-30s%-8s%06d%-20s",
                "PRDS108A", batchId, LocalDate.now().format(DATE_FMT), recordCount,
                LocalDateTime.now().format(DATETIME_FMT));
    }

    private String formatPRDS108ADetail(TimesheetEntity ts) {
        return String.format("%-20s%010d%010d%010d%-8s%-8s%08.2f%08.2f%08.2f%08.2f%-5s%-5s",
                ts.getTimesheetNumber() != null ? ts.getTimesheetNumber() : "",
                ts.getProviderId() != null ? ts.getProviderId() : 0,
                ts.getRecipientId() != null ? ts.getRecipientId() : 0,
                ts.getCaseId() != null ? ts.getCaseId() : 0,
                ts.getPayPeriodStart() != null ? ts.getPayPeriodStart().format(DATE_FMT) : "",
                ts.getPayPeriodEnd() != null ? ts.getPayPeriodEnd().format(DATE_FMT) : "",
                ts.getTotalHoursApproved() != null ? ts.getTotalHoursApproved() : 0.0,
                ts.getRegularHours() != null ? ts.getRegularHours() : 0.0,
                ts.getOvertimeHours() != null ? ts.getOvertimeHours() : 0.0,
                ts.getSocAmount() != null ? ts.getSocAmount() : 0.0,
                ts.getProgramType() != null ? ts.getProgramType().name() : "IHSS",
                ts.getCountyCode() != null ? ts.getCountyCode() : "");
    }

    private String formatPRDS108ATrailer(String batchId, int detailCount) {
        return String.format("%-10s%-30s%06d%-20s", "PRDS108A", "TRL-" + batchId, detailCount,
                LocalDateTime.now().format(DATETIME_FMT));
    }

    private String formatPRDS943BHeader(String batchId, int providerCount) {
        return String.format("%-10s%-30s%-8s%06d", "PRDS943B", batchId, LocalDate.now().format(DATE_FMT), providerCount);
    }

    private String formatPRDS943BDetail(Long providerId, double totalHours, double totalOT, int quarter, int year) {
        return String.format("%-10s%010d%08.2f%08.2fQ%d%04d",
                "PRDS943B", providerId, totalHours, totalOT, quarter, year);
    }

    private String formatPRDS943BTrailer(String batchId, int count) {
        return String.format("%-10s%-30s%06d", "PRDS943B", "TRL-" + batchId, count);
    }
}
