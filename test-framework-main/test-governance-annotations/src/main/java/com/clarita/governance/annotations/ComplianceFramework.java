package com.clarita.governance.annotations;

/**
 * Defines compliance frameworks that business rules may be associated with.
 * Used for traceability between code and regulatory requirements.
 *
 * <p>Each framework represents a set of regulatory controls that may apply
 * to government and healthcare information systems.</p>
 *
 * @since 1.0.0
 */
public enum ComplianceFramework {

    /**
     * CMS Streamlined Modular Certification.
     * <p>Applies to: Medicare/Medicaid Management Information Systems (MMIS).</p>
     * <p>Authority: Centers for Medicare & Medicaid Services.</p>
     * <p>Key requirements: Business rule traceability, audit evidence.</p>
     */
    CMS_SMC("CMS-SMC", "CMS Streamlined Modular Certification",
            "https://www.cms.gov/Research-Statistics-Data-and-Systems/Computer-Data-and-Systems/MMIS"),

    /**
     * Federal Information Security Management Act.
     * <p>Applies to: All federal information systems.</p>
     * <p>Authority: NIST via OMB.</p>
     * <p>Key requirements: Risk management, continuous monitoring.</p>
     */
    FISMA("FISMA", "Federal Information Security Management Act",
          "https://www.cisa.gov/topics/cyber-threats-and-advisories/federal-information-security-modernization-act"),

    /**
     * NIST Special Publication 800-53.
     * <p>Applies to: Federal information systems and organizations.</p>
     * <p>Authority: National Institute of Standards and Technology.</p>
     * <p>Key requirements: Security and privacy controls catalog.</p>
     */
    NIST_800_53("NIST-800-53", "NIST Security and Privacy Controls",
                "https://csrc.nist.gov/publications/detail/sp/800-53/rev-5/final"),

    /**
     * Federal Risk and Authorization Management Program.
     * <p>Applies to: Cloud services used by federal agencies.</p>
     * <p>Authority: GSA, OMB, DHS, DoD.</p>
     * <p>Key requirements: Standardized cloud security assessment.</p>
     */
    FEDRAMP("FedRAMP", "Federal Risk and Authorization Management Program",
            "https://www.fedramp.gov/"),

    /**
     * Health Insurance Portability and Accountability Act.
     * <p>Applies to: Protected Health Information (PHI).</p>
     * <p>Authority: HHS Office for Civil Rights.</p>
     * <p>Key requirements: Privacy, security, breach notification.</p>
     */
    HIPAA("HIPAA", "Health Insurance Portability and Accountability Act",
          "https://www.hhs.gov/hipaa/index.html"),

    /**
     * Defense Information Systems Agency Security Technical Implementation Guides.
     * <p>Applies to: DoD information systems.</p>
     * <p>Authority: Defense Information Systems Agency.</p>
     * <p>Key requirements: Technical security configuration standards.</p>
     */
    DISA_STIG("DISA-STIG", "DoD Security Technical Implementation Guides",
              "https://public.cyber.mil/stigs/"),

    /**
     * Sarbanes-Oxley Act.
     * <p>Applies to: Publicly traded companies.</p>
     * <p>Authority: SEC.</p>
     * <p>Key requirements: Financial reporting controls, audit trails.</p>
     */
    SOX("SOX", "Sarbanes-Oxley Act",
        "https://www.sec.gov/spotlight/sarbanes-oxley.htm"),

    /**
     * California Department of Social Services requirements.
     * <p>Applies to: California social services systems (IHSS, CalWORKs).</p>
     * <p>Authority: California CDSS.</p>
     * <p>Key requirements: State-specific welfare program compliance.</p>
     */
    STATE_CDSS("CA-CDSS", "California Department of Social Services",
               "https://www.cdss.ca.gov/"),

    /**
     * Section 508 Accessibility.
     * <p>Applies to: Federal electronic and information technology.</p>
     * <p>Authority: US Access Board.</p>
     * <p>Key requirements: WCAG 2.0 Level AA compliance.</p>
     */
    SECTION_508("Section-508", "Section 508 Accessibility Requirements",
                "https://www.section508.gov/"),

    /**
     * IRS Publication 1075 - Tax Information Security.
     * <p>Applies to: Systems handling Federal Tax Information (FTI).</p>
     * <p>Authority: Internal Revenue Service.</p>
     * <p>Key requirements: Safeguarding tax information.</p>
     */
    IRS_1075("IRS-1075", "IRS Publication 1075 Tax Information Security",
             "https://www.irs.gov/privacy-disclosure/safeguards-program"),

    /**
     * Criminal Justice Information Services Security Policy.
     * <p>Applies to: Systems accessing FBI criminal justice data.</p>
     * <p>Authority: FBI CJIS Division.</p>
     * <p>Key requirements: Criminal history record information protection.</p>
     */
    CJIS("CJIS", "Criminal Justice Information Services Security Policy",
         "https://www.fbi.gov/services/cjis/cjis-security-policy-resource-center"),

    /**
     * Custom compliance framework.
     * <p>Use for organization-specific requirements not covered by predefined frameworks.</p>
     */
    CUSTOM("CUSTOM", "Custom Compliance Framework", "");

    private final String code;
    private final String fullName;
    private final String referenceUrl;

    ComplianceFramework(String code, String fullName, String referenceUrl) {
        this.code = code;
        this.fullName = fullName;
        this.referenceUrl = referenceUrl;
    }

    /**
     * Returns the short code for this framework.
     * @return short code
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the full name of this framework.
     * @return full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the official reference URL for this framework.
     * @return reference URL
     */
    public String getReferenceUrl() {
        return referenceUrl;
    }

    /**
     * Checks if this framework is a federal requirement.
     * @return true if federal
     */
    public boolean isFederal() {
        return this == FISMA || this == NIST_800_53 || this == FEDRAMP ||
               this == DISA_STIG || this == SECTION_508 || this == IRS_1075 ||
               this == CJIS || this == CMS_SMC;
    }

    /**
     * Checks if this framework relates to healthcare.
     * @return true if healthcare-related
     */
    public boolean isHealthcare() {
        return this == HIPAA || this == CMS_SMC;
    }

    /**
     * Returns frameworks commonly required together.
     * @return array of related frameworks
     */
    public ComplianceFramework[] getRelatedFrameworks() {
        return switch (this) {
            case CMS_SMC -> new ComplianceFramework[]{FISMA, NIST_800_53, HIPAA};
            case FISMA -> new ComplianceFramework[]{NIST_800_53, FEDRAMP};
            case FEDRAMP -> new ComplianceFramework[]{FISMA, NIST_800_53};
            case HIPAA -> new ComplianceFramework[]{NIST_800_53};
            case DISA_STIG -> new ComplianceFramework[]{NIST_800_53, FISMA};
            default -> new ComplianceFramework[]{};
        };
    }
}
