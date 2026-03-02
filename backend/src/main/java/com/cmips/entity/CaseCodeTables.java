package com.cmips.entity;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * DSD Code Table Definitions
 * Per DSD Section 6 - Code Table Definitions
 * All codes from CI-68084, CI-68077, CI-68081, CI-68074, CI-68089
 */
public final class CaseCodeTables {

    private CaseCodeTables() {}

    // ==================== 6.1 Case Status Codes (CI-68084) ====================
    public static final Map<String, String> CASE_STATUS_CODES = new LinkedHashMap<>() {{
        put("CS001", "Pending");
        put("CS002", "Eligible");
        put("CS003", "Presumptive Eligible");
        put("CS004", "Leave");
        put("CS005", "Terminated");
        put("CS006", "Denied");
        put("CS007", "Application Withdrawn");
        put("CS008", "In-Progress");
        put("CS009", "Active");
        put("CS010", "Inactive");
    }};

    // ==================== 6.2 Withdrawal Reason Codes (CI-68077) ====================
    public static final Map<String, String> WITHDRAWAL_REASONS = new LinkedHashMap<>() {{
        put("WO001", "Withdrawal requested by Recipient");
        put("WO002", "Withdrawal Status at Conversion");
    }};

    // Enabled codes only (WO002 is disabled - set at conversion)
    public static final Map<String, String> WITHDRAWAL_REASONS_ENABLED = new LinkedHashMap<>() {{
        put("WO001", "Withdrawal requested by Recipient");
    }};

    // ==================== 6.3 Leave Reason Codes (CI-68081) ====================
    public static final Map<String, String> LEAVE_REASONS = new LinkedHashMap<>() {{
        put("L0001", "Temporarily in Hospital");
        put("L0002", "Temporarily in SNF");
        put("L0003", "Temporarily in ICF");
        put("L0004", "Temporarily in CCF");
        put("L0005", "Temporarily out of State over 6 months");
        put("L0006", "Undervalue disposal of resources");
        put("L0007", "Leave Status at Conversion");
        put("L0008", "Other Facility");
    }};

    // Enabled codes only (L0007 is disabled - set at conversion)
    public static final Map<String, String> LEAVE_REASONS_ENABLED = new LinkedHashMap<>() {{
        put("L0001", "Temporarily in Hospital");
        put("L0002", "Temporarily in SNF");
        put("L0003", "Temporarily in ICF");
        put("L0004", "Temporarily in CCF");
        put("L0005", "Temporarily out of State over 6 months");
        put("L0006", "Undervalue disposal of resources");
        put("L0008", "Other Facility");
    }};

    // ==================== 6.4 Termination Reason Codes (CI-68074) ====================
    public static final Map<String, String> TERMINATION_REASONS = new LinkedHashMap<>() {{
        put("CC501", "No longer in own home");
        put("CC502", "Recipient request");
        put("CC503", "Recipient did not pay IHSS Share of Cost");
        put("CC504", "Out of State longer than 60 days");
        put("CC505", "Moved out of State");
        put("CC506", "Failure to provide needed information");
        put("CC507", "Not returning home from Hospital");
        put("CC508", "Not returning home from CCF");
        put("CC509", "Not returning home from ICF");
        put("CC510", "Not returning home from SNF");
        put("CC511", "Recipient Death");
        put("CC512", "Out of Country longer than 30 days");
        put("CC513", "Whereabouts unknown");
        put("CC514", "Non-cooperation with Medi-Cal");
        put("CC515", "Terminated at Conversion");
        put("CC516", "Suspect SSN");
        put("CC517", "Duplicate SSN");
        put("CC518", "Health Care Certification - Not Received");
        put("CC519", "Non-Compliance - UHV");
        put("CC520", "Health Care Certification - No Need");
        put("CC522", "Enrolled in PACE program");
    }};

    // Enabled codes only (CC515 is disabled - set at conversion)
    public static final Map<String, String> TERMINATION_REASONS_ENABLED = new LinkedHashMap<>() {{
        put("CC501", "No longer in own home");
        put("CC502", "Recipient request");
        put("CC503", "Recipient did not pay IHSS Share of Cost");
        put("CC504", "Out of State longer than 60 days");
        put("CC505", "Moved out of State");
        put("CC506", "Failure to provide needed information");
        put("CC507", "Not returning home from Hospital");
        put("CC508", "Not returning home from CCF");
        put("CC509", "Not returning home from ICF");
        put("CC510", "Not returning home from SNF");
        put("CC511", "Recipient Death");
        put("CC512", "Out of Country longer than 30 days");
        put("CC513", "Whereabouts unknown");
        put("CC514", "Non-cooperation with Medi-Cal");
        put("CC516", "Suspect SSN");
        put("CC517", "Duplicate SSN");
        put("CC518", "Health Care Certification - Not Received");
        put("CC519", "Non-Compliance - UHV");
        put("CC520", "Health Care Certification - No Need");
        put("CC522", "Enrolled in PACE program");
    }};

    // ==================== 6.5 Rescind Reason Codes (CI-68089) ====================
    public static final Map<String, String> RESCIND_REASONS = new LinkedHashMap<>() {{
        put("R0001", "State Hearing Filed before Termination effective");
        put("R0002", "Recipient rescinds request for termination of services");
        put("R0003", "Administrative Error");
        put("R0004", "State Hearing Decision");
        put("R0005", "Medi-Cal Non-Compliance Resolved");
    }};

    // Enabled codes (R0005 is disabled/automated only)
    public static final Map<String, String> RESCIND_REASONS_ENABLED = new LinkedHashMap<>() {{
        put("R0001", "State Hearing Filed before Termination effective");
        put("R0002", "Recipient rescinds request for termination of services");
        put("R0003", "Administrative Error");
        put("R0004", "State Hearing Decision");
    }};

    // ==================== NOA Generation by Rescind Reason ====================
    public static String getNoaForRescindReason(String rescindReasonCode) {
        return switch (rescindReasonCode) {
            case "R0001" -> "SH05 NOA + all NOAs from Eligible/Presumptive Eligible status";
            case "R0002" -> "All NOAs from Eligible/Presumptive Eligible status";
            case "R0003" -> "TR18 NOA";
            case "R0004" -> "All NOAs from Eligible/Presumptive Eligible status";
            case "R0005" -> "TR26 NOA";
            default -> null;
        };
    }

    // ==================== Referral Source Codes (40 values per DSD spec) ====================
    public static final Map<String, String> REFERRAL_SOURCES = new LinkedHashMap<>() {{
        put("SELF", "Self");
        put("PARENT_GUARDIAN", "Parent/Guardian");
        put("LEGAL_GUARDIAN", "Legal Guardian");
        put("SPOUSE_PARTNER", "Spouse/Partner");
        put("SIBLING", "Sibling");
        put("OTHER_RELATIVE", "Other Relative");
        put("FRIEND_NEIGHBOR", "Friend/Neighbor");
        put("DOCTOR_PHYSICIAN", "Doctor/Physician");
        put("HOSPITAL_CLINIC", "Hospital/Clinic");
        put("SOCIAL_WORKER", "Social Worker");
        put("SCHOOL", "School");
        put("PROBATION_PAROLE", "Probation/Parole");
        put("LAW_ENFORCEMENT", "Law Enforcement");
        put("APS", "Adult Protective Services");
        put("CPS", "Child Protective Services");
        put("REGIONAL_CENTER", "Regional Center");
        put("COMMUNITY_ORG", "Community Organization");
        put("FAITH_ORG", "Faith Organization");
        put("MENTAL_HEALTH", "Mental Health Provider");
        put("SUBSTANCE_ABUSE", "Substance Abuse Program");
        put("HOME_HEALTH", "Home Health Agency");
        put("NURSING_HOME", "Nursing Home");
        put("ASSISTED_LIVING", "Assisted Living");
        put("DD_PROGRAM", "Developmental Disability Program");
        put("AREA_AGENCY_AGING", "Area Agency on Aging");
        put("FOOD_BANK", "Food Bank/Pantry");
        put("HOUSING_AUTHORITY", "Housing Authority");
        put("VETERAN_SERVICES", "Veteran Services");
        put("MEDICARE", "Medicare");
        put("MEDI_CAL", "Medi-Cal");
        put("INSURANCE_COMPANY", "Insurance Company");
        put("EMPLOYER", "Employer");
        put("EMPLOYMENT_AGENCY", "Employment Agency");
        put("REHABILITATION", "Rehabilitation Program");
        put("COURT_JUDGE", "Court/Judge");
        put("DEPT_LABOR", "Department of Labor");
        put("DMV", "Department of Motor Vehicles");
        put("OTHER_GOVT_AGENCY", "Other Government Agency");
        put("ANONYMOUS", "Anonymous");
        put("UNKNOWN", "Unknown");
    }};

    // ==================== Meets Residency Requirement ====================
    public static final Map<String, String> RESIDENCY_REQUIREMENT = new LinkedHashMap<>() {{
        put("YES", "Meets Residency Requirement");
        put("NO", "Does Not Meet Residency Requirement");
        put("NON_CA", "Non-California Residence");
        put("WHEREABOUTS_UNKNOWN", "Whereabouts Unknown");
        put("NO_LONGER_IN_HOME", "No Longer in Own Home");
    }};
}
