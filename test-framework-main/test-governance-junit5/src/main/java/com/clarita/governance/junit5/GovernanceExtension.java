package com.clarita.governance.junit5;

import com.clarita.governance.core.GovernanceEngine;
import com.clarita.governance.core.config.GovernanceConfig;
import com.clarita.governance.core.model.ValidationResult;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

/**
 * JUnit 5 extension for governance validation.
 *
 * <p>This extension hooks into the JUnit 5 lifecycle to run governance
 * validation before tests execute and generate reports afterward.</p>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>BeforeAllCallback: Run governance validation</li>
 *   <li>If violations and configured to fail: throw exception (tests don't run)</li>
 *   <li>Tests execute normally</li>
 *   <li>AfterAllCallback: Generate reports</li>
 * </ol>
 *
 * @since 1.0.0
 */
public class GovernanceExtension implements BeforeAllCallback, AfterAllCallback {

    private static final Logger log = LoggerFactory.getLogger(GovernanceExtension.class);

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(GovernanceExtension.class);

    private static final String RESULT_KEY = "validationResult";
    private static final String ENGINE_KEY = "governanceEngine";

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        ValidateGovernance annotation = testClass.getAnnotation(ValidateGovernance.class);

        if (annotation == null) {
            log.debug("No @ValidateGovernance annotation found on {}", testClass.getName());
            return;
        }

        log.info("Running governance validation for test class: {}", testClass.getName());

        // Build configuration from annotation
        GovernanceConfig config = buildConfig(annotation);

        // Create and run engine
        GovernanceEngine engine = new GovernanceEngine(config);
        ValidationResult result = engine.validate();

        // Store for afterAll
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        store.put(RESULT_KEY, result);
        store.put(ENGINE_KEY, engine);

        // Log summary
        log.info("Governance validation result: {}", result.getSummary());

        // Check if we should fail
        if (result.shouldFailBuild()) {
            String message = String.format(
                    "Governance validation failed for %s: %s",
                    testClass.getName(),
                    result.getSummary()
            );
            throw new GovernanceValidationException(message, result);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        ValidationResult result = store.get(RESULT_KEY, ValidationResult.class);
        GovernanceEngine engine = store.get(ENGINE_KEY, GovernanceEngine.class);

        if (result == null || engine == null) {
            return;
        }

        Class<?> testClass = context.getRequiredTestClass();
        ValidateGovernance annotation = testClass.getAnnotation(ValidateGovernance.class);

        if (annotation == null) {
            return;
        }

        // Generate reports
        if (annotation.alwaysGenerateReports() || !result.getViolations().isEmpty()) {
            try {
                engine.generateReports(result);
                log.info("Governance reports generated in: {}", annotation.reportDir());
            } catch (Exception e) {
                log.warn("Failed to generate governance reports", e);
            }
        }
    }

    private GovernanceConfig buildConfig(ValidateGovernance annotation) {
        GovernanceConfig config = new GovernanceConfig();

        // Packages
        config.setBasePackages(Set.of(annotation.basePackages()));
        if (annotation.testPackages().length > 0) {
            config.setTestPackages(Set.of(annotation.testPackages()));
        } else {
            config.setTestPackages(Set.of(annotation.basePackages()));
        }

        // Phase
        config.setCurrentPhase(annotation.phase());

        // Failure behavior
        config.setFailOnCritical(annotation.failOnCritical());
        config.setFailOnHigh(annotation.failOnHigh());
        config.setFailOnMedium(annotation.failOnMedium());
        config.setFailOnLow(false);

        // Reports
        config.setReportEnabled(true);
        config.setReportOutputDir(Path.of(annotation.reportDir()));
        config.setReportFormats(EnumSet.copyOf(Set.of(annotation.reportFormats())));

        return config;
    }

    /**
     * Exception thrown when governance validation fails.
     */
    public static class GovernanceValidationException extends RuntimeException {

        private final ValidationResult result;

        public GovernanceValidationException(String message, ValidationResult result) {
            super(message);
            this.result = result;
        }

        public ValidationResult getResult() {
            return result;
        }
    }
}
