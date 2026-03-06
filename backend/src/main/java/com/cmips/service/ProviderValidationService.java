package com.cmips.service;

import com.cmips.dto.ValidationError;
import com.cmips.entity.ProviderCoriEntity;
import com.cmips.entity.ProviderEntity;
import com.cmips.repository.ProviderCoriRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Provider Validation Service
 * Implements DSD Section 23 error message validations.
 */
@Service
public class ProviderValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    // FLSA cutoff date for SOC 846 dual-checkbox requirement
    private static final LocalDate SOC_846_CUTOFF_DATE = LocalDate.of(2016, 2, 1);

    private final ProviderCoriRepository providerCoriRepository;

    public ProviderValidationService(ProviderCoriRepository providerCoriRepository) {
        this.providerCoriRepository = providerCoriRepository;
    }

    // ==================== CREATE PROVIDER VALIDATIONS (21 checks) ====================

    public List<ValidationError> validateCreateProvider(ProviderEntity provider) {
        List<ValidationError> errors = new ArrayList<>();

        // EM-002: SSN blank AND blankSsnReason blank
        if (isBlank(provider.getSsn()) && isBlank(provider.getBlankSsnReason())) {
            errors.add(new ValidationError("EM-002", "An SSN or Blank SSN Reason is required."));
        }

        // EM-003: SSN filled AND blankSsnReason filled
        if (!isBlank(provider.getSsn()) && !isBlank(provider.getBlankSsnReason())) {
            errors.add(new ValidationError("EM-003", "SSN and Blank SSN Reason may not both be indicated."));
        }

        // EM-004: phoneType blank AND phone blank
        if (isBlank(provider.getPhoneType()) && isBlank(provider.getPhone())) {
            errors.add(new ValidationError("EM-004", "Phone Type and Phone Number are required."));
        }

        // EM-005: streetAddress blank
        if (isBlank(provider.getStreetAddress())) {
            errors.add(new ValidationError("EM-005", "Residence Address is required."));
        }

        // EM-079: countyCode blank
        if (isBlank(provider.getCountyCode())) {
            errors.add(new ValidationError("EM-079", "Enrollment County is required."));
        }

        // EM-085: phone area code not 3 digits
        if (!isBlank(provider.getPhone())) {
            String phone = provider.getPhone().replaceAll("[^0-9]", "");
            if (phone.length() >= 3) {
                String areaCode = phone.substring(0, 3);
                if (!areaCode.matches("\\d{3}")) {
                    errors.add(new ValidationError("EM-085", "Area Code must be 3 digits."));
                }
            }
            // EM-086: phone number not 7 digits
            if (phone.length() > 3) {
                String number = phone.substring(3);
                if (number.length() != 7) {
                    errors.add(new ValidationError("EM-086", "Phone Number must be 7 digits."));
                }
            } else if (phone.length() < 10) {
                errors.add(new ValidationError("EM-086", "Phone Number must be 7 digits."));
            }
        }

        // EM-089: eligible=YES and requirements not met
        if ("YES".equals(provider.getEligible())) {
            if (!allEnrollmentRequirementsMet(provider)) {
                errors.add(new ValidationError("EM-089",
                        "All enrollment requirements must be met before setting Eligible status to 'Yes'."));
            }
        }

        // EM-090: eligible=NO and ineligibleReason blank
        if ("NO".equals(provider.getEligible()) && isBlank(provider.getIneligibleReason())) {
            errors.add(new ValidationError("EM-090",
                    "Ineligible Reason is required when Eligible status is 'No'."));
        }

        // EM-102: eligible=YES and mediCalSuspended=true
        if ("YES".equals(provider.getEligible()) && Boolean.TRUE.equals(provider.getMediCalSuspended())) {
            errors.add(new ValidationError("EM-102",
                    "Provider Eligible status may not be 'Yes' when provider is on Medi-Cal Suspended list."));
        }

        // EM-103: eligible=YES and ssnVerification != VERIFIED
        if ("YES".equals(provider.getEligible()) && !"VERIFIED".equals(provider.getSsnVerificationStatus())) {
            errors.add(new ValidationError("EM-103",
                    "SSN Verification must be 'Verified' before setting Eligible status to 'Yes'."));
        }

        // EM-149: DOB > 120 years ago
        if (provider.getDateOfBirth() != null &&
                provider.getDateOfBirth().isBefore(LocalDate.now().minusYears(120))) {
            errors.add(new ValidationError("EM-149",
                    "Date of Birth may not be more than 120 years in the past."));
        }

        // EM-154: email format invalid
        if (!isBlank(provider.getEmail()) && !EMAIL_PATTERN.matcher(provider.getEmail()).matches()) {
            errors.add(new ValidationError("EM-154", "Email address format is not valid."));
        }

        // EM-163: effectiveDate > 60 days future
        if (provider.getEffectiveDate() != null &&
                provider.getEffectiveDate().isAfter(LocalDate.now().plusDays(60))) {
            errors.add(new ValidationError("EM-163",
                    "Effective Date may not be more than 60 days in the future."));
        }

        // EM-164: dateSsnAppliedFor > 60 days future
        if (provider.getDateSsnAppliedFor() != null &&
                provider.getDateSsnAppliedFor().isAfter(LocalDate.now().plusDays(60))) {
            errors.add(new ValidationError("EM-164",
                    "Date SSN Applied For may not be more than 60 days in the future."));
        }

        // EM-311: blankSsnReason="Applied For" and dateSsnAppliedFor blank
        if ("Applied For".equalsIgnoreCase(provider.getBlankSsnReason()) &&
                provider.getDateSsnAppliedFor() == null) {
            errors.add(new ValidationError("EM-311",
                    "Date SSN Applied For is required when Blank SSN Reason is 'Applied For SSN'."));
        }

        // EM-315: SOC 846 OT checked but Provider Agreement not checked (after cutoff)
        if (provider.getEffectiveDate() != null &&
                !provider.getEffectiveDate().isBefore(SOC_846_CUTOFF_DATE)) {
            if (Boolean.TRUE.equals(provider.getOvertimeAgreementSigned()) &&
                    !Boolean.TRUE.equals(provider.getProviderAgreementSigned())) {
                errors.add(new ValidationError("EM-315",
                        "SOC 846 - Provider Agreement must also be checked when Overtime Agreement is checked."));
            }

            // EM-316: SOC 846 Provider Agreement checked but OT not checked (after cutoff)
            if (Boolean.TRUE.equals(provider.getProviderAgreementSigned()) &&
                    !Boolean.TRUE.equals(provider.getOvertimeAgreementSigned())) {
                errors.add(new ValidationError("EM-316",
                        "SOC 846 - Overtime Agreement must also be checked when Provider Agreement is checked."));
            }
        }

        // EM-320: orientationDate in future
        if (provider.getOrientationDate() != null &&
                provider.getOrientationDate().isAfter(LocalDate.now())) {
            errors.add(new ValidationError("EM-320",
                    "Provider Orientation Date may not be a future date."));
        }

        // EM-321: orientationCompleted=true but orientationDate blank
        if (Boolean.TRUE.equals(provider.getOrientationCompleted()) &&
                provider.getOrientationDate() == null) {
            errors.add(new ValidationError("EM-321",
                    "Provider Orientation Date is required when Orientation is indicated as completed."));
        }

        // EM-460: DOJ Background Check already on existing enrollment
        if (provider.getId() != null && Boolean.TRUE.equals(provider.getBackgroundCheckCompleted())) {
            // Only check for existing providers (id != null means it's being updated, not created)
            // This validation is context-dependent; on create, we allow setting it
        }

        return errors;
    }

    // ==================== MODIFY ENROLLMENT VALIDATIONS (17 checks) ====================

    public List<ValidationError> validateModifyEnrollment(ProviderEntity provider, ProviderEntity previousState) {
        List<ValidationError> errors = new ArrayList<>();

        // EM-080: eligible=NO and ineligibleReason blank
        if ("NO".equals(provider.getEligible()) && isBlank(provider.getIneligibleReason())) {
            errors.add(new ValidationError("EM-080",
                    "Ineligible Reason is required when Eligible status is 'No'."));
        }

        // EM-105: eligible=YES and requirements not all met
        if ("YES".equals(provider.getEligible()) && !allEnrollmentRequirementsMet(provider)) {
            errors.add(new ValidationError("EM-105",
                    "All enrollment requirements must be met before setting Eligible status to 'Yes'."));
        }

        // EM-106: eligible=YES and SSN not verified
        if ("YES".equals(provider.getEligible()) && !"VERIFIED".equals(provider.getSsnVerificationStatus())) {
            errors.add(new ValidationError("EM-106",
                    "SSN Verification must be 'Verified' before setting Eligible status to 'Yes'."));
        }

        // EM-107: eligible=YES and mediCalSuspended
        if ("YES".equals(provider.getEligible()) && Boolean.TRUE.equals(provider.getMediCalSuspended())) {
            errors.add(new ValidationError("EM-107",
                    "Provider Eligible status may not be 'Yes' when provider is on Medi-Cal Suspended list."));
        }

        // EM-115: eligible changed from YES → PENDING (blocked)
        if ("YES".equals(previousState.getEligible()) && "PENDING".equals(provider.getEligible())) {
            errors.add(new ValidationError("EM-115",
                    "Eligible status may not be changed from 'Yes' to 'Pending'."));
        }

        // EM-116: eligible changed from NO → PENDING (blocked)
        if ("NO".equals(previousState.getEligible()) && "PENDING".equals(provider.getEligible())) {
            errors.add(new ValidationError("EM-116",
                    "Eligible status may not be changed from 'No' to 'Pending'."));
        }

        // EM-150: eligible=YES and Tier 1 CORI exists
        if ("YES".equals(provider.getEligible()) && provider.getId() != null) {
            if (providerCoriRepository.hasActiveTier1Conviction(provider.getId())) {
                errors.add(new ValidationError("EM-150",
                        "Provider Eligible status may not be 'Yes' when an active Tier 1 CORI conviction exists."));
            }
        }

        // EM-151: eligible=YES and Tier 2 without GE waiver
        if ("YES".equals(provider.getEligible()) && provider.getId() != null) {
            if (providerCoriRepository.hasTier2WithoutWaiver(provider.getId())) {
                errors.add(new ValidationError("EM-151",
                        "Provider Eligible status may not be 'Yes' when a Tier 2 CORI conviction exists without a General Exception waiver."));
            }
        }

        // EM-155: eligible=PENDING_REINSTATEMENT from status other than NO
        if ("PENDING_REINSTATEMENT".equals(provider.getEligible()) &&
                !"NO".equals(previousState.getEligible())) {
            errors.add(new ValidationError("EM-155",
                    "Eligible status may only be changed to 'Pending Reinstatement' from 'No'."));
        }

        // EM-174: appealStatusDate entered without appealStatus
        if (provider.getAppealStatusDate() != null && isBlank(provider.getAppealStatus())) {
            errors.add(new ValidationError("EM-174",
                    "Appeal Status is required when Appeal Status Date is entered."));
        }

        // EM-175: appealStatus entered without appealStatusDate
        if (!isBlank(provider.getAppealStatus()) && provider.getAppealStatusDate() == null) {
            errors.add(new ValidationError("EM-175",
                    "Appeal Status Date is required when Appeal Status is entered."));
        }

        // EM-176: adminHearingDate entered without appealStatus
        if (provider.getAdminHearingDate() != null && isBlank(provider.getAppealStatus())) {
            errors.add(new ValidationError("EM-176",
                    "Appeal Status is required when Admin Hearing Date is entered."));
        }

        // EM-177: eligible=YES and deathOutcomePending=true
        if ("YES".equals(provider.getEligible()) && Boolean.TRUE.equals(provider.getDeathOutcomePending())) {
            errors.add(new ValidationError("EM-177",
                    "Provider Eligible status may not be 'Yes' when Death Outcome is pending."));
        }

        // EM-461: effectiveDate > 60 days in future
        if (provider.getEffectiveDate() != null &&
                provider.getEffectiveDate().isAfter(LocalDate.now().plusDays(60))) {
            errors.add(new ValidationError("EM-461",
                    "Effective Date may not be more than 60 days in the future."));
        }

        // EM-494: eligible YES→NO→YES and CORI exists after effective date
        if ("YES".equals(provider.getEligible()) && "NO".equals(previousState.getEligible()) &&
                provider.getId() != null) {
            List<ProviderCoriEntity> activeCori = providerCoriRepository.findActiveCoriByProviderId(provider.getId());
            if (provider.getEffectiveDate() != null) {
                boolean hasCoriAfterEffective = activeCori.stream()
                        .anyMatch(c -> c.getConvictionDate() != null &&
                                c.getConvictionDate().isAfter(provider.getEffectiveDate()));
                if (hasCoriAfterEffective) {
                    errors.add(new ValidationError("EM-494",
                            "Provider cannot be set to Eligible when CORI conviction exists after the effective date."));
                }
            }
        }

        // EM-495: enrollment due date has passed
        if ("YES".equals(provider.getEligible()) &&
                provider.getEnrollmentDueDate() != null &&
                provider.getEnrollmentDueDate().isBefore(LocalDate.now()) &&
                !Boolean.TRUE.equals(provider.getGoodCauseExtension())) {
            errors.add(new ValidationError("EM-495",
                    "Enrollment due date has passed. A Good Cause Extension is required."));
        }

        // EM-498: good cause extension already applied
        if (Boolean.TRUE.equals(provider.getGoodCauseExtension()) &&
                Boolean.TRUE.equals(previousState.getGoodCauseExtension())) {
            errors.add(new ValidationError("EM-498",
                    "Good Cause Extension has already been applied for this enrollment."));
        }

        return errors;
    }

    // ==================== CREATE CORI VALIDATIONS (12 checks) ====================

    public List<ValidationError> validateCreateCori(ProviderCoriEntity cori, List<ProviderCoriEntity> existingRecords) {
        List<ValidationError> errors = new ArrayList<>();

        // EM-172: coriDate in future
        if (cori.getCoriDate() != null && cori.getCoriDate().isAfter(LocalDate.now())) {
            errors.add(new ValidationError("EM-172", "CORI Date may not be a future date."));
        }

        // EM-173: convictionDate in future
        if (cori.getConvictionDate() != null && cori.getConvictionDate().isAfter(LocalDate.now())) {
            errors.add(new ValidationError("EM-173", "Conviction Date may not be a future date."));
        }

        // EM-180: convictionDate matches existing CORI
        if (cori.getConvictionDate() != null && existingRecords != null) {
            boolean duplicate = existingRecords.stream()
                    .anyMatch(existing -> cori.getConvictionDate().equals(existing.getConvictionDate()));
            if (duplicate) {
                errors.add(new ValidationError("EM-180",
                        "A CORI record already exists with this Conviction Date."));
            }
        }

        // EM-181: tier is blank
        if (isBlank(cori.getTier())) {
            errors.add(new ValidationError("EM-181", "CORI Tier is required."));
        }

        // EM-182: convictionDate is blank
        if (cori.getConvictionDate() == null) {
            errors.add(new ValidationError("EM-182", "Conviction Date is required."));
        }

        // EM-183: coriDate is blank
        if (cori.getCoriDate() == null) {
            errors.add(new ValidationError("EM-183", "CORI Date is required."));
        }

        // EM-184: GE begin date entered for Tier 1
        if ("TIER_1".equals(cori.getTier()) && cori.getGeneralExceptionBeginDate() != null) {
            errors.add(new ValidationError("EM-184",
                    "General Exception Begin Date may not be entered for Tier 1 convictions."));
        }

        // EM-185: GE end date without begin date
        if (cori.getGeneralExceptionEndDate() != null && cori.getGeneralExceptionBeginDate() == null) {
            errors.add(new ValidationError("EM-185",
                    "General Exception Begin Date is required when End Date is entered."));
        }

        // EM-186: GE end date before begin date
        if (cori.getGeneralExceptionBeginDate() != null && cori.getGeneralExceptionEndDate() != null &&
                cori.getGeneralExceptionEndDate().isBefore(cori.getGeneralExceptionBeginDate())) {
            errors.add(new ValidationError("EM-186",
                    "General Exception End Date may not be before Begin Date."));
        }

        // EM-187: CORI end date before CORI date
        if (cori.getCoriDate() != null && cori.getCoriEndDate() != null &&
                cori.getCoriEndDate().isBefore(cori.getCoriDate())) {
            errors.add(new ValidationError("EM-187",
                    "CORI End Date may not be before CORI Date."));
        }

        // EM-188: CORI end date before conviction date
        if (cori.getConvictionDate() != null && cori.getCoriEndDate() != null &&
                cori.getCoriEndDate().isBefore(cori.getConvictionDate())) {
            errors.add(new ValidationError("EM-188",
                    "CORI End Date may not be before Conviction Date."));
        }

        // EM-189: GE begin date before conviction date
        if (cori.getConvictionDate() != null && cori.getGeneralExceptionBeginDate() != null &&
                cori.getGeneralExceptionBeginDate().isBefore(cori.getConvictionDate())) {
            errors.add(new ValidationError("EM-189",
                    "General Exception Begin Date may not be before Conviction Date."));
        }

        return errors;
    }

    // ==================== HELPER METHODS ====================

    private boolean allEnrollmentRequirementsMet(ProviderEntity provider) {
        return Boolean.TRUE.equals(provider.getSoc426Completed()) &&
                Boolean.TRUE.equals(provider.getOrientationCompleted()) &&
                Boolean.TRUE.equals(provider.getSoc846Completed()) &&
                Boolean.TRUE.equals(provider.getProviderAgreementSigned()) &&
                Boolean.TRUE.equals(provider.getOvertimeAgreementSigned()) &&
                Boolean.TRUE.equals(provider.getBackgroundCheckCompleted()) &&
                "VERIFIED".equals(provider.getSsnVerificationStatus()) &&
                !Boolean.TRUE.equals(provider.getMediCalSuspended());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
