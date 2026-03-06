package com.clarita.governance.core.model;

import com.clarita.governance.annotations.TestCategory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the result of matching a business rule to its tests.
 * Contains information about coverage status and any gaps.
 *
 * @since 1.0.0
 */
public final class MatchResult {

    /**
     * Status of the match between a rule and its tests.
     */
    public enum MatchStatus {
        /** Rule has tests covering all required categories */
        COVERED("Covered", true),
        /** Rule has some tests but missing required categories */
        PARTIALLY_COVERED("Partially Covered", false),
        /** Rule has no tests at all */
        UNCOVERED("Uncovered", false),
        /** Rule is deprecated and excluded from validation */
        DEPRECATED("Deprecated", true),
        /** Rule is ignored via @GovernanceIgnore */
        IGNORED("Ignored", true);

        private final String displayName;
        private final boolean passing;

        MatchStatus(String displayName, boolean passing) {
            this.displayName = displayName;
            this.passing = passing;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isPassing() {
            return passing;
        }
    }

    private final BusinessRuleInfo rule;
    private final List<TestRuleInfo> tests;
    private final MatchStatus status;
    private final Set<TestCategory> coveredCategories;
    private final Set<TestCategory> missingCategories;
    private final double codeCoverage;
    private final boolean coverageMet;

    private MatchResult(Builder builder) {
        this.rule = Objects.requireNonNull(builder.rule, "rule must not be null");
        this.tests = builder.tests != null ? List.copyOf(builder.tests) : List.of();
        this.coveredCategories = builder.coveredCategories != null ?
                Set.copyOf(builder.coveredCategories) : Set.of();
        this.missingCategories = builder.missingCategories != null ?
                Set.copyOf(builder.missingCategories) : Set.of();
        this.codeCoverage = builder.codeCoverage;
        this.coverageMet = builder.coverageMet;
        this.status = determineStatus(builder);
    }

    private MatchStatus determineStatus(Builder builder) {
        if (builder.status != null) {
            return builder.status;
        }
        if (rule.isDeprecated()) {
            return MatchStatus.DEPRECATED;
        }
        if (tests.isEmpty()) {
            return MatchStatus.UNCOVERED;
        }
        if (!missingCategories.isEmpty()) {
            return MatchStatus.PARTIALLY_COVERED;
        }
        return MatchStatus.COVERED;
    }

    public BusinessRuleInfo getRule() {
        return rule;
    }

    public List<TestRuleInfo> getTests() {
        return tests;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public Set<TestCategory> getCoveredCategories() {
        return coveredCategories;
    }

    public Set<TestCategory> getMissingCategories() {
        return missingCategories;
    }

    public double getCodeCoverage() {
        return codeCoverage;
    }

    public boolean isCoverageMet() {
        return coverageMet;
    }

    /**
     * Checks if this match result is passing (no violations).
     * @return true if passing
     */
    public boolean isPassing() {
        return status.isPassing() && (codeCoverage < 0 || coverageMet);
    }

    /**
     * Gets the number of active (non-disabled) tests.
     * @return count of active tests
     */
    public int getActiveTestCount() {
        return (int) tests.stream().filter(TestRuleInfo::isActive).count();
    }

    /**
     * Gets tests grouped by category.
     * @return map of category to tests
     */
    public Map<TestCategory, List<TestRuleInfo>> getTestsByCategory() {
        return tests.stream()
                .collect(Collectors.groupingBy(TestRuleInfo::getCategory));
    }

    /**
     * Gets the coverage percentage as a formatted string.
     * @return coverage string (e.g., "85.5%")
     */
    public String getCoveragePercentage() {
        if (codeCoverage < 0) {
            return "N/A";
        }
        return String.format("%.1f%%", codeCoverage * 100);
    }

    @Override
    public String toString() {
        return "MatchResult{" +
                "ruleId='" + rule.getRuleId() + '\'' +
                ", status=" + status +
                ", testCount=" + tests.size() +
                ", coverage=" + getCoveragePercentage() +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a match result for an uncovered rule.
     * @param rule the business rule
     * @return match result with UNCOVERED status
     */
    public static MatchResult uncovered(BusinessRuleInfo rule) {
        return builder()
                .rule(rule)
                .tests(List.of())
                .missingCategories(rule.getRequiredCategories())
                .status(MatchStatus.UNCOVERED)
                .build();
    }

    /**
     * Creates a match result for an ignored rule.
     * @param rule the business rule
     * @return match result with IGNORED status
     */
    public static MatchResult ignored(BusinessRuleInfo rule) {
        return builder()
                .rule(rule)
                .status(MatchStatus.IGNORED)
                .build();
    }

    public static class Builder {
        private BusinessRuleInfo rule;
        private List<TestRuleInfo> tests;
        private MatchStatus status;
        private Set<TestCategory> coveredCategories;
        private Set<TestCategory> missingCategories;
        private double codeCoverage = -1; // -1 means not measured
        private boolean coverageMet = true;

        public Builder rule(BusinessRuleInfo rule) {
            this.rule = rule;
            return this;
        }

        public Builder tests(List<TestRuleInfo> tests) {
            this.tests = tests;
            return this;
        }

        public Builder status(MatchStatus status) {
            this.status = status;
            return this;
        }

        public Builder coveredCategories(Set<TestCategory> coveredCategories) {
            this.coveredCategories = coveredCategories;
            return this;
        }

        public Builder missingCategories(Set<TestCategory> missingCategories) {
            this.missingCategories = missingCategories;
            return this;
        }

        public Builder codeCoverage(double codeCoverage) {
            this.codeCoverage = codeCoverage;
            return this;
        }

        public Builder coverageMet(boolean coverageMet) {
            this.coverageMet = coverageMet;
            return this;
        }

        public MatchResult build() {
            return new MatchResult(this);
        }
    }
}
