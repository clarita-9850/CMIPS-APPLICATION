package com.clarita.governance.core.model;

import com.clarita.governance.annotations.ComplianceFramework;
import com.clarita.governance.annotations.Severity;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a governance violation - a business rule that fails validation.
 *
 * @since 1.0.0
 */
public final class Violation {

    /**
     * Type of violation detected.
     */
    public enum ViolationType {
        /** No tests exist for the rule */
        NO_TESTS("No Tests", "No test methods annotated with @TestsRule(ruleId=\"%s\") found"),

        /** Tests exist but missing required test categories */
        MISSING_CATEGORY("Missing Category", "Required test category %s not covered"),

        /** Code coverage below minimum threshold */
        LOW_COVERAGE("Low Coverage", "Code coverage %.1f%% is below minimum %.1f%%"),

        /** Test references non-existent rule (orphaned test) */
        ORPHANED_TEST("Orphaned Test", "Test references non-existent rule ID: %s"),

        /** Rule ID does not match required pattern */
        INVALID_RULE_ID("Invalid Rule ID", "Rule ID '%s' does not match pattern: %s"),

        /** Deprecated rule without replacement */
        DEPRECATED_NO_REPLACEMENT("Deprecated", "Deprecated rule has no replacement specified"),

        /** Exclusion has expired */
        EXPIRED_IGNORE("Expired Ignore", "@GovernanceIgnore expired on %s"),

        /** Owner not in valid owners list */
        INVALID_OWNER("Invalid Owner", "Owner '%s' not in allowed list"),

        /** Test stale (not reviewed within required period) */
        STALE_TEST("Stale Test", "Test last reviewed on %s, exceeds %d day review period");

        private final String displayName;
        private final String messageTemplate;

        ViolationType(String displayName, String messageTemplate) {
            this.displayName = displayName;
            this.messageTemplate = messageTemplate;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getMessageTemplate() {
            return messageTemplate;
        }
    }

    private final String ruleId;
    private final ViolationType violationType;
    private final Severity severity;
    private final String message;
    private final String owner;
    private final String sourceLocation;
    private final String remediation;
    private final Set<ComplianceFramework> complianceImpact;

    private Violation(Builder builder) {
        this.ruleId = Objects.requireNonNull(builder.ruleId, "ruleId must not be null");
        this.violationType = Objects.requireNonNull(builder.violationType, "violationType must not be null");
        this.severity = Objects.requireNonNull(builder.severity, "severity must not be null");
        this.message = Objects.requireNonNull(builder.message, "message must not be null");
        this.owner = builder.owner != null ? builder.owner : "";
        this.sourceLocation = builder.sourceLocation != null ? builder.sourceLocation : "";
        this.remediation = builder.remediation != null ? builder.remediation : generateRemediation(builder);
        this.complianceImpact = builder.complianceImpact != null ?
                Set.copyOf(builder.complianceImpact) : Set.of();
    }

    private String generateRemediation(Builder builder) {
        return switch (builder.violationType) {
            case NO_TESTS -> String.format(
                    "Add a test method annotated with @TestsRule(ruleId=\"%s\") in a test class.",
                    builder.ruleId);
            case MISSING_CATEGORY -> String.format(
                    "Add tests for the missing category. Use @TestsRule(ruleId=\"%s\", category=TestCategory.XXX).",
                    builder.ruleId);
            case LOW_COVERAGE ->
                    "Add more test cases to increase code coverage for this method/class.";
            case ORPHANED_TEST -> String.format(
                    "Either add @BusinessRule(ruleId=\"%s\") to the code or update the test's ruleId.",
                    builder.ruleId);
            case INVALID_RULE_ID ->
                    "Update the ruleId to match the required pattern.";
            case DEPRECATED_NO_REPLACEMENT ->
                    "Add deprecationReason to @BusinessRule explaining the replacement.";
            case EXPIRED_IGNORE ->
                    "Either remove @GovernanceIgnore and add tests, or update the 'until' date with approval.";
            case INVALID_OWNER ->
                    "Update the owner to one from the allowed list in governance configuration.";
            case STALE_TEST ->
                    "Review the test and update lastReviewed date in @TestsRule annotation.";
        };
    }

    public String getRuleId() {
        return ruleId;
    }

    public ViolationType getViolationType() {
        return violationType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public String getOwner() {
        return owner;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public String getRemediation() {
        return remediation;
    }

    public Set<ComplianceFramework> getComplianceImpact() {
        return complianceImpact;
    }

    /**
     * Checks if this violation has compliance impact.
     * @return true if any compliance framework is affected
     */
    public boolean hasComplianceImpact() {
        return !complianceImpact.isEmpty();
    }

    /**
     * Gets a short code for this violation (for CI output).
     * @return violation code (e.g., "NO_TESTS:BR-PVM-15")
     */
    public String getCode() {
        return violationType.name() + ":" + ruleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Violation violation = (Violation) o;
        return Objects.equals(ruleId, violation.ruleId) &&
               violationType == violation.violationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, violationType);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s (owner: %s)",
                severity, violationType.getDisplayName(), message, owner);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a NO_TESTS violation for a business rule.
     * @param rule the rule without tests
     * @return violation instance
     */
    public static Violation noTests(BusinessRuleInfo rule) {
        return builder()
                .ruleId(rule.getRuleId())
                .violationType(ViolationType.NO_TESTS)
                .severity(rule.getSeverity())
                .message(String.format("Business rule '%s' has no tests. Description: %s",
                        rule.getRuleId(), rule.getDescription()))
                .owner(rule.getOwner())
                .sourceLocation(rule.getSourceLocation())
                .complianceImpact(rule.getComplianceFrameworks())
                .build();
    }

    public static class Builder {
        private String ruleId;
        private ViolationType violationType;
        private Severity severity;
        private String message;
        private String owner;
        private String sourceLocation;
        private String remediation;
        private Set<ComplianceFramework> complianceImpact;

        public Builder ruleId(String ruleId) {
            this.ruleId = ruleId;
            return this;
        }

        public Builder violationType(ViolationType violationType) {
            this.violationType = violationType;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder sourceLocation(String sourceLocation) {
            this.sourceLocation = sourceLocation;
            return this;
        }

        public Builder remediation(String remediation) {
            this.remediation = remediation;
            return this;
        }

        public Builder complianceImpact(Set<ComplianceFramework> complianceImpact) {
            this.complianceImpact = complianceImpact;
            return this;
        }

        public Violation build() {
            return new Violation(this);
        }
    }
}
