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
 * EDD (Employment Development Department) Response Record — Inbound.
 *
 * EDD sends acknowledgment files in response to our PRDS943B quarterly submissions.
 * Each record confirms receipt and processing status of a provider's payroll detail.
 *
 * File naming: EDD_RESP_QNYYYY_YYYYMMDD.DAT
 *
 * Layout (fixed-width, 80 bytes):
 * - Pos 1-3:    Record Type ("ACK")
 * - Pos 4-13:   Provider ID (10 chars)
 * - Pos 14:     Quarter (1 char: 1-4)
 * - Pos 15-18:  Year (4 chars)
 * - Pos 19-26:  Response Date (YYYYMMDD)
 * - Pos 27-27:  Status Code (A=Accepted, R=Rejected, P=Partial)
 * - Pos 28-29:  Error Code (2 chars, blank if accepted)
 * - Pos 30-69:  Error Message (40 chars, blank if accepted)
 * - Pos 70-80:  Filler (11 chars)
 *
 * Total record length: 80 characters
 */
@FileType(
    name = "edd-response",
    description = "EDD Payroll Acknowledgment Response",
    version = "1.0"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EddResponseRecord {

    @FileColumn(
        order = 1,
        name = "RECORD_TYPE",
        length = 3,
        nullable = false
    )
    private String recordType; // "ACK"

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

    @FileColumn(
        order = 3,
        name = "QUARTER",
        length = 1,
        nullable = false
    )
    private String quarter;

    @FileColumn(
        order = 4,
        name = "YEAR",
        length = 4,
        nullable = false
    )
    private String year;

    @FileColumn(
        order = 5,
        name = "RESPONSE_DATE",
        length = 8,
        format = "yyyyMMdd",
        nullable = false
    )
    private LocalDate responseDate;

    @Validate(notBlank = true, allowedValues = {"A", "R", "P"}, message = "Status must be A, R, or P")
    @FileColumn(
        order = 6,
        name = "STATUS_CODE",
        length = 1,
        nullable = false
    )
    private String statusCode;

    @FileColumn(
        order = 7,
        name = "ERROR_CODE",
        length = 2,
        nullable = true
    )
    private String errorCode;

    @FileColumn(
        order = 8,
        name = "ERROR_MESSAGE",
        length = 40,
        nullable = true
    )
    private String errorMessage;

    @FileColumn(
        order = 9,
        name = "FILLER",
        length = 11,
        nullable = true
    )
    private String filler;

    // --- Convenience methods ---

    public ResponseStatus getStatus() {
        return ResponseStatus.fromCode(statusCode);
    }

    public boolean isAccepted() {
        return "A".equals(statusCode);
    }

    public boolean isRejected() {
        return "R".equals(statusCode);
    }

    public enum ResponseStatus {
        ACCEPTED("A"),
        REJECTED("R"),
        PARTIAL("P");

        private final String code;
        ResponseStatus(String code) { this.code = code; }
        public String getCode() { return code; }

        public static ResponseStatus fromCode(String code) {
            for (ResponseStatus s : values()) {
                if (s.code.equals(code != null ? code.trim() : "")) return s;
            }
            throw new IllegalArgumentException("Unknown EDD response status: " + code);
        }
    }
}
