package com.cmips.service;

import com.cmips.entity.OverpaymentCollectionEntity;
import com.cmips.entity.OverpaymentEntity;
import com.cmips.entity.OverpaymentEntity.OverpaymentStatus;
import com.cmips.repository.OverpaymentCollectionRepository;
import com.cmips.repository.OverpaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Overpayment Recovery Service (DSD Section 27 CI-67319)
 *
 * Business rules:
 *  - Always set up from the recipient case prospective
 *  - Service period may not exceed 12 consecutive calendar months
 *  - Recovery collection will NOT be deducted from payments for pay periods prior to the overpayment service period
 *  - Hours returned to case as overpaid monies collected (for hours-based overpayments)
 *  - On Submit Recovery: status → PENDING_PAYROLL (Payroll Deduction) or ACTIVE (Personal Payment)
 *  - On Stop Collection: status → STOPPED (only from ACTIVE)
 *  - On Cancel Recovery: status → CANCELLED (from PENDING or PENDING_PAYROLL)
 *  - CLOSED is system-set when balance = $0.00
 */
@Service
public class OverpaymentService {

    private static final Logger log = LoggerFactory.getLogger(OverpaymentService.class);

    private final OverpaymentRepository overpaymentRepo;
    private final OverpaymentCollectionRepository collectionRepo;

    public OverpaymentService(OverpaymentRepository overpaymentRepo,
                              OverpaymentCollectionRepository collectionRepo) {
        this.overpaymentRepo = overpaymentRepo;
        this.collectionRepo = collectionRepo;
    }

    /** Returns PENDING and ACTIVE overpayments for a case (default view) */
    public List<OverpaymentEntity> getActiveByCaseId(Long caseId) {
        return overpaymentRepo.findByCaseIdAndStatusOrderByCreatedAtDesc(caseId, OverpaymentStatus.ACTIVE);
    }

    public List<OverpaymentEntity> getAllByCaseId(Long caseId) {
        return overpaymentRepo.findByCaseIdOrderByCreatedAtDesc(caseId);
    }

    public OverpaymentEntity getById(Long id) {
        return overpaymentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Overpayment not found: " + id));
    }

    @Transactional
    public OverpaymentEntity create(OverpaymentEntity op, String userId) {
        // Validate service period <= 12 months
        if (op.getServicePeriodFrom() != null && op.getServicePeriodTo() != null) {
            long months = java.time.temporal.ChronoUnit.MONTHS.between(
                    op.getServicePeriodFrom(), op.getServicePeriodTo());
            if (months > 12) {
                throw new IllegalArgumentException("Overpayment service period may not exceed 12 consecutive months");
            }
        }
        op.setStatus(OverpaymentStatus.PENDING);
        op.setBalance(op.getRecoveryAmount() != null ? op.getRecoveryAmount() : BigDecimal.ZERO);
        op.setCreatedBy(userId);
        OverpaymentEntity saved = overpaymentRepo.save(op);
        log.info("[Overpayment] Created id={} type={} caseId={} by={}", saved.getId(), saved.getOverpaymentType(), saved.getCaseId(), userId);
        return saved;
    }

    @Transactional
    public OverpaymentEntity update(Long id, OverpaymentEntity updates, String userId) {
        OverpaymentEntity existing = getById(id);
        if (existing.getStatus() != OverpaymentStatus.PENDING &&
                existing.getStatus() != OverpaymentStatus.PENDING_PAYROLL) {
            throw new IllegalStateException("Cannot modify overpayment in status: " + existing.getStatus());
        }
        // Per DSD: modifying Reason, Comments, or service period dates clears all pay period indications
        existing.setReason(updates.getReason());
        existing.setComments(updates.getComments());
        existing.setServicePeriodFrom(updates.getServicePeriodFrom());
        existing.setServicePeriodTo(updates.getServicePeriodTo());
        existing.setRecoveryAmount(updates.getRecoveryAmount());
        existing.setRecoveryMethod(updates.getRecoveryMethod());
        existing.setInstallmentType(updates.getInstallmentType());
        existing.setInstallmentAmount(updates.getInstallmentAmount());
        existing.setBalance(updates.getRecoveryAmount());
        if (existing.getStatus() == OverpaymentStatus.PENDING_PAYROLL) {
            existing.setStatus(OverpaymentStatus.PENDING);
        }
        return overpaymentRepo.save(existing);
    }

    @Transactional
    public OverpaymentEntity submitRecovery(Long id, String userId) {
        OverpaymentEntity op = getById(id);
        if (op.getStatus() != OverpaymentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING overpayments can be submitted");
        }
        if (op.getRecoveryMethod() == OverpaymentEntity.RecoveryMethod.PAYROLL_DEDUCTION) {
            op.setStatus(OverpaymentStatus.PENDING_PAYROLL);
        } else {
            op.setStatus(OverpaymentStatus.ACTIVE);
        }
        op.setSubmittedAt(LocalDateTime.now());
        op.setSubmittedBy(userId);
        log.info("[Overpayment] Submitted id={} status={}", id, op.getStatus());
        return overpaymentRepo.save(op);
    }

    @Transactional
    public OverpaymentEntity cancelRecovery(Long id, String userId) {
        OverpaymentEntity op = getById(id);
        if (op.getStatus() != OverpaymentStatus.PENDING &&
                op.getStatus() != OverpaymentStatus.PENDING_PAYROLL) {
            throw new IllegalStateException("Can only cancel PENDING or PENDING_PAYROLL overpayments");
        }
        op.setStatus(OverpaymentStatus.CANCELLED);
        op.setCancelledAt(LocalDateTime.now());
        op.setCancelledBy(userId);
        log.info("[Overpayment] Cancelled id={}", id);
        return overpaymentRepo.save(op);
    }

    @Transactional
    public OverpaymentEntity stopCollection(Long id, String userId) {
        OverpaymentEntity op = getById(id);
        if (op.getStatus() != OverpaymentStatus.ACTIVE) {
            throw new IllegalStateException("Can only stop collection on ACTIVE overpayments");
        }
        if (op.getBalance() == null || op.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Balance is already $0.00 — overpayment is closed");
        }
        op.setStatus(OverpaymentStatus.STOPPED);
        op.setStoppedAt(LocalDateTime.now());
        op.setStoppedBy(userId);
        log.info("[Overpayment] Stop Collection id={}", id);
        return overpaymentRepo.save(op);
    }

    // ─── Collections (personal payments) ───────────────────────────────────

    public List<OverpaymentCollectionEntity> getCollections(Long overpaymentId) {
        return collectionRepo.findByOverpaymentIdOrderByCollectionDateDesc(overpaymentId);
    }

    @Transactional
    public OverpaymentCollectionEntity addCollection(Long overpaymentId,
                                                      OverpaymentCollectionEntity collection,
                                                      String userId) {
        OverpaymentEntity op = getById(overpaymentId);
        if (op.getStatus() != OverpaymentStatus.ACTIVE &&
                op.getStatus() != OverpaymentStatus.PENDING) {
            throw new IllegalStateException("Cannot record collection for overpayment in status: " + op.getStatus());
        }
        collection.setOverpaymentId(overpaymentId);
        collection.setCreatedBy(userId);
        OverpaymentCollectionEntity saved = collectionRepo.save(collection);

        // Reduce balance
        BigDecimal totalCollected = collectionRepo.sumAmountByOverpaymentId(overpaymentId);
        BigDecimal baseAmount = op.getRecoveryAmount() != null ? op.getRecoveryAmount()
                : (op.getTotalNetOverpayment() != null ? op.getTotalNetOverpayment() : BigDecimal.ZERO);
        BigDecimal newBalance = baseAmount.subtract(totalCollected != null ? totalCollected : BigDecimal.ZERO);
        if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
            op.setBalance(BigDecimal.ZERO);
            op.setStatus(OverpaymentStatus.CLOSED);
            log.info("[Overpayment] Auto-closed id={} fully collected", overpaymentId);
        } else {
            op.setBalance(newBalance);
        }
        overpaymentRepo.save(op);
        return saved;
    }

    public Map<String, Object> toMap(OverpaymentEntity op) {
        return Map.ofEntries(
                Map.entry("id", op.getId()),
                Map.entry("caseId", op.getCaseId()),
                Map.entry("caseNumber", op.getCaseNumber() != null ? op.getCaseNumber() : ""),
                Map.entry("overpaymentType", op.getOverpaymentType()),
                Map.entry("program", op.getProgram()),
                Map.entry("overpaidPayeeType", op.getOverpaidPayeeType() != null ? op.getOverpaidPayeeType() : ""),
                Map.entry("overpaidPayeeId", op.getOverpaidPayeeId() != null ? op.getOverpaidPayeeId() : ""),
                Map.entry("overpaidPayeeName", op.getOverpaidPayeeName() != null ? op.getOverpaidPayeeName() : ""),
                Map.entry("recoveryPayeeId", op.getRecoveryPayeeId() != null ? op.getRecoveryPayeeId() : ""),
                Map.entry("recoveryMethod", op.getRecoveryMethod() != null ? op.getRecoveryMethod() : ""),
                Map.entry("totalNetOverpayment", op.getTotalNetOverpayment() != null ? op.getTotalNetOverpayment() : 0),
                Map.entry("recoveryAmount", op.getRecoveryAmount() != null ? op.getRecoveryAmount() : 0),
                Map.entry("balance", op.getBalance() != null ? op.getBalance() : 0),
                Map.entry("status", op.getStatus()),
                Map.entry("servicePeriodFrom", op.getServicePeriodFrom() != null ? op.getServicePeriodFrom().toString() : ""),
                Map.entry("servicePeriodTo", op.getServicePeriodTo() != null ? op.getServicePeriodTo().toString() : ""),
                Map.entry("reason", op.getReason() != null ? op.getReason() : ""),
                Map.entry("createdBy", op.getCreatedBy() != null ? op.getCreatedBy() : ""),
                Map.entry("createdAt", op.getCreatedAt() != null ? op.getCreatedAt().toString() : "")
        );
    }
}
