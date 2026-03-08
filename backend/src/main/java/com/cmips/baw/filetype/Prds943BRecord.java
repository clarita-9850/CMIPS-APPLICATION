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

/**
 * PRDS943B — Payroll Detail Record for EDD (fixed-width, 80 bytes).
 *
 * Outbound quarterly to EDD (Employment Development Department) for
 * unemployment insurance wage reporting. One record per provider,
 * aggregated across all timesheets in the quarter.
 *
 * Layout:
 * - Pos 1-10:  File ID ("PRDS943B  ", 10 chars)
 * - Pos 11-20: Provider ID (10 digits, zero-padded right)
 * - Pos 21-28: Total Hours (8 chars, 00000.00)
 * - Pos 29-36: Overtime Hours (8 chars, 00000.00)
 * - Pos 37-37: Quarter (1 digit: 1-4)
 * - Pos 38-41: Year (4 digits)
 * - Pos 42-80: Filler (39 chars)
 *
 * Total record length: 80 characters
 */
@FileType(
    name = "payroll-detail-edd-943b",
    description = "EDD Payroll Detail Record (PRDS943B)",
    version = "1.0"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prds943BRecord {

    @FileColumn(
        order = 1,
        name = "FILE_ID",
        length = 10,
        nullable = false
    )
    @Builder.Default
    private String fileId = "PRDS943B";

    @FileId
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

    @Validate(notNull = true, min = 0, message = "Total hours must be non-negative")
    @FileColumn(
        order = 3,
        name = "TOTAL_HOURS",
        length = 8,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        format = "00000.00",
        nullable = false
    )
    private BigDecimal totalHours;

    @FileColumn(
        order = 4,
        name = "OVERTIME_HOURS",
        length = 8,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        format = "00000.00",
        nullable = false
    )
    private BigDecimal overtimeHours;

    @Validate(notNull = true, min = 1, max = 4, message = "Quarter must be 1-4")
    @FileColumn(
        order = 5,
        name = "QUARTER",
        length = 1,
        nullable = false
    )
    private Integer quarter;

    @Validate(notNull = true, min = 2018, max = 2100, message = "Year out of range")
    @FileColumn(
        order = 6,
        name = "YEAR",
        length = 4,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private Integer year;

    @FileColumn(
        order = 7,
        name = "FILLER",
        length = 39,
        nullable = true
    )
    private String filler;

    public static Prds943BRecord fromProviderAggregation(
            Long providerId, double totalHours, double overtimeHours, int quarter, int year) {
        return Prds943BRecord.builder()
                .providerId(providerId)
                .totalHours(BigDecimal.valueOf(totalHours))
                .overtimeHours(BigDecimal.valueOf(overtimeHours))
                .quarter(quarter)
                .year(year)
                .build();
    }
}
