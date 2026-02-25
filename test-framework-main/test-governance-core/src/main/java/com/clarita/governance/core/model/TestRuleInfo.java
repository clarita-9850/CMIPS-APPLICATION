package com.clarita.governance.core.model;

import com.clarita.governance.annotations.TestCategory;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * Represents extracted information from a {@link com.clarita.governance.annotations.TestsRule} annotation.
 * This is an immutable data class used internally by the governance engine.
 *
 * @since 1.0.0
 */
public final class TestRuleInfo {

    private final String ruleId;
    private final Set<String> scenarios;
    private final String author;
    private final LocalDate lastReviewed;
    private final TestCategory category;
    private final String notes;
    private final int priority;
    private final boolean disabled;
    private final String disabledReason;

    // Test source location
    private final String testClass;
    private final String testMethod;
    private final int testLineNumber;

    private TestRuleInfo(Builder builder) {
        this.ruleId = Objects.requireNonNull(builder.ruleId, "ruleId must not be null");
        this.scenarios = builder.scenarios != null ? Set.copyOf(builder.scenarios) : Set.of();
        this.author = builder.author != null ? builder.author : "";
        this.lastReviewed = builder.lastReviewed;
        this.category = builder.category != null ? builder.category : TestCategory.UNIT;
        this.notes = builder.notes != null ? builder.notes : "";
        this.priority = builder.priority;
        this.disabled = builder.disabled;
        this.disabledReason = builder.disabledReason != null ? builder.disabledReason : "";
        this.testClass = Objects.requireNonNull(builder.testClass, "testClass must not be null");
        this.testMethod = Objects.requireNonNull(builder.testMethod, "testMethod must not be null");
        this.testLineNumber = builder.testLineNumber;
    }

    public String getRuleId() {
        return ruleId;
    }

    public Set<String> getScenarios() {
        return scenarios;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDate getLastReviewed() {
        return lastReviewed;
    }

    public TestCategory getCategory() {
        return category;
    }

    public String getNotes() {
        return notes;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public String getTestClass() {
        return testClass;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public int getTestLineNumber() {
        return testLineNumber;
    }

    /**
     * Returns a human-readable test location string.
     * @return test location (e.g., "com.example.ServiceTest.testMethod:42")
     */
    public String getTestLocation() {
        return testClass + "." + testMethod + ":" + testLineNumber;
    }

    /**
     * Checks if this test is active (not disabled).
     * @return true if active
     */
    public boolean isActive() {
        return !disabled;
    }

    /**
     * Checks if the test was reviewed within the given number of days.
     * @param days number of days
     * @return true if reviewed within the period, or if no review date is set
     */
    public boolean isReviewedWithin(int days) {
        if (lastReviewed == null) {
            return true; // No review tracking = assume OK
        }
        return lastReviewed.plusDays(days).isAfter(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestRuleInfo that = (TestRuleInfo) o;
        return Objects.equals(testClass, that.testClass) &&
               Objects.equals(testMethod, that.testMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testClass, testMethod);
    }

    @Override
    public String toString() {
        return "TestRuleInfo{" +
                "ruleId='" + ruleId + '\'' +
                ", category=" + category +
                ", testLocation='" + getTestLocation() + '\'' +
                ", active=" + isActive() +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String ruleId;
        private Set<String> scenarios;
        private String author;
        private LocalDate lastReviewed;
        private TestCategory category;
        private String notes;
        private int priority = 100;
        private boolean disabled;
        private String disabledReason;
        private String testClass;
        private String testMethod;
        private int testLineNumber;

        public Builder ruleId(String ruleId) {
            this.ruleId = ruleId;
            return this;
        }

        public Builder scenarios(Set<String> scenarios) {
            this.scenarios = scenarios;
            return this;
        }

        public Builder scenarios(String... scenarios) {
            this.scenarios = Set.of(scenarios);
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder lastReviewed(LocalDate lastReviewed) {
            this.lastReviewed = lastReviewed;
            return this;
        }

        public Builder lastReviewed(String lastReviewed) {
            if (lastReviewed != null && !lastReviewed.isEmpty()) {
                this.lastReviewed = LocalDate.parse(lastReviewed);
            }
            return this;
        }

        public Builder category(TestCategory category) {
            this.category = category;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder disabled(boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        public Builder disabledReason(String disabledReason) {
            this.disabledReason = disabledReason;
            return this;
        }

        public Builder testClass(String testClass) {
            this.testClass = testClass;
            return this;
        }

        public Builder testMethod(String testMethod) {
            this.testMethod = testMethod;
            return this;
        }

        public Builder testLineNumber(int testLineNumber) {
            this.testLineNumber = testLineNumber;
            return this;
        }

        public TestRuleInfo build() {
            return new TestRuleInfo(this);
        }
    }
}
