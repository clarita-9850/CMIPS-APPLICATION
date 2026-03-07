package com.cmips.service;

import com.cmips.dto.SickLeaveLookupRequest;
import com.cmips.dto.SickLeaveLookupResponse;
import com.cmips.dto.SickLeaveSaveRequest;
import com.cmips.entity.CaseEntity;
import com.cmips.entity.ProviderAssignmentEntity;
import com.cmips.entity.ProviderAssignmentEntity.AssignmentStatus;
import com.cmips.entity.ProviderEntity;
import com.cmips.entity.ProviderEntity.ProviderStatus;
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
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
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

        // 3b. DSD SB-3: Provider must be ACTIVE to submit sick leave claims
        if (provider.getProviderStatus() != ProviderEntity.ProviderStatus.ACTIVE) {
            throw new RuntimeException(
                    "Provider '" + providerNumber + "' is not in ACTIVE status and cannot submit sick leave claims.");
        }

        // 3c. DSD SB-3: Provider must have accrued sick leave hours available for this fiscal year
        double accruedBalance = provider.getSickLeaveAccruedHours() != null
                ? provider.getSickLeaveAccruedHours() : 0.0;
        if (accruedBalance <= 0.0) {
            throw new RuntimeException(
                    "Provider '" + providerNumber + "' has no accrued sick leave hours available for this fiscal year.");
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

        // DSD SB-3: Provider must have accrued sick leave hours available
        double accruedHours = provider.getSickLeaveAccruedHours() != null
                ? provider.getSickLeaveAccruedHours() : 0.0;
        if (accruedHours <= 0.0) {
            throw new RuntimeException(
                    "Provider has no accrued sick leave hours available for this fiscal year.");
        }

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

        // DSD SB-3: Claimed hours cannot exceed the provider's accrued sick leave balance
        double claimedHoursDecimal = totalMinutes / 60.0;
        if (claimedHoursDecimal > accruedHours) {
            throw new RuntimeException(String.format(
                    "Claimed hours (%.2f) exceed provider's available sick leave balance (%.2f hours).",
                    claimedHoursDecimal, accruedHours));
        }

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
     * List claims by case ID.
     */
    public List<SickLeaveClaimEntity> listClaimsByCase(Long caseId) {
        return claimRepository.findByCaseIdOrderByClaimEnteredDateDesc(caseId);
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

    // ═══════════════════════════════════════════════════════════════
    // DSD Section 24/32 — PMEC Validation Rules (batch 80/SDINDN)
    // 26 business rules, 19 PMEC error codes
    // ═══════════════════════════════════════════════════════════════

    /**
     * Validate a sick leave claim against all PMEC rules.
     * Returns list of error messages. Empty list = valid.
     */
    public List<String> validateClaimPMEC(SickLeaveClaimEntity claim) {
        List<String> errors = new ArrayList<>();

        // PMEC004: Provider number does not exist
        Optional<ProviderEntity> provOpt = providerRepository.findById(claim.getProviderId());
        if (provOpt.isEmpty()) {
            errors.add("PMEC004: Provider Number does not exist.");
            return errors; // can't continue without provider
        }
        ProviderEntity provider = provOpt.get();

        // PMEC005: Case number does not exist
        Optional<CaseEntity> caseOpt = caseRepository.findById(claim.getCaseId());
        if (caseOpt.isEmpty()) {
            errors.add("PMEC005: Case Number does not exist.");
            return errors;
        }
        CaseEntity caseEntity = caseOpt.get();

        // PMEC003: Provider does not have active provider segment
        boolean hasActiveAssignment = assignmentRepository.existsByProviderIdAndCaseIdAndStatus(
                provider.getId(), caseEntity.getId(), AssignmentStatus.ACTIVE);
        if (!hasActiveAssignment || provider.getProviderStatus() != ProviderStatus.ACTIVE) {
            errors.add("PMEC003: Provider does not have an Active provider segment for the indicated pay period.");
        }

        LocalDate payBegin = claim.getPayPeriodBeginDate();

        // PMEC016: Pay period begin date must be 1st or 16th
        if (payBegin != null) {
            int day = payBegin.getDayOfMonth();
            if (day != 1 && day != 16) {
                errors.add("PMEC016: Service Period From Date must be the 1st or the 16th of a month.");
            }
        }

        // PMEC011: Future pay period
        if (payBegin != null && payBegin.isAfter(LocalDate.now())) {
            errors.add("PMEC011: Sick Leave claim may not be submitted for a future pay period.");
        }

        // Parse time entries
        List<SickLeaveSaveRequest.TimeEntry> entries = parseTimeEntries(claim.getTimeEntries());

        // PMEC019: No entries greater than zero
        int totalMinutes = entries.stream().mapToInt(SickLeaveSaveRequest.TimeEntry::getMinutes).sum();
        if (totalMinutes == 0) {
            errors.add("PMEC019: Entry in at least one time entry field is required.");
        }

        // PMEC020: Negative or invalid values
        for (SickLeaveSaveRequest.TimeEntry e : entries) {
            if (e.getMinutes() < 0) {
                errors.add("PMEC020: Negative or Invalid values not allowed.");
                break;
            }
        }

        // PMEC006: Daily hours > 24
        for (SickLeaveSaveRequest.TimeEntry e : entries) {
            if (e.getMinutes() > 24 * 60) {
                errors.add("PMEC006: Sick Leave Hours may not exceed 24 hours per day on " + e.getDate() + ".");
                break;
            }
        }

        // PMEC012: Hours entered for future date
        for (SickLeaveSaveRequest.TimeEntry e : entries) {
            if (e.getDate() != null) {
                try {
                    LocalDate entryDate = LocalDate.parse(e.getDate());
                    if (entryDate.isAfter(LocalDate.now())) {
                        errors.add("PMEC012: Sick Leave may not be claimed for a future date.");
                        break;
                    }
                } catch (DateTimeParseException ignored) {}
            }
        }

        // PMEC022: Time recorded beyond pay period
        if (payBegin != null) {
            LocalDate payEnd = payBegin.getDayOfMonth() == 1
                    ? payBegin.plusDays(14) : payBegin.with(TemporalAdjusters.lastDayOfMonth());
            for (SickLeaveSaveRequest.TimeEntry e : entries) {
                if (e.getDate() != null && e.getMinutes() > 0) {
                    try {
                        LocalDate entryDate = LocalDate.parse(e.getDate());
                        if (entryDate.isBefore(payBegin) || entryDate.isAfter(payEnd)) {
                            errors.add("PMEC022: Time recorded beyond pay period start date or end date.");
                            break;
                        }
                    } catch (DateTimeParseException ignored) {}
                }
            }
        }

        // Sick leave accrual checks
        double accruedBalance = provider.getSickLeaveAccruedHours() != null
                ? provider.getSickLeaveAccruedHours() : 0.0;

        // PMEC018: No remaining sick leave hours
        if (accruedBalance <= 0.0) {
            errors.add("PMEC018: The provider has no remaining sick leave hours for the fiscal year.");
        }

        // PMEC014: Provider has not met eligibility criteria
        LocalDate eligDate = provider.getSickLeaveEligibleDate();
        if (eligDate == null) {
            errors.add("PMEC014: Provider has not met Sick Leave eligibility criteria.");
        }

        // PMEC013: Provider not eligible until eligibility date
        if (eligDate != null && payBegin != null && payBegin.isBefore(eligDate)) {
            errors.add("PMEC013: Provider not eligible to claim sick leave until " +
                    eligDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) + ".");
        }

        // PMEC008/009/017: Hour increment rules
        if (accruedBalance >= 1.0) {
            // PMEC008: Must claim in at least 1-hour increments when remaining >= 1:00
            for (SickLeaveSaveRequest.TimeEntry e : entries) {
                if (e.getMinutes() > 0 && e.getMinutes() < 60) {
                    errors.add("PMEC008: Sick Leave Hours must be claimed in at least one hour increments when Remaining Hours >= 1:00.");
                    break;
                }
            }
            // PMEC009: Must claim in 30-minute increments when remaining > 1:00
            for (SickLeaveSaveRequest.TimeEntry e : entries) {
                if (e.getMinutes() > 60 && (e.getMinutes() % 30 != 0)) {
                    errors.add("PMEC009: Remaining Sick Leave Hours greater than 1:00 hour must be claimed in 30 minute increments.");
                    break;
                }
            }
        } else if (accruedBalance > 0.0 && accruedBalance < 1.0) {
            // PMEC017: Must claim in 30-minute increments when remaining < 1:00
            if (totalMinutes > 0 && totalMinutes != 30) {
                errors.add("PMEC017: Sick Leave Hours must be claimed in 30 minute increments when Remaining Hours are less than 1:00 hour.");
            }
        }

        // PMEC015: Claimed hours exceed remaining — cutback
        double claimedHoursDecimal = totalMinutes / 60.0;
        if (claimedHoursDecimal > accruedBalance && accruedBalance > 0) {
            // DSD Rule 23: Don't block — cutback and flag
            String fy = getFiscalYearString(payBegin);
            errors.add("PMEC015: Claimed Hours exceed provider's Sick Leave Remaining Hours " +
                    formatHHMM((int)(accruedBalance * 60)) + " for fiscal year " + fy + ". Hours will be cutback.");
        }

        // PMEC007: Provider/Recipient on leave
        if (caseEntity.getCaseStatus() == CaseEntity.CaseStatus.ON_LEAVE) {
            errors.add("PMEC007: Recipient on leave on date Sick Leave is claimed.");
        }

        return errors;
    }

    /**
     * DSD Rule 23: Cutback excess sick leave hours to remaining balance.
     * Creates PRIOR_TO_CUTBACK snapshot status.
     */
    @Transactional
    public SickLeaveClaimEntity cutbackIfNeeded(SickLeaveClaimEntity claim) {
        Optional<ProviderEntity> provOpt = providerRepository.findById(claim.getProviderId());
        if (provOpt.isEmpty()) return claim;
        ProviderEntity provider = provOpt.get();
        double remaining = provider.getSickLeaveAccruedHours() != null ? provider.getSickLeaveAccruedHours() : 0.0;
        int claimedMinutes = claim.getClaimedHours() != null ? claim.getClaimedHours() : 0;
        double claimedHours = claimedMinutes / 60.0;

        if (claimedHours > remaining && remaining > 0) {
            int cutbackMinutes = (int)(remaining * 60);
            log.info("[SickLeave] Cutback: claimNumber={}, claimed={} min, cutback to={} min",
                    claim.getClaimNumber(), claimedMinutes, cutbackMinutes);
            claim.setClaimedHours(cutbackMinutes);
            return claimRepository.save(claim);
        }
        return claim;
    }

    /**
     * DSD Rule 24: Deduct claimed hours from provider's accrued balance.
     */
    @Transactional
    public void deductFromAccrual(SickLeaveClaimEntity claim) {
        providerRepository.findById(claim.getProviderId()).ifPresent(provider -> {
            double remaining = provider.getSickLeaveAccruedHours() != null ? provider.getSickLeaveAccruedHours() : 0.0;
            double claimed = (claim.getClaimedHours() != null ? claim.getClaimedHours() : 0) / 60.0;
            double newBalance = Math.max(0, remaining - claimed);
            provider.setSickLeaveAccruedHours(newBalance);
            providerRepository.save(provider);
            log.info("[SickLeave] Deducted: providerId={}, claimed={} hrs, newBalance={} hrs",
                    claim.getProviderId(), claimed, newBalance);
        });
    }

    /**
     * Generate PRDS108A-compatible sick leave payroll record.
     * DSD Interface: PRDS108A sick leave data to SCO.
     */
    public String generatePayrollRecord(SickLeaveClaimEntity claim) {
        return String.format("%-10s%-30s%010d%010d%08.2f%-8s%-8s%-5s",
                "SLC-PAY",
                claim.getClaimNumber(),
                claim.getProviderId(),
                claim.getCaseId(),
                claim.getClaimedHours() != null ? claim.getClaimedHours() / 60.0 : 0.0,
                claim.getPayPeriodBeginDate() != null
                        ? claim.getPayPeriodBeginDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")) : "",
                claim.getProviderType() != null ? claim.getProviderType() : "IHSS",
                claim.getStatus());
    }

    // ── Helper methods ──

    private List<SickLeaveSaveRequest.TimeEntry> parseTimeEntries(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class, SickLeaveSaveRequest.TimeEntry.class));
        } catch (Exception e) {
            return List.of();
        }
    }

    private String getFiscalYearString(LocalDate date) {
        if (date == null) return "N/A";
        int startYear = date.getMonthValue() >= 7 ? date.getYear() : date.getYear() - 1;
        return startYear + "-" + (startYear + 1);
    }

    private String formatHHMM(int totalMinutes) {
        int h = totalMinutes / 60;
        int m = totalMinutes % 60;
        return String.format("%d:%02d", h, m);
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
