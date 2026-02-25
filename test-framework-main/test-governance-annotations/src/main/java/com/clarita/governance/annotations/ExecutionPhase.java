package com.clarita.governance.annotations;

import java.time.Duration;

/**
 * Defines when governance validation should run in the CI/CD pipeline.
 * Allows different levels of validation based on the build context.
 *
 * <p>Design principle: Fast feedback for developers, comprehensive validation before release.</p>
 *
 * @since 1.0.0
 */
public enum ExecutionPhase {

    /**
     * Every commit - fast, critical only.
     * <p>Target duration: Under 2 minutes total test time.</p>
     * <p>Validates: CRITICAL severity rules only.</p>
     * <p>Use case: Pre-push hooks, immediate developer feedback.</p>
     */
    COMMIT(1, Duration.ofMinutes(2), "Every commit validation"),

    /**
     * Pull request validation.
     * <p>Target duration: Under 10 minutes total test time.</p>
     * <p>Validates: CRITICAL and HIGH severity rules.</p>
     * <p>Use case: PR checks, merge gates.</p>
     */
    PR(2, Duration.ofMinutes(10), "Pull request validation"),

    /**
     * Scheduled nightly runs.
     * <p>Target duration: Under 2 hours total test time.</p>
     * <p>Validates: All severity levels including MEDIUM and LOW.</p>
     * <p>Use case: Complete coverage verification, compliance reporting.</p>
     */
    NIGHTLY(3, Duration.ofHours(2), "Nightly build validation"),

    /**
     * Release candidate builds.
     * <p>Target duration: Under 4 hours total test time.</p>
     * <p>Validates: All rules, generates full audit reports.</p>
     * <p>Use case: Release gates, audit evidence generation.</p>
     */
    RELEASE(4, Duration.ofHours(4), "Release candidate validation"),

    /**
     * Manual trigger only.
     * <p>Target duration: Up to 8 hours.</p>
     * <p>Validates: All rules including performance and E2E tests.</p>
     * <p>Use case: Full regression, pre-audit verification.</p>
     */
    ON_DEMAND(5, Duration.ofHours(8), "On-demand full validation");

    private final int order;
    private final Duration expectedDuration;
    private final String description;

    ExecutionPhase(int order, Duration expectedDuration, String description) {
        this.order = order;
        this.expectedDuration = expectedDuration;
        this.description = description;
    }

    /**
     * Returns the order value (lower = more frequent/faster).
     * @return order value
     */
    public int getOrder() {
        return order;
    }

    /**
     * Returns the expected maximum duration for this phase.
     * @return expected duration
     */
    public Duration getExpectedDuration() {
        return expectedDuration;
    }

    /**
     * Returns a human-readable description.
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this phase includes rules from an earlier phase.
     * <p>A NIGHTLY build includes all rules from COMMIT and PR.</p>
     *
     * @param other the phase to check
     * @return true if this phase includes the other phase
     */
    public boolean includes(ExecutionPhase other) {
        return this.order >= other.order;
    }

    /**
     * Gets the recommended phases for a given severity level.
     * @param severity the severity level
     * @return array of recommended phases
     */
    public static ExecutionPhase[] getRecommendedForSeverity(Severity severity) {
        return switch (severity) {
            case CRITICAL -> new ExecutionPhase[]{COMMIT, PR, NIGHTLY, RELEASE};
            case HIGH -> new ExecutionPhase[]{PR, NIGHTLY, RELEASE};
            case MEDIUM -> new ExecutionPhase[]{NIGHTLY, RELEASE};
            case LOW -> new ExecutionPhase[]{NIGHTLY};
        };
    }

    /**
     * Gets the minimum severity that should be validated in this phase.
     * @return minimum severity level
     */
    public Severity getMinimumSeverity() {
        return switch (this) {
            case COMMIT -> Severity.CRITICAL;
            case PR -> Severity.HIGH;
            case NIGHTLY, RELEASE, ON_DEMAND -> Severity.LOW;
        };
    }
}
