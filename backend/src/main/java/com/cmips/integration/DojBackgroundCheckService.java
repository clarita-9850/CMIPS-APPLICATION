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
 * DOJ (Department of Justice) Background Check / CORI Mock Service
 *
 * This service simulates the California DOJ Criminal Offender Record Information (CORI)
 * background check system used during provider enrollment.
 *
 * Real System: On-demand request via DOJ APPS (Applicant Processing Services)
 * Protocol: Secure web service (SOAP/XML)
 * Return Time: Typically 24-72 hours for electronic results
 */
@Service
public class DojBackgroundCheckService {

    private static final Logger log = LoggerFactory.getLogger(DojBackgroundCheckService.class);

    /**
     * Request payload that would be sent TO DOJ
     * Based on CA DOJ APPS (Applicant Processing Services) format
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BackgroundCheckRequest {
        // Agency Information
        private String oriNumber;              // ORI (Originating Agency Identifier) - 9 chars
        private String agencyName;             // Requesting agency name
        private String agencyMailCode;         // DOJ assigned mail code

        // Applicant/Provider Information
        private String applicantType;          // "CARE_PROVIDER" for IHSS
        private String lastName;
        private String firstName;
        private String middleName;
        private String suffix;                 // Jr, Sr, III, etc.
        private String dateOfBirth;            // YYYY-MM-DD format
        private String placeOfBirth;           // City, State
        private String ssn;                    // Social Security Number
        private String gender;                 // M/F
        private String race;                   // DOJ race codes
        private String eyeColor;
        private String hairColor;
        private String height;                 // In inches (e.g., "068" for 5'8")
        private String weight;                 // In pounds (e.g., "175")

        // Address Information
        private String streetAddress;
        private String city;
        private String state;
        private String zipCode;

        // Request Details
        private String requestType;            // "LIVE_SCAN" or "CARD_SCAN"
        private String levelOfService;         // "DOJ" or "DOJ_FBI"
        private String billingNumber;
        private String reasonForRequest;       // "IHSS_PROVIDER_ENROLLMENT"

        // CMIPS Reference
        private String providerNumber;
        private String requestId;              // CMIPS generated UUID
        private LocalDateTime requestTimestamp;
    }

    /**
     * Response payload received FROM DOJ
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BackgroundCheckResponse {
        // Header
        private String transactionControlNumber; // DOJ assigned TCN
        private String oriNumber;
        private String responseDate;
        private String responseTime;

        // Applicant Confirmation
        private String lastName;
        private String firstName;
        private String dateOfBirth;
        private String ssn;

        // Result Summary
        private String resultStatus;           // RECORD_FOUND, NO_RECORD, PENDING, REJECTED
        private String resultStatusDesc;

        // Criminal Record Details (if found)
        private List<CriminalRecord> criminalRecords;

        // CORI Classification (CMIPS determines this based on records)
        private String coriTier;               // TIER_1, TIER_2, NO_RECORD
        private String coriTierDesc;
        private boolean providerEligible;
        private boolean waiverAvailable;

        // CMIPS Processing
        private String providerNumber;
        private String requestId;
        private LocalDateTime processedTimestamp;
    }

    /**
     * Individual Criminal Record from DOJ
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CriminalRecord {
        private String cycleNumber;            // DOJ cycle tracking number
        private String arrestDate;             // YYYY-MM-DD
        private String arrestAgency;           // ORI of arresting agency
        private String arrestAgencyName;
        private String offenseCode;            // Penal code section
        private String offenseDescription;
        private String offenseType;            // FELONY, MISDEMEANOR, INFRACTION
        private String offenseCategory;        // VIOLENT, PROPERTY, DRUG, etc.
        private String courtCase;
        private String courtDisposition;       // CONVICTED, ACQUITTED, DISMISSED, etc.
        private String dispositionDate;
        private String sentenceType;           // PRISON, JAIL, PROBATION, FINE
        private String sentenceLength;
        private String releaseDate;
        private boolean isConviction;
        private boolean isTier1Crime;          // Per HSC Section 1522 - ineligible, no waiver
        private boolean isTier2Crime;          // Per HSC Section 1522 - waiver available
    }

    /**
     * CORI Classification Tiers per CA HSC Section 1522
     */
    public enum CoriTier {
        TIER_1("TIER_1", "Tier 1 Crime - Provider Ineligible, No Waiver Available",
                "Conviction for crimes specified in HSC 1522(g)(1) - e.g., murder, rape, child abuse"),
        TIER_2("TIER_2", "Tier 2 Crime - Provider May Be Eligible With Waiver",
                "Conviction for crimes specified in HSC 1522(g)(2) - waiver available from CDSS CBCB or recipient"),
        NO_RECORD("NO_RECORD", "No Disqualifying Criminal Record Found",
                "Background check passed - provider eligible for enrollment");

        private final String code;
        private final String description;
        private final String details;

        CoriTier(String code, String description, String details) {
            this.code = code;
            this.description = description;
            this.details = details;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
        public String getDetails() { return details; }
    }

    /**
     * Result Status from DOJ
     */
    public enum ResultStatus {
        RECORD_FOUND("RECORD_FOUND", "Criminal Record(s) Found"),
        NO_RECORD("NO_RECORD", "No Criminal Record Found"),
        PENDING("PENDING", "Request Pending - Awaiting FBI Response"),
        REJECTED("REJECTED", "Request Rejected - Invalid Data");

        private final String code;
        private final String description;

        ResultStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }

    /**
     * Build the exact request payload that would be submitted to DOJ APPS
     */
    public BackgroundCheckRequest buildBackgroundCheckRequest(
            String providerNumber,
            String ssn,
            String lastName,
            String firstName,
            String middleName,
            LocalDate dateOfBirth,
            String gender,
            String streetAddress,
            String city,
            String state,
            String zipCode) {

        return BackgroundCheckRequest.builder()
                // Agency Info - CA CDSS
                .oriNumber("CA0DSSIHSS")               // CDSS IHSS program ORI
                .agencyName("CA DEPT OF SOCIAL SERVICES - IHSS")
                .agencyMailCode("DSS001")

                // Applicant Info
                .applicantType("CARE_PROVIDER")
                .lastName(lastName != null ? lastName.toUpperCase() : "")
                .firstName(firstName != null ? firstName.toUpperCase() : "")
                .middleName(middleName != null ? middleName.toUpperCase() : "")
                .dateOfBirth(dateOfBirth != null ? dateOfBirth.toString() : "")
                .ssn(ssn != null ? ssn.replaceAll("-", "") : "")
                .gender(gender != null ? gender.toUpperCase().substring(0, 1) : "")

                // Address
                .streetAddress(streetAddress != null ? streetAddress.toUpperCase() : "")
                .city(city != null ? city.toUpperCase() : "")
                .state(state != null ? state.toUpperCase() : "CA")
                .zipCode(zipCode != null ? zipCode : "")

                // Request Details
                .requestType("LIVE_SCAN")
                .levelOfService("DOJ_FBI")             // Both state and federal check
                .billingNumber("IHSS" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .reasonForRequest("IHSS_PROVIDER_ENROLLMENT")

                // CMIPS Reference
                .providerNumber(providerNumber)
                .requestId(UUID.randomUUID().toString())
                .requestTimestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Format request as XML payload (DOJ APPS uses SOAP/XML)
     */
    public String formatRequestAsXml(BackgroundCheckRequest request) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n");
        xml.append("    xmlns:apps=\"http://ag.ca.gov/apps/bcs/request\">\n");
        xml.append("  <soapenv:Header>\n");
        xml.append("    <apps:AuthHeader>\n");
        xml.append("      <apps:ORI>").append(request.getOriNumber()).append("</apps:ORI>\n");
        xml.append("      <apps:AgencyName>").append(request.getAgencyName()).append("</apps:AgencyName>\n");
        xml.append("      <apps:MailCode>").append(request.getAgencyMailCode()).append("</apps:MailCode>\n");
        xml.append("    </apps:AuthHeader>\n");
        xml.append("  </soapenv:Header>\n");
        xml.append("  <soapenv:Body>\n");
        xml.append("    <apps:BackgroundCheckRequest>\n");
        xml.append("      <apps:Applicant>\n");
        xml.append("        <apps:ApplicantType>").append(request.getApplicantType()).append("</apps:ApplicantType>\n");
        xml.append("        <apps:LastName>").append(request.getLastName()).append("</apps:LastName>\n");
        xml.append("        <apps:FirstName>").append(request.getFirstName()).append("</apps:FirstName>\n");
        xml.append("        <apps:MiddleName>").append(request.getMiddleName()).append("</apps:MiddleName>\n");
        xml.append("        <apps:DOB>").append(request.getDateOfBirth()).append("</apps:DOB>\n");
        xml.append("        <apps:SSN>").append(maskSsn(request.getSsn())).append("</apps:SSN>\n");
        xml.append("        <apps:Gender>").append(request.getGender()).append("</apps:Gender>\n");
        xml.append("      </apps:Applicant>\n");
        xml.append("      <apps:Address>\n");
        xml.append("        <apps:Street>").append(request.getStreetAddress()).append("</apps:Street>\n");
        xml.append("        <apps:City>").append(request.getCity()).append("</apps:City>\n");
        xml.append("        <apps:State>").append(request.getState()).append("</apps:State>\n");
        xml.append("        <apps:ZipCode>").append(request.getZipCode()).append("</apps:ZipCode>\n");
        xml.append("      </apps:Address>\n");
        xml.append("      <apps:RequestDetails>\n");
        xml.append("        <apps:RequestType>").append(request.getRequestType()).append("</apps:RequestType>\n");
        xml.append("        <apps:LevelOfService>").append(request.getLevelOfService()).append("</apps:LevelOfService>\n");
        xml.append("        <apps:BillingNumber>").append(request.getBillingNumber()).append("</apps:BillingNumber>\n");
        xml.append("        <apps:ReasonForRequest>").append(request.getReasonForRequest()).append("</apps:ReasonForRequest>\n");
        xml.append("      </apps:RequestDetails>\n");
        xml.append("      <apps:ExternalReference>\n");
        xml.append("        <apps:ProviderNumber>").append(request.getProviderNumber()).append("</apps:ProviderNumber>\n");
        xml.append("        <apps:RequestId>").append(request.getRequestId()).append("</apps:RequestId>\n");
        xml.append("      </apps:ExternalReference>\n");
        xml.append("    </apps:BackgroundCheckRequest>\n");
        xml.append("  </soapenv:Body>\n");
        xml.append("</soapenv:Envelope>");

        return xml.toString();
    }

    /**
     * MOCK: Submit background check and receive response
     * In production, this would call DOJ APPS web service
     */
    public BackgroundCheckResponse submitBackgroundCheck(BackgroundCheckRequest request) {
        log.info("DOJ Background Check Request - Provider: {}, Name: {} {}",
                request.getProviderNumber(),
                request.getFirstName(),
                request.getLastName());

        // Log the exact XML payload that would be sent
        String xmlPayload = formatRequestAsXml(request);
        log.debug("DOJ Request Payload:\n{}", xmlPayload);

        // MOCK: Determine response based on test scenarios
        return generateMockResponse(request);
    }

    /**
     * MOCK: Generate response based on test patterns
     * Uses SSN prefix patterns for different scenarios
     */
    private BackgroundCheckResponse generateMockResponse(BackgroundCheckRequest request) {
        String ssn = request.getSsn();
        String tcn = "CA" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                String.format("%06d", (int)(Math.random() * 999999));

        BackgroundCheckResponse.BackgroundCheckResponseBuilder builder = BackgroundCheckResponse.builder()
                .transactionControlNumber(tcn)
                .oriNumber(request.getOriNumber())
                .responseDate(LocalDate.now().toString())
                .responseTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .dateOfBirth(request.getDateOfBirth())
                .ssn(maskSsn(request.getSsn()))
                .providerNumber(request.getProviderNumber())
                .requestId(request.getRequestId())
                .processedTimestamp(LocalDateTime.now());

        // Test patterns based on SSN prefix
        if (ssn.startsWith("111")) {
            // Tier 1 Crime - Murder (ineligible, no waiver)
            return buildTier1Response(builder, request, "187", "MURDER", "1999-05-15");
        } else if (ssn.startsWith("222")) {
            // Tier 1 Crime - Child Abuse (ineligible, no waiver)
            return buildTier1Response(builder, request, "273d", "CHILD ABUSE", "2005-03-20");
        } else if (ssn.startsWith("333")) {
            // Tier 2 Crime - Theft (eligible with waiver)
            return buildTier2Response(builder, request, "484", "PETTY THEFT", "2010-08-12");
        } else if (ssn.startsWith("444")) {
            // Tier 2 Crime - Drug Possession (eligible with waiver)
            return buildTier2Response(builder, request, "11350", "DRUG POSSESSION", "2015-11-30");
        } else if (ssn.startsWith("555")) {
            // Multiple records - Tier 2
            return buildMultipleTier2Response(builder, request);
        } else if (ssn.startsWith("999")) {
            // Request rejected
            return buildRejectedResponse(builder, request);
        }

        // Default: No criminal record found
        return buildNoRecordResponse(builder);
    }

    private BackgroundCheckResponse buildNoRecordResponse(BackgroundCheckResponse.BackgroundCheckResponseBuilder builder) {
        log.info("DOJ Response: NO_RECORD - Provider eligible");
        return builder
                .resultStatus(ResultStatus.NO_RECORD.getCode())
                .resultStatusDesc(ResultStatus.NO_RECORD.getDescription())
                .criminalRecords(new ArrayList<>())
                .coriTier(CoriTier.NO_RECORD.getCode())
                .coriTierDesc(CoriTier.NO_RECORD.getDescription())
                .providerEligible(true)
                .waiverAvailable(false)
                .build();
    }

    private BackgroundCheckResponse buildTier1Response(
            BackgroundCheckResponse.BackgroundCheckResponseBuilder builder,
            BackgroundCheckRequest request,
            String offenseCode,
            String offenseDesc,
            String arrestDate) {

        CriminalRecord record = CriminalRecord.builder()
                .cycleNumber("CYC" + System.currentTimeMillis())
                .arrestDate(arrestDate)
                .arrestAgency("CA0190000")
                .arrestAgencyName("LOS ANGELES POLICE DEPARTMENT")
                .offenseCode("PC " + offenseCode)
                .offenseDescription(offenseDesc)
                .offenseType("FELONY")
                .offenseCategory("VIOLENT")
                .courtDisposition("CONVICTED")
                .dispositionDate(LocalDate.parse(arrestDate).plusMonths(6).toString())
                .sentenceType("PRISON")
                .sentenceLength("15 YEARS")
                .isConviction(true)
                .isTier1Crime(true)
                .isTier2Crime(false)
                .build();

        log.info("DOJ Response: TIER_1 Crime ({}) - Provider INELIGIBLE, no waiver", offenseDesc);

        return builder
                .resultStatus(ResultStatus.RECORD_FOUND.getCode())
                .resultStatusDesc(ResultStatus.RECORD_FOUND.getDescription())
                .criminalRecords(List.of(record))
                .coriTier(CoriTier.TIER_1.getCode())
                .coriTierDesc(CoriTier.TIER_1.getDescription())
                .providerEligible(false)
                .waiverAvailable(false)
                .build();
    }

    private BackgroundCheckResponse buildTier2Response(
            BackgroundCheckResponse.BackgroundCheckResponseBuilder builder,
            BackgroundCheckRequest request,
            String offenseCode,
            String offenseDesc,
            String arrestDate) {

        CriminalRecord record = CriminalRecord.builder()
                .cycleNumber("CYC" + System.currentTimeMillis())
                .arrestDate(arrestDate)
                .arrestAgency("CA0370000")
                .arrestAgencyName("SAN DIEGO COUNTY SHERIFF")
                .offenseCode("PC " + offenseCode)
                .offenseDescription(offenseDesc)
                .offenseType("MISDEMEANOR")
                .offenseCategory("PROPERTY")
                .courtDisposition("CONVICTED")
                .dispositionDate(LocalDate.parse(arrestDate).plusMonths(3).toString())
                .sentenceType("PROBATION")
                .sentenceLength("2 YEARS")
                .isConviction(true)
                .isTier1Crime(false)
                .isTier2Crime(true)
                .build();

        log.info("DOJ Response: TIER_2 Crime ({}) - Provider may be eligible with waiver", offenseDesc);

        return builder
                .resultStatus(ResultStatus.RECORD_FOUND.getCode())
                .resultStatusDesc(ResultStatus.RECORD_FOUND.getDescription())
                .criminalRecords(List.of(record))
                .coriTier(CoriTier.TIER_2.getCode())
                .coriTierDesc(CoriTier.TIER_2.getDescription())
                .providerEligible(false)
                .waiverAvailable(true)
                .build();
    }

    private BackgroundCheckResponse buildMultipleTier2Response(
            BackgroundCheckResponse.BackgroundCheckResponseBuilder builder,
            BackgroundCheckRequest request) {

        List<CriminalRecord> records = new ArrayList<>();

        records.add(CriminalRecord.builder()
                .cycleNumber("CYC001")
                .arrestDate("2008-04-15")
                .offenseCode("PC 484")
                .offenseDescription("PETTY THEFT")
                .offenseType("MISDEMEANOR")
                .courtDisposition("CONVICTED")
                .isConviction(true)
                .isTier1Crime(false)
                .isTier2Crime(true)
                .build());

        records.add(CriminalRecord.builder()
                .cycleNumber("CYC002")
                .arrestDate("2012-09-22")
                .offenseCode("HS 11350")
                .offenseDescription("DRUG POSSESSION")
                .offenseType("MISDEMEANOR")
                .courtDisposition("CONVICTED")
                .isConviction(true)
                .isTier1Crime(false)
                .isTier2Crime(true)
                .build());

        log.info("DOJ Response: Multiple TIER_2 Crimes - Provider may be eligible with waiver");

        return builder
                .resultStatus(ResultStatus.RECORD_FOUND.getCode())
                .resultStatusDesc(ResultStatus.RECORD_FOUND.getDescription())
                .criminalRecords(records)
                .coriTier(CoriTier.TIER_2.getCode())
                .coriTierDesc(CoriTier.TIER_2.getDescription())
                .providerEligible(false)
                .waiverAvailable(true)
                .build();
    }

    private BackgroundCheckResponse buildRejectedResponse(
            BackgroundCheckResponse.BackgroundCheckResponseBuilder builder,
            BackgroundCheckRequest request) {

        log.warn("DOJ Response: REJECTED - Invalid request data");

        return builder
                .resultStatus(ResultStatus.REJECTED.getCode())
                .resultStatusDesc("Request rejected - SSN format invalid or not on file")
                .criminalRecords(new ArrayList<>())
                .coriTier(null)
                .coriTierDesc(null)
                .providerEligible(false)
                .waiverAvailable(false)
                .build();
    }

    private String maskSsn(String ssn) {
        if (ssn == null || ssn.length() < 4) return "XXX-XX-XXXX";
        return "XXX-XX-" + ssn.substring(ssn.length() - 4);
    }
}
