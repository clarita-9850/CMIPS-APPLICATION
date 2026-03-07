package com.cmips.baw.filetype;

import com.cmips.integration.framework.baw.annotation.FileColumn;
import com.cmips.integration.framework.baw.annotation.FileId;
import com.cmips.integration.framework.baw.annotation.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * PRNR998A — TPF Timesheet Batch Header Record (fixed-width, 80 bytes).
 *
 * This is the header record sent by TPF for each batch of scanned timesheets.
 * One header per batch; followed by N detail records (PRNR998C) and one trailer (PRNR998D).
 *
 * Layout:
 * - Pos 1-3:    Record Type ("HDR")
 * - Pos 4-11:   Batch ID (8 chars, zero-padded)
 * - Pos 12-19:  Batch Date (YYYYMMDD)
 * - Pos 20-24:  Record Count (5 digits, zero-padded)
 * - Pos 25-26:  County Code (2 chars)
 * - Pos 27-34:  TPF Site ID (8 chars)
 * - Pos 35-42:  Operator ID (8 chars)
 * - Pos 43-50:  Scan Date (YYYYMMDD)
 * - Pos 51-80:  Filler (30 chars, spaces)
 *
 * Total record length: 80 characters
 */
@FileType(
    name = "tpf-batch-header-998a",
    description = "TPF Timesheet Batch Header (PRNR998A)",
    version = "1.0"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prnr998AHeaderRecord {

    @FileColumn(
        order = 1,
        name = "RECORD_TYPE",
        length = 3,
        nullable = false
    )
    private String recordType; // "HDR"

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
        name = "RECORD_COUNT",
        length = 5,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private Integer recordCount;

    @FileColumn(
        order = 5,
        name = "COUNTY_CODE",
        length = 2,
        nullable = false
    )
    private String countyCode;

    @FileColumn(
        order = 6,
        name = "TPF_SITE_ID",
        length = 8,
        nullable = false
    )
    private String tpfSiteId;

    @FileColumn(
        order = 7,
        name = "OPERATOR_ID",
        length = 8,
        nullable = true
    )
    private String operatorId;

    @FileColumn(
        order = 8,
        name = "SCAN_DATE",
        length = 8,
        format = "yyyyMMdd",
        nullable = true
    )
    private LocalDate scanDate;

    @FileColumn(
        order = 9,
        name = "FILLER",
        length = 30,
        nullable = true
    )
    private String filler;
}
