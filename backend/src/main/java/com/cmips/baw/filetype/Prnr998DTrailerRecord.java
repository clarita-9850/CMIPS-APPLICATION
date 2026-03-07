package com.cmips.baw.filetype;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * PRNR998D — TPF Timesheet Batch Trailer Record (fixed-width, 80 bytes).
 *
 * One trailer per batch; validates record counts and totals for integrity.
 *
 * Layout:
 * - Pos 1-3:    Record Type ("TRL")
 * - Pos 4-11:   Batch ID (8 chars, must match header)
 * - Pos 12-19:  Batch Date (YYYYMMDD, must match header)
 * - Pos 20-24:  Total Detail Records (5 digits, zero-padded)
 * - Pos 25-31:  Total Hours in Batch (7 chars, 1 decimal: 99999.9)
 * - Pos 32-36:  Error Count (5 digits, zero-padded)
 * - Pos 37-41:  Verified Count (5 digits, zero-padded)
 * - Pos 42-80:  Filler (39 chars)
 *
 * Total record length: 80 characters
 */
@FileType(
    name = "tpf-batch-trailer-998d",
    description = "TPF Timesheet Batch Trailer (PRNR998D)",
    version = "1.0"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prnr998DTrailerRecord {

    @FileColumn(
        order = 1,
        name = "RECORD_TYPE",
        length = 3,
        nullable = false
    )
    private String recordType; // "TRL"

    @FileId
    @FileColumn(
        order = 2,
        name = "BATCH_ID",
        length = 8,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private String batchId;

    @FileColumn(
        order = 3,
        name = "BATCH_DATE",
        length = 8,
        format = "yyyyMMdd",
        nullable = false
    )
    private LocalDate batchDate;

    @FileColumn(
        order = 4,
        name = "TOTAL_DETAIL_RECORDS",
        length = 5,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private Integer totalDetailRecords;

    @FileColumn(
        order = 5,
        name = "TOTAL_HOURS",
        length = 7,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        format = "00000.0",
        nullable = false
    )
    private BigDecimal totalHours;

    @FileColumn(
        order = 6,
        name = "ERROR_COUNT",
        length = 5,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private Integer errorCount;

    @FileColumn(
        order = 7,
        name = "VERIFIED_COUNT",
        length = 5,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private Integer verifiedCount;

    @FileColumn(
        order = 8,
        name = "FILLER",
        length = 39,
        nullable = true
    )
    private String filler;
}
