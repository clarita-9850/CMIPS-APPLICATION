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
     * Per DSD pages 245-247, the interface must send:
     *   caseNumber(7), applicationDate(8), lastName(20), firstName(15), middleName(15),
     *   nameSuffix(5), socialSecurityNumber(9), ssnType(1), blankSSNRsn(1), dateOfBirth(20),
     *   gender(1), countyCode(2), districtOfficeCode(2), workerNumber(4),
     *   mailing address (streetAddress1, streetAddress2, city, state, zip)
     *
     * @param caseNumber    CM Case Number (7 chars)
     * @param recipientId   CMIPS recipient identifier
     * @param lastName      recipient last name (max 20)
     * @param firstName     recipient first name (max 15)
     * @param middleName    recipient middle name (max 15, optional)
     * @param nameSuffix    recipient name suffix (max 5, optional)
     * @param ssn           Social Security Number (9 digits)
     * @param ssnType       SSN type code (A=Applied, R=Duplicate Research)
     * @param blankSsnReason blank SSN reason code
     * @param dateOfBirth   DOB (CCYY-MM-DD format)
     * @param gender        M or F
     * @param countyCode    county code (2 chars)
     * @param districtOfficeCode district office (2 chars)
     * @param workerNumber  social worker number (4 chars)
     * @param mailingAddress formatted mailing address string
     */
    public Map<String, Object> sendPR00901A(String caseNumber, String recipientId,
                                             String lastName, String firstName,
                                             String middleName, String nameSuffix,
                                             String ssn, String ssnType, String blankSsnReason,
                                             String dateOfBirth, String gender,
                                             String countyCode, String districtOfficeCode,
                                             String workerNumber, String mailingAddress) {
        log.info("[PR00901A] Create Recipient in Payroll: case={}, recipient={}, name={} {}, county={}, worker={}",
                 caseNumber, recipientId, firstName, lastName, countyCode, workerNumber);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("transactionType", "PR00901A");
        r.put("interface",       "PR00-series");
        r.put("caseNumber",      caseNumber);
        r.put("recipientId",     recipientId);
        r.put("lastName",        lastName);
        r.put("firstName",       firstName);
        r.put("middleName",      middleName);
        r.put("nameSuffix",      nameSuffix);
        r.put("ssn",             ssn != null ? "***-**-" + ssn.substring(Math.max(0, ssn.length() - 4)) : "");
        r.put("ssnType",         ssnType);
        r.put("blankSsnReason",  blankSsnReason);
        r.put("dateOfBirth",     dateOfBirth);
        r.put("gender",          gender);
        r.put("countyCode",      countyCode);
        r.put("districtOfficeCode", districtOfficeCode);
        r.put("workerNumber",    workerNumber);
        r.put("mailingAddress",  mailingAddress);
        r.put("status",          "SENT");
        r.put("message",         "Recipient created in Payroll");
        return r;
    }

    /**
     * PR00901A: Simplified overload for backward compatibility.
     * Uses minimal fields — full-data version preferred for DSD compliance.
     */
    public Map<String, Object> sendPR00901A(String applicationId, String recipientId) {
        log.info("[PR00901A] Create Recipient in Payroll (minimal): application={}, recipient={}",
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
