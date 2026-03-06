package com.cmips.integration.filetypes;

/**
 * DOJ Background Check / CORI File Type Definitions
 *
 * Defines the XML request and response formats for DOJ APPS
 * (Applicant Processing Services) background check integration.
 *
 * File Type: DOJ_CORI_REQUEST
 * Protocol: SOAP/XML web service
 * Format: XML per DOJ APPS specification
 */
public class DojBackgroundCheckFileType {

    /** File type identifier */
    public static final String FILE_TYPE = "DOJ_CORI_REQUEST";

    /** DOJ APPS SOAP endpoint (non-production) */
    public static final String WSDL_URL = "https://apps.doj.ca.gov/bcs/service?wsdl";

    /** SOAP Action for background check request */
    public static final String SOAP_ACTION = "http://ag.ca.gov/apps/bcs/submitBackgroundCheck";

    /** XML namespace for DOJ APPS */
    public static final String NAMESPACE = "http://ag.ca.gov/apps/bcs/request";

    /**
     * Background check XML request record.
     * Represents the applicant data submitted to DOJ for CORI check.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CoriRequestRecord {
        // Agency identifiers (XML attributes)
        private String oriNumber;          // ORI - Originating Agency Identifier
        private String agencyName;
        private String agencyMailCode;

        // Applicant data (XML elements)
        private String applicantType;      // CARE_PROVIDER
        private String lastName;
        private String firstName;
        private String middleName;
        private String suffix;
        private String dateOfBirth;        // YYYY-MM-DD
        private String ssn;                // 9 digits
        private String gender;             // M/F

        // Address
        private String streetAddress;
        private String city;
        private String state;
        private String zipCode;

        // Request details
        private String requestType;        // LIVE_SCAN or CARD_SCAN
        private String levelOfService;     // DOJ or DOJ_FBI
        private String billingNumber;
        private String reasonForRequest;   // IHSS_PROVIDER_ENROLLMENT

        // External reference
        private String providerNumber;
        private String requestId;

        /**
         * Serialize this record to XML format for DOJ APPS SOAP request.
         */
        public String toXml() {
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n");
            xml.append("    xmlns:apps=\"").append(NAMESPACE).append("\">\n");
            xml.append("  <soapenv:Header>\n");
            xml.append("    <apps:AuthHeader>\n");
            xml.append("      <apps:ORI>").append(esc(oriNumber)).append("</apps:ORI>\n");
            xml.append("      <apps:AgencyName>").append(esc(agencyName)).append("</apps:AgencyName>\n");
            xml.append("      <apps:MailCode>").append(esc(agencyMailCode)).append("</apps:MailCode>\n");
            xml.append("    </apps:AuthHeader>\n");
            xml.append("  </soapenv:Header>\n");
            xml.append("  <soapenv:Body>\n");
            xml.append("    <apps:BackgroundCheckRequest>\n");
            xml.append("      <apps:Applicant>\n");
            xml.append("        <apps:ApplicantType>").append(esc(applicantType)).append("</apps:ApplicantType>\n");
            xml.append("        <apps:LastName>").append(esc(lastName)).append("</apps:LastName>\n");
            xml.append("        <apps:FirstName>").append(esc(firstName)).append("</apps:FirstName>\n");
            xml.append("        <apps:MiddleName>").append(esc(middleName)).append("</apps:MiddleName>\n");
            if (suffix != null && !suffix.isEmpty()) {
                xml.append("        <apps:Suffix>").append(esc(suffix)).append("</apps:Suffix>\n");
            }
            xml.append("        <apps:DOB>").append(esc(dateOfBirth)).append("</apps:DOB>\n");
            xml.append("        <apps:SSN>").append(esc(ssn)).append("</apps:SSN>\n");
            xml.append("        <apps:Gender>").append(esc(gender)).append("</apps:Gender>\n");
            xml.append("      </apps:Applicant>\n");
            xml.append("      <apps:Address>\n");
            xml.append("        <apps:Street>").append(esc(streetAddress)).append("</apps:Street>\n");
            xml.append("        <apps:City>").append(esc(city)).append("</apps:City>\n");
            xml.append("        <apps:State>").append(esc(state)).append("</apps:State>\n");
            xml.append("        <apps:ZipCode>").append(esc(zipCode)).append("</apps:ZipCode>\n");
            xml.append("      </apps:Address>\n");
            xml.append("      <apps:RequestDetails>\n");
            xml.append("        <apps:RequestType>").append(esc(requestType)).append("</apps:RequestType>\n");
            xml.append("        <apps:LevelOfService>").append(esc(levelOfService)).append("</apps:LevelOfService>\n");
            xml.append("        <apps:BillingNumber>").append(esc(billingNumber)).append("</apps:BillingNumber>\n");
            xml.append("        <apps:ReasonForRequest>").append(esc(reasonForRequest)).append("</apps:ReasonForRequest>\n");
            xml.append("      </apps:RequestDetails>\n");
            xml.append("      <apps:ExternalReference>\n");
            xml.append("        <apps:ProviderNumber>").append(esc(providerNumber)).append("</apps:ProviderNumber>\n");
            xml.append("        <apps:RequestId>").append(esc(requestId)).append("</apps:RequestId>\n");
            xml.append("      </apps:ExternalReference>\n");
            xml.append("    </apps:BackgroundCheckRequest>\n");
            xml.append("  </soapenv:Body>\n");
            xml.append("</soapenv:Envelope>");
            return xml.toString();
        }
    }

    /**
     * Background check XML response record.
     * Parsed from DOJ APPS SOAP response.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CoriResponseRecord {
        // Transaction info
        private String transactionControlNumber;  // DOJ-assigned TCN
        private String oriNumber;
        private String responseDate;
        private String responseTime;

        // Applicant confirmation
        private String lastName;
        private String firstName;
        private String dateOfBirth;
        private String ssn;

        // Result
        private String resultStatus;      // RECORD_FOUND, NO_RECORD, PENDING, REJECTED
        private String resultStatusDesc;

        // CORI classification
        private String coriTier;          // TIER_1, TIER_2, NO_RECORD
        private String coriTierDesc;
        private boolean providerEligible;
        private boolean waiverAvailable;

        // External reference
        private String providerNumber;
        private String requestId;
    }

    /**
     * Individual criminal record entry from DOJ response.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CriminalRecordEntry {
        private String cycleNumber;
        private String arrestDate;
        private String arrestAgencyOri;
        private String arrestAgencyName;
        private String offenseCode;        // Penal/H&S code section
        private String offenseDescription;
        private String offenseType;        // FELONY, MISDEMEANOR, INFRACTION
        private String offenseCategory;    // VIOLENT, PROPERTY, DRUG, etc.
        private String courtDisposition;   // CONVICTED, ACQUITTED, DISMISSED
        private String dispositionDate;
        private String sentenceType;
        private String sentenceLength;
        private String releaseDate;
        private boolean conviction;
        private boolean tier1Crime;        // HSC 1522(g)(1) - no waiver
        private boolean tier2Crime;        // HSC 1522(g)(2) - waiver available
    }

    /**
     * CORI Tier classification per CA Health & Safety Code Section 1522.
     */
    public enum CoriTier {
        TIER_1("TIER_1",
                "Tier 1 - Ineligible, No Waiver Available",
                "Conviction for crimes listed in HSC 1522(g)(1): murder, rape, child abuse, etc."),
        TIER_2("TIER_2",
                "Tier 2 - May Be Eligible With Waiver",
                "Conviction for crimes listed in HSC 1522(g)(2): waiver available from CDSS CBCB or recipient"),
        NO_RECORD("NO_RECORD",
                "No Disqualifying Criminal Record",
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

    /** XML-escape a string value */
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
