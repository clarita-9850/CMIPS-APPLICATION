package com.clarita.governance.core.model;

import com.clarita.governance.annotations.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents extracted information from a {@link BusinessRule} annotation.
 * This is an immutable data class used internally by the governance engine.
 *
 * @since 1.0.0
 */
public final class BusinessRuleInfo {

    private final String ruleId;
    private final String description;
    private final String owner;
    private final Severity severity;
    private final Set<TestCategory> requiredCategories;
    private final Set<ExecutionPhase> phases;
    private final double minCoverage;
    private final Set<ComplianceFramework> complianceFrameworks;
    private final String requirementRef;
    private final String since;
    private final boolean deprecated;
    private final String deprecationReason;
    private final Set<String> tags;

    // Source location
    private final String sourceClass;
    private final String sourceMethod;
    private final int sourceLineNumber;

    private BusinessRuleInfo(Builder builder) {
        this.ruleId = Objects.requireNonNull(builder.ruleId, "ruleId must not be null");
        this.description = Objects.requireNonNull(builder.description, "description must not be null");
        this.owner = Objects.requireNonNull(builder.owner, "owner must not be null");
        this.severity = builder.severity != null ? builder.severity : Severity.MEDIUM;
        this.requiredCategories = builder.requiredCategories != null ?
                Set.copyOf(builder.requiredCategories) : Set.of(TestCategory.UNIT);
        this.phases = builder.phases != null ?
                Set.copyOf(builder.phases) : Set.of(ExecutionPhase.PR);
        this.minCoverage = builder.minCoverage;
        this.complianceFrameworks = builder.complianceFrameworks != null ?
                Set.copyOf(builder.complianceFrameworks) : Set.of();
        this.requirementRef = builder.requirementRef != null ? builder.requirementRef : "";
        this.since = builder.since != null ? builder.since : "";
        this.deprecated = builder.deprecated;
        this.deprecationReason = builder.deprecationReason != null ? builder.deprecationReason : "";
        this.tags = builder.tags != null ? Set.copyOf(builder.tags) : Set.of();
        this.sourceClass = Objects.requireNonNull(builder.sourceClass, "sourceClass must not be null");
        this.sourceMethod = builder.sourceMethod;
        this.sourceLineNumber = builder.sourceLineNumber;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Set<TestCategory> getRequiredCategories() {
        return requiredCategories;
    }

    public Set<ExecutionPhase> getPhases() {
        return phases;
    }

    public double getMinCoverage() {
        return minCoverage;
    }

    public Set<ComplianceFramework> getComplianceFrameworks() {
        return complianceFrameworks;
    }

    public String getRequirementRef() {
        return requirementRef;
    }

    public String getSince() {
        return since;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public String getDeprecationReason() {
        return deprecationReason;
    }

    public Set<String> getTags() {
        return tags;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public String getSourceMethod() {
        return sourceMethod;
    }

    public int getSourceLineNumber() {
        return sourceLineNumber;
    }

    /**
     * Returns a human-readable source location string.
     * @return source location (e.g., "com.example.Service.method:42")
     */
    public String getSourceLocation() {
        if (sourceMethod != null) {
            return sourceClass + "." + sourceMethod + ":" + sourceLineNumber;
        }
        return sourceClass + ":" + sourceLineNumber;
    }

    /**
     * Checks if this rule should be validated in the given phase.
     * @param phase the execution phase
     * @return true if rule applies to this phase
     */
    public boolean appliesTo(ExecutionPhase phase) {
        return phases.stream().anyMatch(p -> phase.includes(p));
    }

    /**
     * Checks if this rule should fail the build in the given phase.
     * @param phase the execution phase
     * @return true if violations should fail the build
     */
    public boolean shouldFailBuild(ExecutionPhase phase) {
        return severity.shouldFailBuild(phase);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusinessRuleInfo that = (BusinessRuleInfo) o;
        return Objects.equals(ruleId, that.ruleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId);
    }

    @Override
    public String toString() {
        return "BusinessRuleInfo{" +
                "ruleId='" + ruleId + '\'' +
                ", severity=" + severity +
                ", owner='" + owner + '\'' +
                ", sourceLocation='" + getSourceLocation() + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String ruleId;
        private String description;
        private String owner;
        private Severity severity;
        private Set<TestCategory> requiredCategories;
        private Set<ExecutionPhase> phases;
        private double minCoverage = 0.80;
        private Set<ComplianceFramework> complianceFrameworks;
        private String requirementRef;
        private String since;
        private boolean deprecated;
        private String deprecationReason;
        private Set<String> tags;
        private String sourceClass;
        private String sourceMethod;
        private int sourceLineNumber;

        public Builder ruleId(String ruleId) {
            this.ruleId = ruleId;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder requiredCategories(Set<TestCategory> categories) {
            this.requiredCategories = categories;
            return this;
        }

        public Builder requiredCategories(TestCategory... categories) {
            this.requiredCategories = Set.of(categories);
            return this;
        }

        public Builder phases(Set<ExecutionPhase> phases) {
            this.phases = phases;
            return this;
        }

        public Builder phases(ExecutionPhase... phases) {
            this.phases = Set.of(phases);
            return this;
        }

        public Builder minCoverage(double minCoverage) {
            this.minCoverage = minCoverage;
            return this;
        }

        public Builder complianceFrameworks(Set<ComplianceFramework> frameworks) {
            this.complianceFrameworks = frameworks;
            return this;
        }

        public Builder complianceFrameworks(ComplianceFramework... frameworks) {
            this.complianceFrameworks = Set.of(frameworks);
            return this;
        }

        public Builder requirementRef(String requirementRef) {
            this.requirementRef = requirementRef;
            return this;
        }

        public Builder since(String since) {
            this.since = since;
            return this;
        }

        public Builder deprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder deprecationReason(String deprecationReason) {
            this.deprecationReason = deprecationReason;
            return this;
        }

        public Builder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder sourceClass(String sourceClass) {
            this.sourceClass = sourceClass;
            return this;
        }

        public Builder sourceMethod(String sourceMethod) {
            this.sourceMethod = sourceMethod;
            return this;
        }

        public Builder sourceLineNumber(int sourceLineNumber) {
            this.sourceLineNumber = sourceLineNumber;
            return this;
        }

        public BusinessRuleInfo build() {
            return new BusinessRuleInfo(this);
        }
    }
}
