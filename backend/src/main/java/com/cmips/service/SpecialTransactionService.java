package com.cmips.service;

import com.cmips.entity.SpecialTransactionEntity;
import com.cmips.entity.SpecialTransactionEntity.TransactionStatus;
import com.cmips.repository.SpecialTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Special Transaction Service (DSD Section 27 CI-67322)
 *
 * Handles one-time payment/deduction requests for County, CDSS, Vendor, and System sources.
 * Business rules:
 *  - All County/CDSS special transactions require secondary Payroll Approver approval
 *  - Vendor travel claims (Travel Claim, Travel Claim Supplemental) do NOT require secondary approval
 *  - System transactions do NOT require secondary approval
 *  - Can be modified or cancelled up until approval (PENDING_APPROVAL state or before submission)
 *  - Once approved, interfaced to Payroll at end of day for nightly batch
 */
@Service
public class SpecialTransactionService {

    private static final Logger log = LoggerFactory.getLogger(SpecialTransactionService.class);

    private final SpecialTransactionRepository repo;

    public SpecialTransactionService(SpecialTransactionRepository repo) {
        this.repo = repo;
    }

    public List<SpecialTransactionEntity> getByCase(Long caseId) {
        return repo.findByCaseIdOrderByCreatedAtDesc(caseId);
    }

    public SpecialTransactionEntity getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Special transaction not found: " + id));
    }

    @Transactional
    public SpecialTransactionEntity create(SpecialTransactionEntity txn, String userId) {
        txn.setStatus(TransactionStatus.PENDING);
        txn.setCreatedBy(userId);
        SpecialTransactionEntity saved = repo.save(txn);
        log.info("[SpecialTxn] Created id={} type={} caseId={} by={}", saved.getId(), saved.getPayType(), saved.getCaseId(), userId);
        return saved;
    }

    @Transactional
    public SpecialTransactionEntity update(Long id, SpecialTransactionEntity updates, String userId) {
        SpecialTransactionEntity existing = getById(id);
        if (existing.getStatus() != TransactionStatus.PENDING &&
                existing.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Cannot modify a special transaction in status: " + existing.getStatus());
        }
        // Apply updates
        existing.setPayType(updates.getPayType());
        existing.setAmountDollars(updates.getAmountDollars());
        existing.setAmountMinutes(updates.getAmountMinutes());
        existing.setServicePeriodFrom(updates.getServicePeriodFrom());
        existing.setServicePeriodTo(updates.getServicePeriodTo());
        existing.setNotes(updates.getNotes());
        existing.setRateOverride(updates.getRateOverride());
        existing.setBypassHours(updates.getBypassHours());
        existing.setFundingSource(updates.getFundingSource());
        // If was in PENDING_APPROVAL, reset to PENDING since we modified it
        if (existing.getStatus() == TransactionStatus.PENDING_APPROVAL) {
            existing.setStatus(TransactionStatus.PENDING);
        }
        return repo.save(existing);
    }

    @Transactional
    public SpecialTransactionEntity submit(Long id, String userId) {
        SpecialTransactionEntity txn = getById(id);
        if (txn.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING transactions can be submitted");
        }
        // Vendor travel claims and System transactions auto-approve
        boolean requiresApproval = requiresSecondaryApproval(txn);
        if (requiresApproval) {
            txn.setStatus(TransactionStatus.PENDING_APPROVAL);
        } else {
            txn.setStatus(TransactionStatus.APPROVED);
            txn.setApprovedAt(LocalDateTime.now());
            txn.setApprovedBy("SYSTEM");
        }
        txn.setSubmittedAt(LocalDateTime.now());
        txn.setSubmittedBy(userId);
        log.info("[SpecialTxn] Submitted id={} requiresApproval={}", id, requiresApproval);
        return repo.save(txn);
    }

    @Transactional
    public SpecialTransactionEntity approve(Long id, String userId) {
        SpecialTransactionEntity txn = getById(id);
        if (txn.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL transactions can be approved");
        }
        txn.setStatus(TransactionStatus.APPROVED);
        txn.setApprovedAt(LocalDateTime.now());
        txn.setApprovedBy(userId);
        log.info("[SpecialTxn] Approved id={} by={}", id, userId);
        return repo.save(txn);
    }

    @Transactional
    public SpecialTransactionEntity reject(Long id, String reason, String userId) {
        SpecialTransactionEntity txn = getById(id);
        if (txn.getStatus() != TransactionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only PENDING_APPROVAL transactions can be rejected");
        }
        txn.setStatus(TransactionStatus.REJECTED);
        txn.setRejectedAt(LocalDateTime.now());
        txn.setRejectedBy(userId);
        txn.setRejectionReason(reason);
        log.info("[SpecialTxn] Rejected id={} by={}", id, userId);
        return repo.save(txn);
    }

    @Transactional
    public SpecialTransactionEntity cancel(Long id, String userId) {
        SpecialTransactionEntity txn = getById(id);
        if (txn.getStatus() == TransactionStatus.PROCESSED ||
                txn.getStatus() == TransactionStatus.APPROVED) {
            throw new IllegalStateException("Cannot cancel an already APPROVED or PROCESSED transaction");
        }
        txn.setStatus(TransactionStatus.CANCELLED);
        txn.setCancelledAt(LocalDateTime.now());
        txn.setCancelledBy(userId);
        log.info("[SpecialTxn] Cancelled id={} by={}", id, userId);
        return repo.save(txn);
    }

    public List<SpecialTransactionEntity> getPendingApproval() {
        return repo.findByStatusOrderByCreatedAtDesc(TransactionStatus.PENDING_APPROVAL);
    }

    private boolean requiresSecondaryApproval(SpecialTransactionEntity txn) {
        // Vendor travel claims do NOT require secondary approval
        if (txn.getTransactionSource() == SpecialTransactionEntity.TransactionSource.VENDOR) {
            return false;
        }
        // System transactions do NOT require secondary approval
        if (txn.getTransactionSource() == SpecialTransactionEntity.TransactionSource.SYSTEM) {
            return false;
        }
        // All County and CDSS require secondary approval
        return true;
    }

    public Map<String, Object> toMap(SpecialTransactionEntity txn) {
        return Map.ofEntries(
                Map.entry("id", txn.getId()),
                Map.entry("caseId", txn.getCaseId()),
                Map.entry("caseNumber", txn.getCaseNumber() != null ? txn.getCaseNumber() : ""),
                Map.entry("transactionSource", txn.getTransactionSource()),
                Map.entry("payType", txn.getPayType()),
                Map.entry("transactionDirection", txn.getTransactionDirection()),
                Map.entry("payeeType", txn.getPayeeType()),
                Map.entry("payeeId", txn.getPayeeId() != null ? txn.getPayeeId() : ""),
                Map.entry("payeeName", txn.getPayeeName() != null ? txn.getPayeeName() : ""),
                Map.entry("amountType", txn.getAmountType()),
                Map.entry("amountDollars", txn.getAmountDollars() != null ? txn.getAmountDollars() : 0),
                Map.entry("amountMinutes", txn.getAmountMinutes() != null ? txn.getAmountMinutes() : 0),
                Map.entry("servicePeriodFrom", txn.getServicePeriodFrom() != null ? txn.getServicePeriodFrom().toString() : ""),
                Map.entry("servicePeriodTo", txn.getServicePeriodTo() != null ? txn.getServicePeriodTo().toString() : ""),
                Map.entry("status", txn.getStatus()),
                Map.entry("notes", txn.getNotes() != null ? txn.getNotes() : ""),
                Map.entry("createdBy", txn.getCreatedBy() != null ? txn.getCreatedBy() : ""),
                Map.entry("createdAt", txn.getCreatedAt() != null ? txn.getCreatedAt().toString() : ""),
                Map.entry("approvedBy", txn.getApprovedBy() != null ? txn.getApprovedBy() : ""),
                Map.entry("approvedAt", txn.getApprovedAt() != null ? txn.getApprovedAt().toString() : "")
        );
    }
}
