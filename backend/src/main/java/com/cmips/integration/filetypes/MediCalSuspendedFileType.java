package com.cmips.integration.filetypes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * DHCS Medi-Cal Suspended/Ineligible Provider List File Type Definitions
 *
 * Defines the fixed-width record format for the weekly batch file
 * received from DHCS (Department of Health Care Services).
 *
 * File Type: AAWR104A
 * Protocol: SFTP from DHCS
 * Frequency: Weekly
 * Format: Fixed-width, H/D/T record types
 * Record Length: 100 bytes
 */
public class MediCalSuspendedFileType {

    /** File type identifier */
    public static final String FILE_TYPE = "AAWR104A";

    /** SFTP source path */
    public static final String SFTP_SOURCE = "sftp://dhcs.ca.gov/outbound/medi-cal-suspended";

    /**
     * Header record - first record in file.
     *
     * Layout (100 bytes):
     *   Position 1:      Record Type 'H'
     *   Position 2-5:    Source Agency Code (4 chars, 'DHCS')
     *   Position 6-13:   File Date (YYYYMMDD)
     *   Position 14-16:  Sequence Number (001-999)
     *   Position 17-66:  File Description (50 chars)
     *   Position 67-100: Reserved (34 chars)
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HeaderRecord {
        private String sourceAgency;       // 4 chars
        private String fileDate;           // YYYYMMDD
        private String sequenceNumber;     // 001-999
        private String fileDescription;    // 50 chars

        public static HeaderRecord fromFixedWidth(String line) {
            if (line == null || line.length() < 66 || line.charAt(0) != 'H') return null;
            return HeaderRecord.builder()
                    .sourceAgency(line.substring(1, 5).trim())
                    .fileDate(line.substring(5, 13).trim())
                    .sequenceNumber(line.substring(13, 16).trim())
                    .fileDescription(line.substring(16, 66).trim())
                    .build();
        }
    }

    /**
     * Detail record - one per suspended/ineligible provider.
     *
     * Layout (100 bytes):
     *   Position 1:      Record Type 'D'
     *   Position 2:      Action Code (A=Add, C=Change, D=Delete)
     *   Position 3-11:   SSN (9 digits)
     *   Position 12-21:  NPI (10 digits)
     *   Position 22-41:  Last Name (20 chars)
     *   Position 42-56:  First Name (15 chars)
     *   Position 57-64:  Date of Birth (YYYYMMDD)
     *   Position 65:     Status (S=Suspended, I=Ineligible, T=Terminated, E=Excluded)
     *   Position 66-75:  Suspension Type (10 chars)
     *   Position 76-83:  Effective Date (YYYYMMDD)
     *   Position 84-91:  Expiration Date (YYYYMMDD, spaces if permanent)
     *   Position 92-95:  Reason Code (4 chars)
     *   Position 96-97:  Program Code (MC=Medi-Cal, IH=IHSS)
     *   Position 98-100: Reserved (3 chars)
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DetailRecord {
        private String actionCode;         // A, C, D
        private String ssn;                // 9 digits
        private String npi;                // 10 digits
        private String lastName;           // 20 chars
        private String firstName;          // 15 chars
        private String dateOfBirth;        // YYYYMMDD
        private String status;             // S, I, T, E
        private String suspensionType;     // 10 chars
        private String effectiveDate;      // YYYYMMDD
        private String expirationDate;     // YYYYMMDD or spaces
        private String reasonCode;         // 4 chars
        private String programCode;        // MC, IH

        /**
         * Parse a fixed-width line into a DetailRecord.
         */
        public static DetailRecord fromFixedWidth(String line) {
            if (line == null || line.length() < 97 || line.charAt(0) != 'D') return null;
            return DetailRecord.builder()
                    .actionCode(line.substring(1, 2).trim())
                    .ssn(line.substring(2, 11).trim())
                    .npi(line.substring(11, 21).trim())
                    .lastName(line.substring(21, 41).trim())
                    .firstName(line.substring(41, 56).trim())
                    .dateOfBirth(line.substring(56, 64).trim())
                    .status(line.substring(64, 65).trim())
                    .suspensionType(line.substring(65, 75).trim())
                    .effectiveDate(line.substring(75, 83).trim())
                    .expirationDate(line.substring(83, 91).trim())
                    .reasonCode(line.substring(91, 95).trim())
                    .programCode(line.substring(95, 97).trim())
                    .build();
        }

        /**
         * Format this record as a fixed-width string (100 bytes).
         */
        public String toFixedWidth() {
            StringBuilder sb = new StringBuilder(100);
            sb.append("D");
            sb.append(padRight(actionCode, 1));
            sb.append(padRight(ssn, 9));
            sb.append(padRight(npi, 10));
            sb.append(padRight(lastName, 20));
            sb.append(padRight(firstName, 15));
            sb.append(padRight(dateOfBirth, 8));
            sb.append(padRight(status, 1));
            sb.append(padRight(suspensionType, 10));
            sb.append(padRight(effectiveDate, 8));
            sb.append(padRight(expirationDate, 8));
            sb.append(padRight(reasonCode, 4));
            sb.append(padRight(programCode, 2));
            sb.append(padRight("", 3));
            return sb.toString();
        }

        /**
         * Check if this record matches a given provider SSN.
         */
        public boolean matchesSsn(String providerSsn) {
            if (providerSsn == null || ssn == null) return false;
            String cleanSsn = providerSsn.replaceAll("-", "").trim();
            return ssn.trim().equals(cleanSsn);
        }

        /**
         * Parse effective date as LocalDate.
         */
        public LocalDate getEffectiveDateAsLocalDate() {
            try {
                return LocalDate.parse(effectiveDate, DateTimeFormatter.BASIC_ISO_DATE);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * Parse expiration date as LocalDate (null if blank/spaces).
         */
        public LocalDate getExpirationDateAsLocalDate() {
            if (expirationDate == null || expirationDate.isBlank()) return null;
            try {
                return LocalDate.parse(expirationDate, DateTimeFormatter.BASIC_ISO_DATE);
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Trailer record - last record in file.
     *
     * Layout (100 bytes):
     *   Position 1:      Record Type 'T'
     *   Position 2-10:   Record Count (9 digits, zero-padded)
     *   Position 11-100: Reserved (90 chars)
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TrailerRecord {
        private int recordCount;

        public static TrailerRecord fromFixedWidth(String line) {
            if (line == null || line.length() < 10 || line.charAt(0) != 'T') return null;
            try {
                return TrailerRecord.builder()
                        .recordCount(Integer.parseInt(line.substring(1, 10).trim()))
                        .build();
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    private static String padRight(String s, int length) {
        if (s == null) s = "";
        return String.format("%-" + length + "s", s).substring(0, length);
    }
}
