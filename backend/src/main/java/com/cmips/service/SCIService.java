package com.cmips.service;

import com.cmips.repository.ApplicationRepository;
import com.cmips.repository.RecipientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * SCIService — Simulates the Statewide Client Index (SCI) interface.
 *
 * In the real CMIPS system this class would send OI/EL/OM/OU real-time
 * transactions to the SCI mainframe. In this MVP every response is a
 * deterministic mock so the full UI/business-rule flow can be exercised.
 *
 * Business Rules implemented:
 *   BR 32 – SCI CIN Search when CIN already on case (sends CIN + demographics + SSN unless mediCalPseudo)
 *   BR 33 – SCI CIN Search with no CIN/SSN (sends demographics only)
 *   BR 9  – S1 transaction trigger when no/inactive Medi-Cal (handled in ApplicationService)
 *   BR 13 – IH18 trigger when active Medi-Cal (handled in ApplicationService)
 *   BR 14 – OU transaction on demographic change (stub)
 */
@Service
public class SCIService {

    private static final Logger log = LoggerFactory.getLogger(SCIService.class);

    private final ApplicationRepository applicationRepository;
    private final RecipientRepository recipientRepository;

    // Simulated CIN records returned by SCI (OI transaction mock data)
    private static final List<Map<String, Object>> MOCK_SCI_RECORDS = List.of(
        buildRecord("12345678A", "***-**-1234", "Smith",    "John",     "",    "Male",   "1975-03-15", true),
        buildRecord("23456789B", "***-**-5678", "Smith",    "Jonathan", "Jr",  "Male",   "1975-03-15", false),
        buildRecord("34567890C", "***-**-9012", "Smithson", "Maria",    "",    "Female", "1988-07-22", false)
    );

    // Simulated MEDS eligibility (OM transaction mock data)
    private static final Map<String, Map<String, Object>> MOCK_ELIGIBILITY = new HashMap<>();

    static {
        MOCK_ELIGIBILITY.put("12345678A", Map.of(
            "cin",              "12345678A",
            "firstName",        "John",
            "lastName",         "Smith",
            "suffix",           "",
            "gender",           "Male",
            "dob",              "1975-03-15",
            "ssn",              "***-**-1234",
            "aidCode",          "1X",
            "eligibilityStatus","ACTIVE",
            "effectiveDate",    LocalDate.now().minusMonths(6).toString(),
            "endDate",          "",
            "county",           "Sacramento",
            "mediCalActive",    true
        ));
        MOCK_ELIGIBILITY.put("23456789B", Map.of(
            "cin",              "23456789B",
            "firstName",        "Jonathan",
            "lastName",         "Smith",
            "suffix",           "Jr",
            "gender",           "Male",
            "dob",              "1975-03-15",
            "ssn",              "***-**-5678",
            "aidCode",          "9X",
            "eligibilityStatus","INACTIVE",
            "effectiveDate",    "",
            "endDate",          LocalDate.now().minusMonths(2).toString(),
            "county",           "Fresno",
            "mediCalActive",    false
        ));
        MOCK_ELIGIBILITY.put("34567890C", Map.of(
            "cin",              "34567890C",
            "firstName",        "Maria",
            "lastName",         "Smithson",
            "suffix",           "",
            "gender",           "Female",
            "dob",              "1988-07-22",
            "ssn",              "***-**-9012",
            "aidCode",          "9X",
            "eligibilityStatus","INACTIVE",
            "effectiveDate",    "",
            "endDate",          LocalDate.now().minusMonths(1).toString(),
            "county",           "Los Angeles",
            "mediCalActive",    false
        ));
    }

    public SCIService(ApplicationRepository applicationRepository,
                      RecipientRepository recipientRepository) {
        this.applicationRepository = applicationRepository;
        this.recipientRepository = recipientRepository;
    }

    // ============================================================
    // OI TRANSACTION – CIN Search
    // ============================================================

    /**
     * Simulates the SCI OI (CIN Search Send) transaction.
     *
     * BR 32: If cin is present, include cin + demographics + ssn (unless mediCalPseudo).
     *        CIN > 9 chars → truncate to 9.
     * BR 33: No CIN, no SSN → send demographics only.
     *
     * Returns a response map with keys:
     *   status  : "MATCHES_FOUND" | "NO_MATCH"
     *   message : descriptive message (EM-186 text when matches found)
     *   results : List of CIN record maps
     *   sentCriteria : what was sent to SCI (for display in the read-only criteria panel)
     */
    public Map<String, Object> sciSearch(String lastName, String firstName, String dob,
                                          String gender, String cin, String ssn,
                                          boolean mediCalPseudo) {

        // Build the criteria map per BR 32 / BR 33
        Map<String, Object> sentCriteria = new LinkedHashMap<>();
        sentCriteria.put("lastName",  nullToEmpty(lastName));
        sentCriteria.put("firstName", nullToEmpty(firstName));
        sentCriteria.put("gender",    nullToEmpty(gender));
        sentCriteria.put("dob",       nullToEmpty(dob));

        if (cin != null && !cin.isBlank()) {
            // BR 32: CIN exists on case
            String sentCin = cin.length() > 9 ? cin.substring(0, 9) : cin;
            sentCriteria.put("cin", sentCin);
            if (!mediCalPseudo && ssn != null && !ssn.isBlank()) {
                sentCriteria.put("ssn", ssn);
            }
            log.info("[BR32] SCI OI search: CIN={}, demographics sent", sentCin);
        } else if (ssn != null && !ssn.isBlank()) {
            // Implicit: SSN present, no CIN
            sentCriteria.put("ssn", ssn);
            log.info("[BR33+SSN] SCI OI search: SSN present, no CIN");
        } else {
            // BR 33: No CIN, no SSN — demographics only
            log.info("[BR33] SCI OI search: demographics only");
        }

        // Match mock records against search criteria
        List<Map<String, Object>> results = searchMockRecords(lastName, cin);

        if (results.isEmpty()) {
            return Map.of(
                "status",       "NO_MATCH",
                "message",      "CIN does not exist for the applicant",
                "results",      List.of(),
                "sentCriteria", sentCriteria
            );
        }

        // EM-186: Valid matches found
        return Map.of(
            "status",       "MATCHES_FOUND",
            "message",      "EM-186: Valid matches were found, please review matches to obtain " +
                            "Medi-Cal Eligibility record. If matches are invalid select Cancel.",
            "results",      results,
            "sentCriteria", sentCriteria
        );
    }

    // ============================================================
    // EL TRANSACTION – Medi-Cal Eligibility Request
    // ============================================================

    /**
     * Simulates the SCI EL transaction and OM (Medi-Cal Eligibility Return) response.
     *
     * Scenario 3: If CIN is unknown → returns FAILED status.
     */
    public Map<String, Object> getMediCalEligibility(String cin) {
        if (cin == null || cin.isBlank()) {
            return Map.of("status", "FAILED", "message", "CIN is required");
        }

        Map<String, Object> eligibility = MOCK_ELIGIBILITY.get(cin.toUpperCase());
        if (eligibility == null) {
            // Scenario 3: EL/OM transaction not successful
            log.warn("[EL] SCI transaction failed for CIN: {} — unknown CIN in mock data", cin);
            return Map.of(
                "status",  "FAILED",
                "message", "SCI EL transaction was not successful for CIN: " + cin
            );
        }

        log.info("[EL/OM] Medi-Cal eligibility returned for CIN: {}, status: {}",
                 cin, eligibility.get("eligibilityStatus"));
        Map<String, Object> response = new LinkedHashMap<>(eligibility);
        response.put("status", "SUCCESS");
        return response;
    }

    // ============================================================
    // CIN Availability Check (Scenario 6)
    // ============================================================

    /**
     * Checks whether a CIN is already assigned to a DIFFERENT person record in CMIPS.
     * Scenario 6 / EM-202: "Person record with indicated CIN already exists."
     *
     * @param cin           the CIN being checked
     * @param callerAppId   the application initiating the check (excluded from search)
     */
    public Map<String, Object> checkCinAvailability(String cin, String callerAppId) {
        boolean takenByOther = applicationRepository.findByCin(cin)
                .map(a -> !a.getId().equals(callerAppId))
                .orElse(false);

        if (takenByOther) {
            return Map.of(
                "available",   false,
                "errorCode",   "EM-202",
                "message",     "Person record with indicated CIN already exists. " +
                               "Please resolve the conflict and perform CIN clearance again."
            );
        }
        return Map.of("available", true);
    }

    // ============================================================
    // OU TRANSACTION – Demographic Update (BR 14)
    // ============================================================

    /**
     * Simulates the SCI OU transaction sent when Name/DOB/Gender are modified.
     * BR 14: Send OU to SCI when person demographics change.
     */
    public void sendDemographicUpdate(String cin, String lastName, String firstName,
                                       String middleInitial, String dob, String gender) {
        log.info("[BR14] SCI OU transaction: CIN={}, name={} {}, dob={}, gender={}",
                 cin, firstName, lastName, dob, gender);
        // Production: send OU transaction to SCI mainframe
    }

    // ============================================================
    // Helpers
    // ============================================================

    private List<Map<String, Object>> searchMockRecords(String lastName, String cin) {
        List<Map<String, Object>> matches = new ArrayList<>();

        for (Map<String, Object> record : MOCK_SCI_RECORDS) {
            boolean cinMatch  = cin != null && !cin.isBlank() &&
                                cin.equalsIgnoreCase((String) record.get("cin"));
            boolean nameMatch = lastName != null && !lastName.isBlank() &&
                                ((String) record.get("lastName")).toLowerCase()
                                    .startsWith(lastName.toLowerCase());

            if (cinMatch || nameMatch) {
                matches.add(record);
            }
        }
        return matches;
    }

    private static Map<String, Object> buildRecord(String cin, String ssn, String lastName,
                                                    String firstName, String suffix,
                                                    String gender, String dob,
                                                    boolean mediCalEligible) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("cin",             cin);
        r.put("ssn",             ssn);
        r.put("lastName",        lastName);
        r.put("firstName",       firstName);
        r.put("suffix",          suffix);
        r.put("gender",          gender);
        r.put("dob",             dob);
        r.put("mediCalEligible", mediCalEligible);
        return r;
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
