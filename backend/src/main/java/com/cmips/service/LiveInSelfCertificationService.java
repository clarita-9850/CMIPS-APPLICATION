package com.cmips.service;

import com.cmips.dto.LiveInCertEntryResponse;
import com.cmips.dto.LiveInCertLookupRequest;
import com.cmips.dto.LiveInCertSaveRequest;
import com.cmips.entity.CaseEntity;
import com.cmips.entity.LiveInSelfCertificationEntity;
import com.cmips.entity.ProviderAssignmentEntity.AssignmentStatus;
import com.cmips.entity.ProviderEntity;
import com.cmips.entity.RecipientEntity;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * IRS Live-In Provider Self-Certification Service — DSD Section 32, CI-718021/718023/718024
 *
 * Two-screen flow: Search → Entry.
 * TPF staff records self-certification forms from providers claiming "Live-In Excluded" status.
 */
@Service
public class LiveInSelfCertificationService {

    private static final Logger log = LoggerFactory.getLogger(LiveInSelfCertificationService.class);

    private final ProviderRepository providerRepository;
    private final CaseRepository caseRepository;
    private final RecipientRepository recipientRepository;
    private final ProviderAssignmentRepository assignmentRepository;
    private final LiveInSelfCertificationRepository certRepository;

    public LiveInSelfCertificationService(ProviderRepository providerRepository,
                                           CaseRepository caseRepository,
                                           RecipientRepository recipientRepository,
                                           ProviderAssignmentRepository assignmentRepository,
                                           LiveInSelfCertificationRepository certRepository) {
        this.providerRepository = providerRepository;
        this.caseRepository = caseRepository;
        this.recipientRepository = recipientRepository;
        this.assignmentRepository = assignmentRepository;
        this.certRepository = certRepository;
    }

    /**
     * Search "Continue" (CI-718023): Validate provider + case + assignment, return entry details.
     */
    public LiveInCertEntryResponse lookupProviderCase(LiveInCertLookupRequest request) {
        if (request.getProviderNumber() == null || request.getProviderNumber().isBlank()) {
            throw new IllegalArgumentException("Provider Number is required.");
        }
        if (request.getCaseNumber() == null || request.getCaseNumber().isBlank()) {
            throw new IllegalArgumentException("Case Number is required.");
        }

        String providerNumber = request.getProviderNumber().trim();
        String caseNumber = request.getCaseNumber().trim();

        log.info("[LiveInCert] Lookup: providerNumber={}, caseNumber={}", providerNumber, caseNumber);

        // 1. Find provider
        ProviderEntity provider = providerRepository.findByProviderNumber(providerNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Provider Number '" + providerNumber + "' cannot be found in CMIPS."));

        // 2. Find case
        CaseEntity caseEntity = caseRepository.findByCaseNumber(caseNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Case Number '" + caseNumber + "' cannot be found in CMIPS."));

        // 3. Verify provider is actively assigned to case
        boolean assigned = assignmentRepository.existsByProviderIdAndCaseIdAndStatus(
                provider.getId(), caseEntity.getId(), AssignmentStatus.ACTIVE);
        if (!assigned) {
            throw new RuntimeException(
                    "Provider '" + providerNumber + "' is not assigned to Case '" + caseNumber + "'.");
        }

        // 4. Get recipient name
        String recipientName = null;
        if (caseEntity.getRecipientId() != null) {
            Optional<RecipientEntity> recipientOpt = recipientRepository.findById(caseEntity.getRecipientId());
            if (recipientOpt.isPresent()) {
                RecipientEntity r = recipientOpt.get();
                recipientName = formatName(r.getFirstName(), r.getLastName());
            }
        }

        // 5. Get current certification status (most recent record for this provider+case)
        String currentStatus = null;
        LocalDate currentStatusDate = null;
        Optional<LiveInSelfCertificationEntity> existingCert =
                certRepository.findTopByProviderIdAndCaseIdOrderByStatusDateDesc(provider.getId(), caseEntity.getId());
        if (existingCert.isPresent()) {
            currentStatus = existingCert.get().getCertificationStatus();
            currentStatusDate = existingCert.get().getStatusDate();
        }

        // 6. Build response
        LiveInCertEntryResponse resp = new LiveInCertEntryResponse();
        resp.setProviderId(provider.getId());
        resp.setProviderNumber(provider.getProviderNumber());
        resp.setProviderName(formatName(provider.getFirstName(), provider.getLastName()));
        resp.setProviderCounty(caseEntity.getCountyCode());
        resp.setCaseId(caseEntity.getId());
        resp.setCaseNumber(caseEntity.getCaseNumber());
        resp.setRecipientId(caseEntity.getRecipientId());
        resp.setRecipientName(recipientName);
        resp.setCurrentCertificationStatus(currentStatus);
        resp.setStatusDate(currentStatusDate);

        return resp;
    }

    /**
     * Entry "Save" (CI-718024): Persist certification record.
     */
    @Transactional
    public LiveInSelfCertificationEntity saveCertification(LiveInCertSaveRequest request) {
        if (request.getProviderId() == null) {
            throw new IllegalArgumentException("Provider ID is required.");
        }
        if (request.getCaseId() == null) {
            throw new IllegalArgumentException("Case ID is required.");
        }
        if (request.getCertificationStatus() == null || request.getCertificationStatus().isBlank()) {
            throw new IllegalArgumentException("Self-Certification Status is required.");
        }

        String status = request.getCertificationStatus().trim().toUpperCase();
        if (!"YES".equals(status) && !"NO".equals(status)) {
            throw new IllegalArgumentException("Self-Certification Status must be 'YES' or 'NO'.");
        }

        log.info("[LiveInCert] Saving: providerId={}, caseId={}, status={}",
                request.getProviderId(), request.getCaseId(), status);

        // Lookup provider and case for denormalized fields
        ProviderEntity provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found: " + request.getProviderId()));
        CaseEntity caseEntity = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new RuntimeException("Case not found: " + request.getCaseId()));

        String recipientName = null;
        if (caseEntity.getRecipientId() != null) {
            recipientRepository.findById(caseEntity.getRecipientId())
                    .ifPresent(r -> { /* handled below */ });
            Optional<RecipientEntity> rOpt = recipientRepository.findById(caseEntity.getRecipientId());
            if (rOpt.isPresent()) {
                recipientName = formatName(rOpt.get().getFirstName(), rOpt.get().getLastName());
            }
        }

        // Create new certification record
        LiveInSelfCertificationEntity cert = new LiveInSelfCertificationEntity();
        cert.setProviderId(provider.getId());
        cert.setProviderNumber(provider.getProviderNumber());
        cert.setCaseId(caseEntity.getId());
        cert.setCaseNumber(caseEntity.getCaseNumber());
        cert.setRecipientId(caseEntity.getRecipientId());
        cert.setCertificationStatus(status);
        cert.setStatusDate(LocalDate.now());
        cert.setModeOfEntry("MANUAL");
        cert.setProviderCounty(caseEntity.getCountyCode());
        cert.setProviderName(formatName(provider.getFirstName(), provider.getLastName()));
        cert.setRecipientName(recipientName);

        LiveInSelfCertificationEntity saved = certRepository.save(cert);

        log.info("[LiveInCert] Saved: id={}, provider={}, case={}, status={}, date={}",
                saved.getId(), saved.getProviderNumber(), saved.getCaseNumber(),
                saved.getCertificationStatus(), saved.getStatusDate());

        return saved;
    }

    private String formatName(String firstName, String lastName) {
        if (firstName == null && lastName == null) return null;
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        return (first + " " + last).trim();
    }
}
