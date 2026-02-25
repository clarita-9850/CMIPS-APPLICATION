package com.cmips.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MEDSService — Simulates the CMDS103C interface to MEDS (Medi-Cal Eligibility Data System).
 *
 * Architecture: CMIPS → CMDS103C → MEDS
 *
 * This is SEPARATE from the SCI real-time interface (CMOO106A).
 *   - SCIService handles OI/EL/OM/OU real-time transactions (CIN search and eligibility lookup).
 *   - MEDSService handles IH-series triggered/batch transactions (application status updates).
 *
 * IH Transactions via CMDS103C:
 *   IH18 – Pending Application notification to MEDS (BR-13, BR-10)
 *   IH12 – Update Client Information in MEDS   (BR-11)
 *   IH34 – Update Application Data in MEDS     (BR-12)
 */
@Service
public class MEDSService {

    private static final Logger log = LoggerFactory.getLogger(MEDSService.class);

    // ============================================================
    // IH18 – Pending Application (BR-13, BR-10)
    // ============================================================

    /**
     * BR-13: Send IH18 Pending Application transaction to MEDS via CMDS103C.
     * Triggered when a case is created with active Medi-Cal during CIN clearance.
     *
     * BR-10: Also triggered after a SAWS S2 response returns a CIN and CIN re-clearance
     *        confirms active Medi-Cal.
     *
     * @param applicationId CMIPS application identifier
     * @param cin           the confirmed CIN
     * @param reason        trigger reason code — "IHSS_APPLICATION" (BR-13) or "S2_CIN_RECLEARANCE" (BR-10)
     * @return confirmation map { transactionType:"IH18", interface:"CMDS103C", status:"SENT", ... }
     */
    public Map<String, Object> sendIH18PendingApplication(String applicationId,
                                                           String cin,
                                                           String reason) {
        log.info("[BR13][IH18] MEDS Pending Application via CMDS103C: application={}, cin={}, reason={}",
                 applicationId, cin, reason);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("transactionType", "IH18");
        result.put("interface",       "CMDS103C");
        result.put("applicationId",   applicationId);
        result.put("cin",             cin);
        result.put("reason",          reason);
        result.put("status",          "SENT");
        result.put("message",         "IH18 Pending Application sent to MEDS");
        result.put("referenceId",     "MEDS-IH18-" + applicationId);
        return result;
    }

    // ============================================================
    // IH12 – Update Client Information (BR-11)
    // ============================================================

    /**
     * BR-11: Send IH12 Update Client Information transaction to MEDS via CMDS103C.
     *
     * Triggered when Name, DOB, or Gender changes on a person record that has an active
     * Medi-Cal CIN. Keeps MEDS demographic data in sync with CMIPS.
     *
     * In the real system this mirrors the SCI OU transaction (BR-14) — both go out when
     * demographics change, but to different systems via different interfaces.
     *
     * @param cin       the person's CIN
     * @param lastName  updated last name
     * @param firstName updated first name
     * @param dob       updated date of birth (yyyy-MM-dd)
     * @param gender    updated gender (M/F/U)
     */
    public Map<String, Object> sendIH12UpdateClientInfo(String cin,
                                                         String lastName, String firstName,
                                                         String dob, String gender) {
        log.info("[BR11][IH12] MEDS Update Client Info via CMDS103C: cin={}, name={} {}, dob={}, gender={}",
                 cin, firstName, lastName, dob, gender);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("transactionType", "IH12");
        result.put("interface",       "CMDS103C");
        result.put("cin",             cin);
        result.put("lastName",        lastName);
        result.put("firstName",       firstName);
        result.put("dob",             dob);
        result.put("gender",          gender);
        result.put("status",          "SENT");
        result.put("message",         "IH12 Update Client Info sent to MEDS");
        return result;
    }

    // ============================================================
    // IH34 – Update Application Data (BR-12)
    // ============================================================

    /**
     * BR-12: Send IH34 Update Application Data transaction to MEDS via CMDS103C.
     *
     * Triggered when a case transitions from PENDING to DENIED (or other status changes
     * that need to be reflected in MEDS for Medi-Cal eligibility tracking).
     *
     * @param applicationId CMIPS application identifier
     * @param cin           the person's CIN
     * @param newStatus     the new application status (e.g. "DENIED", "APPROVED")
     */
    public Map<String, Object> sendIH34UpdateApplicationData(String applicationId,
                                                              String cin,
                                                              String newStatus) {
        log.info("[BR12][IH34] MEDS Update Application Data via CMDS103C: application={}, cin={}, newStatus={}",
                 applicationId, cin, newStatus);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("transactionType", "IH34");
        result.put("interface",       "CMDS103C");
        result.put("applicationId",   applicationId);
        result.put("cin",             cin);
        result.put("newStatus",       newStatus);
        result.put("status",          "SENT");
        result.put("message",         "IH34 Update Application Data sent to MEDS (newStatus=" + newStatus + ")");
        return result;
    }
}
