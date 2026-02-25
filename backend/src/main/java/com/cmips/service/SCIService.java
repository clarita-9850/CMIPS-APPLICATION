package com.cmips.service;

import com.cmips.repository.ApplicationRepository;
import com.cmips.repository.RecipientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * SCIService — Simulates the CMOO106A SCI Real-Time Interface.
 *
 * Real-world architecture:
 *   CMIPS → CMOO106A Interface File → SCI → MEDS
 *
 * All four transaction types flow through the single CMOO106A interface,
 * differentiated by the TRANSACTION_TYPE field in the fixed-length record:
 *
 *   OI – Inquiry (CIN Search)                           CI-116395
 *   EL – Select with error check (Medi-Cal Eligibility request)
 *   OM – MEDS Eligibility Return (response to EL)
 *   OU – Update (demographic updates sent back to SCI)
 *
 * NOT handled here (separate interfaces):
 *   S1/S2/S8 → SAWSService (CMSD4XXB / SMDS4XXB)
 *   IH18/IH12/IH34 → MEDSService (CMDS103C)
 *   PR00 series → PayrollIntegrationService (internal)
 *
 * ── Return Codes ──────────────────────────────────────────────────────────
 * OI:  000=matches returned   100=no match   811=missing name   600/900/999=system error
 * EL:  300=success/data found  305=no eligibility data (EL/OM transaction not successful)
 * OM:  000=active MC data   100=no match   601=commarea   610=XREF not found
 *      612=SCIMEDS progID  614=MEDS sysID  616=linking SCIMEDS  620=CINXREF read
 *      630=MEDS record not found   640=MEDS read error
 * OU:  300=success   107=minor consent SSN   707/H07=app type   801=CIN invalid
 *      806=alien number   810=sex code invalid
 *
 * ── Active Medi-Cal Determination ─────────────────────────────────────────
 * Active = NOT (medsEligibilityCode.charAt(0) == '9' AND medsEligibilityCode.charAt(2) == '9')
 *
 * ── Business Rules ─────────────────────────────────────────────────────────
 *   BR-32 – CIN exists on case: send CIN + demographics + SSN (unless mediCalPseudo)
 *   BR-33 – No CIN or SSN: send demographics only; SEARCH_LEVEL = 'W' (Wide Address Search)
 *   BR-14 – OU transaction when Name/DOB/Gender modified on person record
 */
@Service
public class SCIService {

    private static final Logger log = LoggerFactory.getLogger(SCIService.class);

    // CMOO106A fixed envelope fields
    private static final String VERSION          = "01";
    private static final String APPLICATION_TYPE = "IHSS";
    private static final String SEARCH_LEVEL     = "W";   // Wide Address Search
    private static final String SEARCH_GROUP_01  = "01";  // Records 1-25

    private final ApplicationRepository applicationRepository;
    private final RecipientRepository   recipientRepository;

    // ── OI search results (mock SCI records, per CONCERNROLEALTERNATEID) ──────

    private static final List<Map<String, Object>> MOCK_SCI_RECORDS = List.of(
        buildRecord("12345678A", "***-**-1234", "Smith",    "John",     "",   "M", "1975-03-15", true,  95),
        buildRecord("23456789B", "***-**-5678", "Smith",    "Jonathan", "Jr", "M", "1975-03-15", false, 88),
        buildRecord("34567890C", "***-**-9012", "Smithson", "Maria",    "",   "F", "1988-07-22", false, 72)
    );

    // ── OM eligibility records (MEDICALELIGINFO table equivalent) ────────────

    /**
     * Eligibility codes follow the Active Medi-Cal rule:
     *   active  = NOT (code[0]=='9' AND code[2]=='9')
     *
     *   "1X0" → code[0]='1', code[2]='0' → NOT (9&&9) → ACTIVE
     *   "9X9" → code[0]='9', code[2]='9' → INACTIVE
     *
     * SSN Verification Codes (single char, stored in MEDICALELIGINFO):
     *   '3' = SSN sight-verified by county staff
     *   'A' = Validated via SSA referral
     *   'Y' = Unvalidated per SSA (name match but DOB/sex mismatch)
     *   '1' = Self-declared
     */
    private static final Map<String, Map<String, Object>> MOCK_ELIGIBILITY = new HashMap<>();

    static {
        MOCK_ELIGIBILITY.put("12345678A", buildEligibilityRecord(
            "12345678A", "MEDS-001", "John",     "Smith",    "",   "M", "1975-03-15",
            "***-**-1234", "10",  "1X0",  "3",
            "Sacramento", LocalDate.now().minusMonths(6).toString(), ""));

        MOCK_ELIGIBILITY.put("23456789B", buildEligibilityRecord(
            "23456789B", "MEDS-002", "Jonathan", "Smith",    "Jr", "M", "1975-03-15",
            "***-**-5678", "9X",  "9X9",  "1",
            "Fresno", "", LocalDate.now().minusMonths(2).toString()));

        MOCK_ELIGIBILITY.put("34567890C", buildEligibilityRecord(
            "34567890C", "MEDS-003", "Maria",    "Smithson", "",   "F", "1988-07-22",
            "***-**-9012", "9X",  "9C9",  "A",
            "Los Angeles", "", LocalDate.now().minusMonths(1).toString()));
    }

    public SCIService(ApplicationRepository applicationRepository,
                      RecipientRepository recipientRepository) {
        this.applicationRepository = applicationRepository;
        this.recipientRepository   = recipientRepository;
    }

    // ============================================================
    // OI TRANSACTION – CIN Search (BR-32, BR-33)
    // ============================================================

    /**
     * Simulates the CMOO106A OI (CIN Search Send) transaction.
     *
     * Outbound record fields used (from CMIPS tables):
     *   SEARCH_GROUP_NUMBER – "01" (records 1-25; SCI supports up to 99 groups / 2,500 matches)
     *   COUNTY_CODE         – from CONCERNROLE table
     *   TRANSACTION_TYPE    – "OI"
     *   APPLICATION_TYPE    – "IHSS"
     *   SEARCH_LEVEL        – "W" (Wide Address Search)
     *   SSN                 – from CONCERNROLE/PRIMARYALTERNATEID (after validation)
     *   DATE_OF_BIRTH       – CENTURY/YEAR/MONTH/DAY from PERSON table
     *   GENDER              – M/F from PERSON table
     *   LAST_NAME           – 20 chars from CONCERNROLENAME
     *   FIRST_NAME          – 15 chars from CONCERNROLENAME
     *   CIN (BR-32 only)    – truncated to 9 chars if longer
     *
     * SSN exclusion list (per DSD spec):
     *   Invalid fixed: 000000000, 111111111, 123456789
     *   Invalid range: 800000000–999999999 (unless alphanumeric pseudo MEDS ID 8______P/9______P)
     *
     * Return codes (in response.returnCode):
     *   "000" – matches found   "100" – no match   "811" – missing name   "600/900/999" – system
     */
    public Map<String, Object> sciSearch(String lastName, String firstName, String dob,
                                          String gender, String cin, String ssn,
                                          boolean mediCalPseudo) {

        // EM-811: last name or first name required
        if ((lastName == null || lastName.isBlank()) && (firstName == null || firstName.isBlank())) {
            log.warn("[OI] Return code 811: last name and first name both missing");
            return buildOiResponse("811", List.of(), null, 0,
                    "Input error: last name or first name is required (return code 811)");
        }

        // Build sent-criteria map per BR-32 / BR-33
        Map<String, Object> sentCriteria = new LinkedHashMap<>();
        sentCriteria.put("lastName",        nullToEmpty(lastName));
        sentCriteria.put("firstName",       nullToEmpty(firstName));
        sentCriteria.put("gender",          nullToEmpty(gender));
        sentCriteria.put("dob",             nullToEmpty(dob));
        sentCriteria.put("applicationType", APPLICATION_TYPE);
        sentCriteria.put("searchLevel",     SEARCH_LEVEL);

        if (cin != null && !cin.isBlank()) {
            // BR-32: CIN exists on case — include CIN + demographics + SSN (unless mediCalPseudo)
            String sentCin = cin.length() > 9 ? cin.substring(0, 9) : cin;
            sentCriteria.put("cin", sentCin);
            if (!mediCalPseudo && ssn != null && !ssn.isBlank() && isValidSsn(ssn)) {
                sentCriteria.put("ssn", ssn);
            } else if (!mediCalPseudo && ssn != null && !ssn.isBlank() && !isValidSsn(ssn)) {
                log.warn("[BR32] SSN failed CMOO106A validation, excluded from OI send");
            }
            log.info("[BR32] SCI OI search: CIN={}, applicationId excluded from criteria panel", sentCin);
        } else if (ssn != null && !ssn.isBlank()) {
            if (isValidSsn(ssn)) {
                sentCriteria.put("ssn", ssn);
                log.info("[BR33+SSN] SCI OI search: SSN present, no CIN");
            } else {
                log.warn("[OI] SSN failed CMOO106A validation (excluded: invalid range or known bad SSN)");
            }
        } else {
            // BR-33: No CIN, no SSN — send demographics only
            log.info("[BR33] SCI OI search: demographics only (SEARCH_LEVEL=W)");
        }

        // Match mock SCI records
        List<Map<String, Object>> results = searchMockRecords(lastName, cin);

        if (results.isEmpty()) {
            log.info("[OI] Return code 100: no match for lastName={}", lastName);
            return buildOiResponse("100", List.of(), sentCriteria, 0,
                    "CIN does not exist for the applicant");
        }

        // Return code 000: matches found — EM-186
        log.info("[OI] Return code 000: {} match(es) for lastName={}", results.size(), lastName);
        return buildOiResponse("000", results, sentCriteria, results.size(),
                "EM-186: Valid matches were found, please review matches to obtain " +
                "Medi-Cal Eligibility record. If matches are invalid select Cancel.");
    }

    // ============================================================
    // EL TRANSACTION – Medi-Cal Eligibility Request → OM Response
    // ============================================================

    /**
     * Simulates the CMOO106A EL transaction.
     * SCI cross-references the CIN against MEDS and returns the OM (Medi-Cal Eligibility Return).
     *
     * Active Medi-Cal rule:  NOT (medsEligibilityCode[0]=='9' AND medsEligibilityCode[2]=='9')
     *
     * EL return codes:
     *   "300" – successful, data returned  (OM returnCode mirrors per MEDS)
     *   "305" – EL/OM transaction not successful (Scenario 3)
     *
     * OM return codes (embedded in response):
     *   "000" – active Medi-Cal data   "100" – no active Medi-Cal   "630" – MEDS record not found
     */
    public Map<String, Object> getMediCalEligibility(String cin) {
        if (cin == null || cin.isBlank()) {
            return Map.of("elReturnCode", "305", "status", "FAILED",
                          "message", "CIN is required (EL transaction not successful)");
        }

        Map<String, Object> eligibility = MOCK_ELIGIBILITY.get(cin.toUpperCase());
        if (eligibility == null) {
            // Scenario 3: EL/OM transaction not successful — CIN not in MEDS
            log.warn("[EL] Return code 305 (Scenario 3): CIN {} not found in MEDS mock data", cin);
            return Map.of(
                "elReturnCode",  "305",
                "omReturnCode",  "630",   // MEDS record not found
                "version",       VERSION,
                "transactionType","OM",
                "status",        "FAILED",
                "message",       "SCI EL transaction was not successful for CIN: " + cin +
                                 " (OM return code 630: MEDS record not found)"
            );
        }

        // Derive active Medi-Cal from medsEligibilityCode using CMOO106A rule
        String medsCode = (String) eligibility.getOrDefault("medsEligibilityCode", "000");
        boolean mediCalActive = isActiveMediCal(medsCode);

        // OM return code: 000 = active MEDS data, 100 = no active Medi-Cal
        String omReturnCode = mediCalActive ? "000" : "100";

        log.info("[EL/OM] Return codes EL=300, OM={}: CIN={}, medsCode={}, active={}",
                 omReturnCode, cin, medsCode, mediCalActive);

        Map<String, Object> response = new LinkedHashMap<>();
        // OM envelope
        response.put("elReturnCode",        "300");          // EL: successful
        response.put("omReturnCode",         omReturnCode);
        response.put("version",              VERSION);
        response.put("transactionType",      "OM");
        response.put("medsId",               eligibility.get("medsId"));
        response.put("ssnVerificationCode",  eligibility.get("ssnVerificationCode"));
        response.put("medsEligibilityCode",  medsCode);
        response.put("mediCalActive",        mediCalActive);
        // Fields from the MEDICALELIGINFO table equivalent
        response.putAll(eligibility);
        // Override computed fields
        response.put("mediCalActive",        mediCalActive);
        response.put("eligibilityStatus",    mediCalActive ? "ACTIVE" : "INACTIVE");
        response.put("status",               "SUCCESS");
        return response;
    }

    // ============================================================
    // CIN Availability Check (Scenario 6 / EM-202)
    // ============================================================

    /**
     * Checks whether a CIN is already assigned to a DIFFERENT person record in CMIPS.
     * This is a local CMIPS DB check, not a CMOO106A transaction.
     *
     * Scenario 6 / EM-202: "Person record with indicated CIN already exists."
     */
    public Map<String, Object> checkCinAvailability(String cin, String callerAppId) {
        boolean takenByOther = applicationRepository.findByCin(cin)
                .map(a -> !a.getId().equals(callerAppId))
                .orElse(false);

        if (takenByOther) {
            return Map.of(
                "available",  false,
                "errorCode",  "EM-202",
                "message",    "Person record with indicated CIN already exists. " +
                              "Please resolve the conflict and perform CIN clearance again."
            );
        }
        return Map.of("available", true);
    }

    // ============================================================
    // OU TRANSACTION – Demographic Update (BR-14)
    // ============================================================

    /**
     * BR-14: Simulate the CMOO106A OU transaction sent when Name/DOB/Gender are modified.
     *
     * OU return codes:
     *   "300" – success   "107" – minor consent SSN   "801" – CIN invalid   "810" – sex code invalid
     *
     * @return OU response map with ouReturnCode and status
     */
    public Map<String, Object> sendDemographicUpdate(String cin, String lastName, String firstName,
                                                      String middleInitial, String dob,
                                                      String gender) {
        log.info("[BR14][OU] CMOO106A OU transaction: CIN={}, name={} {}, dob={}, gender={}",
                 cin, firstName, lastName, dob, gender);

        // Validate inputs
        if (cin == null || cin.isBlank()) {
            log.warn("[OU] Return code 801: CIN is missing or invalid");
            return Map.of("ouReturnCode", "801", "status", "ERROR",
                          "message", "OU return code 801: Client Index Number is missing or invalid");
        }
        String sciGender = normalizeGenderToSci(gender);
        if (sciGender.equals("U")) {
            log.warn("[OU] Return code 810: sex code invalid for gender={}", gender);
            return Map.of("ouReturnCode", "810", "status", "ERROR",
                          "message", "OU return code 810: Sex code is missing or invalid");
        }

        // Production: write OU record to CMOO106A interface file
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ouReturnCode",   "300");   // Confirmation of successful UPDATE
        result.put("version",        VERSION);
        result.put("transactionType","OU");
        result.put("cin",            cin);
        result.put("status",         "SUCCESS");
        result.put("message",        "OU demographic update sent to SCI via CMOO106A (return code 300)");
        return result;
    }

    // ============================================================
    // Helpers — SSN Validation (CMOO106A spec)
    // ============================================================

    /**
     * Returns true if the SSN is valid for inclusion in a CMOO106A OI record.
     *
     * Excluded per spec:
     *   Fixed invalid: 000000000, 111111111, 123456789
     *   Range invalid: 800000000 – 999999999
     *
     * Alphanumeric pseudo MEDS IDs of the form '8_______P' or '9_______P'
     * (9 chars, first char 8 or 9, last char 'P') ARE valid and pass through.
     */
    private boolean isValidSsn(String ssn) {
        if (ssn == null || ssn.isBlank()) return false;
        // Strip dashes and spaces
        String raw = ssn.replaceAll("[\\s\\-]", "");
        if (raw.length() != 9) return false;
        // Alphanumeric pseudo MEDS ID: starts with 8 or 9, ends with P
        if ((raw.charAt(0) == '8' || raw.charAt(0) == '9') && raw.charAt(8) == 'P') {
            return true; // Valid pseudo MEDS ID
        }
        // Must be all numeric from here
        if (!raw.matches("\\d{9}")) return false;
        long n = Long.parseLong(raw);
        if (n == 0L)           return false;  // 000000000
        if (n == 111111111L)   return false;  // 111111111
        if (n == 123456789L)   return false;  // 123456789
        if (n >= 800000000L)   return false;  // 800000000–999999999 range
        return true;
    }

    // ============================================================
    // Helpers — Active Medi-Cal Detection (CMOO106A / MEDS rule)
    // ============================================================

    /**
     * Determines active Medi-Cal status from the MEDS eligibility code.
     * Rule: Active = NOT (code.charAt(0) == '9' AND code.charAt(2) == '9')
     *
     * Examples from mock data:
     *   "1X0" → pos0='1', pos2='0' → active   (typical aid code 10/1X family)
     *   "9X9" → pos0='9', pos2='9' → inactive (aid code 9X family)
     */
    private boolean isActiveMediCal(String medsEligibilityCode) {
        if (medsEligibilityCode == null || medsEligibilityCode.length() < 3) return false;
        return !(medsEligibilityCode.charAt(0) == '9' && medsEligibilityCode.charAt(2) == '9');
    }

    // ============================================================
    // Helpers — Mock data search and construction
    // ============================================================

    private List<Map<String, Object>> searchMockRecords(String lastName, String cin) {
        List<Map<String, Object>> matches = new ArrayList<>();
        for (Map<String, Object> record : MOCK_SCI_RECORDS) {
            boolean cinMatch  = cin != null && !cin.isBlank() &&
                                cin.equalsIgnoreCase((String) record.get("cin"));
            boolean nameMatch = lastName != null && !lastName.isBlank() &&
                                ((String) record.get("lastName")).toLowerCase()
                                    .startsWith(lastName.toLowerCase());
            if (cinMatch || nameMatch) matches.add(record);
        }
        return matches;
    }

    /**
     * Builds a full OI return record matching CMOO106A field layout.
     *
     * Fields populated in the SCI return record per spec:
     *   CLIENT_INDEX_NUMBER, SSN, DOB, GENDER (M/F/U), LAST_NAME, FIRST_NAME,
     *   DEATH_INDICATOR, CONFIDENTIAL_INDICATOR, MEDS_FLAG, SAWS_FLAG, CATS_FLAG,
     *   CCS_FLAG, IHSS_FLAG, MATCHING_SCORE, ALTERNATE_DATA_RETURN_FLAG
     */
    private static Map<String, Object> buildRecord(String cin, String ssn,
                                                    String lastName, String firstName,
                                                    String suffix, String gender,
                                                    String dob, boolean medsFlag,
                                                    int matchingScore) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("cin",                    cin);
        r.put("ssn",                    ssn);
        r.put("lastName",               lastName);
        r.put("firstName",              firstName);
        r.put("suffix",                 suffix);
        r.put("gender",                 gender);  // M/F/U (SCI wire format)
        r.put("dob",                    dob);
        // SCI return record indicator flags
        r.put("deathIndicator",         false);
        r.put("confidentialIndicator",  false);
        r.put("medsFlag",               medsFlag);
        r.put("sawsFlag",               !medsFlag);
        r.put("catsFlag",               false);
        r.put("ccsFlag",                false);
        r.put("ihssFlag",               true);   // Always true — this is the IHSS system
        r.put("matchingScore",          matchingScore);
        r.put("alternateDataReturnFlag",false);
        r.put("mediCalEligible",        medsFlag);  // UI convenience field
        return r;
    }

    /**
     * Builds a full OM (Medi-Cal Eligibility Return) record.
     *
     * Fields from MEDICALELIGINFO table per CMOO106A spec:
     *   MEDS_CIN, MEDS_ID, SSN_VERIFICATION_CODE, MEDS_ELIGIBILITY_CODE,
     *   plus standard demographics and eligibility dates.
     */
    private static Map<String, Object> buildEligibilityRecord(
            String cin, String medsId,
            String firstName, String lastName, String suffix,
            String gender, String dob, String ssn,
            String aidCode, String medsEligibilityCode, String ssnVerificationCode,
            String county, String effectiveDate, String endDate) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("cin",                  cin);
        r.put("medsId",               medsId);
        r.put("firstName",            firstName);
        r.put("lastName",             lastName);
        r.put("suffix",               suffix);
        r.put("gender",               gender);
        r.put("dob",                  dob);
        r.put("ssn",                  ssn);
        r.put("aidCode",              aidCode);
        r.put("medsEligibilityCode",  medsEligibilityCode);  // Active = NOT(pos0=='9' && pos2=='9')
        r.put("ssnVerificationCode",  ssnVerificationCode);  // '3'=sight-verified, 'A'=SSA, 'Y'=unvalidated
        r.put("county",               county);
        r.put("effectiveDate",        effectiveDate);
        r.put("endDate",              endDate);
        return r;
    }

    /**
     * Builds the OI response envelope with CMOO106A header fields.
     */
    private Map<String, Object> buildOiResponse(String returnCode, List<Map<String, Object>> results,
                                                  Map<String, Object> sentCriteria, int totalMatches,
                                                  String message) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("returnCode",          returnCode);
        r.put("version",             VERSION);
        r.put("transactionType",     "RT");          // OI return type
        r.put("totalMatches",        totalMatches);
        r.put("searchGroupNumber",   SEARCH_GROUP_01);
        r.put("applicationTypeUsed", APPLICATION_TYPE);
        r.put("searchLevelUsed",     SEARCH_LEVEL);
        r.put("status",              returnCode.equals("000") ? "MATCHES_FOUND" : "NO_MATCH");
        r.put("message",             message);
        r.put("results",             results);
        if (sentCriteria != null) r.put("sentCriteria", sentCriteria);
        return r;
    }

    /**
     * Normalizes a human-readable gender string to the SCI M/F/U wire format.
     * SCI returns M/F/U — CMIPS stores Male/Female/Other.
     */
    private static String normalizeGenderToSci(String gender) {
        if (gender == null) return "U";
        return switch (gender.trim().toUpperCase()) {
            case "MALE",   "M" -> "M";
            case "FEMALE", "F" -> "F";
            default            -> "U";
        };
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
}
