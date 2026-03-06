package com.clarita.governance.core;

import com.clarita.governance.annotations.ExecutionPhase;
import com.clarita.governance.core.config.GovernanceConfig;
import com.clarita.governance.core.model.ValidationResult;
import com.clarita.governance.core.report.HtmlReportGenerator;
import com.clarita.governance.core.report.JsonReportGenerator;
import com.clarita.governance.core.report.ReportGenerator;
import com.clarita.governance.core.scanner.AnnotationScanner;
import com.clarita.governance.core.validator.GovernanceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Main entry point for the Test Governance Framework.
 *
 * <p>This class provides a high-level API for running governance validation
 * and generating reports. It orchestrates the scanner, validator, and report generators.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * GovernanceConfig config = GovernanceConfig.defaults()
 *     .addBasePackage("com.mycompany.myapp")
 *     .setCurrentPhase(ExecutionPhase.PR)
 *     .setFailOnCritical(true)
 *     .setFailOnHigh(true);
 *
 * GovernanceEngine engine = new GovernanceEngine(config);
 * ValidationResult result = engine.validate();
 *
 * if (result.shouldFailBuild()) {
 *     System.err.println("Governance validation failed: " + result.getSummary());
 *     System.exit(1);
 * }
 *
 * engine.generateReports(result);
 * }</pre>
 *
 * @since 1.0.0
 */
public class GovernanceEngine {

    private static final Logger log = LoggerFactory.getLogger(GovernanceEngine.class);

    private final GovernanceConfig config;
    private final GovernanceValidator validator;
    private final Map<GovernanceConfig.ReportFormat, ReportGenerator> reportGenerators;

    /**
     * Creates a new governance engine with the given configuration.
     *
     * @param config the configuration
     */
    public GovernanceEngine(GovernanceConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.validator = new GovernanceValidator(config);
        this.reportGenerators = initializeReportGenerators();
    }

    /**
     * Creates a new governance engine with minimal configuration.
     *
     * @param basePackage the package to scan
     * @return new engine
     */
    public static GovernanceEngine forPackage(String basePackage) {
        return new GovernanceEngine(GovernanceConfig.minimal(basePackage));
    }

    /**
     * Creates a new governance engine with default configuration.
     *
     * @return new engine with defaults
     */
    public static GovernanceEngine withDefaults() {
        return new GovernanceEngine(GovernanceConfig.defaults());
    }

    private Map<GovernanceConfig.ReportFormat, ReportGenerator> initializeReportGenerators() {
        Map<GovernanceConfig.ReportFormat, ReportGenerator> generators = new EnumMap<>(GovernanceConfig.ReportFormat.class);
        generators.put(GovernanceConfig.ReportFormat.JSON, new JsonReportGenerator(config));
        generators.put(GovernanceConfig.ReportFormat.HTML, new HtmlReportGenerator(config));
        return generators;
    }

    /**
     * Runs governance validation using the configured phase.
     *
     * @return validation result
     */
    public ValidationResult validate() {
        return validate(config.getCurrentPhase());
    }

    /**
     * Runs governance validation for a specific phase.
     *
     * @param phase the execution phase
     * @return validation result
     */
    public ValidationResult validate(ExecutionPhase phase) {
        log.info("Starting governance validation for phase: {}", phase);
        return validator.validate(phase);
    }

    /**
     * Generates all configured reports from a validation result.
     *
     * @param result the validation result
     * @throws IOException if report generation fails
     */
    public void generateReports(ValidationResult result) throws IOException {
        if (!config.isReportEnabled()) {
            log.debug("Report generation is disabled");
            return;
        }

        Path outputDir = config.getReportOutputDir();
        Files.createDirectories(outputDir);

        for (GovernanceConfig.ReportFormat format : config.getReportFormats()) {
            generateReport(result, format);
        }
    }

    /**
     * Generates a report in a specific format.
     *
     * @param result the validation result
     * @param format the report format
     * @throws IOException if report generation fails
     */
    public void generateReport(ValidationResult result, GovernanceConfig.ReportFormat format) throws IOException {
        ReportGenerator generator = reportGenerators.get(format);
        if (generator == null) {
            log.warn("No generator available for format: {}", format);
            return;
        }

        Path outputPath = config.getReportOutputDir().resolve(generator.getDefaultFilename());
        generator.generate(result, outputPath);
        log.info("Generated {} report: {}", format, outputPath);
    }

    /**
     * Runs validation and generates reports in one call.
     *
     * @return validation result
     * @throws IOException if report generation fails
     */
    public ValidationResult validateAndReport() throws IOException {
        ValidationResult result = validate();
        generateReports(result);
        return result;
    }

    /**
     * Runs validation and optionally fails the build based on configuration.
     *
     * <p>This method is designed for CI/CD integration. It will throw a
     * {@link GovernanceViolationException} if the build should fail.</p>
     *
     * @throws GovernanceViolationException if build should fail
     * @throws IOException if report generation fails
     */
    public void validateOrFail() throws IOException {
        ValidationResult result = validateAndReport();

        if (result.shouldFailBuild()) {
            throw new GovernanceViolationException(result);
        }

        log.info("Governance validation passed: {}", result.getSummary());
    }

    /**
     * Gets the configuration.
     *
     * @return config
     */
    public GovernanceConfig getConfig() {
        return config;
    }

    /**
     * Gets the validator.
     *
     * @return validator
     */
    public GovernanceValidator getValidator() {
        return validator;
    }

    /**
     * Gets the scanner.
     *
     * @return scanner
     */
    public AnnotationScanner getScanner() {
        return validator.getScanner();
    }

    /**
     * Registers a custom report generator.
     *
     * @param generator the generator to register
     */
    public void registerReportGenerator(ReportGenerator generator) {
        reportGenerators.put(generator.getFormat(), generator);
    }

    /**
     * Exception thrown when governance validation fails and the build should stop.
     */
    public static class GovernanceViolationException extends RuntimeException {

        private final ValidationResult result;

        public GovernanceViolationException(ValidationResult result) {
            super("Governance validation failed: " + result.getSummary());
            this.result = result;
        }

        public ValidationResult getResult() {
            return result;
        }
    }
}
