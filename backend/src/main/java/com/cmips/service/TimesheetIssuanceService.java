package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * DSD Section 24 — Timesheet Issuance Workflow Service
 * Manages timesheets from generation → mailing → delivery → return tracking.
 */
@Service
public class TimesheetIssuanceService {

    private static final Logger log = LoggerFactory.getLogger(TimesheetIssuanceService.class);

    @Autowired private TimesheetIssuanceRepository issuanceRepo;
    @Autowired private IhssTimesheetRepository tsRepo;

    public List<TimesheetIssuanceEntity> listByCase(Long caseId) {
        return issuanceRepo.findByCaseIdOrderByPayPeriodStartDesc(caseId);
    }

    public List<TimesheetIssuanceEntity> listByProvider(Long providerId) {
        return issuanceRepo.findByProviderIdOrderByPayPeriodStartDesc(providerId);
    }

    public List<TimesheetIssuanceEntity> listPendingGeneration() {
        return issuanceRepo.findPendingForGeneration(LocalDate.now());
    }

    public List<TimesheetIssuanceEntity> listPendingBatchPrint() {
        return issuanceRepo.findPendingForBatchPrint();
    }

    public Optional<TimesheetIssuanceEntity> getByIssuanceNumber(String issuanceNumber) {
        return issuanceRepo.findByIssuanceNumber(issuanceNumber);
    }

    /**
     * Create a new timesheet issuance for a provider-recipient assignment.
     */
    @Transactional
    public TimesheetIssuanceEntity createIssuance(Map<String, Object> request) {
        Long caseId = toLong(request.get("caseId"));
        Long recipientId = toLong(request.get("recipientId"));
        Long providerId = toLong(request.get("providerId"));
        LocalDate ppStart = LocalDate.parse((String) request.get("payPeriodStart"));
        LocalDate ppEnd = LocalDate.parse((String) request.get("payPeriodEnd"));

        // Check for existing active issuance
        List<TimesheetIssuanceEntity> existing = issuanceRepo.findActiveIssuance(caseId, providerId, ppStart, ppEnd);
        if (!existing.isEmpty()) {
            throw new RuntimeException("Active issuance already exists for case=" + caseId
                    + " provider=" + providerId + " period=" + ppStart + " to " + ppEnd);
        }

        TimesheetIssuanceEntity iss = new TimesheetIssuanceEntity();
        iss.setCaseId(caseId);
        iss.setRecipientId(recipientId);
        iss.setProviderId(providerId);
        iss.setPayPeriodStart(ppStart);
        iss.setPayPeriodEnd(ppEnd);
        iss.setTimesheetType((String) request.getOrDefault("timesheetType", "STANDARD"));
        iss.setProgramType((String) request.getOrDefault("programType", "IHSS"));
        iss.setCountyCode((String) request.get("countyCode"));
        iss.setProviderAddress((String) request.get("providerAddress"));
        iss.setCreatedBy((String) request.get("createdBy"));

        String method = (String) request.get("issuanceMethod");
        if (method != null) {
            iss.setIssuanceMethod(TimesheetIssuanceEntity.IssuanceMethod.valueOf(method));
        } else {
            iss.setIssuanceMethod(TimesheetIssuanceEntity.IssuanceMethod.MAIL);
        }

        iss.setStatus(TimesheetIssuanceEntity.IssuanceStatus.PENDING_GENERATION);
        iss.setExpectedReturnDate(ppEnd.plusDays(30));

        log.info("[ISSUANCE] Created issuance for case={} provider={} period={} to {}",
                caseId, providerId, ppStart, ppEnd);
        return issuanceRepo.save(iss);
    }

    /**
     * Generate timesheet — assigns TS number and creates TimesheetEntity.
     */
    @Transactional
    public TimesheetIssuanceEntity generateTimesheet(Long issuanceId) {
        TimesheetIssuanceEntity iss = issuanceRepo.findById(issuanceId)
                .orElseThrow(() -> new RuntimeException("Issuance not found: " + issuanceId));
        if (iss.getStatus() != TimesheetIssuanceEntity.IssuanceStatus.PENDING_GENERATION) {
            throw new RuntimeException("Issuance " + iss.getIssuanceNumber() + " is not pending generation");
        }

        // Create the actual timesheet record
        TimesheetEntity ts = new TimesheetEntity();
        ts.setCaseId(iss.getCaseId());
        ts.setRecipientId(iss.getRecipientId());
        ts.setProviderId(iss.getProviderId());
        ts.setPayPeriodStart(iss.getPayPeriodStart());
        ts.setPayPeriodEnd(iss.getPayPeriodEnd());
        ts.setProgramType("WPCS".equals(iss.getProgramType())
                ? TimesheetEntity.ProgramType.WPCS : TimesheetEntity.ProgramType.IHSS);
        ts.setTimesheetType(resolveTimesheetType(iss.getTimesheetType()));
        ts.setStatus(TimesheetEntity.TimesheetStatus.PENDING_ISSUANCE);
        ts.setCountyCode(iss.getCountyCode());
        ts = tsRepo.save(ts);

        // Update issuance with TS reference
        iss.setTimesheetId(ts.getId());
        iss.setTimesheetNumber(ts.getTimesheetNumber());
        iss.setGenerationDate(LocalDate.now());
        iss.setStatus(TimesheetIssuanceEntity.IssuanceStatus.GENERATED);

        log.info("[ISSUANCE] Generated TS {} for issuance {}", ts.getTimesheetNumber(), iss.getIssuanceNumber());
        return issuanceRepo.save(iss);
    }

    /**
     * Mark timesheet as mailed.
     */
    @Transactional
    public TimesheetIssuanceEntity markMailed(Long issuanceId) {
        TimesheetIssuanceEntity iss = issuanceRepo.findById(issuanceId)
                .orElseThrow(() -> new RuntimeException("Issuance not found: " + issuanceId));
        iss.setMailDate(LocalDate.now());
        iss.setStatus(TimesheetIssuanceEntity.IssuanceStatus.MAILED);

        // Update timesheet status to ISSUED
        if (iss.getTimesheetId() != null) {
            tsRepo.findById(iss.getTimesheetId()).ifPresent(ts -> {
                ts.setStatus(TimesheetEntity.TimesheetStatus.ISSUED);
                ts.setDateIssued(LocalDate.now());
                tsRepo.save(ts);
            });
        }

        log.info("[ISSUANCE] Marked mailed: {}", iss.getIssuanceNumber());
        return issuanceRepo.save(iss);
    }

    /**
     * Mark electronic delivery.
     */
    @Transactional
    public TimesheetIssuanceEntity markDeliveredElectronic(Long issuanceId) {
        TimesheetIssuanceEntity iss = issuanceRepo.findById(issuanceId)
                .orElseThrow(() -> new RuntimeException("Issuance not found: " + issuanceId));
        iss.setDeliveryDate(LocalDate.now());
        iss.setStatus(TimesheetIssuanceEntity.IssuanceStatus.DELIVERED_ELECTRONIC);

        if (iss.getTimesheetId() != null) {
            tsRepo.findById(iss.getTimesheetId()).ifPresent(ts -> {
                ts.setStatus(TimesheetEntity.TimesheetStatus.ISSUED);
                ts.setDateIssued(LocalDate.now());
                ts.setSourceType(TimesheetEntity.SourceType.ELECTRONIC_ESP);
                tsRepo.save(ts);
            });
        }

        return issuanceRepo.save(iss);
    }

    /**
     * Cancel an issuance.
     */
    @Transactional
    public TimesheetIssuanceEntity cancelIssuance(Long issuanceId, String reason) {
        TimesheetIssuanceEntity iss = issuanceRepo.findById(issuanceId)
                .orElseThrow(() -> new RuntimeException("Issuance not found: " + issuanceId));
        iss.setStatus(TimesheetIssuanceEntity.IssuanceStatus.CANCELLED);
        iss.setCancellationReason(reason);

        if (iss.getTimesheetId() != null) {
            tsRepo.findById(iss.getTimesheetId()).ifPresent(ts -> {
                ts.setStatus(TimesheetEntity.TimesheetStatus.CANCELLED);
                tsRepo.save(ts);
            });
        }

        log.info("[ISSUANCE] Cancelled issuance {}: {}", iss.getIssuanceNumber(), reason);
        return issuanceRepo.save(iss);
    }

    /**
     * Reissue a cancelled/lost timesheet.
     */
    @Transactional
    public TimesheetIssuanceEntity reissue(Long originalIssuanceId, String reason) {
        TimesheetIssuanceEntity original = issuanceRepo.findById(originalIssuanceId)
                .orElseThrow(() -> new RuntimeException("Original issuance not found: " + originalIssuanceId));

        // Mark original as reissued
        original.setStatus(TimesheetIssuanceEntity.IssuanceStatus.REISSUED);
        issuanceRepo.save(original);

        // Create new issuance
        TimesheetIssuanceEntity reissued = new TimesheetIssuanceEntity();
        reissued.setCaseId(original.getCaseId());
        reissued.setRecipientId(original.getRecipientId());
        reissued.setProviderId(original.getProviderId());
        reissued.setPayPeriodStart(original.getPayPeriodStart());
        reissued.setPayPeriodEnd(original.getPayPeriodEnd());
        reissued.setTimesheetType(original.getTimesheetType());
        reissued.setProgramType(original.getProgramType());
        reissued.setIssuanceMethod(original.getIssuanceMethod());
        reissued.setCountyCode(original.getCountyCode());
        reissued.setProviderAddress(original.getProviderAddress());
        reissued.setIsReissue(true);
        reissued.setOriginalIssuanceId(originalIssuanceId);
        reissued.setReissueReason(reason);
        reissued.setStatus(TimesheetIssuanceEntity.IssuanceStatus.PENDING_GENERATION);
        reissued.setExpectedReturnDate(original.getPayPeriodEnd().plusDays(30));

        log.info("[ISSUANCE] Reissued from {}: {}", original.getIssuanceNumber(), reason);
        return issuanceRepo.save(reissued);
    }

    /**
     * Batch generate all pending issuances (called by scheduler).
     */
    @Transactional
    public int batchGenerate() {
        List<TimesheetIssuanceEntity> pending = issuanceRepo.findPendingForGeneration(LocalDate.now());
        int count = 0;
        for (TimesheetIssuanceEntity iss : pending) {
            try {
                generateTimesheet(iss.getId());
                count++;
            } catch (Exception e) {
                log.error("[ISSUANCE] Failed to generate TS for issuance {}: {}", iss.getIssuanceNumber(), e.getMessage());
            }
        }
        if (count > 0) {
            log.info("[ISSUANCE] Batch generated {} timesheets", count);
        }
        return count;
    }

    private TimesheetEntity.TimesheetType resolveTimesheetType(String type) {
        if (type == null) return TimesheetEntity.TimesheetType.STANDARD;
        try {
            return TimesheetEntity.TimesheetType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return TimesheetEntity.TimesheetType.STANDARD;
        }
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }
}
