package com.cmips.baw.filetype;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import com.cmips.integration.framework.baw.annotation.Validate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * PRDS108A — Timesheet Processing Summary record for SCO (fixed-width, 150 bytes).
 *
 * Outbound to SCO (State Controller's Office) for warrant generation.
 * One detail record per processed timesheet in the batch.
 *
 * Layout:
 * - Pos 1-20:    Timesheet Number (20 chars, left-aligned)
 * - Pos 21-30:   Provider ID (10 digits, zero-padded right)
 * - Pos 31-40:   Recipient ID (10 digits, zero-padded right)
 * - Pos 41-50:   Case ID (10 digits, zero-padded right)
 * - Pos 51-58:   Pay Period Start (YYYYMMDD)
 * - Pos 59-66:   Pay Period End (YYYYMMDD)
 * - Pos 67-74:   Total Hours Approved (8 chars, 00000.00)
 * - Pos 75-82:   Regular Hours (8 chars, 00000.00)
 * - Pos 83-90:   Overtime Hours (8 chars, 00000.00)
 * - Pos 91-98:   SOC Deduction (8 chars, 00000.00)
 * - Pos 99-103:  Program Type (5 chars: IHSS or WPCS)
 * - Pos 104-108: County Code (5 chars)
 * - Pos 109-116: Date Processed (YYYYMMDD)
 * - Pos 117-150: Filler (34 chars)
 *
 * Total record length: 150 characters
 */
@FileType(
    name = "timesheet-summary-sco-108a",
    description = "SCO Timesheet Processing Summary (PRDS108A)",
    version = "1.0"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prds108ARecord {

    @FileId
    @Validate(notBlank = true, message = "Timesheet number is required")
    @FileColumn(
        order = 1,
        name = "TIMESHEET_NUMBER",
        length = 20,
        nullable = false
    )
    private String timesheetNumber;

    @Validate(notNull = true, message = "Provider ID is required")
    @FileColumn(
        order = 2,
        name = "PROVIDER_ID",
        length = 10,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private Long providerId;

    @Validate(notNull = true, message = "Recipient ID is required")
    @FileColumn(
        order = 3,
        name = "RECIPIENT_ID",
        length = 10,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private Long recipientId;

    @Validate(notNull = true, message = "Case ID is required")
    @FileColumn(
        order = 4,
        name = "CASE_ID",
        length = 10,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private Long caseId;

    @Validate(notNull = true, message = "Pay period start is required")
    @FileColumn(
        order = 5,
        name = "PAY_PERIOD_START",
        length = 8,
        format = "yyyyMMdd",
        nullable = false
    )
    private LocalDate payPeriodStart;

    @Validate(notNull = true, message = "Pay period end is required")
    @FileColumn(
        order = 6,
        name = "PAY_PERIOD_END",
        length = 8,
        format = "yyyyMMdd",
        nullable = false
    )
    private LocalDate payPeriodEnd;

    @FileColumn(
        order = 7,
        name = "TOTAL_HOURS_APPROVED",
        length = 8,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        format = "00000.00",
        nullable = false
    )
    private BigDecimal totalHoursApproved;

    @FileColumn(
        order = 8,
        name = "REGULAR_HOURS",
        length = 8,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        format = "00000.00",
        nullable = false
    )
    private BigDecimal regularHours;

    @FileColumn(
        order = 9,
        name = "OVERTIME_HOURS",
        length = 8,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        format = "00000.00",
        nullable = false
    )
    private BigDecimal overtimeHours;

    @FileColumn(
        order = 10,
        name = "SOC_DEDUCTION",
        length = 8,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        format = "00000.00",
        nullable = false
    )
    private BigDecimal socDeduction;

    @Validate(notBlank = true, allowedValues = {"IHSS", "WPCS"}, message = "Program type must be IHSS or WPCS")
    @FileColumn(
        order = 11,
        name = "PROGRAM_TYPE",
        length = 5,
        nullable = false
    )
    private String programType;

    @FileColumn(
        order = 12,
        name = "COUNTY_CODE",
        length = 5,
        nullable = true
    )
    private String countyCode;

    @FileColumn(
        order = 13,
        name = "DATE_PROCESSED",
        length = 8,
        format = "yyyyMMdd",
        nullable = false
    )
    private LocalDate dateProcessed;

    @FileColumn(
        order = 14,
        name = "FILLER",
        length = 34,
        nullable = true
    )
    private String filler;

    /**
     * Build from a TimesheetEntity.
     */
    public static Prds108ARecord fromTimesheet(
            String timesheetNumber, Long providerId, Long recipientId, Long caseId,
            LocalDate ppStart, LocalDate ppEnd, double totalHours, double regHours,
            double otHours, double socAmt, String programType, String countyCode) {
        return Prds108ARecord.builder()
                .timesheetNumber(timesheetNumber != null ? timesheetNumber : "")
                .providerId(providerId != null ? providerId : 0L)
                .recipientId(recipientId != null ? recipientId : 0L)
                .caseId(caseId != null ? caseId : 0L)
                .payPeriodStart(ppStart)
                .payPeriodEnd(ppEnd)
                .totalHoursApproved(BigDecimal.valueOf(totalHours))
                .regularHours(BigDecimal.valueOf(regHours))
                .overtimeHours(BigDecimal.valueOf(otHours))
                .socDeduction(BigDecimal.valueOf(socAmt))
                .programType(programType != null ? programType : "IHSS")
                .countyCode(countyCode != null ? countyCode : "")
                .dateProcessed(LocalDate.now())
                .build();
    }
}
