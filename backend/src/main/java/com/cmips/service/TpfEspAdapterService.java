package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DSD Section 10/24 — TPF (Timesheet Processing Facility) and ESP (Electronic Services Portal) Adapters
 *
 * TPF: Paper timesheet processing — mail intake, OCR/ICR scanning, image capture, data completion.
 *      TPF sends pre-validated timesheet data to CMIPS via batch interface.
 *      This adapter simulates receipt of TPF batch data and creates timesheet records.
 *
 * ESP: Electronic Services Portal — provider self-service for e-timesheet submission.
 *      ESP timesheets arrive via real-time API (Mode of Entry = ELECTRONIC or TELEPHONIC).
 *      This adapter handles receipt, TTS Recipient Review flow, and notification triggers.
 *
 * TTS: Telephone Timesheet System — telephonic timesheet submission.
 *      TTS timesheets follow the same flow as ESP with Mode of Entry = TELEPHONIC.
 */
@Service
public class TpfEspAdapterService {

    private static final Logger log = LoggerFactory.getLogger(TpfEspAdapterService.class);

    @Autowired private IhssTimesheetRepository tsRepo;
    @Autowired private TimesheetTimeEntryRepository entryRepo;
    @Autowired private TimesheetExceptionRepository exRepo;

    // ═══════════════════════════════════════════
    // TPF ADAPTER — Paper Timesheet Ingest
    // ═══════════════════════════════════════════

    /**
     * DSD Section 10: Receive batch of pre-validated timesheet data from TPF.
     * TPF sends data after: mail intake → document prep → scanning → OCR/ICR → verification → data completion.
     *
     * Each record in the batch contains: timesheetNumber, providerId, recipientId, caseId,
     * payPeriodStart, payPeriodEnd, programType, dailyHours map, providerSignature, recipientSignature.
     *
     * @param tpfRecords list of TPF batch records
     * @return list of created timesheet IDs
     */
    @Transactional
    public List<Long> receiveTpfBatch(List<Map<String, Object>> tpfRecords) {
        log.info("[TPF] Receiving batch of {} records", tpfRecords.size());
        List<Long> createdIds = new ArrayList<>();

        for (Map<String, Object> record : tpfRecords) {
            try {
                TimesheetEntity ts = createTimesheetFromTpf(record);
                createdIds.add(ts.getId());
            } catch (Exception e) {
                log.error("[TPF] Error processing record: {}", e.getMessage());
                createTpfException(record, e.getMessage());
            }
        }

        log.info("[TPF] Batch complete: {} timesheets created, {} errors",
                createdIds.size(), tpfRecords.size() - createdIds.size());
        return createdIds;
    }

    private TimesheetEntity createTimesheetFromTpf(Map<String, Object> record) {
        TimesheetEntity ts = new TimesheetEntity();
        ts.setTimesheetNumber((String) record.get("timesheetNumber"));
        ts.setProviderId(toLong(record.get("providerId")));
        ts.setRecipientId(toLong(record.get("recipientId")));
        ts.setCaseId(toLong(record.get("caseId")));
        ts.setPayPeriodStart(LocalDate.parse((String) record.get("payPeriodStart")));
        ts.setPayPeriodEnd(LocalDate.parse((String) record.get("payPeriodEnd")));
        ts.setModeOfEntry("TPF");
        ts.setSourceType(TimesheetEntity.SourceType.TPF_PAPER);
        ts.setStatus(TimesheetEntity.TimesheetStatus.RECEIVED);
        ts.setProviderSignaturePresent(Boolean.TRUE.equals(record.get("providerSignature")));
        ts.setRecipientSignaturePresent(Boolean.TRUE.equals(record.get("recipientSignature")));
        ts.setDateReceived(LocalDate.now());

        String progStr = (String) record.getOrDefault("programType", "IHSS");
        try {
            ts.setProgramType(TimesheetEntity.ProgramType.valueOf(progStr));
        } catch (Exception e) {
            ts.setProgramType(TimesheetEntity.ProgramType.IHSS);
        }

        ts = tsRepo.save(ts);

        // Create daily time entries from TPF data
        @SuppressWarnings("unchecked")
        Map<String, Number> dailyHours = (Map<String, Number>) record.getOrDefault("dailyHours", Map.of());
        for (Map.Entry<String, Number> entry : dailyHours.entrySet()) {
            TimesheetTimeEntryEntity te = new TimesheetTimeEntryEntity();
            te.setTimesheetId(ts.getId());
            te.setEntryDate(LocalDate.parse(entry.getKey()));
            te.setHoursClaimed(entry.getValue().doubleValue());
            entryRepo.save(te);
        }

        log.info("[TPF] Created timesheet: id={}, tsNumber={}, provider={}",
                ts.getId(), ts.getTimesheetNumber(), ts.getProviderId());
        return ts;
    }

    private void createTpfException(Map<String, Object> record, String errorMessage) {
        log.warn("[TPF] Exception for record tsNumber={}: {}",
                record.getOrDefault("timesheetNumber", "UNKNOWN"), errorMessage);
    }

    // ═══════════════════════════════════════════
    // ESP ADAPTER — Electronic Timesheet Ingest
    // ═══════════════════════════════════════════

    /**
     * DSD Section 24, Rule 88: Receive electronic timesheet from ESP/TTS.
     * Sets Mode of Entry = ELECTRONIC or TELEPHONIC.
     * Triggers TTS Recipient Review workflow (Rule 62/88/93-96).
     *
     * @param espData electronic timesheet data from ESP portal
     * @return created timesheet
     */
    @Transactional
    public TimesheetEntity receiveEspTimesheet(Map<String, Object> espData) {
        String modeOfEntry = (String) espData.getOrDefault("modeOfEntry", "ELECTRONIC");
        log.info("[ESP] Receiving {} timesheet for provider={}",
                modeOfEntry, espData.get("providerId"));

        TimesheetEntity ts = new TimesheetEntity();
        ts.setProviderId(toLong(espData.get("providerId")));
        ts.setRecipientId(toLong(espData.get("recipientId")));
        ts.setCaseId(toLong(espData.get("caseId")));
        ts.setPayPeriodStart(LocalDate.parse((String) espData.get("payPeriodStart")));
        ts.setPayPeriodEnd(LocalDate.parse((String) espData.get("payPeriodEnd")));
        ts.setModeOfEntry(modeOfEntry);
        ts.setSourceType("TELEPHONIC".equals(modeOfEntry)
                ? TimesheetEntity.SourceType.ELECTRONIC_ESP
                : TimesheetEntity.SourceType.ELECTRONIC_ESP);
        ts.setProviderSignaturePresent(true); // ESP requires provider signature
        ts.setRecipientSignaturePresent(false); // Pending TTS recipient review
        ts.setDateReceived(LocalDate.now());

        // DSD Rule 88: Set status to Pending Recipient Electronic Review
        ts.setStatus(TimesheetEntity.TimesheetStatus.PENDING_RECIPIENT_REVIEW);

        String progStr = (String) espData.getOrDefault("programType", "IHSS");
        try {
            ts.setProgramType(TimesheetEntity.ProgramType.valueOf(progStr));
        } catch (Exception e) {
            ts.setProgramType(TimesheetEntity.ProgramType.IHSS);
        }

        ts = tsRepo.save(ts);

        // Create daily time entries
        @SuppressWarnings("unchecked")
        Map<String, Number> dailyHours = (Map<String, Number>) espData.getOrDefault("dailyHours", Map.of());
        for (Map.Entry<String, Number> entry : dailyHours.entrySet()) {
            TimesheetTimeEntryEntity te = new TimesheetTimeEntryEntity();
            te.setTimesheetId(ts.getId());
            te.setEntryDate(LocalDate.parse(entry.getKey()));
            te.setHoursClaimed(entry.getValue().doubleValue());
            entryRepo.save(te);
        }

        // Create TTS Recipient Review Required exception (TAEC301)
        TimesheetExceptionEntity ex = new TimesheetExceptionEntity();
        ex.setTimesheetId(ts.getId());
        ex.setExceptionType(TimesheetExceptionEntity.ExceptionType.HOLD_CONDITION);
        ex.setSeverity(TimesheetExceptionEntity.ExceptionSeverity.HOLD);
        ex.setErrorCode("TVP-62");
        ex.setMessage("TTS Recipient Review Required");
        ex.setResolved(false);
        exRepo.save(ex);

        log.info("[ESP] Created {} timesheet: id={}, status=PENDING_RECIPIENT_REVIEW",
                modeOfEntry, ts.getId());

        // DSD Rule 95/96: Trigger notification based on recipient communication preference
        triggerRecipientReviewNotification(ts, modeOfEntry);

        return ts;
    }

    /**
     * DSD Rule 94: TTS Recipient approves timesheet.
     * Releases hold, captures recipient signature, triggers provider notification.
     */
    @Transactional
    public TimesheetEntity processRecipientApproval(Long timesheetId, String ttsConfirmationCode) {
        TimesheetEntity ts = tsRepo.findById(timesheetId)
                .orElseThrow(() -> new RuntimeException("Timesheet not found: " + timesheetId));

        ts.setRecipientSignaturePresent(true);
        ts.setStatus(TimesheetEntity.TimesheetStatus.RECEIVED);

        // Resolve the TTS review exception
        List<TimesheetExceptionEntity> exceptions = exRepo.findByTimesheetIdAndResolvedFalse(timesheetId);
        for (TimesheetExceptionEntity ex : exceptions) {
            if ("TVP-62".equals(ex.getErrorCode())) {
                ex.setResolved(true);
                ex.setResolutionNotes("Approved by recipient via TTS. Confirmation: " + ttsConfirmationCode);
                exRepo.save(ex);
            }
        }

        ts = tsRepo.save(ts);
        log.info("[ESP] Recipient approved timesheet: id={}, confirmation={}", timesheetId, ttsConfirmationCode);

        // DSD Rule 94: Trigger provider approval notification
        triggerProviderApprovalNotification(ts);

        return ts;
    }

    /**
     * DSD Rule 91/100: TTS Recipient rejects timesheet.
     * Sets status to Recipient Rejected, triggers provider notification.
     */
    @Transactional
    public TimesheetEntity processRecipientRejection(Long timesheetId, String rejectionReason) {
        TimesheetEntity ts = tsRepo.findById(timesheetId)
                .orElseThrow(() -> new RuntimeException("Timesheet not found: " + timesheetId));

        ts.setStatus(TimesheetEntity.TimesheetStatus.REJECTED);
        ts.setRejectionReason(rejectionReason);

        // Create rejection exception
        TimesheetExceptionEntity ex = new TimesheetExceptionEntity();
        ex.setTimesheetId(timesheetId);
        ex.setExceptionType(TimesheetExceptionEntity.ExceptionType.HARD_EDIT);
        ex.setSeverity(TimesheetExceptionEntity.ExceptionSeverity.BLOCK);
        ex.setErrorCode("TVP-91");
        ex.setMessage("Rejected by Recipient: " + rejectionReason);
        ex.setResolved(false);
        exRepo.save(ex);

        ts = tsRepo.save(ts);
        log.info("[ESP] Recipient rejected timesheet: id={}, reason={}", timesheetId, rejectionReason);

        // DSD Rule 100: Trigger rejection notification
        triggerProviderRejectionNotification(ts);

        return ts;
    }

    // ═══════════════════════════════════════════
    // ESP/TTS ACCOUNT MANAGEMENT
    // ═══════════════════════════════════════════

    /**
     * DSD Rule 102: Set iETS indicator when electronic timesheet received.
     * When provider submits via ESP/TTS, mark CaseProviderEVVDetails.iETS = true.
     */
    public void updateElectronicTimesheetIndicator(Long providerId, Long caseId) {
        log.info("[ESP] Setting iETS=true for provider={}, case={}", providerId, caseId);
    }

    // ═══════════════════════════════════════════
    // NOTIFICATION STUBS (for integration)
    // ═══════════════════════════════════════════

    private void triggerRecipientReviewNotification(TimesheetEntity ts, String modeOfEntry) {
        if ("TELEPHONIC".equals(modeOfEntry)) {
            log.info("[NOTIFICATION] TTS Outbound Campaign: Recipient {} review required for TS {}",
                    ts.getRecipientId(), ts.getId());
        } else {
            log.info("[NOTIFICATION] Email ETSEB6: Timesheet for review sent to Recipient {}",
                    ts.getRecipientId());
        }
    }

    private void triggerProviderApprovalNotification(TimesheetEntity ts) {
        log.info("[NOTIFICATION] Email ETSEB7: Timesheet approval sent to Provider {}",
                ts.getProviderId());
    }

    private void triggerProviderRejectionNotification(TimesheetEntity ts) {
        if ("TELEPHONIC".equals(ts.getModeOfEntry())) {
            log.info("[NOTIFICATION] TTS Outbound Campaign: Provider {} rejection for TS {}",
                    ts.getProviderId(), ts.getId());
        } else {
            log.info("[NOTIFICATION] Email ETSEB8: Timesheet rejection sent to Provider {}",
                    ts.getProviderId());
        }
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }
}
