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
 * DOJ (Department of Justice) Background Check Response Record — Inbound.
 *
 * DOJ sends background check result files in response to our CMNR932A submissions.
 * Each record contains the clearance status for a provider.
 *
 * File naming: DOJ_BGC_YYYYMMDD_NNN.DAT
 *
 * Layout (fixed-width, 100 bytes):
 * - Pos 1-10:   File ID ("DOJBGC    ")
 * - Pos 11-14:  Record Type ("RSP " = Response)
 * - Pos 15-24:  Provider ID (10 chars, right-padded)
 * - Pos 25-32:  Check Date (YYYYMMDD)
 * - Pos 33-33:  Result Code (C=Cleared, F=Flagged, P=Pending, D=Denied)
 * - Pos 34-35:  Flag Category (2 chars: blank if cleared, "AR"=Arrest, "CV"=Conviction, etc.)
 * - Pos 36-75:  Result Description (40 chars)
 * - Pos 76-83:  Expiration Date (YYYYMMDD, when clearance expires)
 * - Pos 84-91:  Next Review Date (YYYYMMDD)
 * - Pos 92-100: Filler (9 chars)
 *
 * Total record length: 100 characters
 */
@FileType(
    name = "doj-background-check",
    description = "DOJ Background Check Response",
    version = "1.0"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DojBackgroundCheckRecord {

    @FileColumn(
        order = 1,
        name = "FILE_ID",
        length = 10,
        nullable = false
    )
    @Builder.Default
    private String fileId = "DOJBGC";

    @FileColumn(
        order = 2,
        name = "RECORD_TYPE",
        length = 4,
        nullable = false
    )
    @Builder.Default
    private String recordType = "RSP";

    @FileId
    @Validate(notBlank = true, message = "Provider ID is required")
    @FileColumn(
        order = 3,
        name = "PROVIDER_ID",
        length = 10,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private String providerId;

    @Validate(notNull = true, message = "Check date is required")
    @FileColumn(
        order = 4,
        name = "CHECK_DATE",
        length = 8,
        format = "yyyyMMdd",
        nullable = false
    )
    private LocalDate checkDate;

    @Validate(notBlank = true, allowedValues = {"C", "F", "P", "D"}, message = "Result must be C, F, P, or D")
    @FileColumn(
        order = 5,
        name = "RESULT_CODE",
        length = 1,
        nullable = false
    )
    private String resultCode;

    @FileColumn(
        order = 6,
        name = "FLAG_CATEGORY",
        length = 2,
        nullable = true
    )
    private String flagCategory;

    @FileColumn(
        order = 7,
        name = "RESULT_DESCRIPTION",
        length = 40,
        nullable = true
    )
    private String resultDescription;

    @FileColumn(
        order = 8,
        name = "EXPIRATION_DATE",
        length = 8,
        format = "yyyyMMdd",
        nullable = true
    )
    private LocalDate expirationDate;

    @FileColumn(
        order = 9,
        name = "NEXT_REVIEW_DATE",
        length = 8,
        format = "yyyyMMdd",
        nullable = true
    )
    private LocalDate nextReviewDate;

    @FileColumn(
        order = 10,
        name = "FILLER",
        length = 9,
        nullable = true
    )
    private String filler;

    // --- Convenience methods ---

    public CheckResult getResult() {
        return CheckResult.fromCode(resultCode);
    }

    public boolean isCleared() {
        return "C".equals(resultCode);
    }

    public boolean isDenied() {
        return "D".equals(resultCode);
    }

    public boolean isFlagged() {
        return "F".equals(resultCode);
    }

    public enum CheckResult {
        CLEARED("C"),
        FLAGGED("F"),
        PENDING("P"),
        DENIED("D");

        private final String code;
        CheckResult(String code) { this.code = code; }
        public String getCode() { return code; }

        public static CheckResult fromCode(String code) {
            for (CheckResult r : values()) {
                if (r.code.equals(code != null ? code.trim() : "")) return r;
            }
            throw new IllegalArgumentException("Unknown DOJ result code: " + code);
        }
    }
}
