package com.cmips.baw.filetype;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import com.cmips.integration.framework.baw.annotation.Validate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * EVV (Electronic Visit Verification) Daily Transmission Record — Inbound.
 *
 * DSD Section 24 (1.1.4.10/1.1.9.14): EVV vendor sends daily check-in/check-out
 * data to CMIPS for comparison against submitted electronic timesheets.
 *
 * File naming: EVV_DAILY_YYYYMMDD.DAT
 *
 * Layout (fixed-width, 150 bytes):
 * - Pos 1-3:     Record Type ("EVV")
 * - Pos 4-13:    Provider ID (10 chars, zero-padded right)
 * - Pos 14-23:   Recipient ID (10 chars, zero-padded right)
 * - Pos 24-33:   Case ID (10 chars, zero-padded right)
 * - Pos 34-41:   Service Date (YYYYMMDD)
 * - Pos 42-45:   Check-In Time (HHMM, 24-hour)
 * - Pos 46-49:   Check-Out Time (HHMM, 24-hour)
 * - Pos 50-57:   Hours Worked (8 chars, 00000.00)
 * - Pos 58-67:   Check-In Latitude (10 chars, signed decimal)
 * - Pos 68-78:   Check-In Longitude (11 chars, signed decimal)
 * - Pos 79-88:   Check-Out Latitude (10 chars, signed decimal)
 * - Pos 89-99:   Check-Out Longitude (11 chars, signed decimal)
 * - Pos 100-101: Service Type Code (2 chars: PC=Personal Care, DM=Domestic, MD=Medical, PR=Protective)
 * - Pos 102-102: Verification Method (1 char: G=GPS, T=Telephony, M=Manual)
 * - Pos 103-103: Match Status (1 char: M=Matched, U=Unmatched, P=Pending)
 * - Pos 104-113: Timesheet Number (10 chars, blank if unmatched)
 * - Pos 114-150: Filler (37 chars)
 *
 * Total record length: 150 characters
 */
@FileType(
    name = "evv-daily-record",
    description = "EVV Daily Visit Verification Record",
    version = "1.0"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvvDailyRecord {

    @FileColumn(
        order = 1,
        name = "RECORD_TYPE",
        length = 3,
        nullable = false
    )
    @Builder.Default
    private String recordType = "EVV";

    @FileId
    @Validate(notBlank = true, message = "Provider ID is required")
    @FileColumn(
        order = 2,
        name = "PROVIDER_ID",
        length = 10,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private String providerId;

    @Validate(notBlank = true, message = "Recipient ID is required")
    @FileColumn(
        order = 3,
        name = "RECIPIENT_ID",
        length = 10,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private String recipientId;

    @FileColumn(
        order = 4,
        name = "CASE_ID",
        length = 10,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private String caseId;

    @Validate(notNull = true, message = "Service date is required")
    @FileColumn(
        order = 5,
        name = "SERVICE_DATE",
        length = 8,
        format = "yyyyMMdd",
        nullable = false
    )
    private LocalDate serviceDate;

    @FileColumn(
        order = 6,
        name = "CHECK_IN_TIME",
        length = 4,
        nullable = false
    )
    private String checkInTime;

    @FileColumn(
        order = 7,
        name = "CHECK_OUT_TIME",
        length = 4,
        nullable = true
    )
    private String checkOutTime;

    @FileColumn(
        order = 8,
        name = "HOURS_WORKED",
        length = 8,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private String hoursWorked;

    @FileColumn(
        order = 9,
        name = "CHECK_IN_LATITUDE",
        length = 10,
        nullable = true
    )
    private String checkInLatitude;

    @FileColumn(
        order = 10,
        name = "CHECK_IN_LONGITUDE",
        length = 11,
        nullable = true
    )
    private String checkInLongitude;

    @FileColumn(
        order = 11,
        name = "CHECK_OUT_LATITUDE",
        length = 10,
        nullable = true
    )
    private String checkOutLatitude;

    @FileColumn(
        order = 12,
        name = "CHECK_OUT_LONGITUDE",
        length = 11,
        nullable = true
    )
    private String checkOutLongitude;

    @FileColumn(
        order = 13,
        name = "SERVICE_TYPE_CODE",
        length = 2,
        nullable = false
    )
    private String serviceTypeCode;

    @FileColumn(
        order = 14,
        name = "VERIFICATION_METHOD",
        length = 1,
        nullable = false
    )
    private String verificationMethod;

    @FileColumn(
        order = 15,
        name = "MATCH_STATUS",
        length = 1,
        nullable = true
    )
    private String matchStatus;

    @FileColumn(
        order = 16,
        name = "TIMESHEET_NUMBER",
        length = 10,
        nullable = true
    )
    private String timesheetNumber;

    @FileColumn(
        order = 17,
        name = "FILLER",
        length = 37,
        nullable = true
    )
    private String filler;

    // --- Convenience methods ---

    public String getServiceTypeName() {
        if (serviceTypeCode == null) return "UNKNOWN";
        return switch (serviceTypeCode.trim()) {
            case "PC" -> "PERSONAL_CARE";
            case "DM" -> "DOMESTIC";
            case "MD" -> "MEDICAL";
            case "PR" -> "PROTECTIVE";
            default -> "UNKNOWN";
        };
    }

    public String getVerificationMethodName() {
        if (verificationMethod == null) return "UNKNOWN";
        return switch (verificationMethod.trim()) {
            case "G" -> "GPS";
            case "T" -> "TELEPHONY";
            case "M" -> "MANUAL";
            default -> "UNKNOWN";
        };
    }

    public boolean isMatched() {
        return "M".equals(matchStatus);
    }
}
