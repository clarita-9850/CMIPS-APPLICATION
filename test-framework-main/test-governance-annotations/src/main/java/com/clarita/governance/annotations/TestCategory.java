package com.clarita.governance.annotations;

/**
 * Defines the types of tests that can be required for business rules.
 * Multiple categories can be specified to require comprehensive test coverage.
 *
 * <p>Maps to federal testing requirements:</p>
 * <ul>
 *   <li>UNIT - Component testing per NIST SP 800-53 SA-11</li>
 *   <li>INTEGRATION - Integration testing per SA-11(2)</li>
 *   <li>SECURITY - Security testing per SA-11(1)</li>
 *   <li>COMPLIANCE - Regulatory compliance verification</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum TestCategory {

    /**
     * Isolated unit tests with mocked dependencies.
     * <p>Focus: Individual method/class behavior in isolation.</p>
     * <p>Execution time: Milliseconds.</p>
     */
    UNIT("Unit Test", "Isolated unit tests with mocked dependencies", true),

    /**
     * Tests with real external dependencies (database, APIs, message queues).
     * <p>Focus: Component interaction and data flow.</p>
     * <p>Execution time: Seconds to minutes.</p>
     */
    INTEGRATION("Integration Test", "Tests with external dependencies", true),

    /**
     * API contract verification tests.
     * <p>Focus: Request/response format, status codes, headers.</p>
     * <p>Typically uses tools like Pact or Spring Cloud Contract.</p>
     */
    CONTRACT("Contract Test", "API contract verification", false),

    /**
     * Regulatory compliance verification tests.
     * <p>Focus: Business rules mandated by regulations (CMS, HIPAA, etc.).</p>
     * <p>Required for audit evidence per NIST 800-53 CA-7.</p>
     */
    COMPLIANCE("Compliance Test", "Regulatory compliance verification", true),

    /**
     * Security-focused tests.
     * <p>Focus: Authentication, authorization, input validation, OWASP Top 10.</p>
     * <p>Required per NIST 800-53 SA-11(1).</p>
     */
    SECURITY("Security Test", "Security vulnerability and access control testing", true),

    /**
     * Performance and load tests.
     * <p>Focus: Response time, throughput, resource utilization.</p>
     * <p>Required per NIST 800-53 SC-5 (Denial of Service Protection).</p>
     */
    PERFORMANCE("Performance Test", "Performance and load testing", false),

    /**
     * Boundary and edge case tests.
     * <p>Focus: Null values, empty collections, maximum values, invalid inputs.</p>
     */
    EDGE_CASE("Edge Case Test", "Boundary and edge case testing", false),

    /**
     * Regression tests to prevent reintroduction of fixed bugs.
     * <p>Focus: Previously identified defects.</p>
     */
    REGRESSION("Regression Test", "Tests for previously fixed bugs", false),

    /**
     * End-to-end tests covering complete user workflows.
     * <p>Focus: Full user journey from UI to database and back.</p>
     * <p>Execution time: Minutes.</p>
     */
    E2E("End-to-End Test", "Complete workflow testing", false),

    /**
     * Smoke tests for basic functionality verification.
     * <p>Focus: Critical paths work after deployment.</p>
     * <p>Execution time: Fast.</p>
     */
    SMOKE("Smoke Test", "Basic functionality verification", false),

    /**
     * Accessibility tests for WCAG compliance.
     * <p>Focus: Screen reader compatibility, keyboard navigation, color contrast.</p>
     * <p>Required per Section 508 compliance.</p>
     */
    ACCESSIBILITY("Accessibility Test", "WCAG and Section 508 compliance", false),

    /**
     * Data integrity and validation tests.
     * <p>Focus: Data persistence, transformation, consistency.</p>
     */
    DATA_INTEGRITY("Data Integrity Test", "Data persistence and validation", false);

    private final String displayName;
    private final String description;
    private final boolean commonlyRequired;

    TestCategory(String displayName, String description, boolean commonlyRequired) {
        this.displayName = displayName;
        this.description = description;
        this.commonlyRequired = commonlyRequired;
    }

    /**
     * Human-readable display name for this category.
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Description of what this test category covers.
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Whether this category is commonly required for business rules.
     * @return true if commonly required
     */
    public boolean isCommonlyRequired() {
        return commonlyRequired;
    }

    /**
     * Default categories required for CRITICAL severity rules.
     * @return array of required categories
     */
    public static TestCategory[] getDefaultForCritical() {
        return new TestCategory[]{UNIT, INTEGRATION, COMPLIANCE, SECURITY};
    }

    /**
     * Default categories required for HIGH severity rules.
     * @return array of required categories
     */
    public static TestCategory[] getDefaultForHigh() {
        return new TestCategory[]{UNIT, INTEGRATION};
    }

    /**
     * Default categories required for MEDIUM/LOW severity rules.
     * @return array of required categories
     */
    public static TestCategory[] getDefaultForMediumLow() {
        return new TestCategory[]{UNIT};
    }
}
