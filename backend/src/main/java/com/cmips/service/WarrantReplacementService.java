package com.cmips.service;

import com.cmips.dto.WarrantReplacementDetailResponse;
import com.cmips.dto.WarrantReplacementLookupRequest;
import com.cmips.dto.WarrantReplacementSaveRequest;
import com.cmips.entity.WarrantEntity;
import com.cmips.entity.WarrantEntity.WarrantStatus;
import com.cmips.repository.CaseRepository;
import com.cmips.repository.ProviderRepository;
import com.cmips.repository.RecipientRepository;
import com.cmips.repository.WarrantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Enter Warrant Replacement Service — DSD Section 27, CI-459396/459400/459401
 *
 * Three-screen flow: List → Enter → Details (confirm + save).
 * Allows CGI Back Office staff to enter replacement warrant information from
 * the Replacement Warrant Detail Report provided by SCO.
 */
@Service
public class WarrantReplacementService {

    private static final Logger log = LoggerFactory.getLogger(WarrantReplacementService.class);

    private final WarrantRepository warrantRepository;
    private final CaseRepository caseRepository;
    private final RecipientRepository recipientRepository;
    private final ProviderRepository providerRepository;

    public WarrantReplacementService(WarrantRepository warrantRepository,
                                     CaseRepository caseRepository,
                                     RecipientRepository recipientRepository,
                                     ProviderRepository providerRepository) {
        this.warrantRepository = warrantRepository;
        this.caseRepository = caseRepository;
        this.recipientRepository = recipientRepository;
        this.providerRepository = providerRepository;
    }

    /**
     * Screen 1 (CI-459396): List previously entered warrant replacements.
     */
    public List<WarrantEntity> listReplacements() {
        return warrantRepository.findAllWithReplacementEntry();
    }

    /**
     * Screen 2 "Continue" (CI-459400): Lookup warrant by warrantNumber + issueDate.
     * Returns enriched detail response for the Details screen.
     */
    public WarrantReplacementDetailResponse lookupWarrant(WarrantReplacementLookupRequest request) {
        if (request.getWarrantNumber() == null || request.getWarrantNumber().isBlank()) {
            throw new IllegalArgumentException("Warrant Number is required.");
        }
        if (request.getIssueDate() == null) {
            throw new IllegalArgumentException("Issue Date is required.");
        }
        if (request.getReplacementDate() == null) {
            throw new IllegalArgumentException("Replacement Date is required.");
        }

        String warrantNumber = request.getWarrantNumber().trim();
        log.info("[EnterWarrantReplacement] Looking up warrant: number={}, issueDate={}",
                warrantNumber, request.getIssueDate());

        Optional<WarrantEntity> opt = warrantRepository
                .findByWarrantNumberAndIssueDate(warrantNumber, request.getIssueDate());

        if (opt.isEmpty()) {
            throw new RuntimeException("No warrant found matching Warrant Number '"
                    + warrantNumber + "' and Issue Date '" + request.getIssueDate() + "'.");
        }

        return buildDetailResponse(opt.get(), request.getReplacementDate());
    }

    /**
     * Screen 3 "Save" (CI-459401): Save the replacement entry.
     *
     * Business Rules per DSD:
     * 1. Set replacementDate and replacementEntryDate (today) on the Warrant table.
     * 2. If status is PENDING_REPLACEMENT → update status to PAID, statusDate to replacementDate.
     * 3. If VoidReplacementType is blank → set to "Replacement".
     * 4. If VoidReplacementReason is blank → set to "Lost".
     */
    @Transactional
    public WarrantEntity saveReplacement(WarrantReplacementSaveRequest request) {
        if (request.getWarrantId() == null) {
            throw new IllegalArgumentException("Warrant ID is required.");
        }
        if (request.getReplacementDate() == null) {
            throw new IllegalArgumentException("Replacement Date is required.");
        }

        log.info("[EnterWarrantReplacement] Saving replacement: warrantId={}, replacementDate={}",
                request.getWarrantId(), request.getReplacementDate());

        WarrantEntity warrant = warrantRepository.findById(request.getWarrantId())
                .orElseThrow(() -> new RuntimeException("Warrant not found: " + request.getWarrantId()));

        // Set replacement dates
        warrant.setReplacementDate(request.getReplacementDate());
        warrant.setReplacementEntryDate(LocalDate.now());

        // BR: If status is PENDING_REPLACEMENT → update to PAID
        if (warrant.getStatus() == WarrantStatus.PENDING_REPLACEMENT) {
            warrant.setStatus(WarrantStatus.PAID);
            warrant.setStatusDate(request.getReplacementDate());
        }

        // BR: Default voidReplacementType to "Replacement" if blank
        if (warrant.getVoidReplacementType() == null || warrant.getVoidReplacementType().isBlank()) {
            warrant.setVoidReplacementType("Replacement");
        }

        // BR: Default voidReplacementReason to "Lost" if blank
        if (warrant.getVoidReplacementReason() == null || warrant.getVoidReplacementReason().isBlank()) {
            warrant.setVoidReplacementReason("Lost");
        }

        WarrantEntity saved = warrantRepository.save(warrant);
        log.info("[EnterWarrantReplacement] Saved: warrantNumber={}, status={}, replacementDate={}",
                saved.getWarrantNumber(), saved.getStatus(), saved.getReplacementDate());
        return saved;
    }

    /**
     * Build enriched detail response by joining Warrant + Case + Recipient + Provider.
     */
    private WarrantReplacementDetailResponse buildDetailResponse(WarrantEntity warrant, LocalDate replacementDate) {
        WarrantReplacementDetailResponse resp = new WarrantReplacementDetailResponse();

        resp.setWarrantId(warrant.getId());
        resp.setWarrantNumber(warrant.getWarrantNumber());
        resp.setNetAmount(warrant.getAmount());
        resp.setIssueDate(warrant.getIssueDate());
        resp.setFundingSource(warrant.getFundingSource());
        resp.setReplacementDate(replacementDate);
        resp.setPayType(warrant.getPayType());
        resp.setPayPeriodFrom(warrant.getPayPeriodStart());
        resp.setPayPeriodTo(warrant.getPayPeriodEnd());
        resp.setStatus(warrant.getStatus().name());
        resp.setVoidReplacementType(warrant.getVoidReplacementType());
        resp.setVoidReplacementReason(warrant.getVoidReplacementReason());

        // Payee info from Provider
        resp.setPayeeNumber(warrant.getProviderId());
        if (warrant.getPayeeName() != null && !warrant.getPayeeName().isBlank()) {
            resp.setPayeeName(warrant.getPayeeName());
        } else {
            providerRepository.findByProviderNumber(warrant.getProviderId())
                    .ifPresent(p -> resp.setPayeeName(
                            p.getLastName() + ", " + p.getFirstName()
                                    + (p.getMiddleName() != null ? " " + p.getMiddleName().charAt(0) + "." : "")));
        }

        // Case + Recipient info
        resp.setCaseNumber(warrant.getCaseNumber());
        resp.setCounty(warrant.getCountyCode());
        if (warrant.getRecipientName() != null && !warrant.getRecipientName().isBlank()) {
            resp.setRecipientName(warrant.getRecipientName());
        } else {
            caseRepository.findByCaseNumber(warrant.getCaseNumber()).ifPresent(c -> {
                if (c.getCountyCode() != null) {
                    resp.setCounty(c.getCountyCode());
                }
                recipientRepository.findById(c.getRecipientId())
                        .ifPresent(r -> resp.setRecipientName(
                                r.getLastName() + ", " + r.getFirstName()
                                        + (r.getMiddleName() != null ? " " + r.getMiddleName().charAt(0) + "." : "")));
            });
        }

        return resp;
    }
}
