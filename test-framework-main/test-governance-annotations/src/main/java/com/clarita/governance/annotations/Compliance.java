package com.clarita.governance.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level annotation for compliance metadata.
 *
 * <p>Marks a class as subject to specific compliance frameworks and provides
 * audit-related metadata. This annotation is used for compliance reporting
 * and audit evidence generation.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * @Service
 * @Compliance(
 *     frameworks = {ComplianceFramework.CMS_SMC, ComplianceFramework.HIPAA},
 *     owner = "provider-management-team",
 *     auditContact = "compliance@example.gov",
 *     certificationDate = "2024-01-01",
 *     nextReviewDate = "2025-01-01",
 *     documentationUrl = "https://docs.example.gov/provider-service"
 * )
 * public class ProviderService {
 *     // Service implementation
 * }
 * }</pre>
 *
 * <h2>Compliance Mapping</h2>
 * <p>This annotation supports:</p>
 * <ul>
 *   <li>NIST 800-53 PL-8: Security Architecture</li>
 *   <li>NIST 800-53 CA-7: Continuous Monitoring</li>
 *   <li>CMS SMC: System Documentation Requirements</li>
 * </ul>
 *
 * @see ComplianceFramework
 * @see BusinessRule
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Compliance {

    /**
     * Compliance frameworks applicable to this class.
     *
     * @return applicable frameworks
     */
    ComplianceFramework[] frameworks();

    /**
     * Team or person responsible for compliance.
     *
     * @return compliance owner
     */
    String owner();

    /**
     * Contact email for audit inquiries.
     *
     * @return audit contact email
     */
    String auditContact() default "";

    /**
     * Date of last compliance certification (ISO format: YYYY-MM-DD).
     *
     * <p>Example: "2024-01-01"</p>
     *
     * @return certification date
     */
    String certificationDate() default "";

    /**
     * Date when next compliance review is due (ISO format: YYYY-MM-DD).
     *
     * <p>Example: "2025-01-01"</p>
     *
     * @return next review date
     */
    String nextReviewDate() default "";

    /**
     * URL to compliance documentation.
     *
     * @return documentation URL
     */
    String documentationUrl() default "";

    /**
     * Data classification level handled by this component.
     *
     * <p>Examples: "PUBLIC", "INTERNAL", "CONFIDENTIAL", "PHI", "PII", "FTI"</p>
     *
     * @return data classification
     */
    String dataClassification() default "";

    /**
     * Risk level of this component.
     *
     * <p>Examples: "HIGH", "MODERATE", "LOW"</p>
     * <p>Maps to NIST FIPS 199 impact levels.</p>
     *
     * @return risk level
     */
    String riskLevel() default "";

    /**
     * Security controls implemented by this component.
     *
     * <p>Examples: "AC-2", "AU-2", "SC-8"</p>
     * <p>Maps to NIST 800-53 control identifiers.</p>
     *
     * @return array of control identifiers
     */
    String[] securityControls() default {};
}
