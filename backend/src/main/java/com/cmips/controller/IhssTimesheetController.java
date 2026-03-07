package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.*;
import com.cmips.repository.*;
import com.cmips.service.TimesheetValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * DSD Section 24 — IHSS Time & Attendance Controller
 * Timesheets, validation (50+ rules), hold/release, manual entry,
 * travel claims, FLSA overtime, random sampling, flagged review
 */
@RestController
@RequestMapping("/api/ihss-timesheets")
public class IhssTimesheetController {

    private static final Logger log = LoggerFactory.getLogger(IhssTimesheetController.class);

    @Autowired private IhssTimesheetRepository tsRepo;
    @Autowired private TimesheetTimeEntryRepository entryRepo;
    @Autowired private TimesheetExceptionRepository exRepo;
    @Autowired private TravelClaimRepository travelRepo;
    @Autowired private TravelClaimTimeEntryRepository travelEntryRepo;
    @Autowired private TimesheetValidationService validationService;

    // ═══════════════════════════════════════════
    // TIMESHEET SEARCH & LIST
    // ═══════════════════════════════════════════

    @GetMapping("/search")
    public ResponseEntity<List<TimesheetEntity>> searchTimesheets(
            @RequestParam(required = false) Long caseId,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String programType,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String countyCode) {
        TimesheetEntity.TimesheetStatus statusEnum = status != null ? TimesheetEntity.TimesheetStatus.valueOf(status) : null;
        TimesheetEntity.ProgramType progEnum = programType != null ? TimesheetEntity.ProgramType.valueOf(programType) : null;
        LocalDate from = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate to = toDate != null ? LocalDate.parse(toDate) : null;
        return ResponseEntity.ok(tsRepo.searchTimesheets(caseId, recipientId, providerId, statusEnum, progEnum, from, to, countyCode));
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<TimesheetEntity>> getByCase(@PathVariable Long caseId) {
        return ResponseEntity.ok(tsRepo.findByCaseIdOrderByPayPeriodStartDesc(caseId));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<TimesheetEntity>> getByProvider(@PathVariable Long providerId) {
        return ResponseEntity.ok(tsRepo.findByProviderIdOrderByPayPeriodStartDesc(providerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TimesheetEntity>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.valueOf(status)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTimesheet(@PathVariable Long id) {
        return tsRepo.findById(id).map(ts -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("timesheet", ts);
            result.put("timeEntries", entryRepo.findByTimesheetIdOrderByEntryDateAsc(id));
            result.put("exceptions", exRepo.findByTimesheetIdOrderByRuleNumberAsc(id));
            return ResponseEntity.ok(result);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════════════════════════════════
    // MANUAL ENTRY (DSD Rule 54 — PRDS942A)
    // ═══════════════════════════════════════════

    @PostMapping("/manual-entry")
    public ResponseEntity<?> createManualTimesheet(@RequestBody Map<String, Object> body) {
        try {
            TimesheetEntity ts = new TimesheetEntity();
            ts.setCaseId(getLong(body, "caseId"));
            ts.setRecipientId(getLong(body, "recipientId"));
            ts.setProviderId(getLong(body, "providerId"));
            ts.setProgramType(body.containsKey("programType")
                    ? TimesheetEntity.ProgramType.valueOf((String) body.get("programType")) : TimesheetEntity.ProgramType.IHSS);
            ts.setTimesheetType(body.containsKey("timesheetType")
                    ? TimesheetEntity.TimesheetType.valueOf((String) body.get("timesheetType")) : TimesheetEntity.TimesheetType.STANDARD);
            ts.setPayPeriodStart(LocalDate.parse((String) body.get("payPeriodStart")));
            ts.setPayPeriodEnd(LocalDate.parse((String) body.get("payPeriodEnd")));
            ts.setServiceMonth(ts.getPayPeriodStart().format(DateTimeFormatter.ofPattern("yyyy-MM")));
            ts.setSourceType(TimesheetEntity.SourceType.MANUAL_ENTRY);
            ts.setModeOfEntry("MANUAL");
            ts.setStatus(TimesheetEntity.TimesheetStatus.RECEIVED);
            ts.setDateReceived(LocalDate.now());
            ts.setProviderSignaturePresent(getBool(body, "providerSignaturePresent", true));
            ts.setRecipientSignaturePresent(getBool(body, "recipientSignaturePresent", true));
            ts.setRecipientIsBvi(getBool(body, "recipientIsBvi", false));
            ts.setIsSupplemental(getBool(body, "isSupplemental", false));
            ts.setFlaggedForReview(getBool(body, "flaggedForReview", false));
            ts.setAuthorizedHoursMonthly(getDbl(body, "authorizedHoursMonthly"));
            ts.setAssignedHours(getDbl(body, "assignedHours"));
            ts.setRemainingRecipientHours(getDbl(body, "remainingRecipientHours"));
            ts.setRemainingProviderHours(getDbl(body, "remainingProviderHours"));
            ts.setCountyCode((String) body.get("countyCode"));
            ts.setCreatedBy((String) body.get("createdBy"));
            ts.setNotes((String) body.get("notes"));
            if (body.containsKey("originalTimesheetId")) ts.setOriginalTimesheetId(getLong(body, "originalTimesheetId"));

            ts = tsRepo.save(ts);

            // Daily time entries
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dailyEntries = (List<Map<String, Object>>) body.get("dailyEntries");
            double totalClaimed = 0;
            if (dailyEntries != null) {
                int dayNum = 1;
                for (Map<String, Object> de : dailyEntries) {
                    TimesheetTimeEntryEntity e = new TimesheetTimeEntryEntity();
                    e.setTimesheetId(ts.getId());
                    e.setEntryDate(LocalDate.parse((String) de.get("date")));
                    e.setHoursClaimed(getDbl(de, "hours"));
                    e.setMinutesClaimed(de.containsKey("minutes") ? ((Number) de.get("minutes")).intValue() : null);
                    e.setDayOfPeriod(dayNum++);
                    e.setRecipientEligible(getBool(de, "recipientEligible", true));
                    e.setProviderEligible(getBool(de, "providerEligible", true));
                    e.setRecipientOnLeave(getBool(de, "recipientOnLeave", false));
                    e.setProviderOnLeave(getBool(de, "providerOnLeave", false));
                    DayOfWeek dow = e.getEntryDate().getDayOfWeek();
                    e.setDayOfWeek(dow.getValue());
                    e.setWorkWeekNumber((int) (ChronoUnit.DAYS.between(ts.getPayPeriodStart(), e.getEntryDate()) / 7) + 1);
                    e.setIsFutureDay(e.getEntryDate().isAfter(LocalDate.now()));
                    e.setIsWeekend(dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
                    entryRepo.save(e);
                    totalClaimed += (e.getHoursClaimed() != null ? e.getHoursClaimed() : 0);
                }
            }
            ts.setTotalHoursClaimed(totalClaimed);
            tsRepo.save(ts);

            log.info("[IHSS-TS] Created manual timesheet {} case={} provider={} hours={}", ts.getTimesheetNumber(), ts.getCaseId(), ts.getProviderId(), totalClaimed);
            return ResponseEntity.ok(ts);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // ═══════════════════════════════════════════
    // VALIDATION (50+ DSD Rules)
    // ═══════════════════════════════════════════

    @PostMapping("/{id}/validate")
    public ResponseEntity<?> validateTimesheet(@PathVariable Long id) {
        return tsRepo.findById(id).map(ts -> {
            exRepo.deleteByTimesheetId(id);
            List<TimesheetExceptionEntity> exceptions = validationService.validateTimesheet(ts);
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("timesheetId", id);
            r.put("status", ts.getStatus());
            r.put("totalExceptions", exceptions.size());
            r.put("hardEdits", exceptions.stream().filter(e -> e.getExceptionType() == TimesheetExceptionEntity.ExceptionType.HARD_EDIT).count());
            r.put("softEdits", exceptions.stream().filter(e -> e.getExceptionType() == TimesheetExceptionEntity.ExceptionType.SOFT_EDIT).count());
            r.put("holdConditions", exceptions.stream().filter(e -> e.getExceptionType() == TimesheetExceptionEntity.ExceptionType.HOLD_CONDITION).count());
            r.put("totalHoursApproved", ts.getTotalHoursApproved());
            r.put("exceptions", exceptions);
            return ResponseEntity.ok(r);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════════════════════════════════
    // HOLD / RELEASE / REJECT / VOID
    // ═══════════════════════════════════════════

    @PostMapping("/{id}/release")
    public ResponseEntity<?> releaseHeld(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        return tsRepo.findById(id).map(ts -> {
            if (!ts.getStatus().name().startsWith("HOLD_"))
                return ResponseEntity.badRequest().body(Map.of("error", "Not in HOLD status"));
            String by = body != null ? (String) body.get("releasedBy") : "SYSTEM";
            ts.setHoldReleaseDate(LocalDateTime.now());
            ts.setHoldReleaseBy(by);
            ts.setHasHoldCondition(false);
            ts.setStatus(TimesheetEntity.TimesheetStatus.VALIDATING);
            tsRepo.save(ts);
            exRepo.deleteByTimesheetId(id);
            validationService.validateTimesheet(ts);
            log.info("[IHSS-TS] Released {} by {}. New status={}", ts.getTimesheetNumber(), by, ts.getStatus());
            return ResponseEntity.ok(ts);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return tsRepo.findById(id).map(ts -> {
            ts.setStatus(TimesheetEntity.TimesheetStatus.REJECTED);
            ts.setRejectionReason((String) body.get("reason"));
            ts.setLastModifiedBy((String) body.get("rejectedBy"));
            tsRepo.save(ts);
            return ResponseEntity.ok(ts);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/void")
    public ResponseEntity<?> voidTs(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return tsRepo.findById(id).map(ts -> {
            ts.setStatus(TimesheetEntity.TimesheetStatus.VOID);
            ts.setNotes("VOID: " + body.getOrDefault("reason", "Voided"));
            ts.setLastModifiedBy((String) body.get("voidedBy"));
            tsRepo.save(ts);
            return ResponseEntity.ok(ts);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════════════════════════════════
    // SEND TO PAYROLL (PRDS109A / PRDS942A)
    // ═══════════════════════════════════════════

    @PostMapping("/{id}/send-to-payroll")
    public ResponseEntity<?> sendToPayroll(@PathVariable Long id) {
        return tsRepo.findById(id).map(ts -> {
            if (ts.getStatus() != TimesheetEntity.TimesheetStatus.APPROVED_FOR_PAYROLL)
                return ResponseEntity.badRequest().body(Map.of("error", "Must be APPROVED_FOR_PAYROLL"));
            ts.setStatus(TimesheetEntity.TimesheetStatus.SENT_TO_PAYROLL);
            ts.setDateSentToPayroll(LocalDateTime.now());
            String iface = ts.getSourceType() == TimesheetEntity.SourceType.MANUAL_ENTRY ? "PRDS942A" : "PRDS109A";
            computeOvertime(ts);
            tsRepo.save(ts);
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("timesheetId", id);
            r.put("status", "SENT_TO_PAYROLL");
            r.put("interface", iface);
            r.put("regularHours", ts.getRegularHours());
            r.put("overtimeHours", ts.getOvertimeHours());
            r.put("socDeductionApplies", ts.getSocDeductionApplies());
            r.put("sentAt", ts.getDateSentToPayroll());
            log.info("[IHSS-TS] {} sent via {}. Reg={} OT={}", ts.getTimesheetNumber(), iface, ts.getRegularHours(), ts.getOvertimeHours());
            return ResponseEntity.ok(r);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════════════════════════════════
    // FLSA OVERTIME CALCULATION
    // ═══════════════════════════════════════════

    @GetMapping("/{id}/overtime")
    public ResponseEntity<?> getOvertime(@PathVariable Long id) {
        return tsRepo.findById(id).map(ts -> {
            List<TimesheetTimeEntryEntity> entries = entryRepo.findByTimesheetIdOrderByEntryDateAsc(id);
            Map<Integer, Double> weekly = new TreeMap<>();
            for (TimesheetTimeEntryEntity e : entries) {
                int wk = e.getWorkWeekNumber() != null ? e.getWorkWeekNumber() : 1;
                weekly.merge(wk, e.getHoursApproved() != null ? e.getHoursApproved() : 0, Double::sum);
            }
            List<Map<String, Object>> weeks = new ArrayList<>();
            double totalReg = 0, totalOt = 0;
            for (Map.Entry<Integer, Double> w : weekly.entrySet()) {
                double reg = Math.min(w.getValue(), 40.0);
                double ot = Math.max(0, w.getValue() - 40.0);
                totalReg += reg; totalOt += ot;
                weeks.add(Map.of("week", w.getKey(), "totalHours", w.getValue(), "regularHours", reg, "overtimeHours", ot));
            }
            return ResponseEntity.ok(Map.of("timesheetId", id, "weekBreakdown", weeks,
                    "totalRegularHours", totalReg, "totalOvertimeHours", totalOt, "flsaApplicable", totalOt > 0));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════════════════════════════════
    // RANDOM SAMPLING
    // ═══════════════════════════════════════════

    @GetMapping("/sampling/pending")
    public ResponseEntity<List<TimesheetEntity>> samplingPending() {
        return ResponseEntity.ok(tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.HOLD_RANDOM_SAMPLING));
    }

    @PostMapping("/{id}/sampling-verify")
    public ResponseEntity<?> samplingVerify(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return tsRepo.findById(id).map(ts -> {
            if (ts.getStatus() != TimesheetEntity.TimesheetStatus.HOLD_RANDOM_SAMPLING)
                return ResponseEntity.badRequest().body(Map.of("error", "Not held for sampling"));
            ts.setSamplingVerified(true);
            ts.setSamplingVerifiedBy((String) body.get("verifiedBy"));
            if ("REJECT".equals(body.get("action"))) {
                ts.setStatus(TimesheetEntity.TimesheetStatus.REJECTED);
                ts.setRejectionReason("Rejected during sampling: " + body.getOrDefault("reason", ""));
            } else {
                ts.setHoldReleaseDate(LocalDateTime.now());
                ts.setHoldReleaseBy((String) body.get("verifiedBy"));
                ts.setHasHoldCondition(false);
                ts.setStatus(TimesheetEntity.TimesheetStatus.APPROVED_FOR_PAYROLL);
            }
            tsRepo.save(ts);
            return ResponseEntity.ok(ts);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════════════════════════════════
    // FLAGGED REVIEW
    // ═══════════════════════════════════════════

    @GetMapping("/flagged/pending")
    public ResponseEntity<List<TimesheetEntity>> flaggedPending() {
        return ResponseEntity.ok(tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.HOLD_FLAGGED_REVIEW));
    }

    @PostMapping("/{id}/flagged-review")
    public ResponseEntity<?> flaggedReview(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return tsRepo.findById(id).map(ts -> {
            if (ts.getStatus() != TimesheetEntity.TimesheetStatus.HOLD_FLAGGED_REVIEW)
                return ResponseEntity.badRequest().body(Map.of("error", "Not held for flagged review"));
            ts.setReviewCompleted(true);
            ts.setReviewedBy((String) body.get("reviewedBy"));
            if ("REJECT".equals(body.get("action"))) {
                ts.setStatus(TimesheetEntity.TimesheetStatus.REJECTED);
                ts.setRejectionReason("Rejected during flagged review: " + body.getOrDefault("reason", ""));
            } else {
                ts.setHoldReleaseDate(LocalDateTime.now());
                ts.setHoldReleaseBy((String) body.get("reviewedBy"));
                ts.setHasHoldCondition(false);
                ts.setStatus(TimesheetEntity.TimesheetStatus.APPROVED_FOR_PAYROLL);
            }
            tsRepo.save(ts);
            return ResponseEntity.ok(ts);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════════════════════════════════
    // BATCH: Auto-Release Held Timesheets
    // ═══════════════════════════════════════════

    @PostMapping("/batch/release-held")
    public ResponseEntity<?> batchRelease() {
        List<TimesheetEntity> held = tsRepo.findHeldTimesheetsReadyForRelease();
        int count = 0;
        for (TimesheetEntity ts : held) {
            ts.setHoldReleaseDate(LocalDateTime.now());
            ts.setHoldReleaseBy("SYSTEM");
            ts.setHasHoldCondition(false);
            ts.setStatus(TimesheetEntity.TimesheetStatus.VALIDATING);
            tsRepo.save(ts);
            exRepo.deleteByTimesheetId(ts.getId());
            validationService.validateTimesheet(ts);
            count++;
        }
        log.info("[IHSS-TS] Batch released {} held timesheets", count);
        return ResponseEntity.ok(Map.of("released", count));
    }

    // ═══════════════════════════════════════════
    // TRAVEL CLAIMS (SOC 2275)
    // ═══════════════════════════════════════════

    @GetMapping("/travel-claims/search")
    public ResponseEntity<List<TravelClaimEntity>> searchTravel(
            @RequestParam(required = false) Long caseId, @RequestParam(required = false) Long providerId,
            @RequestParam(required = false) String status, @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        TravelClaimEntity.TravelClaimStatus s = status != null ? TravelClaimEntity.TravelClaimStatus.valueOf(status) : null;
        LocalDate f = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate t = toDate != null ? LocalDate.parse(toDate) : null;
        return ResponseEntity.ok(travelRepo.searchTravelClaims(caseId, providerId, s, f, t));
    }

    @GetMapping("/travel-claims/case/{caseId}")
    public ResponseEntity<List<TravelClaimEntity>> travelByCase(@PathVariable Long caseId) {
        return ResponseEntity.ok(travelRepo.findByCaseIdOrderByPayPeriodStartDesc(caseId));
    }

    @GetMapping("/travel-claims/{id}")
    public ResponseEntity<?> getTravelClaim(@PathVariable Long id) {
        return travelRepo.findById(id).map(tc -> {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("travelClaim", tc);
            r.put("timeEntries", travelEntryRepo.findByTravelClaimIdOrderByEntryDateAsc(id));
            r.put("exceptions", exRepo.findByTravelClaimIdOrderByRuleNumberAsc(id));
            return ResponseEntity.ok(r);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/travel-claims")
    public ResponseEntity<?> createTravelClaim(@RequestBody Map<String, Object> body) {
        try {
            TravelClaimEntity tc = new TravelClaimEntity();
            tc.setCaseId(getLong(body, "caseId"));
            tc.setRecipientId(getLong(body, "recipientId"));
            tc.setProviderId(getLong(body, "providerId"));
            tc.setProgramType(body.containsKey("programType")
                    ? TravelClaimEntity.ProgramType.valueOf((String) body.get("programType")) : TravelClaimEntity.ProgramType.IHSS);
            tc.setPayPeriodStart(LocalDate.parse((String) body.get("payPeriodStart")));
            tc.setPayPeriodEnd(LocalDate.parse((String) body.get("payPeriodEnd")));
            tc.setDateReceived(LocalDate.now());
            tc.setStatus(TravelClaimEntity.TravelClaimStatus.RECEIVED);
            tc.setProviderEligibleForTravel(getBool(body, "providerEligibleForTravel", true));
            tc.setCountyCode((String) body.get("countyCode"));
            tc.setCreatedBy((String) body.get("createdBy"));
            if (body.containsKey("timesheetId")) tc.setTimesheetId(getLong(body, "timesheetId"));
            tc = travelRepo.save(tc);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> entries = (List<Map<String, Object>>) body.get("dailyEntries");
            double total = 0;
            if (entries != null) {
                for (Map<String, Object> de : entries) {
                    TravelClaimTimeEntryEntity e = new TravelClaimTimeEntryEntity();
                    e.setTravelClaimId(tc.getId());
                    e.setEntryDate(LocalDate.parse((String) de.get("date")));
                    e.setTravelHoursClaimed(getDbl(de, "hours"));
                    e.setHasPaidServiceHours(getBool(de, "hasPaidServiceHours", true));
                    e.setHasActiveTravelRecord(getBool(de, "hasActiveTravelRecord", true));
                    e.setWorkWeekNumber((int) (ChronoUnit.DAYS.between(tc.getPayPeriodStart(), e.getEntryDate()) / 7) + 1);
                    travelEntryRepo.save(e);
                    total += (e.getTravelHoursClaimed() != null ? e.getTravelHoursClaimed() : 0);
                }
            }
            tc.setTotalTravelHoursClaimed(total);
            travelRepo.save(tc);
            return ResponseEntity.ok(tc);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/travel-claims/{id}/validate")
    public ResponseEntity<?> validateTravel(@PathVariable Long id) {
        return travelRepo.findById(id).map(tc -> {
            List<TravelClaimTimeEntryEntity> entries = travelEntryRepo.findByTravelClaimIdOrderByEntryDateAsc(id);
            List<TimesheetExceptionEntity> exceptions = validationService.validateTravelClaim(tc, entries);
            travelEntryRepo.saveAll(entries);
            travelRepo.save(tc);
            return ResponseEntity.ok(Map.of("travelClaimId", id, "status", tc.getStatus(),
                    "totalExceptions", exceptions.size(), "exceptions", exceptions));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * DSD Section 24 — Send travel claim to payroll (PRDS108A travel record).
     * Travel claim must be APPROVED_FOR_PAYROLL status.
     */
    @PostMapping("/travel-claims/{id}/send-to-payroll")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<?> sendTravelClaimToPayroll(@PathVariable Long id) {
        return travelRepo.findById(id).map(tc -> {
            if (tc.getStatus() != TravelClaimEntity.TravelClaimStatus.APPROVED_FOR_PAYROLL) {
                return ResponseEntity.badRequest().body(Map.of("error", "Travel claim must be APPROVED_FOR_PAYROLL"));
            }
            tc.setStatus(TravelClaimEntity.TravelClaimStatus.SENT_TO_PAYROLL);
            tc.setDateSentToPayroll(LocalDateTime.now());
            travelRepo.save(tc);

            // Generate PRDS108A-compatible travel payroll record
            String payrollRecord = String.format("TC|%s|%s|%s|%s|%s|%s|%.2f|%.2f|%s",
                    tc.getTravelClaimNumber(), tc.getProviderId(), tc.getRecipientId(),
                    tc.getCaseId(), tc.getPayPeriodStart(), tc.getPayPeriodEnd(),
                    tc.getTotalTravelHoursApproved() != null ? tc.getTotalTravelHoursApproved() : 0.0,
                    tc.getTravelHoursCutback() != null ? tc.getTravelHoursCutback() : 0.0,
                    tc.getProgramType());

            Map<String, Object> r = new LinkedHashMap<>();
            r.put("travelClaimId", id);
            r.put("travelClaimNumber", tc.getTravelClaimNumber());
            r.put("status", tc.getStatus());
            r.put("dateSentToPayroll", tc.getDateSentToPayroll());
            r.put("interfaceType", "PRDS108A-TC");
            r.put("payrollRecord", payrollRecord);
            return ResponseEntity.ok(r);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════════════════════════════════
    // DASHBOARD
    // ═══════════════════════════════════════════

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("pendingValidation", tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.RECEIVED).size());
        s.put("inException", tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.EXCEPTION).size());
        s.put("heldEarly", tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.HOLD_EARLY_SUBMISSION).size());
        s.put("heldLate", tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.HOLD_LATE_SUBMISSION).size());
        s.put("heldExcessive", tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.HOLD_EXCESSIVE_HOURS).size());
        s.put("heldSampling", tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.HOLD_RANDOM_SAMPLING).size());
        s.put("heldFlagged", tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.HOLD_FLAGGED_REVIEW).size());
        s.put("heldBvi", tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.HOLD_BVI_REVIEW).size());
        s.put("approvedForPayroll", tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.APPROVED_FOR_PAYROLL).size());
        s.put("sentToPayroll", tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.SENT_TO_PAYROLL).size());
        s.put("processed", tsRepo.findByStatusOrderByCreatedAtDesc(TimesheetEntity.TimesheetStatus.PROCESSED).size());
        return ResponseEntity.ok(s);
    }

    // ═══════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════

    private void computeOvertime(TimesheetEntity ts) {
        List<TimesheetTimeEntryEntity> entries = entryRepo.findByTimesheetIdOrderByEntryDateAsc(ts.getId());
        Map<Integer, Double> weekly = new TreeMap<>();
        for (TimesheetTimeEntryEntity e : entries) {
            int wk = e.getWorkWeekNumber() != null ? e.getWorkWeekNumber() : 1;
            weekly.merge(wk, e.getHoursApproved() != null ? e.getHoursApproved() : 0, Double::sum);
        }
        double reg = 0, ot = 0;
        for (double h : weekly.values()) { reg += Math.min(h, 40); ot += Math.max(0, h - 40); }
        ts.setRegularHours(reg);
        ts.setOvertimeHours(ot);
        ts.setFlsaOvertimeApplicable(ot > 0);
    }

    private Long getLong(Map<String, Object> m, String k) {
        Object v = m.get(k); return v == null ? null : v instanceof Number ? ((Number)v).longValue() : Long.parseLong(v.toString());
    }
    private Double getDbl(Map<String, Object> m, String k) {
        Object v = m.get(k); return v == null ? null : v instanceof Number ? ((Number)v).doubleValue() : Double.parseDouble(v.toString());
    }
    private Boolean getBool(Map<String, Object> m, String k, boolean def) {
        Object v = m.get(k); return v == null ? def : v instanceof Boolean ? (Boolean)v : Boolean.parseBoolean(v.toString());
    }
}
