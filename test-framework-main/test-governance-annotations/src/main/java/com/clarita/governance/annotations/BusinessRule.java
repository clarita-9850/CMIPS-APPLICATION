package com.clarita.governance.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or class as containing business logic that requires test coverage.
 *
 * <p>This annotation creates a <b>contract</b> that the annotated code must have
 * corresponding tests. The governance framework validates that tests exist and
 * meet the specified requirements.</p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Method-level annotation (recommended for specific business rules):</h3>
 * <pre>{@code
 * @BusinessRule(
 *     ruleId = "BR-PVM-15",
 *     description = "Calculate overtime violation penalties per IHSS regulations",
 *     owner = "provider-management-team",
 *     severity = Severity.CRITICAL,
 *     categories = {TestCategory.UNIT, TestCategory.COMPLIANCE},
 *     compliance = {ComplianceFramework.CMS_SMC, ComplianceFramework.STATE_CDSS}
 * )
 * public OvertimeViolationResult processOvertimeViolation(Provider provider, int count) {
 *     // Implementation
 * }
 * }</pre>
 *
 * <h3>Class-level annotation (for service classes with multiple related rules):</h3>
 * <pre>{@code
 * @BusinessRule(
 *     ruleId = "BR-ELIG-01",
 *     description = "Eligibility determination service",
 *     owner = "eligibility-team",
 *     severity = Severity.HIGH
 * )
 * @Service
 * public class EligibilityService {
 *     // All public methods require tests
 * }
 * }</pre>
 *
 * <h2>Compliance Mapping</h2>
 * <p>This annotation supports federal compliance requirements:</p>
 * <ul>
 *   <li>NIST 800-53 SA-11: Developer Security Testing and Evaluation</li>
 *   <li>NIST 800-53 SA-15: Development Process, Standards, and Tools</li>
 *   <li>CMS SMC: Requirements Traceability Matrix</li>
 * </ul>
 *
 * @see TestsRule
 * @see Severity
 * @see TestCategory
 * @see ComplianceFramework
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusinessRule {

    /**
     * Unique identifier for this business rule.
     *
     * <p>Recommended format: {@code PREFIX-DOMAIN-NUMBER}</p>
     * <p>Examples: "BR-PVM-15", "BR-ELIG-001", "BR-PAY-42"</p>
     *
     * <p>This ID is used to link tests via {@link TestsRule#ruleId()}.</p>
     *
     * @return unique rule identifier
     */
    String ruleId();

    /**
     * Human-readable description of what this business rule does.
     *
     * <p>Should clearly explain the business purpose, not implementation details.</p>
     * <p>This appears in audit reports and documentation.</p>
     *
     * @return rule description
     */
    String description();

    /**
     * Team or person responsible for this business rule.
     *
     * <p>Used for notification routing and audit accountability.</p>
     * <p>Examples: "provider-team", "eligibility-team", "john.smith"</p>
     *
     * @return owner identifier
     */
    String owner();

    /**
     * Criticality level determining when build failures occur.
     *
     * <p>Default: {@link Severity#MEDIUM}</p>
     *
     * @return severity level
     * @see Severity
     */
    Severity severity() default Severity.MEDIUM;

    /**
     * Types of tests required for this rule.
     *
     * <p>Default: {@link TestCategory#UNIT}</p>
     * <p>CRITICAL rules should typically require UNIT, INTEGRATION, and COMPLIANCE.</p>
     *
     * @return required test categories
     * @see TestCategory
     */
    TestCategory[] categories() default {TestCategory.UNIT};

    /**
     * When this rule should be validated in CI/CD.
     *
     * <p>Default: {@link ExecutionPhase#PR}</p>
     * <p>CRITICAL rules should include {@link ExecutionPhase#COMMIT}.</p>
     *
     * @return execution phases
     * @see ExecutionPhase
     */
    ExecutionPhase[] phases() default {ExecutionPhase.PR};

    /**
     * Minimum code coverage required for this rule (0.0 to 1.0).
     *
     * <p>Default: 0.80 (80%)</p>
     * <p>Requires JaCoCo integration to be enabled.</p>
     *
     * @return minimum coverage percentage as decimal
     */
    double minCoverage() default 0.80;

    /**
     * Compliance frameworks this rule is associated with.
     *
     * <p>Default: none</p>
     * <p>Used for compliance reporting and audit evidence generation.</p>
     *
     * @return applicable compliance frameworks
     * @see ComplianceFramework
     */
    ComplianceFramework[] compliance() default {};

    /**
     * External requirement reference (e.g., Jira ticket, confluence page, regulation section).
     *
     * <p>Examples: "IHSS-REG-2024-4.3", "JIRA-1234", "https://..."</p>
     *
     * @return external reference
     */
    String requirementRef() default "";

    /**
     * Version when this rule was added.
     *
     * <p>Example: "1.2.0"</p>
     *
     * @return version string
     */
    String since() default "";

    /**
     * Whether this rule is deprecated.
     *
     * <p>Deprecated rules generate warnings instead of failures.</p>
     *
     * @return true if deprecated
     */
    boolean deprecated() default false;

    /**
     * Reason for deprecation (only applicable if {@link #deprecated()} is true).
     *
     * <p>Example: "Replaced by BR-PVM-16 in v2.0"</p>
     *
     * @return deprecation reason
     */
    String deprecationReason() default "";

    /**
     * Tags for categorization and filtering.
     *
     * <p>Examples: "payment", "security", "audit", "legacy"</p>
     *
     * @return array of tags
     */
    String[] tags() default {};
}
