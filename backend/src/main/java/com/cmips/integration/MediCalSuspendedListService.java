package com.cmips.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Medi-Cal Suspended/Ineligible Provider List Mock Service
 *
 * This service simulates the DHCS (Department of Health Care Services) Medi-Cal
 * Suspended and Ineligible Provider List interface.
 *
 * Real System: Weekly batch file received from DHCS
 * Protocol: SFTP file transfer from DHCS
 * Format: Fixed-width or CSV file per DHCS specifications
 */
@Service
public class MediCalSuspendedListService {

    private static final Logger log = LoggerFactory.getLogger(MediCalSuspendedListService.class);

    /**
     * Lookup request to check if provider is on Medi-Cal suspended/ineligible list
     * This represents what CMIPS sends when checking a provider
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MediCalLookupRequest {
        // Provider identifiers - at least one required
        private String ssn;                    // Provider SSN
        private String npi;                    // National Provider Identifier (if applicable)
        private String lastName;
        private String firstName;
        private String dateOfBirth;            // YYYY-MM-DD

        // CMIPS reference
        private String providerNumber;
        private String requestId;
        private LocalDateTime requestTimestamp;
    }

    /**
     * Response from Medi-Cal suspended list lookup
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MediCalLookupResponse {
        // Match result
        private boolean matchFound;
        private String matchConfidence;        // EXACT, PARTIAL, NONE

        // Provider info from list (if matched)
        private String ssn;
        private String npi;
        private String lastName;
        private String firstName;
        private String dateOfBirth;

        // Suspension/Ineligibility details
        private String status;                 // SUSPENDED, INELIGIBLE, TERMINATED
        private String statusDesc;
        private String suspensionType;         // See SuspensionType enum
        private String suspensionTypeDesc;
        private LocalDate effectiveDate;       // When suspension started
        private LocalDate expirationDate;      // When suspension ends (if applicable)
        private String reasonCode;             // DHCS reason code
        private String reasonDesc;
        private String actionNumber;           // DHCS action reference number

        // Additional details
        private String sanctionSource;         // DHCS, OIG, GSA, etc.
        private List<String> excludedPrograms; // MEDI_CAL, IHSS, etc.
        private String reinstateConditions;    // Conditions for reinstatement

        // File/batch info
        private String fileDate;               // Date of source file
        private String fileSequence;           // Batch sequence number

        // CMIPS processing
        private String providerNumber;
        private String requestId;
        private LocalDateTime processedTimestamp;
    }

    /**
     * Record format received from DHCS weekly batch file
     * This is the raw format in the suspended/ineligible list file
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MediCalSuspendedRecord {
        // Record identifier
        private String recordType;             // H=Header, D=Detail, T=Trailer
        private String actionCode;             // A=Add, C=Change, D=Delete

        // Provider identification
        private String ssn;
        private String npi;
        private String lastName;
        private String firstName;
        private String middleName;
        private String suffix;
        private String dateOfBirth;
        private String gender;

        // Address
        private String address1;
        private String address2;
        private String city;
        private String state;
        private String zipCode;

        // Suspension details
        private String status;
        private String suspensionType;
        private String effectiveDate;
        private String expirationDate;
        private String reasonCode;
        private String reasonText;
        private String actionNumber;
        private String sanctionSource;

        // Programs affected
        private String programCode;            // MC=Medi-Cal, IH=IHSS
        private String excludedServices;       // Specific services excluded
    }

    /**
     * Provider status on Medi-Cal list
     */
    public enum ProviderStatus {
        ACTIVE("ACTIVE", "Provider in good standing - not on suspended/ineligible list"),
        SUSPENDED("SUSPENDED", "Provider temporarily suspended from Medi-Cal program"),
        INELIGIBLE("INELIGIBLE", "Provider permanently ineligible for Medi-Cal program"),
        TERMINATED("TERMINATED", "Provider terminated from Medi-Cal program"),
        EXCLUDED("EXCLUDED", "Provider excluded per federal sanctions (OIG/GSA)");

        private final String code;
        private final String description;

        ProviderStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }

    /**
     * Types of Medi-Cal suspensions
     */
    public enum SuspensionType {
        FRAUD("FRAUD", "Suspended due to fraud or abuse"),
        BILLING("BILLING", "Suspended due to billing irregularities"),
        QUALITY("QUALITY", "Suspended due to quality of care concerns"),
        LICENSE("LICENSE", "Suspended due to license revocation/suspension"),
        CONVICTION("CONVICTION", "Suspended due to criminal conviction"),
        EXCLUSION_OIG("EXCLUSION_OIG", "Excluded per OIG (Office of Inspector General)"),
        EXCLUSION_GSA("EXCLUSION_GSA", "Excluded per GSA (General Services Administration)"),
        OVERPAYMENT("OVERPAYMENT", "Suspended due to overpayment recovery"),
        COMPLIANCE("COMPLIANCE", "Suspended due to compliance violations"),
        OWNERSHIP("OWNERSHIP", "Suspended due to ownership/control issues");

        private final String code;
        private final String description;

        SuspensionType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }

    /**
     * Build lookup request for Medi-Cal suspended list check
     */
    public MediCalLookupRequest buildLookupRequest(
            String providerNumber,
            String ssn,
            String lastName,
            String firstName,
            LocalDate dateOfBirth) {

        return MediCalLookupRequest.builder()
                .ssn(ssn != null ? ssn.replaceAll("-", "") : "")
                .lastName(lastName != null ? lastName.toUpperCase() : "")
                .firstName(firstName != null ? firstName.toUpperCase() : "")
                .dateOfBirth(dateOfBirth != null ? dateOfBirth.toString() : "")
                .providerNumber(providerNumber)
                .requestId(UUID.randomUUID().toString())
                .requestTimestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Format the batch file record (fixed-width format received from DHCS)
     */
    public String formatSuspendedRecordAsFixedWidth(MediCalSuspendedRecord record) {
        StringBuilder sb = new StringBuilder();

        // Detail Record Format (100 bytes per record)
        sb.append(padRight(record.getRecordType(), 1));              // Pos 1: Record Type
        sb.append(padRight(record.getActionCode(), 1));              // Pos 2: Action Code
        sb.append(padRight(record.getSsn(), 9));                     // Pos 3-11: SSN
        sb.append(padRight(record.getNpi(), 10));                    // Pos 12-21: NPI
        sb.append(padRight(record.getLastName(), 20));               // Pos 22-41: Last Name
        sb.append(padRight(record.getFirstName(), 15));              // Pos 42-56: First Name
        sb.append(padRight(record.getDateOfBirth(), 8));             // Pos 57-64: DOB (YYYYMMDD)
        sb.append(padRight(record.getStatus(), 1));                  // Pos 65: Status (S/I/T/E)
        sb.append(padRight(record.getSuspensionType(), 10));         // Pos 66-75: Suspension Type
        sb.append(padRight(record.getEffectiveDate(), 8));           // Pos 76-83: Effective Date
        sb.append(padRight(record.getExpirationDate(), 8));          // Pos 84-91: Expiration Date
        sb.append(padRight(record.getReasonCode(), 4));              // Pos 92-95: Reason Code
        sb.append(padRight(record.getProgramCode(), 2));             // Pos 96-97: Program Code
        sb.append(padRight("", 3));                                  // Pos 98-100: Reserved

        return sb.toString();
    }

    /**
     * Generate sample batch file format (what CMIPS receives from DHCS weekly)
     */
    public String generateSampleBatchFile() {
        StringBuilder file = new StringBuilder();
        String fileDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        // Header Record
        file.append("H");                                            // Record Type
        file.append("DHCS");                                         // Source Agency
        file.append(fileDate);                                       // File Date
        file.append("001");                                          // Sequence Number
        file.append(padRight("MEDI-CAL SUSPENDED/INELIGIBLE LIST", 50));
        file.append(padRight("", 36));                               // Reserved
        file.append("\n");

        // Sample Detail Records (for testing)
        file.append(formatSuspendedRecordAsFixedWidth(MediCalSuspendedRecord.builder()
                .recordType("D").actionCode("A")
                .ssn("666111111").lastName("TESTFRAUD").firstName("JOHN")
                .dateOfBirth("19800115").status("S")
                .suspensionType("FRAUD").effectiveDate("20230601")
                .reasonCode("FR01").programCode("MC")
                .build()));
        file.append("\n");

        file.append(formatSuspendedRecordAsFixedWidth(MediCalSuspendedRecord.builder()
                .recordType("D").actionCode("A")
                .ssn("666222222").lastName("TESTEXCLUDE").firstName("JANE")
                .dateOfBirth("19750320").status("E")
                .suspensionType("EXCLUSION_OIG").effectiveDate("20220815")
                .reasonCode("OI01").programCode("MC")
                .build()));
        file.append("\n");

        // Trailer Record
        file.append("T");                                            // Record Type
        file.append(padLeft("2", 9, '0'));                           // Record Count
        file.append(padRight("", 90));                               // Reserved

        return file.toString();
    }

    /**
     * MOCK: Check provider against Medi-Cal suspended list
     * In production, this would query the imported DHCS batch data
     */
    public MediCalLookupResponse checkSuspendedList(MediCalLookupRequest request) {
        log.info("Medi-Cal Suspended List Check - Provider: {}, SSN: {}XXX-XX-{}",
                request.getProviderNumber(),
                request.getSsn().substring(0, 3),
                request.getSsn().substring(request.getSsn().length() - 4));

        // MOCK: Determine response based on test scenarios
        return generateMockResponse(request);
    }

    /**
     * MOCK: Generate response based on SSN patterns for testing
     */
    private MediCalLookupResponse generateMockResponse(MediCalLookupRequest request) {
        String ssn = request.getSsn();
        String fileDate = LocalDate.now().minusDays(3).format(DateTimeFormatter.BASIC_ISO_DATE);

        MediCalLookupResponse.MediCalLookupResponseBuilder builder = MediCalLookupResponse.builder()
                .providerNumber(request.getProviderNumber())
                .requestId(request.getRequestId())
                .processedTimestamp(LocalDateTime.now())
                .fileDate(fileDate)
                .fileSequence("001");

        // Test patterns based on SSN prefix
        if (ssn.startsWith("666")) {
            // SSN starting with 666 = SUSPENDED for fraud
            return buildSuspendedResponse(builder, request, SuspensionType.FRAUD);
        } else if (ssn.startsWith("777")) {
            // SSN starting with 777 = INELIGIBLE (permanently)
            return buildIneligibleResponse(builder, request);
        } else if (ssn.startsWith("888")) {
            // SSN starting with 888 = OIG Exclusion
            return buildExcludedResponse(builder, request, SuspensionType.EXCLUSION_OIG);
        } else if (ssn.startsWith("999")) {
            // SSN starting with 999 = GSA Exclusion
            return buildExcludedResponse(builder, request, SuspensionType.EXCLUSION_GSA);
        }

        // Default: Not on suspended list (eligible)
        return buildNotFoundResponse(builder, request);
    }

    private MediCalLookupResponse buildNotFoundResponse(
            MediCalLookupResponse.MediCalLookupResponseBuilder builder,
            MediCalLookupRequest request) {

        log.info("Medi-Cal Check Result: NOT FOUND - Provider not on suspended/ineligible list");

        return builder
                .matchFound(false)
                .matchConfidence("NONE")
                .status(ProviderStatus.ACTIVE.getCode())
                .statusDesc(ProviderStatus.ACTIVE.getDescription())
                .build();
    }

    private MediCalLookupResponse buildSuspendedResponse(
            MediCalLookupResponse.MediCalLookupResponseBuilder builder,
            MediCalLookupRequest request,
            SuspensionType suspensionType) {

        log.info("Medi-Cal Check Result: SUSPENDED ({}) - Provider suspended from Medi-Cal",
                suspensionType.getCode());

        return builder
                .matchFound(true)
                .matchConfidence("EXACT")
                .ssn(maskSsn(request.getSsn()))
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .dateOfBirth(request.getDateOfBirth())
                .status(ProviderStatus.SUSPENDED.getCode())
                .statusDesc(ProviderStatus.SUSPENDED.getDescription())
                .suspensionType(suspensionType.getCode())
                .suspensionTypeDesc(suspensionType.getDescription())
                .effectiveDate(LocalDate.now().minusMonths(6))
                .expirationDate(LocalDate.now().plusMonths(18)) // Temporary - has end date
                .reasonCode("FR01")
                .reasonDesc("Fraud/Abuse - Billing irregularities identified")
                .actionNumber("DHCS-2024-" + System.currentTimeMillis() % 100000)
                .sanctionSource("DHCS")
                .excludedPrograms(List.of("MEDI_CAL", "IHSS"))
                .reinstateConditions("Complete compliance training and repay identified overpayments")
                .build();
    }

    private MediCalLookupResponse buildIneligibleResponse(
            MediCalLookupResponse.MediCalLookupResponseBuilder builder,
            MediCalLookupRequest request) {

        log.info("Medi-Cal Check Result: INELIGIBLE - Provider permanently ineligible");

        return builder
                .matchFound(true)
                .matchConfidence("EXACT")
                .ssn(maskSsn(request.getSsn()))
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .dateOfBirth(request.getDateOfBirth())
                .status(ProviderStatus.INELIGIBLE.getCode())
                .statusDesc(ProviderStatus.INELIGIBLE.getDescription())
                .suspensionType(SuspensionType.CONVICTION.getCode())
                .suspensionTypeDesc(SuspensionType.CONVICTION.getDescription())
                .effectiveDate(LocalDate.now().minusYears(2))
                .expirationDate(null) // Permanent - no end date
                .reasonCode("CV01")
                .reasonDesc("Criminal conviction - healthcare fraud")
                .actionNumber("DHCS-2022-" + System.currentTimeMillis() % 100000)
                .sanctionSource("DHCS")
                .excludedPrograms(List.of("MEDI_CAL", "IHSS", "MEDICARE"))
                .reinstateConditions(null) // Permanent exclusion
                .build();
    }

    private MediCalLookupResponse buildExcludedResponse(
            MediCalLookupResponse.MediCalLookupResponseBuilder builder,
            MediCalLookupRequest request,
            SuspensionType exclusionType) {

        String source = exclusionType == SuspensionType.EXCLUSION_OIG ? "OIG" : "GSA";

        log.info("Medi-Cal Check Result: EXCLUDED ({}) - Federal exclusion", source);

        return builder
                .matchFound(true)
                .matchConfidence("EXACT")
                .ssn(maskSsn(request.getSsn()))
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .dateOfBirth(request.getDateOfBirth())
                .status(ProviderStatus.EXCLUDED.getCode())
                .statusDesc(ProviderStatus.EXCLUDED.getDescription())
                .suspensionType(exclusionType.getCode())
                .suspensionTypeDesc(exclusionType.getDescription())
                .effectiveDate(LocalDate.now().minusYears(1))
                .expirationDate(null) // Federal exclusions typically indefinite
                .reasonCode(source + "01")
                .reasonDesc("Federal exclusion - " + source + " sanction list match")
                .actionNumber(source + "-" + LocalDate.now().getYear() + "-" + System.currentTimeMillis() % 100000)
                .sanctionSource(source)
                .excludedPrograms(List.of("MEDI_CAL", "IHSS", "MEDICARE", "MEDICAID", "ALL_FEDERAL"))
                .reinstateConditions("Apply for reinstatement with " + source + " after minimum exclusion period")
                .build();
    }

    private String maskSsn(String ssn) {
        if (ssn == null || ssn.length() < 4) return "XXX-XX-XXXX";
        return "XXX-XX-" + ssn.substring(ssn.length() - 4);
    }

    private String padRight(String s, int length) {
        if (s == null) s = "";
        return String.format("%-" + length + "s", s).substring(0, Math.min(length, Math.max(s.length(), length)));
    }

    private String padLeft(String s, int length, char padChar) {
        if (s == null) s = "";
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < length; i++) {
            sb.append(padChar);
        }
        sb.append(s);
        return sb.length() > length ? sb.substring(sb.length() - length) : sb.toString();
    }
}
