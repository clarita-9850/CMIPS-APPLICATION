package com.cmips.service;

import com.cmips.entity.*;
import com.cmips.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * DSD Section 24 — SOC Deduction Evaluation Process
 *
 * Implements SOCDEP Rules 01-30 (process rules) and Triggers 1-17.
 * Called after timesheet validation passes (no hard edits) to determine
 * whether SOC (Share of Cost) deduction applies to the payment.
 *
 * Key concepts:
 * - Funding Source Aid Codes: 2K (Full-scope Medi-Cal SOC), 2L (IHSS SOC),
 *   2M (WPCS SOC), 2N (IHSS Residual)
 * - Eligibility Status digit: '3' = Certified, '5' = Uncertified SOC
 * - SOC Indicator: 'Y' = deduct SOC, 'N' = no deduction
 * - MEDS POS: Medi-Cal Eligibility Data System Point of Service verification
 */
@Service
public class SocDeductionEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(SocDeductionEvaluationService.class);

    private static final Set<String> SOC_AID_CODES = Set.of("2K", "2L", "2M");
    private static final String RESIDUAL_AID_CODE = "2N";
    private static final long MEDS_POS_LOOKBACK_MONTHS = 13;

    @Autowired private IhssTimesheetRepository tsRepo;
    @Autowired private CaseRepository caseRepo;
    @Autowired private TimesheetExceptionRepository exRepo;
    @Autowired private MedsPosApiClient medsPosApiClient;

    /**
     * SOC Deduction Trigger evaluation — DSD Triggers 1-17.
     * Determines whether to run the full SOC Deduction Evaluation Process.
     * Returns the SOC indicator result: 'Y', 'N', or 'EVAL' (needs full evaluation).
     */
    public String evaluateSocTrigger(TimesheetEntity ts) {
        log.info("[SOC-TRIGGER] Evaluating SOC trigger for timesheet {} (type={}, source={})",
                ts.getTimesheetNumber(), ts.getTimesheetType(), ts.getSourceType());

        // Trigger 1: Advance Pay (TPF Paper) → SOC = 'N', A/P Reconciling = 'Y'
        if (Boolean.TRUE.equals(ts.getIsAdvancePay())
                && ts.getSourceType() == TimesheetEntity.SourceType.TPF_PAPER) {
            log.info("[SOC-TRIGGER] Trigger 1: Advance Pay (TPF) → SOC='N'");
            ts.setSocDeductionApplies(false);
            ts.setAdvancePayReconciled(false); // A/P Reconciling indicator = Y (not yet reconciled)
            return "N";
        }

        // Trigger 2: Advance Pay (Electronic) → SOC = 'N', A/P Reconciling = 'Y'
        if (Boolean.TRUE.equals(ts.getIsAdvancePay())
                && ts.getSourceType() == TimesheetEntity.SourceType.ELECTRONIC_ESP) {
            log.info("[SOC-TRIGGER] Trigger 2: Advance Pay (Electronic) → SOC='N'");
            ts.setSocDeductionApplies(false);
            ts.setAdvancePayReconciled(false);
            return "N";
        }

        // Trigger 3: Arrears (TPF Paper) → Trigger SOC Evaluation
        if (ts.getTimesheetType() == TimesheetEntity.TimesheetType.NEXT_ARREARS
                && ts.getSourceType() == TimesheetEntity.SourceType.TPF_PAPER) {
            log.info("[SOC-TRIGGER] Trigger 3: Arrears (TPF) → Evaluate SOC");
            return "EVAL";
        }

        // Trigger 4: Arrears (Electronic) → Trigger SOC Evaluation
        if (ts.getTimesheetType() == TimesheetEntity.TimesheetType.NEXT_ARREARS
                && ts.getSourceType() == TimesheetEntity.SourceType.ELECTRONIC_ESP) {
            log.info("[SOC-TRIGGER] Trigger 4: Arrears (Electronic) → Evaluate SOC");
            return "EVAL";
        }

        // Trigger 5: WPCS (TPF Paper) → Trigger SOC Evaluation
        if (ts.getProgramType() == TimesheetEntity.ProgramType.WPCS
                && ts.getSourceType() == TimesheetEntity.SourceType.TPF_PAPER) {
            log.info("[SOC-TRIGGER] Trigger 5: WPCS (TPF) → Evaluate SOC");
            return "EVAL";
        }

        // Trigger 6: WPCS (Electronic) → Trigger SOC Evaluation
        if (ts.getProgramType() == TimesheetEntity.ProgramType.WPCS
                && ts.getSourceType() == TimesheetEntity.SourceType.ELECTRONIC_ESP) {
            log.info("[SOC-TRIGGER] Trigger 6: WPCS (Electronic) → Evaluate SOC");
            return "EVAL";
        }

        // Trigger 7: Manual Entry (Staff, IHSS Standard) → Trigger SOC Evaluation
        if (ts.getSourceType() == TimesheetEntity.SourceType.MANUAL_ENTRY
                && ts.getProgramType() == TimesheetEntity.ProgramType.IHSS) {
            log.info("[SOC-TRIGGER] Trigger 7: Manual Entry (IHSS) → Evaluate SOC");
            return "EVAL";
        }

        // Trigger 8: Manual Entry (Staff, WPCS) → Trigger SOC Evaluation
        if (ts.getSourceType() == TimesheetEntity.SourceType.MANUAL_ENTRY
                && ts.getProgramType() == TimesheetEntity.ProgramType.WPCS) {
            log.info("[SOC-TRIGGER] Trigger 8: WPCS Manual Entry → Evaluate SOC");
            return "EVAL";
        }

        // Trigger 9: Supplemental (TPF Paper) → Trigger SOC Evaluation
        if (Boolean.TRUE.equals(ts.getIsSupplemental())
                && ts.getSourceType() == TimesheetEntity.SourceType.TPF_PAPER) {
            log.info("[SOC-TRIGGER] Trigger 9: Supplemental (TPF) → Evaluate SOC");
            return "EVAL";
        }

        // Trigger 10: Supplemental (Electronic) → Trigger SOC Evaluation
        if (Boolean.TRUE.equals(ts.getIsSupplemental())
                && ts.getSourceType() == TimesheetEntity.SourceType.ELECTRONIC_ESP) {
            log.info("[SOC-TRIGGER] Trigger 10: Supplemental (Electronic) → Evaluate SOC");
            return "EVAL";
        }

        // Trigger 11: Standard IHSS (TPF Paper) → Trigger SOC Evaluation
        if (ts.getTimesheetType() == TimesheetEntity.TimesheetType.STANDARD
                && ts.getSourceType() == TimesheetEntity.SourceType.TPF_PAPER
                && ts.getProgramType() == TimesheetEntity.ProgramType.IHSS) {
            log.info("[SOC-TRIGGER] Trigger 11: Standard IHSS (TPF) → Evaluate SOC");
            return "EVAL";
        }

        // Trigger 12: Payment Correction – Void → SOC = 'N' (original SOC refunded)
        // Trigger 13: Payment Correction – Void/Reissue → Trigger SOC Evaluation on reissue
        // Trigger 14: Payment Correction – Supplemental → Trigger SOC Evaluation
        // (These are handled by the payment correction workflow, not direct TS processing)

        // Trigger 15: Special Transaction not subject to SOC → SOC = 'N'
        // (Live-in providers, certain EVV exemptions, etc.)
        if (ts.getTimesheetType() == TimesheetEntity.TimesheetType.LIVE_IN) {
            log.info("[SOC-TRIGGER] Trigger 15: Live-In (special transaction) → SOC='N'");
            ts.setSocDeductionApplies(false);
            return "N";
        }

        // Trigger 16: Auto-release from MEDS POS Error Hold → Re-evaluate SOC
        // (Handled when hold is released, calls evaluateSocProcess directly)

        // Trigger 17: EVV Exception → Trigger SOC Evaluation
        if (ts.getTimesheetType() == TimesheetEntity.TimesheetType.EVV_EXCEPTION) {
            log.info("[SOC-TRIGGER] Trigger 17: EVV Exception → Evaluate SOC");
            return "EVAL";
        }

        // Default for standard Electronic ESP IHSS → Evaluate SOC
        if (ts.getSourceType() == TimesheetEntity.SourceType.ELECTRONIC_ESP
                && ts.getProgramType() == TimesheetEntity.ProgramType.IHSS) {
            log.info("[SOC-TRIGGER] Default: Electronic ESP IHSS → Evaluate SOC");
            return "EVAL";
        }

        log.info("[SOC-TRIGGER] No trigger matched → SOC='N'");
        ts.setSocDeductionApplies(false);
        return "N";
    }

    /**
     * SOC Deduction Evaluation Process — DSD SOCDEP Rules 01-30.
     * Called when trigger evaluation returns 'EVAL'.
     * Determines whether SOC deduction applies and the amount.
     */
    public SocEvaluationResult evaluateSocProcess(TimesheetEntity ts) {
        log.info("[SOC-EVAL] Running SOC Deduction Evaluation for timesheet {}", ts.getTimesheetNumber());

        CaseEntity caseEntity = caseRepo.findById(ts.getCaseId()).orElse(null);
        if (caseEntity == null) {
            log.warn("[SOC-EVAL] Case {} not found. SOC='N'", ts.getCaseId());
            ts.setSocDeductionApplies(false);
            return new SocEvaluationResult("N", 0.0, "Case not found");
        }

        String fundingSource = caseEntity.getFundingSource();
        String mediCalStatus = caseEntity.getMediCalStatus();
        Double socAmount = caseEntity.getShareOfCostAmount();

        // SOCDEP 01: Read Funding Source Aid Code from case
        log.info("[SOC-EVAL] SOCDEP 01: fundingSource={}, mediCalStatus={}, socAmount={}",
                fundingSource, mediCalStatus, socAmount);

        // SOCDEP 28: No SOC — eligibility status NOT '3' or '5'
        if (mediCalStatus == null || (!mediCalStatus.contains("3") && !mediCalStatus.contains("5"))) {
            log.info("[SOC-EVAL] SOCDEP 28: No SOC status (not '3' or '5') → SOC='N'");
            ts.setSocDeductionApplies(false);
            ts.setSocCertified(false);
            return new SocEvaluationResult("N", 0.0, "Medi-Cal status does not indicate SOC");
        }

        // SOCDEP 16/17: IHSS Residual (Aid Code 2N)
        if (RESIDUAL_AID_CODE.equals(fundingSource)) {
            return evaluateResidualSoc(ts, caseEntity, mediCalStatus, socAmount);
        }

        // SOCDEP 02-15: Standard SOC evaluation (Aid Codes 2K, 2L, 2M)
        if (fundingSource != null && SOC_AID_CODES.contains(fundingSource)) {
            return evaluateStandardSoc(ts, caseEntity, mediCalStatus, socAmount);
        }

        // No matching aid code → no SOC
        log.info("[SOC-EVAL] Funding source '{}' not SOC-applicable → SOC='N'", fundingSource);
        ts.setSocDeductionApplies(false);
        return new SocEvaluationResult("N", 0.0, "Funding source not SOC-applicable");
    }

    /**
     * SOCDEP 02-15: Standard SOC Evaluation for aid codes 2K/2L/2M.
     */
    private SocEvaluationResult evaluateStandardSoc(TimesheetEntity ts, CaseEntity caseEntity,
                                                     String mediCalStatus, Double socAmount) {
        // SOCDEP 02: Certified Medi-Cal SOC (eligibility status '3')
        if (mediCalStatus.contains("3")) {
            log.info("[SOC-EVAL] SOCDEP 02: Certified SOC (status '3')");
            ts.setSocCertified(true);

            // SOCDEP 03: Check non-reversed SOC amount
            if (socAmount != null && socAmount > 0) {
                log.info("[SOC-EVAL] SOCDEP 03: Non-reversed SOC amount={} > 0 → SOC='Y'", socAmount);
                ts.setSocDeductionApplies(true);
                ts.setSocAmount(socAmount);
                return new SocEvaluationResult("Y", socAmount, "Certified SOC — deduction applies");
            } else {
                // SOCDEP 03a: SOC amount is zero or reversed → SOC='N'
                log.info("[SOC-EVAL] SOCDEP 03a: SOC amount zero/reversed → SOC='N'");
                ts.setSocDeductionApplies(false);
                ts.setSocAmount(0.0);
                return new SocEvaluationResult("N", 0.0, "Certified SOC but amount is zero/met");
            }
        }

        // SOCDEP 04-10: Uncertified SOC (eligibility status '5')
        if (mediCalStatus.contains("5")) {
            log.info("[SOC-EVAL] SOCDEP 04: Uncertified SOC (status '5')");
            ts.setSocCertified(false);

            // SOCDEP 04: Check if pay period is outside 13 months
            LocalDate payPeriodStart = ts.getPayPeriodStart();
            long monthsBetween = ChronoUnit.MONTHS.between(payPeriodStart, LocalDate.now());
            if (monthsBetween > MEDS_POS_LOOKBACK_MONTHS) {
                // SOCDEP 05: Outside 13-month window — check non-reversed SOC
                log.info("[SOC-EVAL] SOCDEP 05: Pay period {} is >13 months ago", payPeriodStart);
                if (socAmount != null && socAmount > 0) {
                    ts.setSocDeductionApplies(true);
                    ts.setSocAmount(socAmount);
                    return new SocEvaluationResult("Y", socAmount,
                            "Uncertified SOC, outside 13-month window, SOC amount > 0");
                } else {
                    ts.setSocDeductionApplies(false);
                    return new SocEvaluationResult("N", 0.0,
                            "Uncertified SOC, outside 13-month window, SOC amount zero");
                }
            }

            // SOCDEP 06: Within 13-month window — process MEDS POS eligibility transaction
            log.info("[SOC-EVAL] SOCDEP 06: Within 13-month window — sending MEDS POS 270 request");
            MedsPosResult medsPosResult = sendMedsPosRequest(ts, caseEntity);

            // SOCDEP 07: Check MEDS POS response
            if (medsPosResult.hasError()) {
                // SOCDEP 29: MEDS POS error → Hold
                log.info("[SOC-EVAL] SOCDEP 29: MEDS POS error → Hold-MEDS POS Error");
                ts.setStatus(TimesheetEntity.TimesheetStatus.HOLD_USER_REVIEW);
                ts.setHasHoldCondition(true);
                ts.setHoldReleaseDate(java.time.LocalDateTime.now().plusDays(5));
                ts.setMedsPosVerified(false);

                TimesheetExceptionEntity ex = new TimesheetExceptionEntity();
                ex.setTimesheetId(ts.getId());
                ex.setExceptionType(TimesheetExceptionEntity.ExceptionType.HOLD_CONDITION);
                ex.setSeverity(TimesheetExceptionEntity.ExceptionSeverity.HOLD);
                ex.setRuleNumber(0);
                ex.setErrorCode("MEDS-POS-ERR");
                ex.setMessage("MEDS POS eligibility verification failed: " + medsPosResult.errorMessage
                        + ". Hold for manual review.");
                exRepo.save(ex);

                return new SocEvaluationResult("HOLD", 0.0, "MEDS POS error — held for review");
            }

            // SOCDEP 08: MEDS POS shows previously certified
            if (medsPosResult.previouslyCertified) {
                log.info("[SOC-EVAL] SOCDEP 08: MEDS POS shows previously certified");
                ts.setSocCertified(true);
                ts.setMedsPosVerified(true);
                if (socAmount != null && socAmount > 0) {
                    ts.setSocDeductionApplies(true);
                    ts.setSocAmount(socAmount);
                    return new SocEvaluationResult("Y", socAmount,
                            "MEDS POS confirmed — SOC deduction applies");
                } else {
                    ts.setSocDeductionApplies(false);
                    return new SocEvaluationResult("N", 0.0,
                            "MEDS POS confirmed but SOC amount zero/met");
                }
            }

            // SOCDEP 09: MEDS POS — no certification found
            log.info("[SOC-EVAL] SOCDEP 09: MEDS POS — no certification, non-reversed SOC check");
            ts.setMedsPosVerified(true);
            if (socAmount != null && socAmount > 0) {
                ts.setSocDeductionApplies(true);
                ts.setSocAmount(socAmount);
                return new SocEvaluationResult("Y", socAmount,
                        "Uncertified but non-reversed SOC > 0");
            }

            // SOCDEP 10: No errors, no SOC
            ts.setSocDeductionApplies(false);
            return new SocEvaluationResult("N", 0.0, "MEDS POS verified, no SOC obligation");
        }

        ts.setSocDeductionApplies(false);
        return new SocEvaluationResult("N", 0.0, "No SOC evaluation path matched");
    }

    /**
     * SOCDEP 16/17: IHSS Residual evaluation (Aid Code 2N).
     */
    private SocEvaluationResult evaluateResidualSoc(TimesheetEntity ts, CaseEntity caseEntity,
                                                     String mediCalStatus, Double socAmount) {
        // SOCDEP 16: Zero or met SOC → SOC = 'N'
        if (socAmount == null || socAmount <= 0) {
            log.info("[SOC-EVAL] SOCDEP 16: IHSS Residual (2N), SOC zero/met → SOC='N'");
            ts.setSocDeductionApplies(false);
            return new SocEvaluationResult("N", 0.0, "IHSS Residual — SOC zero or met");
        }

        // SOCDEP 17: Unmet SOC → SOC = 'Y'
        log.info("[SOC-EVAL] SOCDEP 17: IHSS Residual (2N), unmet SOC={} → SOC='Y'", socAmount);
        ts.setSocDeductionApplies(true);
        ts.setSocAmount(socAmount);
        return new SocEvaluationResult("Y", socAmount, "IHSS Residual — unmet SOC deduction applies");
    }

    /**
     * SOCDEP 29/30: Medi-Cal/SOC Details record unavailable handling.
     */
    public void handleMedsPosErrorRelease(TimesheetEntity ts) {
        log.info("[SOC-EVAL] SOCDEP 30: Auto-release from MEDS POS Error Hold for TS {}",
                ts.getTimesheetNumber());
        // Re-evaluate SOC after hold release (Trigger 16)
        String trigger = evaluateSocTrigger(ts);
        if ("EVAL".equals(trigger)) {
            evaluateSocProcess(ts);
        }
    }

    /**
     * MEDS POS 270/271 eligibility inquiry — delegates to MedsPosApiClient.
     * When meds.pos.api.enabled=true, sends real API call to MEDS.
     * When disabled (default), MedsPosApiClient returns mock based on mediCalStatus.
     */
    private MedsPosResult sendMedsPosRequest(TimesheetEntity ts, CaseEntity caseEntity) {
        MedsPosApiClient.MedsPosResponse response = medsPosApiClient.queryEligibility(ts, caseEntity);
        return new MedsPosResult(
                response.previouslyCertified,
                response.hasError,
                response.errorMessage
        );
    }

    /**
     * Full SOC evaluation pipeline — called from TimesheetValidationService.
     */
    public SocEvaluationResult evaluateFullSocPipeline(TimesheetEntity ts) {
        String triggerResult = evaluateSocTrigger(ts);
        if ("EVAL".equals(triggerResult)) {
            return evaluateSocProcess(ts);
        }
        return new SocEvaluationResult(triggerResult, 0.0,
                "N".equals(triggerResult) ? "SOC not applicable per trigger" : "Trigger returned: " + triggerResult);
    }

    // --- Inner classes ---

    public static class SocEvaluationResult {
        public final String indicator; // Y, N, or HOLD
        public final double amount;
        public final String reason;

        public SocEvaluationResult(String indicator, double amount, String reason) {
            this.indicator = indicator;
            this.amount = amount;
            this.reason = reason;
        }
    }

    private static class MedsPosResult {
        final boolean previouslyCertified;
        final boolean hasError;
        final String errorMessage;

        MedsPosResult(boolean previouslyCertified, boolean hasError, String errorMessage) {
            this.previouslyCertified = previouslyCertified;
            this.hasError = hasError;
            this.errorMessage = errorMessage;
        }

        boolean hasError() { return hasError; }
    }
}
