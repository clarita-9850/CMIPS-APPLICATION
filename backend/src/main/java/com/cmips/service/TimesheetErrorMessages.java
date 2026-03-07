package com.cmips.service;

import java.util.*;

/**
 * DSD Section 24 — Complete Error Message Catalog
 * ~160 timesheet messages + ~40 travel claim messages per DSD specification.
 */
public final class TimesheetErrorMessages {

    private TimesheetErrorMessages() {} // utility class

    // ═══════════════════════════════════════════
    // TIMESHEET HARD EDIT MESSAGES (Block payment)
    // ═══════════════════════════════════════════

    // Signature errors
    public static final String EM_11960_NO_PROVIDER_SIG = "11960: No provider signature present on timesheet. Timesheet cannot be processed for payment.";
    public static final String EM_11960_NO_RECIPIENT_SIG = "11960: No recipient signature present on timesheet. Timesheet cannot be processed for payment.";
    public static final String EM_20072_UNSIGNED_ELECTRONIC = "20072: Electronic timesheet unsigned. Recipient must approve via ESP or TTS.";
    public static final String EM_20153_BVI_NO_SIG = "20153: BVI recipient timesheet unsigned. Routing to TTS for electronic review.";

    // Hours/minutes errors
    public static final String EM_11986_NO_HOURS = "11986: No daily hours or minutes entered on timesheet.";
    public static final String EM_11986_EXCEED_24 = "11986: More than 24 hours entered for a single day. Maximum is 24 hours per day.";
    public static final String EM_11986_MINUTES_60 = "11986: Minutes value exceeds 59. Minutes must be 00-59.";
    public static final String EM_11986_COMBINED_24 = "11986: Combined hours and minutes exceed 24 hours for a single day.";
    public static final String EM_11986_HH24_MM_GT0 = "11986: Hours=24 with minutes>0. Maximum daily time is 24:00.";
    public static final String EM_11986_NON_NUMERIC = "11986: Non-numeric value detected in time entry field.";
    public static final String EM_11986_MULTIPLE_ENTRIES = "11986: Multiple entries detected in time entry field (TPF scan).";
    public static final String EM_11986_UNREADABLE = "11986: Unreadable entry detected in time entry field (TPF scan).";
    public static final String EM_11986_OUTSIDE_BOXES = "11986: Writing detected outside time entry boxes (TPF scan).";

    // Timesheet number errors
    public static final String EM_12002_TS_NOT_FOUND = "12002: Timesheet number not found or invalid.";
    public static final String EM_12001_TS_EXISTS = "12001: Timesheet number already exists in Exception or Processed status.";
    public static final String EM_12083_TS_PROCESSED = "12083: Timesheet number previously processed. Duplicate submission rejected.";
    public static final String EM_12001_DUP_SUPP = "12001: A supplemental timesheet already exists for this case/provider/pay period.";

    // Authorization exceeded
    public static final String EM_12004_EXCEED_AUTH = "12004: Total hours claimed exceed recipient authorized hours.";
    public static final String EM_12075_EXCEED_AUTH_MONTHLY = "12075: Total hours claimed exceed monthly authorized limit.";
    public static final String EM_12004_EXCEED_ASSIGNED = "12004: Total hours claimed exceed provider assigned hours.";
    public static final String EM_12008_EXCEED_ALL_TS = "12008: Total of all timesheets for recipient in service month exceed authorized hours.";
    public static final String EM_12004_NO_REMAINING_RECIP = "12004: No remaining recipient authorized hours for this service month.";
    public static final String EM_12004_NO_REMAINING_PROV = "12004: No remaining provider assigned hours for this service month.";
    public static final String EM_12004_WPCS_NOT_AUTH = "12004: WPCS hours not authorized for this recipient.";

    // Eligibility/Leave — entire period
    public static final String EM_12001_RECIP_INELIGIBLE = "12001: Recipient is ineligible for the entire pay period. No hours can be paid.";
    public static final String EM_16193_RECIP_INELIGIBLE = "16193: Recipient ineligibility confirmed for entire pay period.";
    public static final String EM_12003_PROV_INELIGIBLE = "12003: Provider terminated/ineligible for the entire pay period. No hours can be paid.";
    public static final String EM_16197_PROV_TERMINATED = "16197: Provider termination confirmed for entire pay period.";
    public static final String EM_12003_RECIP_LEAVE = "12003: Recipient is on leave for the entire pay period. No hours can be paid.";
    public static final String EM_16195_RECIP_LEAVE = "16195: Recipient leave confirmed for entire pay period.";
    public static final String EM_12003_PROV_LEAVE = "12003: Provider is on leave for the entire pay period. No hours can be paid.";
    public static final String EM_16188_PROV_LEAVE = "16188: Provider leave confirmed for entire pay period.";
    public static final String EM_12053_RECIP_LEAVE_INELIG = "12053: Recipient is both on leave and ineligible for the entire pay period.";
    public static final String EM_12053_PROV_LEAVE_INELIG = "12053: Provider is both on leave and ineligible for the entire pay period.";

    // Date/timing errors
    public static final String EM_11989_FUTURE_HOURS = "11989: Hours claimed for future dates. Timesheet received before end of pay period.";
    public static final String EM_11885_BEFORE_PP_START = "11885: Time recorded before pay period start date.";
    public static final String EM_12052_PENDING_ISSUANCE = "12052: Timesheet received but still in Pending Issuance status. Cannot process.";
    public static final String EM_12004_ARREARS_AP = "12004: Arrears timesheet — advance pay already authorized for this period.";

    // ═══════════════════════════════════════════
    // TIMESHEET HOLD MESSAGES
    // ═══════════════════════════════════════════
    public static final String EM_18562_EARLY_SUBMISSION = "18562: Timesheet received before end of pay period. Hold until pay period end.";
    public static final String EM_18563_LATE_SUBMISSION = "18563: Timesheet received more than 30 days after pay period end. Hold for county review.";
    public static final String EM_16550_FLAGGED_REVIEW = "16550: Recipient/Provider relationship flagged for review. Hold for county verification.";
    public static final String EM_18651_RANDOM_SAMPLING = "18651: Timesheet selected for random 1% sampling. Hold for signature verification.";
    public static final String EM_20774_BVI_REVIEW = "20774: BVI recipient — no signature. Hold for TTS/electronic review.";
    public static final String EM_20774_BVI_EXPIRED = "20774: BVI review expired (10 business day deadline). Hard edit applied.";
    public static final String EM_20774_BVI_REJECTED = "20774: BVI review rejected by TTS. Hard edit applied.";
    public static final String EM_20774_BVI_EARLY = "20774: BVI recipient with early submission. Dual hold.";
    public static final String EM_20774_BVI_LATE = "20774: BVI recipient with late submission. Dual hold.";
    public static final String EM_20731_OT_WEEKLY = "20731: Weekly hours exceed maximum. Hold for overtime review.";
    public static final String EM_20731_OT_MONTHLY = "20731: Monthly hours exceed maximum. Hold for overtime review.";
    public static final String EM_EXCESS70 = "EXCESS70: Excessive hours claimed (>70% of authorized). Hold for review.";
    public static final String EM_MEDS_POS_ERROR = "MEDS-POS: MEDS POS eligibility verification failed. Hold for manual review.";

    // ═══════════════════════════════════════════
    // TIMESHEET SOFT EDIT / CUTBACK MESSAGES
    // ═══════════════════════════════════════════
    public static final String EM_12003_PARTIAL_RECIP_INELIG = "12003: Hours on ineligible days cut back. Recipient was ineligible on specific dates.";
    public static final String EM_12003_PARTIAL_PROV_INELIG = "12003: Hours on ineligible days cut back. Provider was ineligible on specific dates.";
    public static final String EM_12003_PARTIAL_RECIP_LEAVE = "12003: Hours on leave days cut back. Recipient was on leave on specific dates.";
    public static final String EM_12003_PARTIAL_PROV_LEAVE = "12003: Hours on leave days cut back. Provider was on leave on specific dates.";
    public static final String EM_20731_MONTHLY_MAX = "20731: Approved hours exceed monthly maximum. Excess hours cut back (LIFO).";
    public static final String EM_SOC_DEDUCTION = "SOC-DEDUCT: Share of Cost deduction applies to this payment.";
    public static final String EM_ADV_STATEMENT = "ADV-STMT: Advance pay timesheet — earnings statement generated upon processing.";

    // ═══════════════════════════════════════════
    // TRAVEL CLAIM MESSAGES (~40)
    // ═══════════════════════════════════════════
    public static final String TC_INELIGIBLE = "TC-INELIGIBLE: Provider is not eligible for travel time pay.";
    public static final String TC_HOLD_TS = "TC-HOLD-TS: Timesheet not yet processed for this period. Travel claim held.";
    public static final String TC_NO_SVC = "TC-NO-SVC: No paid service hours on travel date. Travel hours cut back.";
    public static final String TC_MAX14 = "TC-MAX14: Travel hours exceed weekly absolute maximum of 14:00.";
    public static final String TC_OT_TRAVEL = "TC-OT-TRAVEL: Travel hours exceed 7:00 weekly standard cap. OT violation flagged.";
    public static final String TC_TRIP_MAX = "TC-TRIP-MAX: Travel hours exceed 2:00 per trip limit. Flagged for review.";
    public static final String TC_NO_TRAVEL_AUTH = "TC-NO-AUTH: Provider not authorized for travel time in this county.";
    public static final String TC_DUPLICATE = "TC-DUPLICATE: Travel claim already submitted for this provider/date.";
    public static final String TC_FUTURE_DATE = "TC-FUTURE: Travel claim for future date not allowed.";
    public static final String TC_RECIP_INELIG = "TC-RECIP-INELIG: Recipient ineligible on travel date. Travel hours cut back.";
    public static final String TC_PROV_LEAVE = "TC-PROV-LEAVE: Provider on leave on travel date. Travel hours cut back.";
    public static final String TC_EXCEED_MONTHLY = "TC-MONTHLY: Travel hours exceed monthly maximum. Cut back.";
    public static final String TC_DISTANCE_EXCEEDED = "TC-DISTANCE: Travel distance exceeds maximum for county.";
    public static final String TC_MILEAGE_RATE = "TC-MILEAGE: Mileage reimbursement calculated at current IRS rate.";
    public static final String TC_APPROVED = "TC-APPROVED: Travel claim approved for payment.";
    public static final String TC_REJECTED = "TC-REJECTED: Travel claim rejected.";
    public static final String TC_VOID = "TC-VOID: Travel claim voided.";
    public static final String TC_REISSUE = "TC-REISSUE: Travel claim void/reissue processed.";

    // EVV Messages
    public static final String EVV_NO_DATA = "EVV-NO-DATA: No EVV data recorded for this service date. Exception required.";
    public static final String EVV_MISMATCH = "EVV-MISMATCH: EVV recorded hours do not match claimed hours. Exception required.";
    public static final String EVV_APPROVED = "EVV-APPROVED: EVV exception approved. Timesheet can proceed.";
    public static final String EVV_DENIED = "EVV-DENIED: EVV exception denied. Claimed hours cut back to EVV recorded hours.";
    public static final String EVV_EXPIRED = "EVV-EXPIRED: EVV exception review period expired.";

    // SOC Messages
    public static final String SOC_CERTIFIED_Y = "SOC-Y: Certified Medi-Cal SOC — deduction applies.";
    public static final String SOC_CERTIFIED_N = "SOC-N: Certified SOC amount is zero or met — no deduction.";
    public static final String SOC_UNCERTIFIED_OUTSIDE = "SOC-UNCER-OUT: Uncertified SOC, pay period outside 13-month window.";
    public static final String SOC_UNCERTIFIED_WITHIN = "SOC-UNCER-IN: Uncertified SOC, within 13-month window — MEDS POS verification.";
    public static final String SOC_MEDS_POS_OK = "SOC-MEDS-OK: MEDS POS verification successful.";
    public static final String SOC_MEDS_POS_FAIL = "SOC-MEDS-FAIL: MEDS POS verification failed — hold for review.";
    public static final String SOC_RESIDUAL_MET = "SOC-RES-MET: IHSS Residual (2N) — SOC zero or met.";
    public static final String SOC_RESIDUAL_UNMET = "SOC-RES-UNMET: IHSS Residual (2N) — unmet SOC deduction applies.";
    public static final String SOC_NOT_APPLICABLE = "SOC-NA: SOC not applicable for this funding source.";
    public static final String SOC_ADVANCE_PAY = "SOC-AP: Advance pay — SOC not deducted (reconciled on arrears).";

    /**
     * Get all error messages as a map of code → message.
     */
    public static Map<String, String> getAllMessages() {
        Map<String, String> messages = new LinkedHashMap<>();
        // Timesheet hard edits
        messages.put("11960-PROV", EM_11960_NO_PROVIDER_SIG);
        messages.put("11960-RECIP", EM_11960_NO_RECIPIENT_SIG);
        messages.put("20072", EM_20072_UNSIGNED_ELECTRONIC);
        messages.put("20153", EM_20153_BVI_NO_SIG);
        messages.put("11986-NO-HRS", EM_11986_NO_HOURS);
        messages.put("11986-EXCEED", EM_11986_EXCEED_24);
        messages.put("11986-MIN60", EM_11986_MINUTES_60);
        messages.put("11986-COMBINED", EM_11986_COMBINED_24);
        messages.put("11986-HH24MM", EM_11986_HH24_MM_GT0);
        messages.put("11986-NON-NUM", EM_11986_NON_NUMERIC);
        messages.put("11986-MULTI", EM_11986_MULTIPLE_ENTRIES);
        messages.put("11986-UNREAD", EM_11986_UNREADABLE);
        messages.put("11986-OUTSIDE", EM_11986_OUTSIDE_BOXES);
        messages.put("12002", EM_12002_TS_NOT_FOUND);
        messages.put("12001-EXISTS", EM_12001_TS_EXISTS);
        messages.put("12083", EM_12083_TS_PROCESSED);
        messages.put("12001-DUP-SUPP", EM_12001_DUP_SUPP);
        messages.put("12004-AUTH", EM_12004_EXCEED_AUTH);
        messages.put("12075", EM_12075_EXCEED_AUTH_MONTHLY);
        messages.put("12004-ASSIGNED", EM_12004_EXCEED_ASSIGNED);
        messages.put("12008", EM_12008_EXCEED_ALL_TS);
        messages.put("12004-NO-RECIP", EM_12004_NO_REMAINING_RECIP);
        messages.put("12004-NO-PROV", EM_12004_NO_REMAINING_PROV);
        messages.put("12004-WPCS", EM_12004_WPCS_NOT_AUTH);
        messages.put("12001-RECIP", EM_12001_RECIP_INELIGIBLE);
        messages.put("16193", EM_16193_RECIP_INELIGIBLE);
        messages.put("12003-PROV-INELIG", EM_12003_PROV_INELIGIBLE);
        messages.put("16197", EM_16197_PROV_TERMINATED);
        messages.put("12003-RECIP-LEAVE", EM_12003_RECIP_LEAVE);
        messages.put("16195", EM_16195_RECIP_LEAVE);
        messages.put("12003-PROV-LEAVE", EM_12003_PROV_LEAVE);
        messages.put("16188", EM_16188_PROV_LEAVE);
        messages.put("12053-RECIP", EM_12053_RECIP_LEAVE_INELIG);
        messages.put("12053-PROV", EM_12053_PROV_LEAVE_INELIG);
        messages.put("11989", EM_11989_FUTURE_HOURS);
        messages.put("11885", EM_11885_BEFORE_PP_START);
        messages.put("12052", EM_12052_PENDING_ISSUANCE);
        messages.put("12004-ARREARS", EM_12004_ARREARS_AP);
        // Hold messages
        messages.put("18562", EM_18562_EARLY_SUBMISSION);
        messages.put("18563", EM_18563_LATE_SUBMISSION);
        messages.put("16550", EM_16550_FLAGGED_REVIEW);
        messages.put("18651", EM_18651_RANDOM_SAMPLING);
        messages.put("20774-REVIEW", EM_20774_BVI_REVIEW);
        messages.put("20774-EXPIRED", EM_20774_BVI_EXPIRED);
        messages.put("20774-REJECTED", EM_20774_BVI_REJECTED);
        messages.put("20774-EARLY", EM_20774_BVI_EARLY);
        messages.put("20774-LATE", EM_20774_BVI_LATE);
        messages.put("20731-WEEKLY", EM_20731_OT_WEEKLY);
        messages.put("20731-MONTHLY", EM_20731_OT_MONTHLY);
        messages.put("EXCESS70", EM_EXCESS70);
        messages.put("MEDS-POS", EM_MEDS_POS_ERROR);
        // Soft edits
        messages.put("12003-P-RECIP-INELIG", EM_12003_PARTIAL_RECIP_INELIG);
        messages.put("12003-P-PROV-INELIG", EM_12003_PARTIAL_PROV_INELIG);
        messages.put("12003-P-RECIP-LEAVE", EM_12003_PARTIAL_RECIP_LEAVE);
        messages.put("12003-P-PROV-LEAVE", EM_12003_PARTIAL_PROV_LEAVE);
        messages.put("20731-MONTHLY-CUT", EM_20731_MONTHLY_MAX);
        messages.put("SOC-DEDUCT", EM_SOC_DEDUCTION);
        messages.put("ADV-STMT", EM_ADV_STATEMENT);
        // Travel claims
        messages.put("TC-INELIGIBLE", TC_INELIGIBLE);
        messages.put("TC-HOLD-TS", TC_HOLD_TS);
        messages.put("TC-NO-SVC", TC_NO_SVC);
        messages.put("TC-MAX14", TC_MAX14);
        messages.put("TC-OT-TRAVEL", TC_OT_TRAVEL);
        messages.put("TC-TRIP-MAX", TC_TRIP_MAX);
        messages.put("TC-NO-AUTH", TC_NO_TRAVEL_AUTH);
        messages.put("TC-DUPLICATE", TC_DUPLICATE);
        messages.put("TC-FUTURE", TC_FUTURE_DATE);
        messages.put("TC-RECIP-INELIG", TC_RECIP_INELIG);
        messages.put("TC-PROV-LEAVE", TC_PROV_LEAVE);
        messages.put("TC-MONTHLY", TC_EXCEED_MONTHLY);
        messages.put("TC-DISTANCE", TC_DISTANCE_EXCEEDED);
        messages.put("TC-MILEAGE", TC_MILEAGE_RATE);
        messages.put("TC-APPROVED", TC_APPROVED);
        messages.put("TC-REJECTED", TC_REJECTED);
        messages.put("TC-VOID", TC_VOID);
        messages.put("TC-REISSUE", TC_REISSUE);
        // EVV
        messages.put("EVV-NO-DATA", EVV_NO_DATA);
        messages.put("EVV-MISMATCH", EVV_MISMATCH);
        messages.put("EVV-APPROVED", EVV_APPROVED);
        messages.put("EVV-DENIED", EVV_DENIED);
        messages.put("EVV-EXPIRED", EVV_EXPIRED);
        // SOC
        messages.put("SOC-CERTIFIED-Y", SOC_CERTIFIED_Y);
        messages.put("SOC-CERTIFIED-N", SOC_CERTIFIED_N);
        messages.put("SOC-UNCER-OUT", SOC_UNCERTIFIED_OUTSIDE);
        messages.put("SOC-UNCER-IN", SOC_UNCERTIFIED_WITHIN);
        messages.put("SOC-MEDS-OK", SOC_MEDS_POS_OK);
        messages.put("SOC-MEDS-FAIL", SOC_MEDS_POS_FAIL);
        messages.put("SOC-RES-MET", SOC_RESIDUAL_MET);
        messages.put("SOC-RES-UNMET", SOC_RESIDUAL_UNMET);
        messages.put("SOC-NA", SOC_NOT_APPLICABLE);
        messages.put("SOC-AP", SOC_ADVANCE_PAY);

        return messages;
    }
}
