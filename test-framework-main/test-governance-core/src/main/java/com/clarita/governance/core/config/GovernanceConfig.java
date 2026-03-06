package com.clarita.governance.core.config;

import com.clarita.governance.annotations.ComplianceFramework;
import com.clarita.governance.annotations.ExecutionPhase;
import com.clarita.governance.annotations.Severity;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Configuration for the governance validation engine.
 * This class holds all settings that control validation behavior.
 *
 * @since 1.0.0
 */
public class GovernanceConfig {

    // Scanning
    private Set<String> basePackages = new HashSet<>();
    private Set<String> excludePackages = new HashSet<>();
    private Set<Pattern> excludePatterns = new HashSet<>();
    private Set<String> testPackages = new HashSet<>();
    private boolean parallelScan = true;
    private boolean cacheEnabled = true;

    // Execution
    private ExecutionPhase currentPhase = ExecutionPhase.PR;
    private int validationTimeout = 300; // seconds

    // Failure behavior
    private boolean failOnCritical = true;
    private boolean failOnHigh = true;
    private boolean failOnMedium = false;
    private boolean failOnLow = false;
    private boolean failOnOrphanedTests = false;
    private boolean failOnDeprecatedWithoutReplacement = false;

    // Validation rules
    private boolean requireOwner = true;
    private Set<String> validOwners = new HashSet<>();
    private Pattern ruleIdPattern = Pattern.compile("^[A-Z]{2,5}-[A-Z]{2,5}-\\d{2,4}$");
    private int descriptionMinLength = 10;
    private boolean requireComplianceTag = false;
    private Set<ComplianceFramework> allowedFrameworks = EnumSet.allOf(ComplianceFramework.class);

    // Reports
    private boolean reportEnabled = true;
    private Path reportOutputDir = Path.of("target/governance-reports");
    private Set<ReportFormat> reportFormats = EnumSet.of(ReportFormat.JSON, ReportFormat.HTML);
    private boolean includePassing = true;
    private boolean includeSourceLocations = true;
    private boolean includeTestLocations = true;
    private String htmlTitle = "Test Governance Report";
    private String htmlLogo = "";
    private String htmlCustomCss = "";

    // Compliance
    private Set<ComplianceFramework> trackedFrameworks = new HashSet<>();
    private boolean perFrameworkReports = true;
    private Map<ComplianceFramework, Double> frameworkCoverage = new EnumMap<>(ComplianceFramework.class);

    // Coverage (JaCoCo integration)
    private boolean coverageEnabled = false;
    private Path jacocoReportPath = Path.of("target/site/jacoco/jacoco.xml");
    private boolean enforceMinimumCoverage = true;
    private double defaultMinimumCoverage = 0.80;

    // Application metadata
    private String applicationName = "";
    private String applicationVersion = "";

    /**
     * Report output formats.
     */
    public enum ReportFormat {
        JSON, HTML, PDF, XML, CSV, MARKDOWN
    }

    // Getters and setters

    public Set<String> getBasePackages() {
        return basePackages;
    }

    public GovernanceConfig setBasePackages(Set<String> basePackages) {
        this.basePackages = basePackages;
        return this;
    }

    public GovernanceConfig addBasePackage(String pkg) {
        this.basePackages.add(pkg);
        return this;
    }

    public Set<String> getExcludePackages() {
        return excludePackages;
    }

    public GovernanceConfig setExcludePackages(Set<String> excludePackages) {
        this.excludePackages = excludePackages;
        return this;
    }

    public Set<Pattern> getExcludePatterns() {
        return excludePatterns;
    }

    public GovernanceConfig setExcludePatterns(Set<Pattern> excludePatterns) {
        this.excludePatterns = excludePatterns;
        return this;
    }

    public GovernanceConfig addExcludePattern(String pattern) {
        this.excludePatterns.add(Pattern.compile(pattern));
        return this;
    }

    public Set<String> getTestPackages() {
        return testPackages;
    }

    public GovernanceConfig setTestPackages(Set<String> testPackages) {
        this.testPackages = testPackages;
        return this;
    }

    public boolean isParallelScan() {
        return parallelScan;
    }

    public GovernanceConfig setParallelScan(boolean parallelScan) {
        this.parallelScan = parallelScan;
        return this;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public GovernanceConfig setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        return this;
    }

    public ExecutionPhase getCurrentPhase() {
        return currentPhase;
    }

    public GovernanceConfig setCurrentPhase(ExecutionPhase currentPhase) {
        this.currentPhase = currentPhase;
        return this;
    }

    public int getValidationTimeout() {
        return validationTimeout;
    }

    public GovernanceConfig setValidationTimeout(int validationTimeout) {
        this.validationTimeout = validationTimeout;
        return this;
    }

    public boolean isFailOnCritical() {
        return failOnCritical;
    }

    public GovernanceConfig setFailOnCritical(boolean failOnCritical) {
        this.failOnCritical = failOnCritical;
        return this;
    }

    public boolean isFailOnHigh() {
        return failOnHigh;
    }

    public GovernanceConfig setFailOnHigh(boolean failOnHigh) {
        this.failOnHigh = failOnHigh;
        return this;
    }

    public boolean isFailOnMedium() {
        return failOnMedium;
    }

    public GovernanceConfig setFailOnMedium(boolean failOnMedium) {
        this.failOnMedium = failOnMedium;
        return this;
    }

    public boolean isFailOnLow() {
        return failOnLow;
    }

    public GovernanceConfig setFailOnLow(boolean failOnLow) {
        this.failOnLow = failOnLow;
        return this;
    }

    public boolean shouldFailOn(Severity severity) {
        return switch (severity) {
            case CRITICAL -> failOnCritical;
            case HIGH -> failOnHigh;
            case MEDIUM -> failOnMedium;
            case LOW -> failOnLow;
        };
    }

    public boolean isFailOnOrphanedTests() {
        return failOnOrphanedTests;
    }

    public GovernanceConfig setFailOnOrphanedTests(boolean failOnOrphanedTests) {
        this.failOnOrphanedTests = failOnOrphanedTests;
        return this;
    }

    public boolean isFailOnDeprecatedWithoutReplacement() {
        return failOnDeprecatedWithoutReplacement;
    }

    public GovernanceConfig setFailOnDeprecatedWithoutReplacement(boolean failOnDeprecatedWithoutReplacement) {
        this.failOnDeprecatedWithoutReplacement = failOnDeprecatedWithoutReplacement;
        return this;
    }

    public boolean isRequireOwner() {
        return requireOwner;
    }

    public GovernanceConfig setRequireOwner(boolean requireOwner) {
        this.requireOwner = requireOwner;
        return this;
    }

    public Set<String> getValidOwners() {
        return validOwners;
    }

    public GovernanceConfig setValidOwners(Set<String> validOwners) {
        this.validOwners = validOwners;
        return this;
    }

    public Pattern getRuleIdPattern() {
        return ruleIdPattern;
    }

    public GovernanceConfig setRuleIdPattern(Pattern ruleIdPattern) {
        this.ruleIdPattern = ruleIdPattern;
        return this;
    }

    public GovernanceConfig setRuleIdPattern(String pattern) {
        this.ruleIdPattern = Pattern.compile(pattern);
        return this;
    }

    public int getDescriptionMinLength() {
        return descriptionMinLength;
    }

    public GovernanceConfig setDescriptionMinLength(int descriptionMinLength) {
        this.descriptionMinLength = descriptionMinLength;
        return this;
    }

    public boolean isRequireComplianceTag() {
        return requireComplianceTag;
    }

    public GovernanceConfig setRequireComplianceTag(boolean requireComplianceTag) {
        this.requireComplianceTag = requireComplianceTag;
        return this;
    }

    public Set<ComplianceFramework> getAllowedFrameworks() {
        return allowedFrameworks;
    }

    public GovernanceConfig setAllowedFrameworks(Set<ComplianceFramework> allowedFrameworks) {
        this.allowedFrameworks = allowedFrameworks;
        return this;
    }

    public boolean isReportEnabled() {
        return reportEnabled;
    }

    public GovernanceConfig setReportEnabled(boolean reportEnabled) {
        this.reportEnabled = reportEnabled;
        return this;
    }

    public Path getReportOutputDir() {
        return reportOutputDir;
    }

    public GovernanceConfig setReportOutputDir(Path reportOutputDir) {
        this.reportOutputDir = reportOutputDir;
        return this;
    }

    public Set<ReportFormat> getReportFormats() {
        return reportFormats;
    }

    public GovernanceConfig setReportFormats(Set<ReportFormat> reportFormats) {
        this.reportFormats = reportFormats;
        return this;
    }

    public boolean isIncludePassing() {
        return includePassing;
    }

    public GovernanceConfig setIncludePassing(boolean includePassing) {
        this.includePassing = includePassing;
        return this;
    }

    public boolean isIncludeSourceLocations() {
        return includeSourceLocations;
    }

    public GovernanceConfig setIncludeSourceLocations(boolean includeSourceLocations) {
        this.includeSourceLocations = includeSourceLocations;
        return this;
    }

    public boolean isIncludeTestLocations() {
        return includeTestLocations;
    }

    public GovernanceConfig setIncludeTestLocations(boolean includeTestLocations) {
        this.includeTestLocations = includeTestLocations;
        return this;
    }

    public String getHtmlTitle() {
        return htmlTitle;
    }

    public GovernanceConfig setHtmlTitle(String htmlTitle) {
        this.htmlTitle = htmlTitle;
        return this;
    }

    public String getHtmlLogo() {
        return htmlLogo;
    }

    public GovernanceConfig setHtmlLogo(String htmlLogo) {
        this.htmlLogo = htmlLogo;
        return this;
    }

    public String getHtmlCustomCss() {
        return htmlCustomCss;
    }

    public GovernanceConfig setHtmlCustomCss(String htmlCustomCss) {
        this.htmlCustomCss = htmlCustomCss;
        return this;
    }

    public Set<ComplianceFramework> getTrackedFrameworks() {
        return trackedFrameworks;
    }

    public GovernanceConfig setTrackedFrameworks(Set<ComplianceFramework> trackedFrameworks) {
        this.trackedFrameworks = trackedFrameworks;
        return this;
    }

    public boolean isPerFrameworkReports() {
        return perFrameworkReports;
    }

    public GovernanceConfig setPerFrameworkReports(boolean perFrameworkReports) {
        this.perFrameworkReports = perFrameworkReports;
        return this;
    }

    public Map<ComplianceFramework, Double> getFrameworkCoverage() {
        return frameworkCoverage;
    }

    public GovernanceConfig setFrameworkCoverage(Map<ComplianceFramework, Double> frameworkCoverage) {
        this.frameworkCoverage = frameworkCoverage;
        return this;
    }

    public boolean isCoverageEnabled() {
        return coverageEnabled;
    }

    public GovernanceConfig setCoverageEnabled(boolean coverageEnabled) {
        this.coverageEnabled = coverageEnabled;
        return this;
    }

    public Path getJacocoReportPath() {
        return jacocoReportPath;
    }

    public GovernanceConfig setJacocoReportPath(Path jacocoReportPath) {
        this.jacocoReportPath = jacocoReportPath;
        return this;
    }

    public boolean isEnforceMinimumCoverage() {
        return enforceMinimumCoverage;
    }

    public GovernanceConfig setEnforceMinimumCoverage(boolean enforceMinimumCoverage) {
        this.enforceMinimumCoverage = enforceMinimumCoverage;
        return this;
    }

    public double getDefaultMinimumCoverage() {
        return defaultMinimumCoverage;
    }

    public GovernanceConfig setDefaultMinimumCoverage(double defaultMinimumCoverage) {
        this.defaultMinimumCoverage = defaultMinimumCoverage;
        return this;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public GovernanceConfig setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public GovernanceConfig setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
        return this;
    }

    /**
     * Creates a default configuration.
     * @return default config
     */
    public static GovernanceConfig defaults() {
        return new GovernanceConfig();
    }

    /**
     * Creates a minimal configuration for quick testing.
     * @param basePackage the package to scan
     * @return minimal config
     */
    public static GovernanceConfig minimal(String basePackage) {
        return new GovernanceConfig()
                .addBasePackage(basePackage)
                .setReportFormats(EnumSet.of(ReportFormat.JSON));
    }
}
