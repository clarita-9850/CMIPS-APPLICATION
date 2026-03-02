package com.cmips.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PayrollIntegrationService — Simulates the PR00-series internal payroll interfaces.
 *
 * Architecture: CMIPS → PR00 series → CMIPS Payroll System (internal)
 *
 * These are NOT external mainframe interfaces — they are internal CMIPS-to-Payroll
 * transactions triggered by case management events.
 *
 * Transactions:
 *   PR00901A – Create Recipient in Payroll System          (triggered on case Save)
 *   PR00922A – Update Person Request                       (triggered on Modify Person)
 *   PR00923A – Update Person SSN                           (triggered on Modify Alternative ID for SSN)
 *   PR00924A – Update Person Address                       (triggered on address create/modify)
 *   PR00925A – Update Recipient Worker Number              (triggered on Change from Case Home)
 *
 * Note: PR00901A trigger is in CaseService (on case creation).
 *       PR00922A–925A triggers are in the person/address modification flows.
 */
@Service
public class PayrollIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(PayrollIntegrationService.class);

    // ============================================================
    // PR00901A – Create Recipient in Payroll
    // ============================================================

    /**
     * PR00901A: Create a new Recipient record in the CMIPS Payroll System.
     * Triggered on Save of Create Case when a new recipient needs a payroll identity.
     *
     * @param applicationId CMIPS application identifier
     * @param recipientId   CMIPS recipient identifier
     */
    public Map<String, Object> sendPR00901A(String applicationId, String recipientId) {
        log.info("[PR00901A] Create Recipient in Payroll: application={}, recipient={}",
                 applicationId, recipientId);
        return buildResponse("PR00901A", recipientId, "Recipient created in Payroll");
    }

    // ============================================================
    // PR00922A – Update Person Request
    // ============================================================

    /**
     * PR00922A: Update Person Request in the Payroll System.
     * Triggered when a person record is modified (name, DOB, gender changes).
     *
     * @param recipientId CMIPS recipient identifier
     */
    public Map<String, Object> sendPR00922A(String recipientId) {
        log.info("[PR00922A] Update Person Request in Payroll: recipient={}", recipientId);
        return buildResponse("PR00922A", recipientId, "Person request updated in Payroll");
    }

    // ============================================================
    // PR00923A – Update Person SSN
    // ============================================================

    /**
     * PR00923A: Update Person SSN in the Payroll System.
     * Triggered when the SSN Alternative ID is modified on a person record.
     *
     * @param recipientId CMIPS recipient identifier
     * @param ssn         new SSN (masked for logging)
     */
    public Map<String, Object> sendPR00923A(String recipientId, String ssn) {
        log.info("[PR00923A] Update Person SSN in Payroll: recipient={}, ssn=***-**-****",
                 recipientId);
        return buildResponse("PR00923A", recipientId, "Person SSN updated in Payroll");
    }

    // ============================================================
    // PR00924A – Update Person Address
    // ============================================================

    /**
     * PR00924A: Update Person Address in the Payroll System.
     * Triggered on address creation or modification for a recipient.
     *
     * @param recipientId CMIPS recipient identifier
     * @param address     the new/updated address (street, city, state, zip)
     */
    public Map<String, Object> sendPR00924A(String recipientId, String address) {
        log.info("[PR00924A] Update Person Address in Payroll: recipient={}, address={}",
                 recipientId, address);
        return buildResponse("PR00924A", recipientId, "Person address updated in Payroll");
    }

    // ============================================================
    // PR00925A – Update Recipient Worker Number
    // ============================================================

    /**
     * PR00925A: Update Recipient Worker Number in the Payroll System.
     * Triggered when a recipient's assigned worker changes ("Change from Case Home").
     *
     * @param recipientId  CMIPS recipient identifier
     * @param workerNumber new worker identifier
     */
    public Map<String, Object> sendPR00925A(String recipientId, String workerNumber) {
        log.info("[PR00925A] Update Recipient Worker Number in Payroll: recipient={}, worker={}",
                 recipientId, workerNumber);
        return buildResponse("PR00925A", recipientId, "Recipient worker number updated in Payroll");
    }

    // ============================================================
    // Helper
    // ============================================================

    private Map<String, Object> buildResponse(String txType, String recipientId, String message) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("transactionType", txType);
        r.put("interface",       "PR00-series");
        r.put("recipientId",     recipientId);
        r.put("status",          "SENT");
        r.put("message",         message);
        return r;
    }
}
