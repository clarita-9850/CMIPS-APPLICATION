package com.clarita.governance.annotations;

/**
 * Defines the severity level for business rules.
 * Severity determines when build failures occur and how violations are prioritized.
 *
 * <p>Compliance mapping:</p>
 * <ul>
 *   <li>CRITICAL - Maps to NIST 800-53 HIGH impact controls</li>
 *   <li>HIGH - Maps to NIST 800-53 MODERATE impact controls</li>
 *   <li>MEDIUM - Maps to NIST 800-53 LOW impact controls</li>
 *   <li>LOW - Informational, no compliance impact</li>
 * </ul>
 *
 * @since 1.0.0
 */
public enum Severity {

    /**
     * Build fails immediately on any commit.
     * <p>Use for: Financial calculations, security controls, compliance-critical logic.</p>
     * <p>Examples: Payment processing, HIPAA data handling, authentication logic.</p>
     */
    CRITICAL(1, true, true, "Build fails immediately on any commit"),

    /**
     * Build fails on PR merge.
     * <p>Use for: Core business logic that must be tested before release.</p>
     * <p>Examples: Eligibility calculations, state transitions, workflow logic.</p>
     */
    HIGH(2, true, false, "Build fails on PR merge"),

    /**
     * Warning shown, build continues.
     * <p>Use for: Standard features that should have tests but won't block release.</p>
     * <p>Examples: UI logic, non-critical validations, logging.</p>
     */
    MEDIUM(3, false, false, "Warning shown, build continues"),

    /**
     * Checked only in nightly builds.
     * <p>Use for: Edge cases, nice-to-haves, exploratory features.</p>
     * <p>Examples: Error message formatting, fallback behaviors.</p>
     */
    LOW(4, false, false, "Checked only in nightly builds");

    private final int priority;
    private final boolean failOnPR;
    private final boolean failOnCommit;
    private final String description;

    Severity(int priority, boolean failOnPR, boolean failOnCommit, String description) {
        this.priority = priority;
        this.failOnPR = failOnPR;
        this.failOnCommit = failOnCommit;
        this.description = description;
    }

    /**
     * Returns the priority order (1 = highest priority).
     * @return priority value
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Whether violations at this severity should fail PR builds.
     * @return true if PR builds should fail on violations
     */
    public boolean isFailOnPR() {
        return failOnPR;
    }

    /**
     * Whether violations at this severity should fail commit builds.
     * @return true if commit builds should fail on violations
     */
    public boolean isFailOnCommit() {
        return failOnCommit;
    }

    /**
     * Human-readable description of this severity level.
     * @return description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this severity is higher than or equal to the given severity.
     * @param other the severity to compare against
     * @return true if this severity is higher or equal
     */
    public boolean isAtLeast(Severity other) {
        return this.priority <= other.priority;
    }

    /**
     * Checks if this severity should cause a build failure for the given phase.
     * @param phase the execution phase
     * @return true if the build should fail
     */
    public boolean shouldFailBuild(ExecutionPhase phase) {
        return switch (phase) {
            case COMMIT -> failOnCommit;
            case PR -> failOnPR || failOnCommit;
            case NIGHTLY, RELEASE, ON_DEMAND -> true;
        };
    }
}
