package com.cmips.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SAWSService — Simulates the CMSD4XXB interface to SAWS (Statewide Automated Welfare System).
 *
 * Architecture: CMIPS → CMSD4XXB → SAWS  (completely separate from the SCI/MEDS real-time path)
 *
 * This is NOT the SCI interface (CMOO106A). SCI handles OI/EL/OM/OU in real-time.
 * SAWS handles Medi-Cal eligibility determination when no active Medi-Cal is found
 * during CIN clearance.
 *
 * Transactions:
 *   S1 (CMSD4XXB) – IHSS Referral for Medi-Cal Eligibility Determination   (BR-9)
 *   S2 (CMSD4XXB) – SAWS returns CIN after determining eligibility          (BR-10)
 *   S8 (SMDS4XXB) – Notification of IHSS Pending status                    (BR-16)
 *
 * S8 is triggered when the case has active Medi-Cal but aid codes are NOT 10, 20, or 60.
 */
@Service
public class SAWSService {

    private static final Logger log = LoggerFactory.getLogger(SAWSService.class);

    // ============================================================
    // S1 TRANSACTION – IHSS Referral for Medi-Cal Determination (BR-9)
    // ============================================================

    /**
     * BR-9: Send S1 IHSS Referral to SAWS via CMSD4XXB.
     *
     * Called from ApplicationService.saveWithoutCIN() when CIN clearance was performed
     * but no active Medi-Cal was found (or no CIN was selected).
     *
     * In production this sends a fixed-length record to the CMSD4XXB interface file.
     * SAWS processes the referral and sends back an S2 response (asynchronous).
     *
     * @param applicationId CMIPS application identifier
     * @param cin           CIN if one was found during OI search (may be blank)
     * @param lastName      applicant last name (from CONCERNROLENAME)
     * @param firstName     applicant first name
     * @param dob           date of birth (yyyy-MM-dd)
     * @param gender        M/F/U (CMIPS gender mapped to SCI/SAWS code)
     * @param countyCode    county from CONCERNROLE table
     * @return confirmation map { transactionType:"S1", interface:"CMSD4XXB", status:"SENT", ... }
     */
    public Map<String, Object> sendS1Referral(String applicationId, String cin,
                                               String lastName, String firstName,
                                               String dob, String gender,
                                               String countyCode) {
        log.info("[BR9][S1] SAWS IHSS Referral via CMSD4XXB: application={}, cin={}, name={} {}, county={}",
                 applicationId, cin, firstName, lastName, countyCode);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("transactionType", "S1");
        result.put("interface",       "CMSD4XXB");
        result.put("applicationId",   applicationId);
        result.put("countyCode",      countyCode != null ? countyCode : "");
        result.put("cin",             cin != null ? cin : "");
        result.put("status",          "SENT");
        result.put("message",         "S1 IHSS Referral for Medi-Cal Determination sent to SAWS");
        result.put("referenceId",     "SAWS-S1-" + applicationId);
        return result;
    }

    // ============================================================
    // S2 TRANSACTION – SAWS returns CIN after processing S1 (BR-10)
    // ============================================================

    /**
     * BR-10: Simulate SAWS returning a CIN via S2 response.
     *
     * In production this is received asynchronously from SAWS after they complete the
     * Medi-Cal eligibility determination. CMIPS then:
     *  1. Compares the returned CIN to any CIN already on the person record.
     *  2. If the CIN is new (not on the person), auto-updates the CIN.
     *  3. Performs a CIN re-clearance.
     *  4. Sends a MEDS IH18 Pending Application (handled by MEDSService).
     *
     * @return map { transactionType:"S2", assignedCin, status:"CIN_ASSIGNED", ... }
     */
    public Map<String, Object> simulateS2Response(String applicationId) {
        String sawsAssignedCin = generateSawsCin();
        log.info("[BR10][S2] SAWS S2 response simulated: application={}, assignedCin={}",
                 applicationId, sawsAssignedCin);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("transactionType", "S2");
        result.put("interface",       "CMSD4XXB");
        result.put("applicationId",   applicationId);
        result.put("assignedCin",     sawsAssignedCin);
        result.put("status",          "CIN_ASSIGNED");
        result.put("message",         "SAWS assigned CIN via S2 response — CIN re-clearance required");
        return result;
    }

    // ============================================================
    // S8 TRANSACTION – Notification of IHSS Pending Status (BR-16)
    // ============================================================

    /**
     * BR-16: Send S8 Notification to SAWS via SMDS4XXB when:
     *   - Case has active Medi-Cal, AND
     *   - The aid code is NOT one of 10, 20, or 60.
     *
     * Called from ApplicationService.selectCINWithDemographicCheck() after confirming
     * active Medi-Cal with a non-standard aid code.
     *
     * @param applicationId CMIPS application identifier
     * @param cin           the assigned CIN
     * @param aidCode       the Medi-Cal aid code (must not be 10, 20, or 60)
     */
    public Map<String, Object> sendS8Notification(String applicationId, String cin, String aidCode) {
        log.info("[BR16][S8] SAWS S8 Notification via SMDS4XXB: application={}, cin={}, aidCode={}",
                 applicationId, cin, aidCode);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("transactionType", "S8");
        result.put("interface",       "SMDS4XXB");
        result.put("applicationId",   applicationId);
        result.put("cin",             cin);
        result.put("aidCode",         aidCode);
        result.put("status",          "SENT");
        result.put("message",         "S8 IHSS Pending notification sent to SAWS (aidCode=" + aidCode + ")");
        return result;
    }

    // ============================================================
    // Helper
    // ============================================================

    /**
     * Generates a simulated CIN in the same 8-digit + 1-letter format used by SCI
     * (e.g. "99123456Z"). Used only for the simulated S2 response.
     */
    private String generateSawsCin() {
        long digits = (long) (Math.random() * 100_000_000L);
        char letter = (char) ('A' + (int) (Math.random() * 26));
        return String.format("%08d%c", digits, letter);
    }
}
