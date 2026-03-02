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
     * Search workers by criteria.
     *
     * Query params (all optional, AND-combined):
     *   workerNumber  — exact or partial match on worker number
     *   lastName      — case-insensitive partial match
     *   firstName     — case-insensitive partial match
     *   districtOffice — exact match
     *   unit          — exact match
     *   position      — exact match (e.g. CASE_WORKER, SUPERVISOR, MANAGER)
     *   language      — exact match (e.g. English, Spanish)
     *
     * Returns list of matched worker records (max 50).
     */
    @GetMapping("/search")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<List<Map<String, String>>> searchUsers(
            @RequestParam(required = false) String workerNumber,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String districtOffice,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String language) {

        // Require at least one search criterion (BR OS — prevent open-ended queries)
        boolean hasAnyCriteria = !isBlank(workerNumber) || !isBlank(lastName) || !isBlank(firstName)
                || !isBlank(districtOffice) || !isBlank(unit) || !isBlank(position) || !isBlank(language);
        if (!hasAnyCriteria) {
            return ResponseEntity.badRequest().build();
        }

        List<Map<String, String>> results = MOCK_WORKERS.stream()
                .filter(w -> isBlank(workerNumber) || contains(w.get("workerNumber"), workerNumber))
                .filter(w -> isBlank(lastName)     || contains(w.get("lastName"), lastName))
                .filter(w -> isBlank(firstName)    || contains(w.get("firstName"), firstName))
                .filter(w -> isBlank(districtOffice) || w.get("districtOffice").equalsIgnoreCase(districtOffice))
                .filter(w -> isBlank(unit)         || w.get("unit").equalsIgnoreCase(unit))
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
        roster.add(worker("W10001", "Smith",    "John",     "Sacramento", "Unit A", "CASE_WORKER", "English"));
        roster.add(worker("W10002", "Johnson",  "Mary",     "Sacramento", "Unit A", "CASE_WORKER", "English"));
        roster.add(worker("W10003", "Garcia",   "Luis",     "Sacramento", "Unit B", "CASE_WORKER", "Spanish"));
        roster.add(worker("W10004", "Williams", "Karen",    "Sacramento", "Unit B", "SUPERVISOR",  "English"));
        roster.add(worker("W10005", "Brown",    "Michael",  "Sacramento", "Unit C", "CASE_WORKER", "English"));

        // District Office: Los Angeles
        roster.add(worker("W20001", "Martinez", "Sofia",    "Los Angeles", "Unit 1", "CASE_WORKER", "Spanish"));
        roster.add(worker("W20002", "Davis",    "Robert",   "Los Angeles", "Unit 1", "CASE_WORKER", "English"));
        roster.add(worker("W20003", "Miller",   "Jennifer", "Los Angeles", "Unit 2", "CASE_WORKER", "English"));
        roster.add(worker("W20004", "Wilson",   "James",    "Los Angeles", "Unit 2", "SUPERVISOR",  "English"));
        roster.add(worker("W20005", "Taylor",   "Patricia", "Los Angeles", "Unit 3", "CASE_WORKER", "English"));
        roster.add(worker("W20006", "Anderson", "Linda",    "Los Angeles", "Unit 3", "CASE_WORKER", "Chinese"));
        roster.add(worker("W20007", "Thomas",   "Charles",  "Los Angeles", "Unit 4", "CASE_WORKER", "English"));
        roster.add(worker("W20008", "Jackson",  "Barbara",  "Los Angeles", "Unit 4", "MANAGER",     "English"));

        // District Office: San Francisco
        roster.add(worker("W30001", "White",    "Steven",   "San Francisco", "Unit X", "CASE_WORKER", "English"));
        roster.add(worker("W30002", "Harris",   "Jessica",  "San Francisco", "Unit X", "CASE_WORKER", "English"));
        roster.add(worker("W30003", "Martin",   "Daniel",   "San Francisco", "Unit Y", "CASE_WORKER", "Spanish"));
        roster.add(worker("W30004", "Thompson", "Nancy",    "San Francisco", "Unit Y", "SUPERVISOR",  "English"));
        roster.add(worker("W30005", "Moore",    "Paul",     "San Francisco", "Unit Z", "CASE_WORKER", "English"));

        // District Office: San Diego
        roster.add(worker("W40001", "Lee",      "Kevin",    "San Diego", "Unit 1", "CASE_WORKER", "English"));
        roster.add(worker("W40002", "Clark",    "Betty",    "San Diego", "Unit 1", "CASE_WORKER", "Spanish"));
        roster.add(worker("W40003", "Lewis",    "George",   "San Diego", "Unit 2", "SUPERVISOR",  "English"));
        roster.add(worker("W40004", "Robinson", "Sandra",   "San Diego", "Unit 2", "CASE_WORKER", "English"));

        // District Office: Fresno
        roster.add(worker("W50001", "Walker",   "Donald",   "Fresno", "Unit A", "CASE_WORKER", "English"));
        roster.add(worker("W50002", "Hall",     "Dorothy",  "Fresno", "Unit A", "CASE_WORKER", "Spanish"));
        roster.add(worker("W50003", "Allen",    "Kenneth",  "Fresno", "Unit B", "SUPERVISOR",  "English"));

        return Collections.unmodifiableList(roster);
    }

    private static Map<String, String> worker(String number, String last, String first,
                                               String office, String unit,
                                               String position, String language) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("workerNumber",    number);
        m.put("lastName",        last);
        m.put("firstName",       first);
        m.put("fullName",        last + ", " + first);
        m.put("districtOffice",  office);
        m.put("unit",            unit);
        m.put("position",        position);
        m.put("language",        language);
        return m;
    }
}
