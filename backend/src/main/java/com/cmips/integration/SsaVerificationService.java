package com.cmips.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * SSA (Social Security Administration) SSN Verification Mock Service
 *
 * This service simulates the SSA SSNVS (Social Security Number Verification Service)
 * which is used to verify provider SSNs during enrollment.
 *
 * Real System: Weekly batch file submitted to SSA
 * Protocol: SFTP file transfer to SSA
 * Format: Fixed-width text file per SSA specifications
 */
@Service
public class SsaVerificationService {

    private static final Logger log = LoggerFactory.getLogger(SsaVerificationService.class);

    /**
     * Request payload that would be sent TO SSA
     * Based on SSA SSNVS (Social Security Number Verification Service) format
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SsnVerificationRequest {
        // File Header (sent once per batch)
        private String submitterEin;           // Employer Identification Number (9 chars)
        private String submitterName;          // Agency name (up to 57 chars)
        private String fileDate;               // YYYYMMDD format
        private String fileSequence;           // 001-999 for multiple files per day

        // Individual Record (one per provider)
        private String ssn;                    // 9 digits, no dashes
        private String lastName;               // Up to 26 chars
        private String firstName;              // Up to 15 chars
        private String middleName;             // Up to 15 chars (optional)
        private String dateOfBirth;            // YYYYMMDD format
        private String gender;                 // M/F
        private String providerNumber;         // CMIPS internal reference (20 chars)
    }

    /**
     * Response payload received FROM SSA
     * SSA returns verification results via return file
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SsnVerificationResponse {
        // Header info
        private String submitterEin;
        private String responseDate;           // YYYYMMDD
        private String originalFileDate;

        // Individual Record Response
        private String ssn;
        private String providerNumber;
        private String verificationCode;       // See VerificationCode enum
        private String verificationCodeDesc;
        private String lastName;
        private String firstName;
        private String dateOfBirth;

        // CMIPS processing fields
        private LocalDateTime processedTimestamp;
        private String batchId;
    }

    /**
     * SSA Verification Return Codes
     * Per SSA SSNVS Return Code documentation
     */
    public enum VerificationCode {
        VERIFIED("1", "SSN/Name/DOB Verified - Match found"),
        NAME_MISMATCH("2", "SSN Valid, Name Does Not Match"),
        DOB_MISMATCH("3", "SSN Valid, DOB Does Not Match"),
        NAME_DOB_MISMATCH("4", "SSN Valid, Name and DOB Do Not Match"),
        SSN_NOT_FOUND("5", "SSN Not in SSA Records"),
        SSN_DECEASED("6", "SSN Belongs to Deceased Individual"),
        SSN_INVALID_FORMAT("7", "SSN Has Invalid Format"),
        SSN_NOT_ISSUED("8", "SSN Not Issued Yet");

        private final String code;
        private final String description;

        VerificationCode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }

    /**
     * Build the exact request payload that would be submitted to SSA
     */
    public SsnVerificationRequest buildVerificationRequest(
            String ssn,
            String lastName,
            String firstName,
            String middleName,
            LocalDate dateOfBirth,
            String gender,
            String providerNumber) {

        return SsnVerificationRequest.builder()
                .submitterEin("943456789")                    // CA CDSS EIN
                .submitterName("CA DEPT OF SOCIAL SERVICES CMIPS")
                .fileDate(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
                .fileSequence("001")
                .ssn(ssn != null ? ssn.replaceAll("-", "") : "")
                .lastName(lastName != null ? lastName.toUpperCase().trim() : "")
                .firstName(firstName != null ? firstName.toUpperCase().trim() : "")
                .middleName(middleName != null ? middleName.toUpperCase().trim() : "")
                .dateOfBirth(dateOfBirth != null ? dateOfBirth.format(DateTimeFormatter.BASIC_ISO_DATE) : "")
                .gender(gender != null ? gender.toUpperCase().substring(0, 1) : "")
                .providerNumber(providerNumber != null ? providerNumber : "")
                .build();
    }

    /**
     * Convert request to fixed-width file format (SSA specification)
     * This is the exact format that would be transmitted to SSA via SFTP
     */
    public String formatRequestAsFixedWidth(SsnVerificationRequest request) {
        StringBuilder sb = new StringBuilder();

        // File Header Record (Type H)
        sb.append("H");                                          // Position 1: Record Type
        sb.append(padRight(request.getSubmitterEin(), 9));       // Position 2-10: EIN
        sb.append(padRight(request.getSubmitterName(), 57));     // Position 11-67: Submitter Name
        sb.append(request.getFileDate());                        // Position 68-75: File Date
        sb.append(request.getFileSequence());                    // Position 76-78: Sequence
        sb.append(padRight("", 22));                             // Position 79-100: Reserved
        sb.append("\n");

        // Data Record (Type D)
        sb.append("D");                                          // Position 1: Record Type
        sb.append(padRight(request.getSsn(), 9));                // Position 2-10: SSN
        sb.append(padRight(request.getLastName(), 26));          // Position 11-36: Last Name
        sb.append(padRight(request.getFirstName(), 15));         // Position 37-51: First Name
        sb.append(padRight(request.getMiddleName(), 15));        // Position 52-66: Middle Name
        sb.append(request.getDateOfBirth());                     // Position 67-74: DOB
        sb.append(request.getGender());                          // Position 75: Gender
        sb.append(padRight(request.getProviderNumber(), 20));    // Position 76-95: Provider Ref
        sb.append(padRight("", 5));                              // Position 96-100: Reserved
        sb.append("\n");

        // Trailer Record (Type T)
        sb.append("T");                                          // Position 1: Record Type
        sb.append(padLeft("1", 9, '0'));                         // Position 2-10: Record Count
        sb.append(padRight("", 90));                             // Position 11-100: Reserved

        return sb.toString();
    }

    /**
     * MOCK: Simulate SSA verification response
     * In production, this would parse the return file from SSA
     */
    public SsnVerificationResponse verifySSN(SsnVerificationRequest request) {
        log.info("SSA SSN Verification Request - SSN: {}XXX-XX-{}",
                request.getSsn().substring(0, 3),
                request.getSsn().substring(7));

        // Log the exact payload that would be sent
        String payload = formatRequestAsFixedWidth(request);
        log.debug("SSA Request Payload:\n{}", payload);

        // MOCK: Determine response based on test scenarios
        VerificationCode verificationCode = determineVerificationCode(request);

        SsnVerificationResponse response = SsnVerificationResponse.builder()
                .submitterEin(request.getSubmitterEin())
                .responseDate(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
                .originalFileDate(request.getFileDate())
                .ssn(request.getSsn())
                .providerNumber(request.getProviderNumber())
                .verificationCode(verificationCode.getCode())
                .verificationCodeDesc(verificationCode.getDescription())
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .dateOfBirth(request.getDateOfBirth())
                .processedTimestamp(LocalDateTime.now())
                .batchId(UUID.randomUUID().toString())
                .build();

        log.info("SSA SSN Verification Response - Code: {} ({})",
                verificationCode.getCode(), verificationCode.getDescription());

        return response;
    }

    /**
     * MOCK: Determine verification result based on test patterns
     * Allows testing different scenarios by using specific SSN patterns
     */
    private VerificationCode determineVerificationCode(SsnVerificationRequest request) {
        String ssn = request.getSsn();

        // Test patterns for different scenarios
        if (ssn.startsWith("000")) {
            return VerificationCode.SSN_INVALID_FORMAT;
        } else if (ssn.startsWith("999")) {
            return VerificationCode.SSN_NOT_FOUND;
        } else if (ssn.startsWith("888")) {
            return VerificationCode.SSN_DECEASED;
        } else if (ssn.startsWith("777")) {
            return VerificationCode.NAME_MISMATCH;
        } else if (ssn.startsWith("666")) {
            return VerificationCode.DOB_MISMATCH;
        } else if (ssn.startsWith("555")) {
            return VerificationCode.NAME_DOB_MISMATCH;
        } else if (ssn.startsWith("111")) {
            return VerificationCode.SSN_NOT_ISSUED;
        }

        // Default: Verified
        return VerificationCode.VERIFIED;
    }

    private String padRight(String s, int length) {
        if (s == null) s = "";
        return String.format("%-" + length + "s", s).substring(0, length);
    }

    private String padLeft(String s, int length, char padChar) {
        if (s == null) s = "";
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < length; i++) {
            sb.append(padChar);
        }
        sb.append(s);
        return sb.toString().substring(0, length);
    }
}
