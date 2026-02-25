package com.clarita.governance.core.model;

import com.clarita.governance.annotations.ComplianceFramework;
import com.clarita.governance.annotations.ExecutionPhase;
import com.clarita.governance.annotations.Severity;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contains the complete result of governance validation.
 * This is the main output of the validation pipeline.
 *
 * @since 1.0.0
 */
public final class ValidationResult {

    /**
     * Overall status of the validation.
     */
    public enum OverallStatus {
        /** All rules pass validation */
        PASSED("Passed", true),
        /** Has warnings but no failures */
        WARNING("Warning", true),
        /** Has violations that should fail the build */
        FAILED("Failed", false);

        private final String displayName;
        private final boolean shouldContinue;

        OverallStatus(String displayName, boolean shouldContinue) {
            this.displayName = displayName;
            this.shouldContinue = shouldContinue;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean shouldContinue() {
            return shouldContinue;
        }
    }

    private final ExecutionPhase phase;
    private final Instant timestamp;
    private final Duration duration;
    private final int totalRules;
    private final int coveredRules;
    private final double coveragePercentage;
    private final List<MatchResult> matchResults;
    private final List<Violation> violations;
    private final OverallStatus overallStatus;
    private final Set<String> scannedPackages;
    private final String applicationName;
    private final String applicationVersion;
    private final String frameworkVersion;

    private ValidationResult(Builder builder) {
        this.phase = Objects.requireNonNull(builder.phase, "phase must not be null");
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.duration = builder.duration != null ? builder.duration : Duration.ZERO;
        this.matchResults = builder.matchResults != null ? List.copyOf(builder.matchResults) : List.of();
        this.violations = builder.violations != null ? List.copyOf(builder.violations) : List.of();
        this.scannedPackages = builder.scannedPackages != null ?
                Set.copyOf(builder.scannedPackages) : Set.of();
        this.applicationName = builder.applicationName != null ? builder.applicationName : "";
        this.applicationVersion = builder.applicationVersion != null ? builder.applicationVersion : "";
        this.frameworkVersion = builder.frameworkVersion != null ? builder.frameworkVersion : "1.0.0";

        // Calculate derived values
        this.totalRules = matchResults.size();
        this.coveredRules = (int) matchResults.stream()
                .filter(MatchResult::isPassing)
                .count();
        this.coveragePercentage = totalRules > 0 ?
                (double) coveredRules / totalRules * 100 : 100.0;
        this.overallStatus = determineStatus(builder);
    }

    private OverallStatus determineStatus(Builder builder) {
        if (builder.overallStatus != null) {
            return builder.overallStatus;
        }
        if (violations.isEmpty()) {
            return OverallStatus.PASSED;
        }
        boolean hasFailure = violations.stream()
                .anyMatch(v -> v.getSeverity().shouldFailBuild(phase));
        return hasFailure ? OverallStatus.FAILED : OverallStatus.WARNING;
    }

    // Getters
    public ExecutionPhase getPhase() { return phase; }
    public Instant getTimestamp() { return timestamp; }
    public Duration getDuration() { return duration; }
    public int getTotalRules() { return totalRules; }
    public int getCoveredRules() { return coveredRules; }
    public double getCoveragePercentage() { return coveragePercentage; }
    public List<MatchResult> getMatchResults() { return matchResults; }
    public List<Violation> getViolations() { return violations; }
    public OverallStatus getOverallStatus() { return overallStatus; }
    public Set<String> getScannedPackages() { return scannedPackages; }
    public String getApplicationName() { return applicationName; }
    public String getApplicationVersion() { return applicationVersion; }
    public String getFrameworkVersion() { return frameworkVersion; }

    /**
     * Gets violation count by severity.
     * @return map of severity to count
     */
    public Map<Severity, Long> getViolationsBySeverity() {
        return violations.stream()
                .collect(Collectors.groupingBy(Violation::getSeverity, Collectors.counting()));
    }

    /**
     * Gets violations grouped by owner.
     * @return map of owner to violations
     */
    public Map<String, List<Violation>> getViolationsByOwner() {
        return violations.stream()
                .collect(Collectors.groupingBy(Violation::getOwner));
    }

    /**
     * Gets coverage summary by compliance framework.
     * @return map of framework to coverage info
     */
    public Map<ComplianceFramework, ComplianceSummary> getComplianceSummary() {
        Map<ComplianceFramework, List<MatchResult>> byFramework = new HashMap<>();

        for (MatchResult result : matchResults) {
            for (ComplianceFramework framework : result.getRule().getComplianceFrameworks()) {
                byFramework.computeIfAbsent(framework, k -> new ArrayList<>()).add(result);
            }
        }

        Map<ComplianceFramework, ComplianceSummary> summary = new EnumMap<>(ComplianceFramework.class);
        for (Map.Entry<ComplianceFramework, List<MatchResult>> entry : byFramework.entrySet()) {
            List<MatchResult> results = entry.getValue();
            int total = results.size();
            int covered = (int) results.stream().filter(MatchResult::isPassing).count();
            double percentage = total > 0 ? (double) covered / total * 100 : 100.0;
            summary.put(entry.getKey(), new ComplianceSummary(entry.getKey(), total, covered, percentage));
        }

        return summary;
    }

    /**
     * Gets count of critical violations.
     * @return count
     */
    public int getCriticalViolationCount() {
        return (int) violations.stream()
                .filter(v -> v.getSeverity() == Severity.CRITICAL)
                .count();
    }

    /**
     * Gets count of high violations.
     * @return count
     */
    public int getHighViolationCount() {
        return (int) violations.stream()
                .filter(v -> v.getSeverity() == Severity.HIGH)
                .count();
    }

    /**
     * Checks if the build should fail based on violations.
     * @return true if build should fail
     */
    public boolean shouldFailBuild() {
        return overallStatus == OverallStatus.FAILED;
    }

    /**
     * Gets a summary string for console output.
     * @return summary string
     */
    public String getSummary() {
        return String.format(
                "Governance Validation: %s | Rules: %d/%d (%.1f%%) | Violations: %d (Critical: %d, High: %d)",
                overallStatus.getDisplayName(),
                coveredRules, totalRules, coveragePercentage,
                violations.size(),
                getCriticalViolationCount(),
                getHighViolationCount()
        );
    }

    @Override
    public String toString() {
        return getSummary();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Summary information for a compliance framework.
     */
    public record ComplianceSummary(
            ComplianceFramework framework,
            int totalRules,
            int coveredRules,
            double coveragePercentage
    ) {
        public boolean isPassing() {
            return coveredRules == totalRules;
        }

        public String getStatus() {
            return isPassing() ? "COMPLIANT" : "NON-COMPLIANT";
        }
    }

    public static class Builder {
        private ExecutionPhase phase;
        private Instant timestamp;
        private Duration duration;
        private List<MatchResult> matchResults;
        private List<Violation> violations;
        private OverallStatus overallStatus;
        private Set<String> scannedPackages;
        private String applicationName;
        private String applicationVersion;
        private String frameworkVersion;

        public Builder phase(ExecutionPhase phase) {
            this.phase = phase;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public Builder matchResults(List<MatchResult> matchResults) {
            this.matchResults = matchResults;
            return this;
        }

        public Builder violations(List<Violation> violations) {
            this.violations = violations;
            return this;
        }

        public Builder overallStatus(OverallStatus overallStatus) {
            this.overallStatus = overallStatus;
            return this;
        }

        public Builder scannedPackages(Set<String> scannedPackages) {
            this.scannedPackages = scannedPackages;
            return this;
        }

        public Builder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder applicationVersion(String applicationVersion) {
            this.applicationVersion = applicationVersion;
            return this;
        }

        public Builder frameworkVersion(String frameworkVersion) {
            this.frameworkVersion = frameworkVersion;
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(this);
        }
    }
}
