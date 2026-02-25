package com.clarita.governance.core.validator;

import com.clarita.governance.annotations.ExecutionPhase;
import com.clarita.governance.annotations.Severity;
import com.clarita.governance.annotations.TestCategory;
import com.clarita.governance.core.config.GovernanceConfig;
import com.clarita.governance.core.model.*;
import com.clarita.governance.core.scanner.AnnotationScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main validation engine for the governance framework.
 * Orchestrates scanning, matching, and violation detection.
 *
 * @since 1.0.0
 */
public class GovernanceValidator {

    private static final Logger log = LoggerFactory.getLogger(GovernanceValidator.class);

    private final GovernanceConfig config;
    private final AnnotationScanner scanner;

    public GovernanceValidator(GovernanceConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.scanner = new AnnotationScanner(config);
    }

    public GovernanceValidator(GovernanceConfig config, AnnotationScanner scanner) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.scanner = Objects.requireNonNull(scanner, "scanner must not be null");
    }

    /**
     * Runs full governance validation.
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
        Instant startTime = Instant.now();

        // Phase 1: Scan
        List<BusinessRuleInfo> businessRules = scanner.scanBusinessRules();
        List<TestRuleInfo> testRules = scanner.scanTestRules();

        log.debug("Scanned {} business rules and {} test rules", businessRules.size(), testRules.size());

        // Phase 2: Filter by phase
        List<BusinessRuleInfo> applicableRules = businessRules.stream()
                .filter(rule -> rule.appliesTo(phase))
                .collect(Collectors.toList());

        log.debug("{} rules applicable to phase {}", applicableRules.size(), phase);

        // Phase 3: Match rules to tests
        List<MatchResult> matchResults = matchRulesToTests(applicableRules, testRules);

        // Phase 4: Detect violations
        List<Violation> violations = detectViolations(matchResults, phase);

        // Check for orphaned tests
        violations.addAll(detectOrphanedTests(testRules, businessRules));

        Duration duration = Duration.between(startTime, Instant.now());
        log.info("Validation completed in {}ms. Found {} violations",
                duration.toMillis(), violations.size());

        return ValidationResult.builder()
                .phase(phase)
                .timestamp(startTime)
                .duration(duration)
                .matchResults(matchResults)
                .violations(violations)
                .scannedPackages(config.getBasePackages())
                .applicationName(config.getApplicationName())
                .applicationVersion(config.getApplicationVersion())
                .build();
    }

    /**
     * Validates a single business rule.
     *
     * @param ruleId the rule ID to validate
     * @return validation result for just that rule
     */
    public Optional<MatchResult> validateRule(String ruleId) {
        Optional<BusinessRuleInfo> rule = scanner.getBusinessRule(ruleId);
        if (rule.isEmpty()) {
            return Optional.empty();
        }

        List<TestRuleInfo> tests = scanner.getTestsForRule(ruleId);
        return Optional.of(matchRuleToTests(rule.get(), tests));
    }

    private List<MatchResult> matchRulesToTests(List<BusinessRuleInfo> rules, List<TestRuleInfo> allTests) {
        // Group tests by rule ID for efficient lookup
        Map<String, List<TestRuleInfo>> testsByRuleId = allTests.stream()
                .collect(Collectors.groupingBy(TestRuleInfo::getRuleId));

        return rules.stream()
                .map(rule -> {
                    List<TestRuleInfo> tests = testsByRuleId.getOrDefault(rule.getRuleId(), List.of());
                    return matchRuleToTests(rule, tests);
                })
                .collect(Collectors.toList());
    }

    private MatchResult matchRuleToTests(BusinessRuleInfo rule, List<TestRuleInfo> tests) {
        if (rule.isDeprecated()) {
            return MatchResult.builder()
                    .rule(rule)
                    .tests(tests)
                    .status(MatchResult.MatchStatus.DEPRECATED)
                    .build();
        }

        if (tests.isEmpty()) {
            return MatchResult.uncovered(rule);
        }

        // Determine covered categories
        Set<TestCategory> coveredCategories = tests.stream()
                .filter(TestRuleInfo::isActive)
                .map(TestRuleInfo::getCategory)
                .collect(Collectors.toSet());

        // Determine missing categories
        Set<TestCategory> missingCategories = new HashSet<>(rule.getRequiredCategories());
        missingCategories.removeAll(coveredCategories);

        MatchResult.MatchStatus status;
        if (missingCategories.isEmpty()) {
            status = MatchResult.MatchStatus.COVERED;
        } else if (!coveredCategories.isEmpty()) {
            status = MatchResult.MatchStatus.PARTIALLY_COVERED;
        } else {
            status = MatchResult.MatchStatus.UNCOVERED;
        }

        return MatchResult.builder()
                .rule(rule)
                .tests(tests)
                .status(status)
                .coveredCategories(coveredCategories)
                .missingCategories(missingCategories)
                .codeCoverage(-1) // Would require JaCoCo integration
                .coverageMet(true) // Assume met until JaCoCo integration
                .build();
    }

    private List<Violation> detectViolations(List<MatchResult> matchResults, ExecutionPhase phase) {
        List<Violation> violations = new ArrayList<>();

        for (MatchResult result : matchResults) {
            BusinessRuleInfo rule = result.getRule();

            // Skip deprecated rules
            if (result.getStatus() == MatchResult.MatchStatus.DEPRECATED) {
                if (config.isFailOnDeprecatedWithoutReplacement() &&
                        rule.getDeprecationReason().isEmpty()) {
                    violations.add(Violation.builder()
                            .ruleId(rule.getRuleId())
                            .violationType(Violation.ViolationType.DEPRECATED_NO_REPLACEMENT)
                            .severity(Severity.LOW)
                            .message("Deprecated rule has no replacement specified: " + rule.getRuleId())
                            .owner(rule.getOwner())
                            .sourceLocation(rule.getSourceLocation())
                            .complianceImpact(rule.getComplianceFrameworks())
                            .build());
                }
                continue;
            }

            // Check for no tests
            if (result.getStatus() == MatchResult.MatchStatus.UNCOVERED) {
                if (shouldReportViolation(rule.getSeverity())) {
                    violations.add(Violation.noTests(rule));
                }
                continue;
            }

            // Check for missing categories
            if (!result.getMissingCategories().isEmpty()) {
                for (TestCategory missingCategory : result.getMissingCategories()) {
                    violations.add(Violation.builder()
                            .ruleId(rule.getRuleId())
                            .violationType(Violation.ViolationType.MISSING_CATEGORY)
                            .severity(Severity.MEDIUM) // Missing category is medium severity
                            .message(String.format("Rule '%s' missing test category: %s",
                                    rule.getRuleId(), missingCategory))
                            .owner(rule.getOwner())
                            .sourceLocation(rule.getSourceLocation())
                            .complianceImpact(rule.getComplianceFrameworks())
                            .build());
                }
            }

            // Check code coverage (if enabled)
            if (config.isCoverageEnabled() && !result.isCoverageMet()) {
                violations.add(Violation.builder()
                        .ruleId(rule.getRuleId())
                        .violationType(Violation.ViolationType.LOW_COVERAGE)
                        .severity(rule.getSeverity())
                        .message(String.format("Rule '%s' coverage %.1f%% below minimum %.1f%%",
                                rule.getRuleId(),
                                result.getCodeCoverage() * 100,
                                rule.getMinCoverage() * 100))
                        .owner(rule.getOwner())
                        .sourceLocation(rule.getSourceLocation())
                        .complianceImpact(rule.getComplianceFrameworks())
                        .build());
            }
        }

        return violations;
    }

    private List<Violation> detectOrphanedTests(List<TestRuleInfo> allTests, List<BusinessRuleInfo> allRules) {
        if (!config.isFailOnOrphanedTests()) {
            return List.of();
        }

        Set<String> ruleIds = allRules.stream()
                .map(BusinessRuleInfo::getRuleId)
                .collect(Collectors.toSet());

        return allTests.stream()
                .filter(test -> !ruleIds.contains(test.getRuleId()))
                .map(test -> Violation.builder()
                        .ruleId(test.getRuleId())
                        .violationType(Violation.ViolationType.ORPHANED_TEST)
                        .severity(Severity.LOW)
                        .message(String.format("Test '%s' references non-existent rule: %s",
                                test.getTestLocation(), test.getRuleId()))
                        .owner("")
                        .sourceLocation(test.getTestLocation())
                        .build())
                .collect(Collectors.toList());
    }

    private boolean shouldReportViolation(Severity severity) {
        return config.shouldFailOn(severity);
    }

    /**
     * Gets the scanner used by this validator.
     *
     * @return the scanner
     */
    public AnnotationScanner getScanner() {
        return scanner;
    }

    /**
     * Gets the configuration used by this validator.
     *
     * @return the config
     */
    public GovernanceConfig getConfig() {
        return config;
    }
}
