package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Payment Service (DSD Section 27 — View Payment Information CI-67324)
 *
 * Provides payment search and view functionality:
 *  - Payment Search by Person (provider or recipient across all cases state-wide)
 *  - Payment Search by Case (all payees for a specific case)
 *  - View Payment Details (full warrant + earnings info + tabs)
 *  - Payment History (all void/reissue/replacement activity for a warrant)
 *  - Void/Stop/Reissue/Replacement requests (CI-67323/67325/67326/67318)
 *  - Cashed Warrant Copy requests (CI-67327)
 *  - Forged Endorsement Affidavit records (CI-67317)
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final WarrantRepository warrantRepo;
    private final VoidReissueRequestRepository voidReissueRepo;
    private final CashedWarrantCopyRequestRepository cashedCopyRepo;
    private final ForgedEndorsementAffidavitRepository affidavitRepo;

    public PaymentService(WarrantRepository warrantRepo,
                          VoidReissueRequestRepository voidReissueRepo,
                          CashedWarrantCopyRequestRepository cashedCopyRepo,
                          ForgedEndorsementAffidavitRepository affidavitRepo) {
        this.warrantRepo = warrantRepo;
        this.voidReissueRepo = voidReissueRepo;
        this.cashedCopyRepo = cashedCopyRepo;
        this.affidavitRepo = affidavitRepo;
    }

    // ─── Payment Search (by Person) ────────────────────────────────────────────
    // Searches payments by provider or recipient ID across all cases state-wide.
    // Default: next 3 service periods + current + previous 3 (Advance/Restaurant only).
    // Can filter by Service Period date range (max 13 months) or Warrant Number.

    public List<Map<String, Object>> searchByPerson(String payeeId,
                                                      String servicePeriodFrom,
                                                      String servicePeriodTo,
                                                      String issueFrom,
                                                      String issueTo,
                                                      String warrantNumber) {
        List<WarrantEntity> warrants;
        if (warrantNumber != null && !warrantNumber.isBlank()) {
            warrants = warrantRepo.findByWarrantNumber(warrantNumber)
                    .map(List::of).orElse(List.of());
        } else if (payeeId != null && !payeeId.isBlank()) {
            if (servicePeriodFrom != null && servicePeriodTo != null) {
                warrants = warrantRepo.findByProviderAndDateRange(
                        payeeId,
                        LocalDate.parse(servicePeriodFrom),
                        LocalDate.parse(servicePeriodTo));
            } else {
                warrants = warrantRepo.findByProviderIdOrderByIssueDateDesc(payeeId);
            }
        } else {
            warrants = List.of();
        }
        return warrants.stream().map(this::toSummaryMap).collect(Collectors.toList());
    }

    // ─── Payment Search (by Case) ──────────────────────────────────────────────

    public List<Map<String, Object>> searchByCase(String caseNumber,
                                                    String servicePeriodFrom,
                                                    String servicePeriodTo,
                                                    String issueFrom,
                                                    String issueTo,
                                                    String payeeName,
                                                    String warrantNumber) {
        List<WarrantEntity> warrants;
        if (warrantNumber != null && !warrantNumber.isBlank()) {
            warrants = warrantRepo.findByWarrantNumber(warrantNumber)
                    .map(List::of).orElse(List.of());
        } else if (caseNumber != null && !caseNumber.isBlank()) {
            if (servicePeriodFrom != null && servicePeriodTo != null) {
                warrants = warrantRepo.findByProviderAndDateRange(
                        caseNumber,
                        LocalDate.parse(servicePeriodFrom),
                        LocalDate.parse(servicePeriodTo));
            } else {
                warrants = warrantRepo.findByCaseNumberOrderByIssueDateDesc(caseNumber);
            }
        } else {
            warrants = List.of();
        }
        return warrants.stream().map(this::toSummaryMap).collect(Collectors.toList());
    }

    // ─── View Payment Details ──────────────────────────────────────────────────

    public Map<String, Object> getPaymentDetails(Long warrantId) {
        WarrantEntity w = warrantRepo.findById(warrantId)
                .orElseThrow(() -> new IllegalArgumentException("Warrant not found: " + warrantId));

        Map<String, Object> detail = new LinkedHashMap<>();
        // Payee section
        detail.put("payeeNumber", w.getProviderId());
        detail.put("payeeName", w.getPayeeName() != null ? w.getPayeeName() : "");
        detail.put("taxRelationship", "Non-Family Provider"); // Mock

        // Case section
        detail.put("caseNumber", w.getCaseNumber());
        detail.put("recipientName", w.getRecipientName() != null ? w.getRecipientName() : "");
        detail.put("county", w.getCountyCode());
        detail.put("districtOffice", w.getCountyCode() + " 01 District Office");

        // Payment section
        detail.put("warrantId", w.getId());
        detail.put("warrantNumber", w.getWarrantNumber());
        detail.put("issueDate", w.getIssueDate() != null ? w.getIssueDate().toString() : "");
        detail.put("paidDate", w.getPaidDate() != null ? w.getPaidDate().toString() : "");
        detail.put("amount", w.getAmount());
        detail.put("status", w.getStatus());
        detail.put("statusDate", w.getStatusDate() != null ? w.getStatusDate().toString() : "");
        detail.put("fundingSource", w.getFundingSource() != null ? w.getFundingSource() : "");
        detail.put("payType", w.getPayType() != null ? w.getPayType() : "");
        detail.put("eft", false); // Mock
        detail.put("payPeriodStart", w.getPayPeriodStart() != null ? w.getPayPeriodStart().toString() : "");
        detail.put("payPeriodEnd", w.getPayPeriodEnd() != null ? w.getPayPeriodEnd().toString() : "");

        // Replacement info
        detail.put("voidReplacementType", w.getVoidReplacementType() != null ? w.getVoidReplacementType() : "");
        detail.put("voidReplacementReason", w.getVoidReplacementReason() != null ? w.getVoidReplacementReason() : "");
        detail.put("replacementDate", w.getReplacementDate() != null ? w.getReplacementDate().toString() : "");

        // Activity (void/reissue history)
        List<Map<String, Object>> voidActivity = voidReissueRepo.findByWarrantIdOrderByCreatedAtDesc(warrantId)
                .stream().map(this::voidToMap).collect(Collectors.toList());
        detail.put("voidReissueActivity", voidActivity);

        // Cashed warrant copies
        List<Map<String, Object>> cashedCopies = cashedCopyRepo.findByWarrantIdOrderByCreatedAtDesc(warrantId)
                .stream().map(this::cashedCopyToMap).collect(Collectors.toList());
        detail.put("cashedWarrantCopies", cashedCopies);

        // Forged endorsement affidavits
        List<Map<String, Object>> affidavits = affidavitRepo.findByWarrantIdOrderByCreatedAtDesc(warrantId)
                .stream().map(this::affidavitToMap).collect(Collectors.toList());
        detail.put("forgedEndorsementAffidavits", affidavits);

        return detail;
    }

    // ─── Void / Stop / Reissue / Replacement ──────────────────────────────────

    @Transactional
    public VoidReissueRequestEntity requestVoidOrReissue(Long warrantId,
                                                          String requestType,
                                                          String voidReason,
                                                          String notes,
                                                          String userId) {
        WarrantEntity w = warrantRepo.findById(warrantId)
                .orElseThrow(() -> new IllegalArgumentException("Warrant not found: " + warrantId));

        VoidReissueRequestEntity req = new VoidReissueRequestEntity();
        req.setWarrantId(warrantId);
        req.setWarrantNumber(w.getWarrantNumber());
        req.setRequestType(VoidReissueRequestEntity.RequestType.valueOf(requestType));
        req.setVoidReason(VoidReissueRequestEntity.VoidReason.valueOf(voidReason));
        req.setNotes(notes);
        req.setStatus(VoidReissueRequestEntity.RequestStatus.PENDING);
        req.setCreatedBy(userId);

        // Update warrant status to PENDING_REPLACEMENT
        w.setStatus(WarrantEntity.WarrantStatus.PENDING_REPLACEMENT);
        w.setVoidReplacementReason(voidReason);
        w.setVoidReplacementType(requestType);
        warrantRepo.save(w);

        VoidReissueRequestEntity saved = voidReissueRepo.save(req);
        log.info("[Payment] Void/Reissue request created id={} warrant={} type={} reason={}", saved.getId(), w.getWarrantNumber(), requestType, voidReason);
        return saved;
    }

    // ─── Cashed Warrant Copy Request ──────────────────────────────────────────

    @Transactional
    public CashedWarrantCopyRequestEntity requestCashedCopy(Long warrantId, String reason, String userId) {
        WarrantEntity w = warrantRepo.findById(warrantId)
                .orElseThrow(() -> new IllegalArgumentException("Warrant not found: " + warrantId));

        CashedWarrantCopyRequestEntity req = new CashedWarrantCopyRequestEntity();
        req.setWarrantId(warrantId);
        req.setWarrantNumber(w.getWarrantNumber());
        req.setRequestDate(LocalDate.now());
        req.setReason(reason);
        req.setStatus(CashedWarrantCopyRequestEntity.RequestStatus.PENDING);
        req.setCreatedBy(userId);

        CashedWarrantCopyRequestEntity saved = cashedCopyRepo.save(req);
        log.info("[Payment] Cashed copy request id={} warrant={}", saved.getId(), w.getWarrantNumber());
        return saved;
    }

    @Transactional
    public CashedWarrantCopyRequestEntity cancelCashedCopy(Long requestId, String userId) {
        CashedWarrantCopyRequestEntity req = cashedCopyRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));
        if (req.getStatus() != CashedWarrantCopyRequestEntity.RequestStatus.PENDING) {
            throw new IllegalStateException("Can only cancel PENDING requests");
        }
        req.setStatus(CashedWarrantCopyRequestEntity.RequestStatus.CANCELLED);
        req.setCancelledAt(LocalDateTime.now());
        req.setCancelledBy(userId);
        return cashedCopyRepo.save(req);
    }

    // ─── Forged Endorsement Affidavit ─────────────────────────────────────────

    @Transactional
    public ForgedEndorsementAffidavitEntity createAffidavit(Long warrantId,
                                                              ForgedEndorsementAffidavitEntity affidavit,
                                                              String userId) {
        WarrantEntity w = warrantRepo.findById(warrantId)
                .orElseThrow(() -> new IllegalArgumentException("Warrant not found: " + warrantId));
        affidavit.setWarrantId(warrantId);
        affidavit.setWarrantNumber(w.getWarrantNumber());
        affidavit.setStatus(ForgedEndorsementAffidavitEntity.AffidavitStatus.ACTIVE);
        affidavit.setCreatedBy(userId);
        ForgedEndorsementAffidavitEntity saved = affidavitRepo.save(affidavit);
        log.info("[Payment] Forged endorsement affidavit id={} warrant={}", saved.getId(), w.getWarrantNumber());
        return saved;
    }

    @Transactional
    public ForgedEndorsementAffidavitEntity updateAffidavit(Long id,
                                                             ForgedEndorsementAffidavitEntity updates,
                                                             String userId) {
        ForgedEndorsementAffidavitEntity existing = affidavitRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Affidavit not found: " + id));
        if (existing.getStatus() == ForgedEndorsementAffidavitEntity.AffidavitStatus.CANCELLED) {
            throw new IllegalStateException("Cannot modify cancelled affidavit");
        }
        existing.setAffidavitSignedDate(updates.getAffidavitSignedDate());
        existing.setSubmittedToScoDate(updates.getSubmittedToScoDate());
        existing.setScoResponseDate(updates.getScoResponseDate());
        existing.setScoResponse(updates.getScoResponse());
        existing.setScoResponseNotes(updates.getScoResponseNotes());
        existing.setNotes(updates.getNotes());
        return affidavitRepo.save(existing);
    }

    @Transactional
    public ForgedEndorsementAffidavitEntity cancelAffidavit(Long id, String userId) {
        ForgedEndorsementAffidavitEntity existing = affidavitRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Affidavit not found: " + id));
        existing.setStatus(ForgedEndorsementAffidavitEntity.AffidavitStatus.CANCELLED);
        existing.setCancelledAt(LocalDateTime.now());
        existing.setCancelledBy(userId);
        return affidavitRepo.save(existing);
    }

    // ─── Helper mappers ────────────────────────────────────────────────────────

    private Map<String, Object> toSummaryMap(WarrantEntity w) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", w.getId());
        m.put("warrantNumber", w.getWarrantNumber());
        m.put("serviceFrom", w.getPayPeriodStart() != null ? w.getPayPeriodStart().toString() : "");
        m.put("issued", w.getIssueDate() != null ? w.getIssueDate().toString() : "");
        m.put("status", w.getStatus());
        m.put("gross", w.getAmount());
        m.put("net", w.getAmount()); // Simplified
        m.put("hours", "00:00");
        m.put("soc", 0);
        m.put("payeeName", w.getPayeeName() != null ? w.getPayeeName() : "");
        m.put("type", w.getPayType() != null ? w.getPayType() : "");
        m.put("fundingSource", w.getFundingSource() != null ? w.getFundingSource() : "");
        m.put("county", w.getCountyCode());
        return m;
    }

    private Map<String, Object> voidToMap(VoidReissueRequestEntity v) {
        return Map.of(
                "id", v.getId(),
                "requestType", v.getRequestType(),
                "voidReason", v.getVoidReason(),
                "status", v.getStatus(),
                "createdBy", v.getCreatedBy() != null ? v.getCreatedBy() : "",
                "createdAt", v.getCreatedAt() != null ? v.getCreatedAt().toString() : "",
                "scoConfirmationDate", v.getScoConfirmationDate() != null ? v.getScoConfirmationDate().toString() : ""
        );
    }

    private Map<String, Object> cashedCopyToMap(CashedWarrantCopyRequestEntity c) {
        return Map.of(
                "id", c.getId(),
                "requestDate", c.getRequestDate() != null ? c.getRequestDate().toString() : "",
                "status", c.getStatus(),
                "reason", c.getReason() != null ? c.getReason() : "",
                "createdBy", c.getCreatedBy() != null ? c.getCreatedBy() : ""
        );
    }

    private Map<String, Object> affidavitToMap(ForgedEndorsementAffidavitEntity a) {
        return Map.of(
                "id", a.getId(),
                "affidavitSignedDate", a.getAffidavitSignedDate() != null ? a.getAffidavitSignedDate().toString() : "",
                "submittedToScoDate", a.getSubmittedToScoDate() != null ? a.getSubmittedToScoDate().toString() : "",
                "scoResponseDate", a.getScoResponseDate() != null ? a.getScoResponseDate().toString() : "",
                "scoResponse", a.getScoResponse() != null ? a.getScoResponse() : "",
                "status", a.getStatus()
        );
    }
}
