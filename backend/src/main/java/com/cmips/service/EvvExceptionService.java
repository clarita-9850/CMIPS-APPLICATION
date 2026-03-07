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
 * DSD Section 24 — EVV Exception Approval Workflow Service
 * Handles EVV exception submission, review, approval/denial.
 */
@Service
public class EvvExceptionService {

    private static final Logger log = LoggerFactory.getLogger(EvvExceptionService.class);

    @Autowired private EvvExceptionRepository evvRepo;
    @Autowired private IhssTimesheetRepository tsRepo;

    public List<EvvExceptionEntity> listByTimesheet(Long timesheetId) {
        return evvRepo.findByTimesheetIdOrderByServiceDateAsc(timesheetId);
    }

    public List<EvvExceptionEntity> listByProvider(Long providerId) {
        return evvRepo.findByProviderIdOrderByServiceDateDesc(providerId);
    }

    public List<EvvExceptionEntity> listByCase(Long caseId) {
        return evvRepo.findByCaseIdOrderByServiceDateDesc(caseId);
    }

    public List<EvvExceptionEntity> listPendingByCounty(String countyCode) {
        return evvRepo.findByCountyCodeAndStatusOrderByCreatedAtAsc(
                countyCode, EvvExceptionEntity.EvvExceptionStatus.PENDING_REVIEW);
    }

    public List<EvvExceptionEntity> listAllPending() {
        return evvRepo.findByStatusOrderByCreatedAtAsc(EvvExceptionEntity.EvvExceptionStatus.PENDING_REVIEW);
    }

    public Optional<EvvExceptionEntity> getByExceptionNumber(String exceptionNumber) {
        return evvRepo.findByExceptionNumber(exceptionNumber);
    }

    @Transactional
    public EvvExceptionEntity submitException(Map<String, Object> request) {
        EvvExceptionEntity evv = new EvvExceptionEntity();
        evv.setTimesheetId(toLong(request.get("timesheetId")));
        evv.setProviderId(toLong(request.get("providerId")));
        evv.setRecipientId(toLong(request.get("recipientId")));
        evv.setCaseId(toLong(request.get("caseId")));
        evv.setServiceDate(LocalDate.parse((String) request.get("serviceDate")));
        evv.setHoursClaimed(toDouble(request.get("hoursClaimed")));
        evv.setEvvHoursRecorded(toDouble(request.get("evvHoursRecorded")));

        if (evv.getHoursClaimed() != null && evv.getEvvHoursRecorded() != null) {
            evv.setHoursDiscrepancy(evv.getHoursClaimed() - evv.getEvvHoursRecorded());
        }

        String reasonStr = (String) request.get("exceptionReason");
        if (reasonStr != null) {
            evv.setExceptionReason(EvvExceptionEntity.EvvExceptionReason.valueOf(reasonStr));
        }
        evv.setReasonDescription((String) request.get("reasonDescription"));
        evv.setCountyCode((String) request.get("countyCode"));
        evv.setSubmittedDate(LocalDate.now());
        evv.setStatus(EvvExceptionEntity.EvvExceptionStatus.PENDING_REVIEW);

        log.info("[EVV-EXC] Submitted EVV exception for provider={}, case={}, date={}",
                evv.getProviderId(), evv.getCaseId(), evv.getServiceDate());
        return evvRepo.save(evv);
    }

    @Transactional
    public EvvExceptionEntity approveException(Long id, String reviewedBy, String reviewNotes) {
        EvvExceptionEntity evv = evvRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("EVV Exception not found: " + id));
        if (evv.getStatus() != EvvExceptionEntity.EvvExceptionStatus.PENDING_REVIEW) {
            throw new RuntimeException("EVV Exception " + evv.getExceptionNumber() + " is not pending review");
        }
        evv.setStatus(EvvExceptionEntity.EvvExceptionStatus.APPROVED);
        evv.setReviewedDate(LocalDate.now());
        evv.setReviewedBy(reviewedBy);
        evv.setReviewNotes(reviewNotes);

        // If linked to timesheet, release the hold
        if (evv.getTimesheetId() != null) {
            tsRepo.findById(evv.getTimesheetId()).ifPresent(ts -> {
                if (ts.getStatus() == TimesheetEntity.TimesheetStatus.HOLD_USER_REVIEW) {
                    ts.setStatus(TimesheetEntity.TimesheetStatus.APPROVED_FOR_PAYROLL);
                    ts.setHasHoldCondition(false);
                    ts.setHoldReleaseBy(reviewedBy);
                    ts.setHoldReleaseDate(java.time.LocalDateTime.now());
                    tsRepo.save(ts);
                    log.info("[EVV-EXC] Released hold on timesheet {} after EVV approval", ts.getTimesheetNumber());
                }
            });
        }

        log.info("[EVV-EXC] Approved EVV exception {}", evv.getExceptionNumber());
        return evvRepo.save(evv);
    }

    @Transactional
    public EvvExceptionEntity denyException(Long id, String reviewedBy, String denialReason) {
        EvvExceptionEntity evv = evvRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("EVV Exception not found: " + id));
        if (evv.getStatus() != EvvExceptionEntity.EvvExceptionStatus.PENDING_REVIEW) {
            throw new RuntimeException("EVV Exception " + evv.getExceptionNumber() + " is not pending review");
        }
        evv.setStatus(EvvExceptionEntity.EvvExceptionStatus.DENIED);
        evv.setReviewedDate(LocalDate.now());
        evv.setReviewedBy(reviewedBy);
        evv.setDenialReason(denialReason);

        log.info("[EVV-EXC] Denied EVV exception {}: {}", evv.getExceptionNumber(), denialReason);
        return evvRepo.save(evv);
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private Double toDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).doubleValue();
        return Double.parseDouble(val.toString());
    }
}
