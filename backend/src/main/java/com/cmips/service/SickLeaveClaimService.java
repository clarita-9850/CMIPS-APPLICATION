package com.cmips.service;

import com.cmips.dto.SickLeaveLookupRequest;
import com.cmips.dto.SickLeaveLookupResponse;
import com.cmips.dto.SickLeaveSaveRequest;
import com.cmips.entity.CaseEntity;
import com.cmips.entity.ProviderAssignmentEntity;
import com.cmips.entity.ProviderAssignmentEntity.AssignmentStatus;
import com.cmips.entity.ProviderEntity;
import com.cmips.entity.RecipientEntity;
import com.cmips.entity.SickLeaveClaimEntity;
import com.cmips.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sick Leave Claim Service — DSD Section 32, CI-790531/790532/794527-794530
 *
 * Manual Entry flow: entry → timeEntries → list with edit/cancel/view.
 */
@Service
public class SickLeaveClaimService {

    private static final Logger log = LoggerFactory.getLogger(SickLeaveClaimService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ProviderRepository providerRepository;
    private final CaseRepository caseRepository;
    private final RecipientRepository recipientRepository;
    private final ProviderAssignmentRepository assignmentRepository;
    private final SickLeaveClaimRepository claimRepository;

    public SickLeaveClaimService(ProviderRepository providerRepository,
                                  CaseRepository caseRepository,
                                  RecipientRepository recipientRepository,
                                  ProviderAssignmentRepository assignmentRepository,
                                  SickLeaveClaimRepository claimRepository) {
        this.providerRepository = providerRepository;
        this.caseRepository = caseRepository;
        this.recipientRepository = recipientRepository;
        this.assignmentRepository = assignmentRepository;
        this.claimRepository = claimRepository;
    }

    /**
     * CI-790531: Validate provider + case + pay period, return details for Time Entries screen.
     */
    public SickLeaveLookupResponse lookupForEntry(SickLeaveLookupRequest request) {
        if (request.getProviderNumber() == null || request.getProviderNumber().isBlank()) {
            throw new IllegalArgumentException("Provider Number is required.");
        }
        if (request.getCaseNumber() == null || request.getCaseNumber().isBlank()) {
            throw new IllegalArgumentException("Recipient Case Number is required.");
        }
        if (request.getPayPeriodBeginDate() == null || request.getPayPeriodBeginDate().isBlank()) {
            throw new IllegalArgumentException("Pay Period Begin Date is required.");
        }

        String providerNumber = request.getProviderNumber().trim();
        String caseNumber = request.getCaseNumber().trim();
        LocalDate payPeriodDate = parseDate(request.getPayPeriodBeginDate().trim());

        if (payPeriodDate == null) {
            throw new IllegalArgumentException("Pay Period Begin Date is not a valid date.");
        }
        if (payPeriodDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Pay Period Begin Date cannot be in the future.");
        }

        log.info("[SickLeave] Lookup: provider={}, case={}, payPeriod={}",
                providerNumber, caseNumber, payPeriodDate);

        // 1. Find provider
        ProviderEntity provider = providerRepository.findByProviderNumber(providerNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Provider Number '" + providerNumber + "' cannot be found in CMIPS."));

        // 2. Find case
        CaseEntity caseEntity = caseRepository.findByCaseNumber(caseNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Recipient Case Number '" + caseNumber + "' cannot be found in CMIPS."));

        // 3. Verify active assignment
        boolean assigned = assignmentRepository.existsByProviderIdAndCaseIdAndStatus(
                provider.getId(), caseEntity.getId(), AssignmentStatus.ACTIVE);
        if (!assigned) {
            throw new RuntimeException(
                    "Provider '" + providerNumber + "' is not actively assigned to Case '" + caseNumber + "'.");
        }

        // 4. Get provider type from assignment
        String providerType = "IHSS"; // default
        List<ProviderAssignmentEntity> assignments = assignmentRepository.findByProviderIdAndStatus(
                provider.getId(), AssignmentStatus.ACTIVE);
        for (ProviderAssignmentEntity a : assignments) {
            if (a.getCaseId().equals(caseEntity.getId()) && a.getProviderType() != null) {
                providerType = a.getProviderType();
                break;
            }
        }

        // 5. Get recipient name
        String recipientName = null;
        if (caseEntity.getRecipientId() != null) {
            Optional<RecipientEntity> recipientOpt = recipientRepository.findById(caseEntity.getRecipientId());
            if (recipientOpt.isPresent()) {
                RecipientEntity r = recipientOpt.get();
                recipientName = formatName(r.getFirstName(), r.getLastName());
            }
        }

        // 6. Build response
        SickLeaveLookupResponse resp = new SickLeaveLookupResponse();
        resp.setProviderId(provider.getId());
        resp.setProviderNumber(provider.getProviderNumber());
        resp.setProviderName(formatName(provider.getFirstName(), provider.getLastName()));
        resp.setProviderType(providerType);
        resp.setCaseId(caseEntity.getId());
        resp.setCaseNumber(caseEntity.getCaseNumber());
        resp.setRecipientId(caseEntity.getRecipientId());
        resp.setRecipientName(recipientName);
        resp.setServicePeriodFrom(payPeriodDate);

        return resp;
    }

    /**
     * CI-790532: Save new sick leave claim with time entries.
     */
    @Transactional
    public SickLeaveClaimEntity saveClaim(SickLeaveSaveRequest request) {
        if (request.getProviderId() == null) {
            throw new IllegalArgumentException("Provider ID is required.");
        }
        if (request.getCaseId() == null) {
            throw new IllegalArgumentException("Case ID is required.");
        }
        if (request.getPayPeriodBeginDate() == null || request.getPayPeriodBeginDate().isBlank()) {
            throw new IllegalArgumentException("Pay Period Begin Date is required.");
        }
        if (request.getTimeEntries() == null || request.getTimeEntries().isEmpty()) {
            throw new IllegalArgumentException("At least one time entry is required.");
        }

        LocalDate payPeriodDate = LocalDate.parse(request.getPayPeriodBeginDate());

        // Lookup provider and case
        ProviderEntity provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found: " + request.getProviderId()));
        CaseEntity caseEntity = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new RuntimeException("Case not found: " + request.getCaseId()));

        // Get provider type from assignment
        String providerType = "IHSS";
        List<ProviderAssignmentEntity> assignments = assignmentRepository.findByProviderIdAndStatus(
                provider.getId(), AssignmentStatus.ACTIVE);
        for (ProviderAssignmentEntity a : assignments) {
            if (a.getCaseId().equals(caseEntity.getId()) && a.getProviderType() != null) {
                providerType = a.getProviderType();
                break;
            }
        }

        // Get recipient name
        String recipientName = null;
        if (caseEntity.getRecipientId() != null) {
            recipientRepository.findById(caseEntity.getRecipientId())
                    .ifPresent(r -> {});
            Optional<RecipientEntity> rOpt = recipientRepository.findById(caseEntity.getRecipientId());
            if (rOpt.isPresent()) {
                recipientName = formatName(rOpt.get().getFirstName(), rOpt.get().getLastName());
            }
        }

        // Calculate total minutes
        int totalMinutes = request.getTimeEntries().stream()
                .mapToInt(SickLeaveSaveRequest.TimeEntry::getMinutes)
                .sum();

        // Serialize time entries to JSON
        String timeEntriesJson;
        try {
            timeEntriesJson = objectMapper.writeValueAsString(request.getTimeEntries());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize time entries.", e);
        }

        // Generate claim number: SLC-YYYYMMDD-XXXXX
        String claimNumber = generateClaimNumber();

        log.info("[SickLeave] Saving claim: provider={}, case={}, payPeriod={}, totalMinutes={}, claimNumber={}",
                provider.getProviderNumber(), caseEntity.getCaseNumber(), payPeriodDate, totalMinutes, claimNumber);

        SickLeaveClaimEntity claim = new SickLeaveClaimEntity();
        claim.setClaimNumber(claimNumber);
        claim.setProviderId(provider.getId());
        claim.setProviderNumber(provider.getProviderNumber());
        claim.setCaseId(caseEntity.getId());
        claim.setCaseNumber(caseEntity.getCaseNumber());
        claim.setRecipientId(caseEntity.getRecipientId());
        claim.setProviderName(formatName(provider.getFirstName(), provider.getLastName()));
        claim.setRecipientName(recipientName);
        claim.setProviderType(providerType);
        claim.setPayPeriodBeginDate(payPeriodDate);
        claim.setServicePeriodFrom(payPeriodDate);
        claim.setClaimedHours(totalMinutes);
        claim.setTimeEntries(timeEntriesJson);
        claim.setModeOfEntry("MANUAL");
        claim.setClaimEnteredDate(LocalDate.now());
        claim.setStatus("ACTIVE");

        SickLeaveClaimEntity saved = claimRepository.save(claim);

        log.info("[SickLeave] Saved: id={}, claimNumber={}, totalMinutes={}",
                saved.getId(), saved.getClaimNumber(), saved.getClaimedHours());

        return saved;
    }

    /**
     * CI-794527: List all active claims for a provider.
     */
    public List<SickLeaveClaimEntity> listClaimsByProvider(Long providerId) {
        return claimRepository.findByProviderIdOrderByClaimEnteredDateDesc(providerId);
    }

    /**
     * CI-794528: Update time entries for an existing claim (same-day, manual only).
     */
    @Transactional
    public SickLeaveClaimEntity updateClaim(String claimNumber, SickLeaveSaveRequest request) {
        SickLeaveClaimEntity claim = claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new RuntimeException("Claim not found: " + claimNumber));

        // Business rule: only editable if manual and entered today
        if (!"MANUAL".equals(claim.getModeOfEntry())) {
            throw new RuntimeException("Only manually entered claims can be modified.");
        }
        if (!LocalDate.now().equals(claim.getClaimEnteredDate())) {
            throw new RuntimeException("Claims can only be modified on the same day they were entered.");
        }
        if ("CANCELLED".equals(claim.getStatus())) {
            throw new RuntimeException("Cancelled claims cannot be modified.");
        }

        if (request.getTimeEntries() == null || request.getTimeEntries().isEmpty()) {
            throw new IllegalArgumentException("At least one time entry is required.");
        }

        // Recalculate total
        int totalMinutes = request.getTimeEntries().stream()
                .mapToInt(SickLeaveSaveRequest.TimeEntry::getMinutes)
                .sum();

        String timeEntriesJson;
        try {
            timeEntriesJson = objectMapper.writeValueAsString(request.getTimeEntries());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize time entries.", e);
        }

        claim.setClaimedHours(totalMinutes);
        claim.setTimeEntries(timeEntriesJson);

        log.info("[SickLeave] Updated claim: claimNumber={}, newTotalMinutes={}", claimNumber, totalMinutes);

        return claimRepository.save(claim);
    }

    /**
     * CI-794530: Cancel a claim (same-day, manual only).
     */
    @Transactional
    public SickLeaveClaimEntity cancelClaim(String claimNumber) {
        SickLeaveClaimEntity claim = claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new RuntimeException("Claim not found: " + claimNumber));

        if (!"MANUAL".equals(claim.getModeOfEntry())) {
            throw new RuntimeException("Only manually entered claims can be cancelled.");
        }
        if (!LocalDate.now().equals(claim.getClaimEnteredDate())) {
            throw new RuntimeException("Claims can only be cancelled on the same day they were entered.");
        }
        if ("CANCELLED".equals(claim.getStatus())) {
            throw new RuntimeException("Claim is already cancelled.");
        }

        claim.setStatus("CANCELLED");

        log.info("[SickLeave] Cancelled claim: claimNumber={}", claimNumber);

        return claimRepository.save(claim);
    }

    /**
     * CI-794529: Get single claim by claim number (for view/edit).
     */
    public SickLeaveClaimEntity getClaimByNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new RuntimeException("Claim not found: " + claimNumber));
    }

    private String generateClaimNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(10000, 99999);
        return "SLC-" + datePart + "-" + random;
    }

    private LocalDate parseDate(String dateStr) {
        // Try YYYY-MM-DD first
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException ignored) {}
        // Try MM/DD/YYYY
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        } catch (DateTimeParseException ignored) {}
        return null;
    }

    private String formatName(String firstName, String lastName) {
        if (firstName == null && lastName == null) return null;
        String first = firstName != null ? firstName : "";
        String last = lastName != null ? lastName : "";
        return (first + " " + last).trim();
    }
}
