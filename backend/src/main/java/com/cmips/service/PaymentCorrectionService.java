package com.cmips.service;

import com.cmips.entity.PaymentCorrectionEntity;
import com.cmips.entity.PaymentCorrectionEntity.CorrectionStatus;
import com.cmips.repository.PaymentCorrectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Payment Correction Service (DSD Section 27 CI-67321)
 *
 * Business rules:
 *  - All corrections require secondary Payroll Approver approval
 *  - Time entries entered on a day-by-day basis
 *  - WPCS_RECIPIENT_ON_LEAVE: up to 7 days while recipient on leave
 *  - Can be modified or cancelled up until approved
 *  - Interfaced to Payroll at end of day after approval for nightly batch
 */
@Service
public class PaymentCorrectionService {

    private static final Logger log = LoggerFactory.getLogger(PaymentCorrectionService.class);

    private final PaymentCorrectionRepository repo;

    public PaymentCorrectionService(PaymentCorrectionRepository repo) {
        this.repo = repo;
    }

    public List<PaymentCorrectionEntity> getByCase(Long caseId) {
        return repo.findByCaseIdOrderByCreatedAtDesc(caseId);
    }

    public PaymentCorrectionEntity getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment correction not found: " + id));
    }

    @Transactional
    public PaymentCorrectionEntity create(PaymentCorrectionEntity correction, String userId) {
        correction.setStatus(CorrectionStatus.PENDING);
        correction.setCreatedBy(userId);
        PaymentCorrectionEntity saved = repo.save(correction);
        log.info("[PayCorr] Created id={} type={} caseId={} by={}", saved.getId(), saved.getCorrectionType(), saved.getCaseId(), userId);
        return saved;
    }

    @Transactional
    public PaymentCorrectionEntity update(Long id, PaymentCorrectionEntity updates, String userId) {
        PaymentCorrectionEntity existing = getById(id);
        if (existing.getStatus() != CorrectionStatus.PENDING &&
                existing.getStatus() != CorrectionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Cannot modify correction in status: " + existing.getStatus());
        }
        existing.setHoursCorrectedMinutes(updates.getHoursCorrectedMinutes());
        existing.setDailyTimeEntries(updates.getDailyTimeEntries());
        existing.setNotes(updates.getNotes());
        existing.setPayPeriodStart(updates.getPayPeriodStart());
        existing.setPayPeriodEnd(updates.getPayPeriodEnd());
        if (existing.getStatus() == CorrectionStatus.PENDING_APPROVAL) {
            existing.setStatus(CorrectionStatus.PENDING);
        }
        return repo.save(existing);
    }

    @Transactional
    public PaymentCorrectionEntity submit(Long id, String userId) {
        PaymentCorrectionEntity correction = getById(id);
        if (correction.getStatus() != CorrectionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING corrections can be submitted");
        }
        correction.setStatus(CorrectionStatus.PENDING_APPROVAL);
        correction.setSubmittedAt(LocalDateTime.now());
        correction.setSubmittedBy(userId);
        log.info("[PayCorr] Submitted id={}", id);
        return repo.save(correction);
    }

    @Transactional
    public PaymentCorrectionEntity approve(Long id, String userId) {
        PaymentCorrectionEntity correction = getById(id);
        if (correction.getStatus() != CorrectionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL corrections can be approved");
        }
        correction.setStatus(CorrectionStatus.APPROVED);
        correction.setApprovedAt(LocalDateTime.now());
        correction.setApprovedBy(userId);
        log.info("[PayCorr] Approved id={} by={}", id, userId);
        return repo.save(correction);
    }

    @Transactional
    public PaymentCorrectionEntity reject(Long id, String reason, String userId) {
        PaymentCorrectionEntity correction = getById(id);
        if (correction.getStatus() != CorrectionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL corrections can be rejected");
        }
        correction.setStatus(CorrectionStatus.REJECTED);
        correction.setRejectedAt(LocalDateTime.now());
        correction.setRejectedBy(userId);
        correction.setRejectionReason(reason);
        log.info("[PayCorr] Rejected id={} by={}", id, userId);
        return repo.save(correction);
    }

    @Transactional
    public PaymentCorrectionEntity cancel(Long id, String userId) {
        PaymentCorrectionEntity correction = getById(id);
        if (correction.getStatus() == CorrectionStatus.APPROVED ||
                correction.getStatus() == CorrectionStatus.PROCESSED) {
            throw new IllegalStateException("Cannot cancel APPROVED or PROCESSED correction");
        }
        correction.setStatus(CorrectionStatus.CANCELLED);
        correction.setCancelledAt(LocalDateTime.now());
        correction.setCancelledBy(userId);
        log.info("[PayCorr] Cancelled id={} by={}", id, userId);
        return repo.save(correction);
    }

    public List<PaymentCorrectionEntity> getPendingApproval() {
        return repo.findByStatusOrderByCreatedAtDesc(CorrectionStatus.PENDING_APPROVAL);
    }

    public Map<String, Object> toMap(PaymentCorrectionEntity c) {
        return Map.ofEntries(
                Map.entry("id", c.getId()),
                Map.entry("caseId", c.getCaseId()),
                Map.entry("caseNumber", c.getCaseNumber() != null ? c.getCaseNumber() : ""),
                Map.entry("providerId", c.getProviderId() != null ? c.getProviderId() : ""),
                Map.entry("providerName", c.getProviderName() != null ? c.getProviderName() : ""),
                Map.entry("correctionType", c.getCorrectionType()),
                Map.entry("payPeriodStart", c.getPayPeriodStart() != null ? c.getPayPeriodStart().toString() : ""),
                Map.entry("payPeriodEnd", c.getPayPeriodEnd() != null ? c.getPayPeriodEnd().toString() : ""),
                Map.entry("hoursCorrectedMinutes", c.getHoursCorrectedMinutes() != null ? c.getHoursCorrectedMinutes() : 0),
                Map.entry("status", c.getStatus()),
                Map.entry("notes", c.getNotes() != null ? c.getNotes() : ""),
                Map.entry("createdBy", c.getCreatedBy() != null ? c.getCreatedBy() : ""),
                Map.entry("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : ""),
                Map.entry("approvedBy", c.getApprovedBy() != null ? c.getApprovedBy() : ""),
                Map.entry("approvedAt", c.getApprovedAt() != null ? c.getApprovedAt().toString() : "")
        );
    }
}
