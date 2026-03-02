package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User / Worker Search REST Controller
 * Implements DSD CI-67746 — User Search (Phase 5A)
 *
 * Endpoint: GET /api/users/search
 * Used by Create Case / Create Referral / Assigned Worker field to look up
 * eligible caseworkers by criteria (worker number, name, district office, unit,
 * position, language).
 *
 * For MVP, returns a static mock roster that is filtered in-memory.
 * In production this would query the CMIPS UserEntity table or Keycloak attributes.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserSearchController {

    private static final Logger log = LoggerFactory.getLogger(UserSearchController.class);

    /** Mock worker roster — 25 workers across several district offices and positions */
    private static final List<Map<String, String>> MOCK_WORKERS = buildMockRoster();

    /**
     * Search workers by criteria (DSD CI-67746 — User Search).
     *
     * Query params (all optional, AND-combined — 9 per DSD page 64):
     *   workerNumber   — partial match on worker number
     *   username       — partial match on username
     *   lastName       — case-insensitive partial match
     *   firstName      — case-insensitive partial match
     *   districtOffice — exact match
     *   unit           — exact match
     *   zipCode        — exact match
     *   position       — exact match (e.g. CASE_WORKER, SUPERVISOR, MANAGER)
     *   language       — exact match (e.g. English, Spanish)
     *
     * Returns list of matched worker records (max 50) with 7 DSD result columns:
     *   firstName, lastName, workerNumber, districtOffice, language, language2, caseCount
     */
    @GetMapping("/search")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<List<Map<String, String>>> searchUsers(
            @RequestParam(required = false) String workerNumber,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String districtOffice,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) String zipCode,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String language) {

        // Require at least one search criterion (BR OS — prevent open-ended queries)
        boolean hasAnyCriteria = !isBlank(workerNumber) || !isBlank(username)
                || !isBlank(lastName) || !isBlank(firstName)
                || !isBlank(districtOffice) || !isBlank(unit) || !isBlank(zipCode)
                || !isBlank(position) || !isBlank(language);
        if (!hasAnyCriteria) {
            return ResponseEntity.badRequest().build();
        }

        List<Map<String, String>> results = MOCK_WORKERS.stream()
                .filter(w -> isBlank(workerNumber) || contains(w.get("workerNumber"), workerNumber))
                .filter(w -> isBlank(username)     || contains(w.get("username"), username))
                .filter(w -> isBlank(lastName)     || contains(w.get("lastName"), lastName))
                .filter(w -> isBlank(firstName)    || contains(w.get("firstName"), firstName))
                .filter(w -> isBlank(districtOffice) || w.get("districtOffice").equalsIgnoreCase(districtOffice))
                .filter(w -> isBlank(unit)         || w.get("unit").equalsIgnoreCase(unit))
                .filter(w -> isBlank(zipCode)      || w.get("zipCode").equals(zipCode))
                .filter(w -> isBlank(position)     || w.get("position").equalsIgnoreCase(position))
                .filter(w -> isBlank(language)     || w.get("language").equalsIgnoreCase(language))
                .limit(50)
                .collect(Collectors.toList());

        log.info("[user-search] criteria=lastName:{} firstName:{} DO:{} → {} results",
                 lastName, firstName, districtOffice, results.size());
        return ResponseEntity.ok(results);
    }

    // ==================== HELPERS ====================

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static boolean contains(String field, String query) {
        if (field == null) return false;
        return field.toLowerCase().contains(query.toLowerCase());
    }

    // ==================== MOCK DATA ====================

    private static List<Map<String, String>> buildMockRoster() {
        List<Map<String, String>> roster = new ArrayList<>();

        // District Office: Sacramento
        roster.add(worker("W10001", "jsmith",    "Smith",    "John",     "Sacramento", "Unit A", "95814", "CASE_WORKER", "English", "",        "12"));
        roster.add(worker("W10002", "mjohnson",  "Johnson",  "Mary",     "Sacramento", "Unit A", "95814", "CASE_WORKER", "English", "",        "8"));
        roster.add(worker("W10003", "lgarcia",   "Garcia",   "Luis",     "Sacramento", "Unit B", "95822", "CASE_WORKER", "Spanish", "English", "15"));
        roster.add(worker("W10004", "kwilliams", "Williams", "Karen",    "Sacramento", "Unit B", "95822", "SUPERVISOR",  "English", "",        "5"));
        roster.add(worker("W10005", "mbrown",    "Brown",    "Michael",  "Sacramento", "Unit C", "95831", "CASE_WORKER", "English", "",        "10"));

        // District Office: Los Angeles
        roster.add(worker("W20001", "smartinez", "Martinez", "Sofia",    "Los Angeles", "Unit 1", "90012", "CASE_WORKER", "Spanish", "English", "18"));
        roster.add(worker("W20002", "rdavis",    "Davis",    "Robert",   "Los Angeles", "Unit 1", "90012", "CASE_WORKER", "English", "",        "14"));
        roster.add(worker("W20003", "jmiller",   "Miller",   "Jennifer", "Los Angeles", "Unit 2", "90015", "CASE_WORKER", "English", "",        "11"));
        roster.add(worker("W20004", "jwilson",   "Wilson",   "James",    "Los Angeles", "Unit 2", "90015", "SUPERVISOR",  "English", "Spanish", "3"));
        roster.add(worker("W20005", "ptaylor",   "Taylor",   "Patricia", "Los Angeles", "Unit 3", "90020", "CASE_WORKER", "English", "",        "9"));
        roster.add(worker("W20006", "landerson", "Anderson", "Linda",    "Los Angeles", "Unit 3", "90020", "CASE_WORKER", "Chinese", "English", "7"));
        roster.add(worker("W20007", "cthomas",   "Thomas",   "Charles",  "Los Angeles", "Unit 4", "90025", "CASE_WORKER", "English", "",        "13"));
        roster.add(worker("W20008", "bjackson",  "Jackson",  "Barbara",  "Los Angeles", "Unit 4", "90025", "MANAGER",     "English", "Spanish", "2"));

        // District Office: San Francisco
        roster.add(worker("W30001", "swhite",    "White",    "Steven",   "San Francisco", "Unit X", "94102", "CASE_WORKER", "English", "",        "6"));
        roster.add(worker("W30002", "jharris",   "Harris",   "Jessica",  "San Francisco", "Unit X", "94102", "CASE_WORKER", "English", "",        "11"));
        roster.add(worker("W30003", "dmartin",   "Martin",   "Daniel",   "San Francisco", "Unit Y", "94110", "CASE_WORKER", "Spanish", "English", "16"));
        roster.add(worker("W30004", "nthompson", "Thompson", "Nancy",    "San Francisco", "Unit Y", "94110", "SUPERVISOR",  "English", "",        "4"));
        roster.add(worker("W30005", "pmoore",    "Moore",    "Paul",     "San Francisco", "Unit Z", "94115", "CASE_WORKER", "English", "",        "8"));

        // District Office: San Diego
        roster.add(worker("W40001", "klee",      "Lee",      "Kevin",    "San Diego", "Unit 1", "92101", "CASE_WORKER", "English", "",        "10"));
        roster.add(worker("W40002", "bclark",    "Clark",    "Betty",    "San Diego", "Unit 1", "92101", "CASE_WORKER", "Spanish", "English", "14"));
        roster.add(worker("W40003", "glewis",    "Lewis",    "George",   "San Diego", "Unit 2", "92110", "SUPERVISOR",  "English", "",        "3"));
        roster.add(worker("W40004", "srobinson", "Robinson", "Sandra",   "San Diego", "Unit 2", "92110", "CASE_WORKER", "English", "",        "7"));

        // District Office: Fresno
        roster.add(worker("W50001", "dwalker",   "Walker",   "Donald",   "Fresno", "Unit A", "93721", "CASE_WORKER", "English", "",        "9"));
        roster.add(worker("W50002", "dhall",     "Hall",     "Dorothy",  "Fresno", "Unit A", "93721", "CASE_WORKER", "Spanish", "English", "12"));
        roster.add(worker("W50003", "kallen",    "Allen",    "Kenneth",  "Fresno", "Unit B", "93727", "SUPERVISOR",  "English", "",        "2"));

        return Collections.unmodifiableList(roster);
    }

    private static Map<String, String> worker(String number, String username,
                                               String last, String first,
                                               String office, String unit, String zip,
                                               String position, String language,
                                               String language2, String caseCount) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("workerNumber",    number);
        m.put("username",        username);
        m.put("lastName",        last);
        m.put("firstName",       first);
        m.put("fullName",        last + ", " + first);
        m.put("districtOffice",  office);
        m.put("unit",            unit);
        m.put("zipCode",         zip);
        m.put("position",        position);
        m.put("language",        language);
        m.put("language2",       language2);
        m.put("caseCount",       caseCount);
        return m;
    }
}
