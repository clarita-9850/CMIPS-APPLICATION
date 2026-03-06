package com.clarita.governance.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Links a test method to a business rule.
 *
 * <p>This annotation establishes <b>bidirectional traceability</b> between tests
 * and the business rules they verify. The governance framework validates that
 * every {@link BusinessRule} has at least one corresponding test.</p>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic usage:</h3>
 * <pre>{@code
 * @Test
 * @TestsRule(ruleId = "BR-PVM-15")
 * void testOvertimeViolation_firstViolation_returnsWarning() {
 *     // Test implementation
 * }
 * }</pre>
 *
 * <h3>With scenarios and metadata:</h3>
 * <pre>{@code
 * @Test
 * @TestsRule(
 *     ruleId = "BR-PVM-15",
 *     scenarios = {"first_violation_warning", "penalty_calculation"},
 *     author = "mythreya",
 *     lastReviewed = "2024-01-15",
 *     category = TestCategory.COMPLIANCE,
 *     notes = "Covers happy path and boundary conditions"
 * )
 * void testOvertimeViolation_fullScenario() {
 *     // Test implementation
 * }
 * }</pre>
 *
 * <h3>Testing multiple rules (repeatable):</h3>
 * <pre>{@code
 * @Test
 * @TestsRule(ruleId = "BR-PVM-15", scenarios = {"calculation"})
 * @TestsRule(ruleId = "BR-PVM-16", scenarios = {"notification"})
 * void testOvertimeWorkflow_calculatesAndNotifies() {
 *     // Test that covers multiple business rules
 * }
 * }</pre>
 *
 * <h2>Compliance Mapping</h2>
 * <p>This annotation supports requirements traceability per:</p>
 * <ul>
 *   <li>NIST 800-53 SA-11: Developer Security Testing and Evaluation</li>
 *   <li>CMS SMC: Requirements Traceability Matrix</li>
 *   <li>IEEE 829: Test Documentation Standard</li>
 * </ul>
 *
 * @see BusinessRule
 * @see TestCategory
 * @see TestsRules
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(TestsRules.class)
public @interface TestsRule {

    /**
     * The business rule ID this test verifies.
     *
     * <p>Must match an existing {@link BusinessRule#ruleId()}.</p>
     *
     * @return the rule ID to link
     */
    String ruleId();

    /**
     * Specific scenarios covered by this test.
     *
     * <p>Useful for documenting what aspects of the rule are tested.</p>
     * <p>Examples: "happy_path", "null_input", "max_boundary", "error_handling"</p>
     *
     * @return array of scenario names
     */
    String[] scenarios() default {};

    /**
     * Author of this test.
     *
     * <p>Used for audit trails and accountability.</p>
     *
     * @return author identifier
     */
    String author() default "";

    /**
     * Date when this test was last reviewed (ISO format: YYYY-MM-DD).
     *
     * <p>Example: "2024-01-15"</p>
     * <p>Used for compliance reporting to show test maintenance.</p>
     *
     * @return last review date
     */
    String lastReviewed() default "";

    /**
     * Type of test (unit, integration, compliance, etc.).
     *
     * <p>Default: {@link TestCategory#UNIT}</p>
     * <p>Should match one of the categories required by the business rule.</p>
     *
     * @return test category
     * @see TestCategory
     */
    TestCategory category() default TestCategory.UNIT;

    /**
     * Additional notes about this test.
     *
     * <p>Useful for documenting test strategy, known limitations, etc.</p>
     *
     * @return notes text
     */
    String notes() default "";

    /**
     * Priority of this test within the rule's test suite.
     *
     * <p>Lower numbers = higher priority (run first).</p>
     * <p>Default: 100 (medium priority)</p>
     *
     * @return priority value
     */
    int priority() default 100;

    /**
     * Whether this test is currently disabled.
     *
     * <p>Disabled tests are tracked but don't count toward coverage.</p>
     *
     * @return true if disabled
     */
    boolean disabled() default false;

    /**
     * Reason for disabling this test (only applicable if {@link #disabled()} is true).
     *
     * @return reason for being disabled
     */
    String disabledReason() default "";
}
