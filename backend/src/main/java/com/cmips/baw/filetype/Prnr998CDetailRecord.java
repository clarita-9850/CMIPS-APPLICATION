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
 * PRNR998C — TPF Timesheet Detail Record (fixed-width, 200 bytes).
 *
 * One record per scanned timesheet in the TPF batch. Contains the OCR/ICR-captured
 * data from the paper timesheet after TPF verification and data completion.
 *
 * Layout:
 * - Pos 1-3:     Record Type ("DTL")
 * - Pos 4-11:    Batch ID (8 chars, links to PRNR998A header)
 * - Pos 12-21:   Timesheet Number (10 chars)
 * - Pos 22-30:   Provider ID (9 chars, SSN format)
 * - Pos 31-39:   Recipient ID (9 chars)
 * - Pos 40-49:   Case Number (10 chars)
 * - Pos 50-57:   Pay Period Start (YYYYMMDD)
 * - Pos 58-65:   Pay Period End (YYYYMMDD)
 * - Pos 66-69:   Program Type (4 chars: IHSS or WPCS)
 * - Pos 70-74:   Total Hours Claimed (5 chars, 1 decimal implied: 999.9)
 * - Pos 75-134:  Daily Hours (15 days × 4 chars each = 60 chars, 1 decimal: 99.9)
 * - Pos 135:     Provider Signature (Y/N)
 * - Pos 136:     Recipient Signature (Y/N)
 * - Pos 137-144: Date Received at TPF (YYYYMMDD)
 * - Pos 145-152: Image ID (8 chars — OCR scan reference)
 * - Pos 153-153: Verification Status (V=Verified, U=Unverified, E=Error)
 * - Pos 154-155: Error Code (2 chars, blank if no error)
 * - Pos 156-200: Filler (45 chars)
 *
 * Total record length: 200 characters
 */
@FileType(
    name = "tpf-timesheet-detail-998c",
    description = "TPF Timesheet Detail Record (PRNR998C)",
    version = "1.0"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prnr998CDetailRecord {

    @FileColumn(
        order = 1,
        name = "RECORD_TYPE",
        length = 3,
        nullable = false
    )
    private String recordType; // "DTL"

    @FileColumn(
        order = 2,
        name = "BATCH_ID",
        length = 8,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private String batchId;

    @FileId
    @FileColumn(
        order = 3,
        name = "TIMESHEET_NUMBER",
        length = 10,
        nullable = false
    )
    private String timesheetNumber;

    @FileColumn(
        order = 4,
        name = "PROVIDER_ID",
        length = 9,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private String providerId;

    @FileColumn(
        order = 5,
        name = "RECIPIENT_ID",
        length = 9,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        nullable = false
    )
    private String recipientId;

    @FileColumn(
        order = 6,
        name = "CASE_NUMBER",
        length = 10,
        nullable = false
    )
    private String caseNumber;

    @FileColumn(
        order = 7,
        name = "PAY_PERIOD_START",
        length = 8,
        format = "yyyyMMdd",
        nullable = false
    )
    private LocalDate payPeriodStart;

    @FileColumn(
        order = 8,
        name = "PAY_PERIOD_END",
        length = 8,
        format = "yyyyMMdd",
        nullable = false
    )
    private LocalDate payPeriodEnd;

    @FileColumn(
        order = 9,
        name = "PROGRAM_TYPE",
        length = 4,
        nullable = false
    )
    private String programType; // "IHSS" or "WPCS"

    @FileColumn(
        order = 10,
        name = "TOTAL_HOURS_CLAIMED",
        length = 5,
        padChar = '0',
        alignment = FileColumn.Alignment.RIGHT,
        format = "000.0",
        nullable = false
    )
    private BigDecimal totalHoursClaimed;

    /**
     * Daily hours for up to 15 days of the pay period.
     * 15 × 4 chars = 60 chars total, each in format 99.9.
     * Stored as a packed string; parsed by Prnr998ParserService.
     */
    @FileColumn(
        order = 11,
        name = "DAILY_HOURS",
        length = 60,
        nullable = true
    )
    private String dailyHoursBlock;

    @FileColumn(
        order = 12,
        name = "PROVIDER_SIGNATURE",
        length = 1,
        nullable = false
    )
    private String providerSignature; // Y or N

    @FileColumn(
        order = 13,
        name = "RECIPIENT_SIGNATURE",
        length = 1,
        nullable = false
    )
    private String recipientSignature; // Y or N

    @FileColumn(
        order = 14,
        name = "DATE_RECEIVED",
        length = 8,
        format = "yyyyMMdd",
        nullable = true
    )
    private LocalDate dateReceived;

    @FileColumn(
        order = 15,
        name = "IMAGE_ID",
        length = 8,
        nullable = true
    )
    private String imageId;

    @FileColumn(
        order = 16,
        name = "VERIFICATION_STATUS",
        length = 1,
        nullable = false
    )
    private String verificationStatus; // V, U, or E

    @FileColumn(
        order = 17,
        name = "ERROR_CODE",
        length = 2,
        nullable = true
    )
    private String errorCode;

    @FileColumn(
        order = 18,
        name = "FILLER",
        length = 45,
        nullable = true
    )
    private String filler;

    // --- Convenience methods ---

    public boolean isProviderSigned() {
        return "Y".equals(providerSignature);
    }

    public boolean isRecipientSigned() {
        return "Y".equals(recipientSignature);
    }

    public boolean isVerified() {
        return "V".equals(verificationStatus);
    }

    public boolean hasError() {
        return "E".equals(verificationStatus);
    }

    public enum VerificationStatus {
        VERIFIED("V"),
        UNVERIFIED("U"),
        ERROR("E");

        private final String code;
        VerificationStatus(String code) { this.code = code; }
        public String getCode() { return code; }

        public static VerificationStatus fromCode(String code) {
            for (VerificationStatus s : values()) {
                if (s.code.equals(code != null ? code.trim() : "")) return s;
            }
            throw new IllegalArgumentException("Unknown verification status: " + code);
        }
    }
}
