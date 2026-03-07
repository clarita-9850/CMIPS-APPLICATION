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
 * DSD Section 24 — BVI Queue & Review Service
 * TVP Rules 62, 63, 64, 74.
 * Manages BVI (Blind/Visually Impaired) recipient timesheet review queue.
 */
@Service
public class BviReviewService {

    private static final Logger log = LoggerFactory.getLogger(BviReviewService.class);

    @Autowired private BviReviewRepository bviRepo;
    @Autowired private IhssTimesheetRepository tsRepo;
    @Autowired private TimesheetExceptionRepository exRepo;

    public List<BviReviewEntity> listPendingQueue() {
        return bviRepo.findByStatusOrderByCreatedAtAsc(BviReviewEntity.BviReviewStatus.PENDING_RECIPIENT_REVIEW);
    }

    public List<BviReviewEntity> listPendingByCounty(String countyCode) {
        return bviRepo.findByCountyCodeAndStatusOrderByCreatedAtAsc(
                countyCode, BviReviewEntity.BviReviewStatus.PENDING_RECIPIENT_REVIEW);
    }

    public List<BviReviewEntity> listByRecipient(Long recipientId) {
        return bviRepo.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }

    public Optional<BviReviewEntity> getByReviewNumber(String reviewNumber) {
        return bviRepo.findByReviewNumber(reviewNumber);
    }

    /**
     * Rule 62: Create BVI review record when BVI recipient timesheet enters hold.
     * Called from TimesheetValidationService when BVI hold is triggered.
     */
    @Transactional
    public BviReviewEntity createBviReview(TimesheetEntity ts) {
        BviReviewEntity bvi = new BviReviewEntity();
        bvi.setTimesheetId(ts.getId());
        bvi.setTimesheetNumber(ts.getTimesheetNumber());
        bvi.setRecipientId(ts.getRecipientId());
        bvi.setProviderId(ts.getProviderId());
        bvi.setCaseId(ts.getCaseId());
        bvi.setPayPeriodStart(ts.getPayPeriodStart());
        bvi.setPayPeriodEnd(ts.getPayPeriodEnd());
        bvi.setTotalHoursClaimed(ts.getTotalHoursClaimed());
        bvi.setTtsRegistered(true);
        bvi.setCountyCode(ts.getCountyCode());

        // Rule 74: Early/Late submission flag
        if (ts.getDateReceived() != null && ts.getPayPeriodEnd() != null) {
            bvi.setEarlySubmission(ts.getDateReceived().isBefore(ts.getPayPeriodEnd()));
            long daysAfter = java.time.temporal.ChronoUnit.DAYS.between(ts.getPayPeriodEnd(), ts.getDateReceived());
            bvi.setLateSubmission(daysAfter > 30);
        }

        log.info("[BVI] Created BVI review {} for TS {} (recipient={})",
                bvi.getReviewNumber(), ts.getTimesheetNumber(), ts.getRecipientId());
        return bviRepo.save(bvi);
    }

    /**
     * Rule 63: TTS approves BVI review — release hold and process for payment.
     */
    @Transactional
    public BviReviewEntity approveBviReview(Long id, String confirmationCode) {
        BviReviewEntity bvi = bviRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("BVI Review not found: " + id));
        if (bvi.getStatus() != BviReviewEntity.BviReviewStatus.PENDING_RECIPIENT_REVIEW) {
            throw new RuntimeException("BVI Review " + bvi.getReviewNumber() + " is not pending");
        }

        bvi.setStatus(BviReviewEntity.BviReviewStatus.APPROVED_BY_TTS);
        bvi.setTtsConfirmationDate(LocalDate.now());
        bvi.setTtsConfirmationCode(confirmationCode);

        // Release hold on timesheet
        tsRepo.findById(bvi.getTimesheetId()).ifPresent(ts -> {
            if (ts.getStatus() == TimesheetEntity.TimesheetStatus.HOLD_BVI_REVIEW) {
                ts.setStatus(TimesheetEntity.TimesheetStatus.APPROVED_FOR_PAYROLL);
                ts.setHasHoldCondition(false);
                ts.setHoldReleaseBy("TTS-BVI");
                ts.setHoldReleaseDate(java.time.LocalDateTime.now());
                ts.setRecipientSignaturePresent(true); // TTS confirmation counts as signature
                tsRepo.save(ts);
                log.info("[BVI] Rule 63: Released BVI hold on TS {} — approved by TTS", ts.getTimesheetNumber());
            }
        });

        return bviRepo.save(bvi);
    }

    /**
     * Rule 64: TTS rejects BVI review — create hard edit exception.
     */
    @Transactional
    public BviReviewEntity rejectBviReview(Long id, String rejectionReason) {
        BviReviewEntity bvi = bviRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("BVI Review not found: " + id));
        if (bvi.getStatus() != BviReviewEntity.BviReviewStatus.PENDING_RECIPIENT_REVIEW) {
            throw new RuntimeException("BVI Review " + bvi.getReviewNumber() + " is not pending");
        }

        bvi.setStatus(BviReviewEntity.BviReviewStatus.REJECTED_BY_TTS);
        bvi.setTtsRejectionReason(rejectionReason);

        // Transition timesheet to EXCEPTION with hard edit
        tsRepo.findById(bvi.getTimesheetId()).ifPresent(ts -> {
            ts.setStatus(TimesheetEntity.TimesheetStatus.EXCEPTION);
            ts.setHasHoldCondition(false);
            ts.setHasHardEdit(true);

            TimesheetExceptionEntity ex = new TimesheetExceptionEntity();
            ex.setTimesheetId(ts.getId());
            ex.setExceptionType(TimesheetExceptionEntity.ExceptionType.HARD_EDIT);
            ex.setSeverity(TimesheetExceptionEntity.ExceptionSeverity.BLOCK);
            ex.setRuleNumber(64);
            ex.setErrorCode("20774");
            ex.setMessage("BVI recipient review rejected by TTS: " + rejectionReason
                    + ". Timesheet cannot be processed for payment.");
            exRepo.save(ex);

            tsRepo.save(ts);
            log.info("[BVI] Rule 64: Rejected BVI review for TS {} — hard edit applied", ts.getTimesheetNumber());
        });

        return bviRepo.save(bvi);
    }

    /**
     * Expire overdue BVI reviews (10 business day deadline).
     * Called by batch scheduler.
     */
    @Transactional
    public int expireOverdueReviews() {
        List<BviReviewEntity> expired = bviRepo.findExpiredPendingReviews(LocalDate.now());
        for (BviReviewEntity bvi : expired) {
            bvi.setStatus(BviReviewEntity.BviReviewStatus.EXPIRED);

            // Create hard edit on timesheet
            tsRepo.findById(bvi.getTimesheetId()).ifPresent(ts -> {
                ts.setStatus(TimesheetEntity.TimesheetStatus.EXCEPTION);
                ts.setHasHoldCondition(false);
                ts.setHasHardEdit(true);

                TimesheetExceptionEntity ex = new TimesheetExceptionEntity();
                ex.setTimesheetId(ts.getId());
                ex.setExceptionType(TimesheetExceptionEntity.ExceptionType.HARD_EDIT);
                ex.setSeverity(TimesheetExceptionEntity.ExceptionSeverity.BLOCK);
                ex.setRuleNumber(62);
                ex.setErrorCode("20774");
                ex.setMessage("BVI recipient review expired (10 business day deadline). Timesheet cannot be processed.");
                exRepo.save(ex);

                tsRepo.save(ts);
            });

            bviRepo.save(bvi);
        }
        if (!expired.isEmpty()) {
            log.info("[BVI] Expired {} overdue BVI reviews", expired.size());
        }
        return expired.size();
    }
}
