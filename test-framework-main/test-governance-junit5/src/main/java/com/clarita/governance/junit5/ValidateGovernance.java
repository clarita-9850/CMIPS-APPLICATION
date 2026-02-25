package com.clarita.governance.junit5;

import com.clarita.governance.annotations.ExecutionPhase;
import com.clarita.governance.core.config.GovernanceConfig;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Annotation to enable governance validation in JUnit 5 tests.
 *
 * <p>Apply this annotation to a test class to automatically run governance
 * validation before tests execute. If validation fails and failOnViolations
 * is true, tests will not run.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * @ValidateGovernance(
 *     basePackages = {"com.mycompany.myapp.service", "com.mycompany.myapp.controller"},
 *     phase = ExecutionPhase.PR,
 *     failOnCritical = true,
 *     failOnHigh = true,
 *     reportDir = "target/governance-reports"
 * )
 * class ApplicationGovernanceTest {
 *
 *     @Test
 *     void contextLoads() {
 *         // This test triggers governance validation
 *     }
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(GovernanceExtension.class)
public @interface ValidateGovernance {

    /**
     * Packages to scan for business rules.
     *
     * @return array of package names
     */
    String[] basePackages();

    /**
     * Packages to scan for test rules.
     * Defaults to basePackages if not specified.
     *
     * @return array of test package names
     */
    String[] testPackages() default {};

    /**
     * Execution phase for validation.
     *
     * @return execution phase
     */
    ExecutionPhase phase() default ExecutionPhase.PR;

    /**
     * Whether to fail on CRITICAL severity violations.
     *
     * @return true to fail on critical
     */
    boolean failOnCritical() default true;

    /**
     * Whether to fail on HIGH severity violations.
     *
     * @return true to fail on high
     */
    boolean failOnHigh() default true;

    /**
     * Whether to fail on MEDIUM severity violations.
     *
     * @return true to fail on medium
     */
    boolean failOnMedium() default false;

    /**
     * Directory for report output.
     *
     * @return report directory path
     */
    String reportDir() default "target/governance-reports";

    /**
     * Report formats to generate.
     *
     * @return array of report formats
     */
    GovernanceConfig.ReportFormat[] reportFormats() default {
            GovernanceConfig.ReportFormat.JSON,
            GovernanceConfig.ReportFormat.HTML
    };

    /**
     * Whether to generate reports even when validation passes.
     *
     * @return true to always generate reports
     */
    boolean alwaysGenerateReports() default true;
}
