package com.cmips.integration.filetypes;

import java.lang.annotation.*;

/**
 * SSA SSNVS File Type Definitions
 *
 * Defines the fixed-width record formats for SSA Social Security Number
 * Verification Service (SSNVS) batch files.
 *
 * Send File: CMRS701E - submitted to SSA via SFTP
 * Receive File: CMRR701D - returned from SSA with verification codes
 *
 * Each record is 100 bytes fixed-width per SSA specification.
 */
public class SsaVerificationFileType {

    /** File type identifier for the send file */
    public static final String SEND_FILE_TYPE = "CMRS701E";

    /** File type identifier for the receive file */
    public static final String RECEIVE_FILE_TYPE = "CMRR701D";

    /** SFTP destination for SSA */
    public static final String SFTP_DESTINATION = "sftp://ssnvs.ssa.gov/inbound";

    /**
     * Send record - one per provider being verified.
     * Fixed-width: 100 bytes per record.
     *
     * Layout:
     *   Position 1:      Record Type (H=Header, D=Data, T=Trailer)
     *   Position 2-10:   SSN (9 digits, no dashes)
     *   Position 11-36:  Last Name (26 chars)
     *   Position 37-51:  First Name (15 chars)
     *   Position 52-66:  Middle Name (15 chars)
     *   Position 67-74:  Date of Birth (MMDDYYYY)
     *   Position 75:     Gender (M/F)
     *   Position 76-95:  Provider Number / External Reference (20 chars)
     *   Position 96-100: Reserved (5 chars, spaces)
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SsaVerificationSendRecord {

        /** SSN - 9 digits, no dashes. Primary dedup key. */
        private String ssn;

        /** Provider last name - up to 26 characters */
        private String lastName;

        /** Provider first name - up to 15 characters */
        private String firstName;

        /** Provider middle name - up to 15 characters (optional) */
        private String middleName;

        /** Date of birth in MMDDYYYY format */
        private String dateOfBirth;

        /** Gender code: M or F */
        private String gender;

        /** CMIPS provider number for cross-reference */
        private String providerNumber;

        /**
         * Format this record as a fixed-width string (100 bytes).
         */
        public String toFixedWidth() {
            StringBuilder sb = new StringBuilder(100);
            sb.append("D");                                    // Record Type
            sb.append(padRight(ssn, 9));                       // SSN
            sb.append(padRight(lastName, 26));                 // Last Name
            sb.append(padRight(firstName, 15));                // First Name
            sb.append(padRight(middleName, 15));               // Middle Name
            sb.append(padRight(dateOfBirth, 8));               // DOB
            sb.append(padRight(gender, 1));                    // Gender
            sb.append(padRight(providerNumber, 20));           // Provider Ref
            sb.append(padRight("", 5));                        // Reserved
            return sb.toString();
        }
    }

    /**
     * Receive record - verification result from SSA.
     * Fixed-width: 100 bytes per record.
     *
     * Layout:
     *   Position 1:      Record Type (H/D/T)
     *   Position 2-10:   SSN
     *   Position 11-36:  Last Name
     *   Position 37-51:  First Name
     *   Position 52-66:  Middle Name
     *   Position 67-74:  Date of Birth (MMDDYYYY)
     *   Position 75:     Gender
     *   Position 76:     Verification Code (1-8)
     *   Position 77-96:  Provider Number / External Reference
     *   Position 97-100: Reserved
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SsaVerificationReceiveRecord {

        /** SSN returned from SSA */
        private String ssn;

        /** Last name as returned */
        private String lastName;

        /** First name as returned */
        private String firstName;

        /** Middle name as returned */
        private String middleName;

        /** DOB as returned */
        private String dateOfBirth;

        /** Gender as returned */
        private String gender;

        /**
         * SSA Verification Code:
         *  1 = SSN/Name/DOB Verified
         *  2 = SSN Valid, Name Does Not Match
         *  3 = SSN Valid, DOB Does Not Match
         *  4 = SSN Valid, Name and DOB Do Not Match
         *  5 = SSN Not in SSA Records
         *  6 = SSN Belongs to Deceased Individual
         *  7 = SSN Has Invalid Format
         *  8 = SSN Not Issued Yet
         */
        private String verificationCode;

        /** CMIPS provider number cross-reference */
        private String providerNumber;

        /**
         * Parse a fixed-width line (100 bytes) into this record.
         */
        public static SsaVerificationReceiveRecord fromFixedWidth(String line) {
            if (line == null || line.length() < 96) return null;
            if (line.charAt(0) != 'D') return null; // Only parse data records

            return SsaVerificationReceiveRecord.builder()
                    .ssn(line.substring(1, 10).trim())
                    .lastName(line.substring(10, 36).trim())
                    .firstName(line.substring(36, 51).trim())
                    .middleName(line.substring(51, 66).trim())
                    .dateOfBirth(line.substring(66, 74).trim())
                    .gender(line.substring(74, 75).trim())
                    .verificationCode(line.substring(75, 76).trim())
                    .providerNumber(line.substring(76, 96).trim())
                    .build();
        }
    }

    /**
     * File header record format.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FileHeader {
        private String submitterEin;       // 9 chars - Employer ID
        private String submitterName;      // 57 chars
        private String fileDate;           // 8 chars YYYYMMDD
        private String fileSequence;       // 3 chars 001-999

        public String toFixedWidth() {
            StringBuilder sb = new StringBuilder(100);
            sb.append("H");
            sb.append(padRight(submitterEin, 9));
            sb.append(padRight(submitterName, 57));
            sb.append(padRight(fileDate, 8));
            sb.append(padRight(fileSequence, 3));
            sb.append(padRight("", 22));
            return sb.toString();
        }
    }

    /**
     * File trailer record format.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FileTrailer {
        private int recordCount;

        public String toFixedWidth() {
            StringBuilder sb = new StringBuilder(100);
            sb.append("T");
            sb.append(String.format("%09d", recordCount));
            sb.append(padRight("", 90));
            return sb.toString();
        }
    }

    private static String padRight(String s, int length) {
        if (s == null) s = "";
        return String.format("%-" + length + "s", s).substring(0, length);
    }
}
