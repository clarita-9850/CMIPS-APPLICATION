package com.clarita.governance.spring;

import com.clarita.governance.annotations.ComplianceFramework;
import com.clarita.governance.annotations.ExecutionPhase;
import com.clarita.governance.core.config.GovernanceConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

/**
 * Spring Boot configuration properties for governance.
 * Maps YAML/properties configuration to the governance engine.
 *
 * <h2>Example Configuration (application.yml)</h2>
 * <pre>{@code
 * governance:
 *   enabled: true
 *   scan:
 *     base-packages:
 *       - com.mycompany.myapp.service
 *       - com.mycompany.myapp.controller
 *   execution:
 *     phase: PR
 *   failure:
 *     on-critical: true
 *     on-high: true
 *   report:
 *     output-dir: target/governance-reports
 *     formats:
 *       - JSON
 *       - HTML
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "governance")
public class GovernanceProperties {

    /**
     * Enable/disable governance validation.
     */
    private boolean enabled = true;

    /**
     * Scanning configuration.
     */
    private Scan scan = new Scan();

    /**
     * Execution configuration.
     */
    private Execution execution = new Execution();

    /**
     * Failure configuration.
     */
    private Failure failure = new Failure();

    /**
     * Validation rules configuration.
     */
    private Validation validation = new Validation();

    /**
     * Report configuration.
     */
    private Report report = new Report();

    /**
     * Compliance configuration.
     */
    private Compliance compliance = new Compliance();

    /**
     * Coverage configuration.
     */
    private Coverage coverage = new Coverage();

    /**
     * Application metadata.
     */
    private String applicationName = "";
    private String applicationVersion = "";

    // Getters and setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Scan getScan() {
        return scan;
    }

    public void setScan(Scan scan) {
        this.scan = scan;
    }

    public Execution getExecution() {
        return execution;
    }

    public void setExecution(Execution execution) {
        this.execution = execution;
    }

    public Failure getFailure() {
        return failure;
    }

    public void setFailure(Failure failure) {
        this.failure = failure;
    }

    public Validation getValidation() {
        return validation;
    }

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Compliance getCompliance() {
        return compliance;
    }

    public void setCompliance(Compliance compliance) {
        this.compliance = compliance;
    }

    public Coverage getCoverage() {
        return coverage;
    }

    public void setCoverage(Coverage coverage) {
        this.coverage = coverage;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    /**
     * Converts these properties to a GovernanceConfig object.
     *
     * @return GovernanceConfig
     */
    public GovernanceConfig toConfig() {
        GovernanceConfig config = new GovernanceConfig();

        // Scan
        config.setBasePackages(new HashSet<>(scan.getBasePackages()));
        config.setExcludePackages(new HashSet<>(scan.getExcludePackages()));
        config.setTestPackages(new HashSet<>(scan.getTestPackages()));
        config.setParallelScan(scan.isParallel());
        config.setCacheEnabled(scan.isCacheEnabled());

        // Execution
        config.setCurrentPhase(execution.getPhase());
        config.setValidationTimeout(execution.getTimeout());

        // Failure
        config.setFailOnCritical(failure.isOnCritical());
        config.setFailOnHigh(failure.isOnHigh());
        config.setFailOnMedium(failure.isOnMedium());
        config.setFailOnLow(failure.isOnLow());
        config.setFailOnOrphanedTests(failure.isOnOrphanedTests());
        config.setFailOnDeprecatedWithoutReplacement(failure.isOnDeprecatedWithoutReplacement());

        // Validation
        config.setRequireOwner(validation.isRequireOwner());
        config.setValidOwners(new HashSet<>(validation.getValidOwners()));
        if (validation.getRuleIdPattern() != null && !validation.getRuleIdPattern().isEmpty()) {
            config.setRuleIdPattern(validation.getRuleIdPattern());
        }
        config.setDescriptionMinLength(validation.getDescriptionMinLength());
        config.setRequireComplianceTag(validation.isRequireComplianceTag());

        // Report
        config.setReportEnabled(report.isEnabled());
        if (report.getOutputDir() != null && !report.getOutputDir().isEmpty()) {
            config.setReportOutputDir(java.nio.file.Path.of(report.getOutputDir()));
        }
        if (!report.getFormats().isEmpty()) {
            config.setReportFormats(EnumSet.copyOf(report.getFormats()));
        }
        config.setIncludePassing(report.isIncludePassing());
        config.setIncludeSourceLocations(report.isIncludeSourceLocations());
        config.setIncludeTestLocations(report.isIncludeTestLocations());
        config.setHtmlTitle(report.getHtmlTitle());

        // Compliance
        config.setTrackedFrameworks(EnumSet.copyOf(
                compliance.getFrameworks().isEmpty() ?
                        EnumSet.noneOf(ComplianceFramework.class) :
                        EnumSet.copyOf(compliance.getFrameworks())
        ));
        config.setPerFrameworkReports(compliance.isPerFrameworkReports());

        // Coverage
        config.setCoverageEnabled(coverage.isEnabled());
        if (coverage.getJacocoReportPath() != null && !coverage.getJacocoReportPath().isEmpty()) {
            config.setJacocoReportPath(java.nio.file.Path.of(coverage.getJacocoReportPath()));
        }
        config.setEnforceMinimumCoverage(coverage.isEnforceMinimum());
        config.setDefaultMinimumCoverage(coverage.getDefaultMinimum());

        // Metadata
        config.setApplicationName(applicationName);
        config.setApplicationVersion(applicationVersion);

        return config;
    }

    // Nested configuration classes

    public static class Scan {
        private List<String> basePackages = new ArrayList<>();
        private List<String> excludePackages = new ArrayList<>();
        private List<String> excludePatterns = new ArrayList<>();
        private List<String> testPackages = new ArrayList<>();
        private boolean parallel = true;
        private boolean cacheEnabled = true;

        public List<String> getBasePackages() { return basePackages; }
        public void setBasePackages(List<String> basePackages) { this.basePackages = basePackages; }
        public List<String> getExcludePackages() { return excludePackages; }
        public void setExcludePackages(List<String> excludePackages) { this.excludePackages = excludePackages; }
        public List<String> getExcludePatterns() { return excludePatterns; }
        public void setExcludePatterns(List<String> excludePatterns) { this.excludePatterns = excludePatterns; }
        public List<String> getTestPackages() { return testPackages; }
        public void setTestPackages(List<String> testPackages) { this.testPackages = testPackages; }
        public boolean isParallel() { return parallel; }
        public void setParallel(boolean parallel) { this.parallel = parallel; }
        public boolean isCacheEnabled() { return cacheEnabled; }
        public void setCacheEnabled(boolean cacheEnabled) { this.cacheEnabled = cacheEnabled; }
    }

    public static class Execution {
        private ExecutionPhase phase = ExecutionPhase.PR;
        private int timeout = 300;

        public ExecutionPhase getPhase() { return phase; }
        public void setPhase(ExecutionPhase phase) { this.phase = phase; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }

    public static class Failure {
        private boolean onCritical = true;
        private boolean onHigh = true;
        private boolean onMedium = false;
        private boolean onLow = false;
        private boolean onOrphanedTests = false;
        private boolean onDeprecatedWithoutReplacement = false;

        public boolean isOnCritical() { return onCritical; }
        public void setOnCritical(boolean onCritical) { this.onCritical = onCritical; }
        public boolean isOnHigh() { return onHigh; }
        public void setOnHigh(boolean onHigh) { this.onHigh = onHigh; }
        public boolean isOnMedium() { return onMedium; }
        public void setOnMedium(boolean onMedium) { this.onMedium = onMedium; }
        public boolean isOnLow() { return onLow; }
        public void setOnLow(boolean onLow) { this.onLow = onLow; }
        public boolean isOnOrphanedTests() { return onOrphanedTests; }
        public void setOnOrphanedTests(boolean onOrphanedTests) { this.onOrphanedTests = onOrphanedTests; }
        public boolean isOnDeprecatedWithoutReplacement() { return onDeprecatedWithoutReplacement; }
        public void setOnDeprecatedWithoutReplacement(boolean v) { this.onDeprecatedWithoutReplacement = v; }
    }

    public static class Validation {
        private boolean requireOwner = true;
        private List<String> validOwners = new ArrayList<>();
        private String ruleIdPattern = "^[A-Z]{2,5}-[A-Z]{2,5}-\\d{2,4}$";
        private int descriptionMinLength = 10;
        private boolean requireComplianceTag = false;

        public boolean isRequireOwner() { return requireOwner; }
        public void setRequireOwner(boolean requireOwner) { this.requireOwner = requireOwner; }
        public List<String> getValidOwners() { return validOwners; }
        public void setValidOwners(List<String> validOwners) { this.validOwners = validOwners; }
        public String getRuleIdPattern() { return ruleIdPattern; }
        public void setRuleIdPattern(String ruleIdPattern) { this.ruleIdPattern = ruleIdPattern; }
        public int getDescriptionMinLength() { return descriptionMinLength; }
        public void setDescriptionMinLength(int descriptionMinLength) { this.descriptionMinLength = descriptionMinLength; }
        public boolean isRequireComplianceTag() { return requireComplianceTag; }
        public void setRequireComplianceTag(boolean requireComplianceTag) { this.requireComplianceTag = requireComplianceTag; }
    }

    public static class Report {
        private boolean enabled = true;
        private String outputDir = "target/governance-reports";
        private List<GovernanceConfig.ReportFormat> formats = List.of(
                GovernanceConfig.ReportFormat.JSON,
                GovernanceConfig.ReportFormat.HTML
        );
        private boolean includePassing = true;
        private boolean includeSourceLocations = true;
        private boolean includeTestLocations = true;
        private String htmlTitle = "Test Governance Report";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getOutputDir() { return outputDir; }
        public void setOutputDir(String outputDir) { this.outputDir = outputDir; }
        public List<GovernanceConfig.ReportFormat> getFormats() { return formats; }
        public void setFormats(List<GovernanceConfig.ReportFormat> formats) { this.formats = formats; }
        public boolean isIncludePassing() { return includePassing; }
        public void setIncludePassing(boolean includePassing) { this.includePassing = includePassing; }
        public boolean isIncludeSourceLocations() { return includeSourceLocations; }
        public void setIncludeSourceLocations(boolean v) { this.includeSourceLocations = v; }
        public boolean isIncludeTestLocations() { return includeTestLocations; }
        public void setIncludeTestLocations(boolean v) { this.includeTestLocations = v; }
        public String getHtmlTitle() { return htmlTitle; }
        public void setHtmlTitle(String htmlTitle) { this.htmlTitle = htmlTitle; }
    }

    public static class Compliance {
        private List<ComplianceFramework> frameworks = new ArrayList<>();
        private boolean perFrameworkReports = true;

        public List<ComplianceFramework> getFrameworks() { return frameworks; }
        public void setFrameworks(List<ComplianceFramework> frameworks) { this.frameworks = frameworks; }
        public boolean isPerFrameworkReports() { return perFrameworkReports; }
        public void setPerFrameworkReports(boolean perFrameworkReports) { this.perFrameworkReports = perFrameworkReports; }
    }

    public static class Coverage {
        private boolean enabled = false;
        private String jacocoReportPath = "target/site/jacoco/jacoco.xml";
        private boolean enforceMinimum = true;
        private double defaultMinimum = 0.80;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getJacocoReportPath() { return jacocoReportPath; }
        public void setJacocoReportPath(String jacocoReportPath) { this.jacocoReportPath = jacocoReportPath; }
        public boolean isEnforceMinimum() { return enforceMinimum; }
        public void setEnforceMinimum(boolean enforceMinimum) { this.enforceMinimum = enforceMinimum; }
        public double getDefaultMinimum() { return defaultMinimum; }
        public void setDefaultMinimum(double defaultMinimum) { this.defaultMinimum = defaultMinimum; }
    }
}
