package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.CaseAppealHearingHistory;
import com.cmips.entity.CaseCodeTables;
import com.cmips.entity.CaseEntity;
import com.cmips.entity.StateHearingEntity;
import com.cmips.repository.CaseAppealHearingHistoryRepository;
import com.cmips.repository.CaseRepository;
import com.cmips.repository.StateHearingRepository;
import com.cmips.repository.RecipientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * State Hearing Controller — per DSD Section 20 (CI-67779) + Section 25 (CI-67705/67680/67689/67695)
 *
 * Implements:
 *   - State Hearing Search (CI-67779) — 4 criteria, 6-month limit
 *   - Create State Hearing (CI-67680) — EM CM 027/028, BR CM 04/05
 *   - Modify State Hearing (CI-67689) — EM CM 029-037, BR CM 06/06a
 *   - View State Hearing (CI-67695) — includes Previously Scheduled Hearings
 *   - State Hearing List (CI-67705) — by case
 *   - Code Tables — DSD-exact codes (EO, RS, CS, SSHS)
 */
@RestController
@RequestMapping("/api/state-hearings")
@CrossOrigin(origins = "*")
public class StateHearingController {

    private static final Logger log = LoggerFactory.getLogger(StateHearingController.class);

    private final StateHearingRepository stateHearingRepository;
    private final CaseRepository caseRepository;
    private final RecipientRepository recipientRepository;
    private final CaseAppealHearingHistoryRepository hearingHistoryRepository;

    public StateHearingController(StateHearingRepository stateHearingRepository,
                                  CaseRepository caseRepository,
                                  RecipientRepository recipientRepository,
                                  CaseAppealHearingHistoryRepository hearingHistoryRepository) {
        this.stateHearingRepository = stateHearingRepository;
        this.caseRepository = caseRepository;
        this.recipientRepository = recipientRepository;
        this.hearingHistoryRepository = hearingHistoryRepository;
    }

    // ==================== SEARCH (DSD Section 20 CI-67779) ====================

    /**
     * DSD State Hearing Search — CI-67779
     *
     * @param stateHearingStatus SSHS code: SSHS001=Requested, SSHS002=Scheduled, SSHS003=Resolved, SSHS004=Requested And Scheduled
     * @param countyCode         County code (required)
     * @param fromDate           Hearing Request From Date (required)
     * @param toDate             Hearing Request To Date (optional — defaults to fromDate + 6 months)
     */
    @GetMapping("/search")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> searchStateHearings(
            @RequestParam String stateHearingStatus,
            @RequestParam String countyCode,
            @RequestParam String fromDate,
            @RequestParam(required = false) String toDate) {

        if (stateHearingStatus == null || stateHearingStatus.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "EM OS 004: State Hearing Status must be indicated"));
        }
        if (countyCode == null || countyCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "County must be indicated"));
        }
        if (fromDate == null || fromDate.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Hearing Request From Date must be indicated"));
        }

        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to;
        if (toDate != null && !toDate.isBlank()) {
            to = LocalDate.parse(toDate);
        } else {
            to = from.plusMonths(6);
        }

        // DSD: Search limited to 6-month period
        if (to.isAfter(from.plusMonths(6))) {
            to = from.plusMonths(6);
        }

        if (to.isBefore(from)) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Hearing Request To Date cannot be before From Date"));
        }

        List<StateHearingEntity> results;
        String statusFilter = mapSshsToStatus(stateHearingStatus);

        if ("SSHS004".equals(stateHearingStatus)) {
            results = stateHearingRepository.searchStateHearingsRequestedAndScheduled(
                    countyCode, from, to);
        } else {
            results = stateHearingRepository.searchStateHearings(
                    countyCode, from, to, statusFilter);
        }

        List<Map<String, Object>> response = results.stream().map(sh -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", sh.getId());
            row.put("caseId", sh.getCaseId());
            row.put("caseNumber", sh.getCaseNumber());
            row.put("recipientId", sh.getRecipientId());
            row.put("recipientName", sh.getRecipientName());
            row.put("stateHearingStatus", sh.getStatus());
            row.put("stateHearingStatusDisplay", getStatusDisplay(sh.getStatus()));
            row.put("hearingRequestDate", sh.getHearingRequestDate());
            row.put("scheduledHearingDate", sh.getScheduledHearingDate());
            row.put("hearingOutcome", sh.getHearingOutcome());
            row.put("outcomeDate", sh.getOutcomeDate());
            return row;
        }).collect(Collectors.toList());

        log.info("State Hearing Search: status={}, county={}, from={}, to={} → {} results",
                stateHearingStatus, countyCode, from, to, response.size());

        return ResponseEntity.ok(response);
    }

    // ==================== GET (View State Hearing — CI-67695) ====================

    @GetMapping("/{id}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getStateHearing(@PathVariable Long id) {
        return stateHearingRepository.findById(id)
                .map(sh -> {
                    Map<String, Object> detail = toDetailMap(sh);
                    // DSD View screen: include "Previously Scheduled Hearings" history
                    List<CaseAppealHearingHistory> history =
                        hearingHistoryRepository.findByAppealIdOrderByHearingDateDesc(id);
                    List<Map<String, Object>> historyList = history.stream().map(h -> {
                        Map<String, Object> hMap = new LinkedHashMap<>();
                        hMap.put("id", h.getId());
                        hMap.put("hearingDate", h.getHearingDate());
                        hMap.put("rescheduledReason", h.getRescheduledReason());
                        hMap.put("rescheduledReasonDisplay",
                            CaseCodeTables.RESCHEDULED_REASON.getOrDefault(h.getRescheduledReason(), h.getRescheduledReason()));
                        return hMap;
                    }).collect(Collectors.toList());
                    detail.put("previouslyScheduledHearings", historyList);
                    return ResponseEntity.ok(detail);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== LIST BY CASE (State Hearing List — CI-67705) ====================

    @GetMapping("/by-case/{caseId}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getStateHearingsByCase(@PathVariable Long caseId) {
        List<StateHearingEntity> hearings = stateHearingRepository.findByCaseId(caseId);
        List<Map<String, Object>> response = hearings.stream().map(sh -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", sh.getId());
            row.put("appealNumber", sh.getAppealNumber());
            row.put("hearingRequestDate", sh.getHearingRequestDate());
            row.put("scheduledHearingDate", sh.getScheduledHearingDate());
            row.put("hearingOutcome", sh.getHearingOutcome());
            row.put("outcomeDisplay", getOutcomeDisplay(sh.getHearingOutcome()));
            row.put("status", sh.getStatus());
            row.put("statusDisplay", getStatusDisplay(sh.getStatus()));
            // DSD: "Action" column — View or Edit
            row.put("isResolved", sh.isResolved());
            return row;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ==================== CREATE (Create State Hearing — CI-67680) ====================

    /**
     * Create State Hearing — per DSD CI-67680
     *
     * DSD Screen fields:
     *   Section 1 "State Hearing Request": Number* (required), Request Date* (required, default=today)
     *   Section 2 "Details": Scheduled Hearing Date, Rescheduled Reason, Outcome, Outcome Date, Compliance Form Sent Date
     *
     * Error Messages (Create):
     *   EM CM 027: Scheduled Hearing Date cannot be before the Request Date
     *   EM CM 028: Scheduled Hearing Date is required when the Reschedule Reason is indicated
     *
     * Business Rules:
     *   BR CM 04: Set status to "Requested"
     *   BR CM 05: When scheduled hearing date not blank → Set status to "Scheduled"
     *   Outcome defaults to "Pending" (EO501) on Create
     */
    @PostMapping
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> createStateHearing(@RequestBody Map<String, Object> request) {
        // --- Validate required fields ---
        Long caseId = request.get("caseId") != null ? Long.valueOf(request.get("caseId").toString()) : null;
        // Accept both "appealNumber" and "hearingNumber" as field names
        String appealNumber = (String) request.get("appealNumber");
        if (appealNumber == null || appealNumber.isBlank()) {
            appealNumber = (String) request.get("hearingNumber");
        }

        if (caseId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Case ID is required"));
        }
        if (appealNumber == null || appealNumber.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "State Hearing Number is required"));
        }
        if (appealNumber.length() > 20) {
            return ResponseEntity.badRequest().body(Map.of("error", "State Hearing Number cannot exceed 20 characters"));
        }

        Optional<CaseEntity> caseOpt = caseRepository.findById(caseId);
        if (caseOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Case not found"));
        }
        CaseEntity caseEntity = caseOpt.get();

        // Parse dates
        String reqDateStr = (String) request.get("hearingRequestDate");
        LocalDate requestDate = (reqDateStr != null && !reqDateStr.isBlank())
            ? LocalDate.parse(reqDateStr) : LocalDate.now();

        String schedDateStr = (String) request.get("scheduledHearingDate");
        LocalDate scheduledDate = (schedDateStr != null && !schedDateStr.isBlank())
            ? LocalDate.parse(schedDateStr) : null;

        String rescheduledReason = (String) request.get("rescheduledReason");

        // --- EM CM 027: Scheduled Hearing Date cannot be before the Request Date ---
        if (scheduledDate != null && scheduledDate.isBefore(requestDate)) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "EM CM 027: Scheduled Hearing Date cannot be before the Request Date"));
        }

        // --- EM CM 028: Scheduled Hearing Date is required when the Reschedule Reason is indicated ---
        if (rescheduledReason != null && !rescheduledReason.isBlank() && scheduledDate == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "EM CM 028: Scheduled Hearing Date is required when the Reschedule Reason is indicated"));
        }

        // --- Build entity ---
        StateHearingEntity sh = new StateHearingEntity();
        sh.setAppealNumber(appealNumber);
        sh.setCaseId(caseId);
        sh.setCaseNumber(caseEntity.getCaseNumber());
        sh.setRecipientId(caseEntity.getRecipientId());
        sh.setCountyCode(caseEntity.getCountyCode());
        sh.setHearingRequestDate(requestDate);
        sh.setScheduledHearingDate(scheduledDate);
        sh.setRescheduledReason(rescheduledReason);

        // DSD: Outcome defaults to "Pending" (EO501) on Create
        sh.setHearingOutcome("EO501");

        sh.setIssue((String) request.get("issue"));
        sh.setNotes((String) request.get("notes"));
        sh.setCreatedBy("system");

        // Get recipient name
        if (caseEntity.getRecipientId() != null) {
            recipientRepository.findById(caseEntity.getRecipientId()).ifPresent(r ->
                sh.setRecipientName(r.getFullName()));
        }

        // Status auto-calculated via @PrePersist (BR CM 04/05)
        StateHearingEntity saved = stateHearingRepository.save(sh);

        // Update CaseEntity.stateHearing aggregate status
        updateCaseHearingStatus(caseEntity);

        log.info("Created State Hearing {} (number={}) for case {} — status={}",
            saved.getId(), appealNumber, caseId, saved.getStatus());
        return ResponseEntity.ok(toDetailMap(saved));
    }

    // ==================== MODIFY (Modify State Hearing — CI-67689) ====================

    /**
     * Modify State Hearing — per DSD CI-67689
     *
     * DSD Screen: Number (read-only), Request Date (read-only), Status (read-only/auto-calculated)
     * Editable: Scheduled Hearing Date, Rescheduled Reason, Outcome, Outcome Date, Compliance Form Sent Date
     *
     * Error Messages (Modify):
     *   EM CM 029: Scheduled Hearing Date cannot be before the Request Date
     *   EM CM 030: Scheduled Hearing Date is required when a Reschedule Reason is indicated
     *   EM CM 031: Reschedule Reason required when the Scheduled Hearing Date is updated
     *   EM CM 032: A Scheduled Hearing Date is required for this Outcome (except Conditional Withdrawal, Erroneous Entry, Complete Withdrawal)
     *   EM CM 033: Outcome Date cannot be before the Request Date
     *   EM CM 035: Compliance Form Sent Date cannot be before the Outcome Date
     *   EM CM 036: Outcome Date is required when the outcome is indicated (except Pending, Erroneous Entry)
     *   EM CM 037: Outcome is required when the Outcome Date is indicated
     *
     * Business Rules:
     *   BR CM 06: When Scheduled Hearing Date not blank → Set status to "Scheduled"
     *   BR CM 06a: When outcome other than Pending + Outcome Date → Set status to "Resolved"
     *              + Set all fields except Compliance Form Sent Date to non-editable
     */
    @PutMapping("/{id}")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> modifyStateHearing(@PathVariable Long id,
                                                 @RequestBody Map<String, Object> request) {
        Optional<StateHearingEntity> opt = stateHearingRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StateHearingEntity sh = opt.get();

        // --- BR CM 06a: If already Resolved, only Compliance Form Sent Date is editable ---
        if (sh.isResolved()) {
            String compDateStr = (String) request.get("complianceFormSentDate");
            if (compDateStr != null && !compDateStr.isBlank()) {
                LocalDate compDate = LocalDate.parse(compDateStr);

                // --- EM CM 035: Compliance Form Sent Date cannot be before the Outcome Date ---
                if (sh.getOutcomeDate() != null && compDate.isBefore(sh.getOutcomeDate())) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "EM CM 035: Compliance Form Sent Date cannot be before the Outcome Date"));
                }

                sh.setComplianceFormSentDate(compDate);
                sh.setUpdatedBy("system");
                StateHearingEntity saved = stateHearingRepository.save(sh);
                log.info("Updated Compliance Form Sent Date on Resolved State Hearing {}", id);
                return ResponseEntity.ok(toDetailMap(saved));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "BR CM 06a: State Hearing is Resolved — only Compliance Form Sent Date can be updated"));
            }
        }

        // --- Parse incoming fields ---
        String schedDateStr = (String) request.get("scheduledHearingDate");
        LocalDate newScheduledDate = (schedDateStr != null && !schedDateStr.isBlank())
            ? LocalDate.parse(schedDateStr) : null;

        String rescheduledReason = (String) request.get("rescheduledReason");

        String outcomeCode = (String) request.get("hearingOutcome");

        String outDateStr = (String) request.get("outcomeDate");
        LocalDate outcomeDate = (outDateStr != null && !outDateStr.isBlank())
            ? LocalDate.parse(outDateStr) : null;

        String compDateStr = (String) request.get("complianceFormSentDate");
        LocalDate complianceDate = (compDateStr != null && !compDateStr.isBlank())
            ? LocalDate.parse(compDateStr) : null;

        // ---- VALIDATION: EM CM 029-037 ----

        // --- EM CM 029: Scheduled Hearing Date cannot be before the Request Date ---
        if (newScheduledDate != null && newScheduledDate.isBefore(sh.getHearingRequestDate())) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "EM CM 029: Scheduled Hearing Date cannot be before the Request Date"));
        }

        // --- EM CM 030: Scheduled Hearing Date is required when a Reschedule Reason is indicated ---
        if (rescheduledReason != null && !rescheduledReason.isBlank() && newScheduledDate == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "EM CM 030: Scheduled Hearing Date is required when a Reschedule Reason is indicated"));
        }

        // --- EM CM 031: Reschedule Reason required when the Scheduled Hearing Date is updated ---
        boolean scheduledDateChanged = false;
        if (newScheduledDate != null && sh.getScheduledHearingDate() != null
                && !newScheduledDate.equals(sh.getScheduledHearingDate())) {
            scheduledDateChanged = true;
        } else if (newScheduledDate != null && sh.getScheduledHearingDate() == null) {
            // First time setting a scheduled date is not a "reschedule" — no reason needed
            scheduledDateChanged = false;
        }
        if (scheduledDateChanged && (rescheduledReason == null || rescheduledReason.isBlank())) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "EM CM 031: Reschedule Reason is required when the Scheduled Hearing Date is updated"));
        }

        // --- EM CM 032: A Scheduled Hearing Date is required for this Outcome ---
        // (except Conditional Withdrawal EO502, Erroneous Entry EO506, Complete Withdrawal EO503)
        if (outcomeCode != null && !outcomeCode.isBlank() && !"EO501".equals(outcomeCode)) {
            if (!CaseCodeTables.OUTCOMES_NO_HEARING_DATE_REQUIRED.contains(outcomeCode)) {
                LocalDate effectiveScheduledDate = newScheduledDate != null ? newScheduledDate : sh.getScheduledHearingDate();
                if (effectiveScheduledDate == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "EM CM 032: A Scheduled Hearing Date is required for this Outcome"));
                }
            }
        }

        // --- EM CM 033: Outcome Date cannot be before the Request Date ---
        if (outcomeDate != null && outcomeDate.isBefore(sh.getHearingRequestDate())) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "EM CM 033: Outcome Date cannot be before the Request Date"));
        }

        // --- EM CM 035: Compliance Form Sent Date cannot be before the Outcome Date ---
        if (complianceDate != null && outcomeDate != null && complianceDate.isBefore(outcomeDate)) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "EM CM 035: Compliance Form Sent Date cannot be before the Outcome Date"));
        }

        // --- EM CM 036: Outcome Date is required when the outcome is indicated ---
        // (except Pending EO501, Erroneous Entry EO506)
        if (outcomeCode != null && !outcomeCode.isBlank()) {
            if (!CaseCodeTables.OUTCOMES_NO_OUTCOME_DATE_REQUIRED.contains(outcomeCode)) {
                if (outcomeDate == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "EM CM 036: Outcome Date is required when the Outcome is indicated"));
                }
            }
        }

        // --- EM CM 037: Outcome is required when the Outcome Date is indicated ---
        if (outcomeDate != null && (outcomeCode == null || outcomeCode.isBlank() || "EO501".equals(outcomeCode))) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "EM CM 037: Outcome is required when the Outcome Date is indicated"));
        }

        // ---- HEARING HISTORY: Track rescheduled dates ----
        // If the scheduled date is being changed and the old one existed, save it to history
        if (scheduledDateChanged && sh.getScheduledHearingDate() != null) {
            CaseAppealHearingHistory history = new CaseAppealHearingHistory();
            history.setAppealId(sh.getId());
            history.setHearingDate(sh.getScheduledHearingDate());
            // Use the old rescheduled reason if available, otherwise the new one
            history.setRescheduledReason(
                rescheduledReason != null ? rescheduledReason : sh.getRescheduledReason());
            hearingHistoryRepository.save(history);
            log.info("Saved hearing history: appeal={}, oldDate={}, reason={}",
                sh.getId(), sh.getScheduledHearingDate(), rescheduledReason);
        }

        // ---- UPDATE FIELDS ----
        if (newScheduledDate != null) {
            sh.setScheduledHearingDate(newScheduledDate);
        }
        if (rescheduledReason != null) {
            sh.setRescheduledReason(rescheduledReason);
        }
        if (outcomeCode != null) {
            sh.setHearingOutcome(outcomeCode);
        }
        if (outcomeDate != null) {
            sh.setOutcomeDate(outcomeDate);
        }
        if (complianceDate != null) {
            sh.setComplianceFormSentDate(complianceDate);
        }

        // Notes and issue can be updated
        if (request.containsKey("notes")) {
            sh.setNotes((String) request.get("notes"));
        }
        if (request.containsKey("issue")) {
            sh.setIssue((String) request.get("issue"));
        }

        sh.setUpdatedBy("system");

        // Status auto-recalculated via @PreUpdate (BR CM 06/06a)
        StateHearingEntity saved = stateHearingRepository.save(sh);

        // Update case-level aggregate status
        CaseEntity caseEntity = caseRepository.findById(sh.getCaseId()).orElse(null);
        if (caseEntity != null) {
            updateCaseHearingStatus(caseEntity);
        }

        log.info("Modified State Hearing {} — status={}, outcome={}",
            id, saved.getStatus(), saved.getHearingOutcome());
        return ResponseEntity.ok(toDetailMap(saved));
    }

    // ==================== CODE TABLES ====================

    @GetMapping("/code-tables/status")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getStateHearingStatusCodes() {
        List<Map<String, String>> codes = CaseCodeTables.STATE_HEARING_STATUS.entrySet().stream()
                .map(e -> Map.of("code", e.getKey(), "description", e.getValue()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(codes);
    }

    @GetMapping("/code-tables/outcome")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getOutcomeCodes() {
        List<Map<String, String>> codes = CaseCodeTables.CASE_APPEAL_OUTCOME.entrySet().stream()
                .map(e -> Map.of("code", e.getKey(), "description", e.getValue()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(codes);
    }

    @GetMapping("/code-tables/rescheduled-reason")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> getRescheduledReasonCodes() {
        List<Map<String, String>> codes = CaseCodeTables.RESCHEDULED_REASON.entrySet().stream()
                .map(e -> Map.of("code", e.getKey(), "description", e.getValue()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(codes);
    }

    // ==================== SEED DATA ====================

    @PostConstruct
    public void seedStateHearingData() {
        if (stateHearingRepository.count() > 0) {
            log.info("State hearings already seeded ({} records)", stateHearingRepository.count());
            return;
        }

        log.info("Seeding mock state hearing data...");

        List<CaseEntity> allCases = caseRepository.findAll();
        if (allCases.isEmpty()) {
            log.warn("No cases found — state hearing seed data deferred");
            return;
        }

        LocalDate today = LocalDate.now();
        Random rng = new Random(42);

        String[] issues = {
            "Denial of IHSS services",
            "Reduction of authorized hours",
            "Termination of IHSS services",
            "Share of Cost determination",
            "Protective supervision denial",
            "Paramedical services denial",
            "Change in authorized tasks",
            "Provider payment dispute"
        };

        // DSD outcome codes for resolved hearings
        String[] resolvedOutcomes = {"EO2", "EO3", "EO502", "EO503", "EO504", "EO505"};

        int seeded = 0;
        int appealCounter = 1000;

        for (CaseEntity c : allCases) {
            int count = 1 + rng.nextInt(3);
            for (int i = 0; i < count; i++) {
                StateHearingEntity sh = new StateHearingEntity();
                sh.setAppealNumber("SH-" + (++appealCounter));
                sh.setCaseId(c.getId());
                sh.setCaseNumber(c.getCaseNumber());
                sh.setRecipientId(c.getRecipientId());
                sh.setCountyCode(c.getCountyCode());
                sh.setCreatedBy("seed");

                if (c.getRecipientId() != null) {
                    recipientRepository.findById(c.getRecipientId()).ifPresent(r ->
                        sh.setRecipientName(r.getFullName()));
                }

                int daysAgo = 1 + rng.nextInt(180);
                sh.setHearingRequestDate(today.minusDays(daysAgo));
                sh.setIssue(issues[rng.nextInt(issues.length)]);

                // DSD: Outcome defaults to Pending on Create
                sh.setHearingOutcome("EO501");

                int statusType = rng.nextInt(4);
                switch (statusType) {
                    case 0: // Requested only — Pending outcome, no schedule
                        break;
                    case 1: // Scheduled — has future scheduled date
                        sh.setScheduledHearingDate(today.plusDays(5 + rng.nextInt(60)));
                        break;
                    case 2: // Scheduled (past date) — still pending outcome
                        sh.setScheduledHearingDate(today.minusDays(1 + rng.nextInt(30)));
                        break;
                    case 3: // Resolved — has outcome (not Pending) + outcome date
                        sh.setScheduledHearingDate(today.minusDays(30 + rng.nextInt(90)));
                        sh.setHearingOutcome(resolvedOutcomes[rng.nextInt(resolvedOutcomes.length)]);
                        sh.setOutcomeDate(sh.getScheduledHearingDate().plusDays(1 + rng.nextInt(14)));
                        break;
                }

                stateHearingRepository.save(sh);
                seeded++;
            }

            updateCaseHearingStatus(c);
        }

        log.info("Seeded {} state hearing records across {} cases", seeded, allCases.size());
    }

    // ==================== HELPERS ====================

    /** Update the CaseEntity.stateHearing aggregate field based on all hearings for the case */
    private void updateCaseHearingStatus(CaseEntity caseEntity) {
        List<StateHearingEntity> caseHearings = stateHearingRepository.findByCaseId(caseEntity.getId());
        boolean hasRequested = caseHearings.stream().anyMatch(h -> "REQUESTED".equals(h.getStatus()));
        boolean hasScheduled = caseHearings.stream().anyMatch(h -> "SCHEDULED".equals(h.getStatus()));
        if (hasRequested && hasScheduled) {
            caseEntity.setStateHearing("Requested And Scheduled");
        } else if (hasScheduled) {
            caseEntity.setStateHearing("Scheduled");
        } else if (hasRequested) {
            caseEntity.setStateHearing("Requested");
        } else {
            caseEntity.setStateHearing("Resolved");
        }
        caseRepository.save(caseEntity);
    }

    private String mapSshsToStatus(String sshsCode) {
        return switch (sshsCode) {
            case "SSHS001" -> "REQUESTED";
            case "SSHS002" -> "SCHEDULED";
            case "SSHS003" -> "RESOLVED";
            case "SSHS004" -> null; // handled specially
            default -> null;
        };
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "";
        return switch (status) {
            case "REQUESTED" -> "Requested";
            case "SCHEDULED" -> "Scheduled";
            case "RESOLVED" -> "Resolved";
            default -> status;
        };
    }

    private String getOutcomeDisplay(String outcomeCode) {
        if (outcomeCode == null) return "";
        return CaseCodeTables.CASE_APPEAL_OUTCOME.getOrDefault(outcomeCode, outcomeCode);
    }

    private Map<String, Object> toDetailMap(StateHearingEntity sh) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", sh.getId());
        map.put("appealNumber", sh.getAppealNumber());
        map.put("caseId", sh.getCaseId());
        map.put("caseNumber", sh.getCaseNumber());
        map.put("recipientId", sh.getRecipientId());
        map.put("recipientName", sh.getRecipientName());
        map.put("countyCode", sh.getCountyCode());
        map.put("status", sh.getStatus());
        map.put("statusDisplay", getStatusDisplay(sh.getStatus()));
        map.put("isResolved", sh.isResolved());
        map.put("hearingRequestDate", sh.getHearingRequestDate());
        map.put("scheduledHearingDate", sh.getScheduledHearingDate());
        map.put("hearingOutcome", sh.getHearingOutcome());
        map.put("outcomeDisplay", getOutcomeDisplay(sh.getHearingOutcome()));
        map.put("outcomeDate", sh.getOutcomeDate());
        map.put("rescheduledReason", sh.getRescheduledReason());
        map.put("rescheduledReasonDisplay",
            CaseCodeTables.RESCHEDULED_REASON.getOrDefault(sh.getRescheduledReason(), sh.getRescheduledReason()));
        map.put("issue", sh.getIssue());
        map.put("notes", sh.getNotes());
        map.put("complianceFormSentDate", sh.getComplianceFormSentDate());
        map.put("createdAt", sh.getCreatedAt());
        map.put("createdBy", sh.getCreatedBy());
        map.put("updatedAt", sh.getUpdatedAt());
        return map;
    }
}
