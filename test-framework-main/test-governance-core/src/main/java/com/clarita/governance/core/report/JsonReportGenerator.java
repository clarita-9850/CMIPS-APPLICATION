package com.clarita.governance.core.report;

import com.clarita.governance.core.config.GovernanceConfig;
import com.clarita.governance.core.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates JSON reports from validation results.
 * Produces machine-readable output for CI/CD integration.
 *
 * @since 1.0.0
 */
public class JsonReportGenerator implements ReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(JsonReportGenerator.class);

    private final ObjectMapper objectMapper;
    private final GovernanceConfig config;

    public JsonReportGenerator() {
        this(GovernanceConfig.defaults());
    }

    public JsonReportGenerator(GovernanceConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public GovernanceConfig.ReportFormat getFormat() {
        return GovernanceConfig.ReportFormat.JSON;
    }

    @Override
    public void generate(ValidationResult result, Path outputPath) throws IOException {
        log.info("Generating JSON report to: {}", outputPath);

        // Ensure parent directory exists
        Files.createDirectories(outputPath.getParent());

        Map<String, Object> report = buildReportMap(result);
        objectMapper.writeValue(outputPath.toFile(), report);

        log.info("JSON report generated successfully: {} bytes", Files.size(outputPath));
    }

    @Override
    public String generateToString(ValidationResult result) {
        try {
            Map<String, Object> report = buildReportMap(result);
            return objectMapper.writeValueAsString(report);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate JSON report", e);
        }
    }

    private Map<String, Object> buildReportMap(ValidationResult result) {
        Map<String, Object> report = new LinkedHashMap<>();

        // Header
        report.put("header", buildHeader(result));

        // Summary
        report.put("summary", buildSummary(result));

        // Compliance Summary
        if (!result.getComplianceSummary().isEmpty()) {
            report.put("complianceSummary", buildComplianceSummary(result));
        }

        // Ownership Summary
        report.put("ownershipSummary", buildOwnershipSummary(result));

        // Rules
        report.put("rules", buildRules(result));

        // Violations
        report.put("violations", buildViolations(result));

        return report;
    }

    private Map<String, Object> buildHeader(ValidationResult result) {
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("generatedAt", result.getTimestamp().toString());
        header.put("phase", result.getPhase().name());
        header.put("applicationName", result.getApplicationName());
        header.put("applicationVersion", result.getApplicationVersion());
        header.put("frameworkVersion", result.getFrameworkVersion());
        header.put("scannedPackages", new ArrayList<>(result.getScannedPackages()));
        header.put("durationMs", result.getDuration().toMillis());
        return header;
    }

    private Map<String, Object> buildSummary(ValidationResult result) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalRules", result.getTotalRules());
        summary.put("coveredRules", result.getCoveredRules());
        summary.put("coveragePercentage", Math.round(result.getCoveragePercentage() * 10) / 10.0);
        summary.put("overallStatus", result.getOverallStatus().name());

        Map<String, Long> violationsBySeverity = result.getViolationsBySeverity().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name().toLowerCase() + "Violations",
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        summary.put("criticalViolations", violationsBySeverity.getOrDefault("criticalViolations", 0L));
        summary.put("highViolations", violationsBySeverity.getOrDefault("highViolations", 0L));
        summary.put("mediumViolations", violationsBySeverity.getOrDefault("mediumViolations", 0L));
        summary.put("lowViolations", violationsBySeverity.getOrDefault("lowViolations", 0L));

        return summary;
    }

    private Map<String, Object> buildComplianceSummary(ValidationResult result) {
        Map<String, Object> compliance = new LinkedHashMap<>();

        for (var entry : result.getComplianceSummary().entrySet()) {
            var summary = entry.getValue();
            Map<String, Object> frameworkData = new LinkedHashMap<>();
            frameworkData.put("framework", entry.getKey().getCode());
            frameworkData.put("fullName", entry.getKey().getFullName());
            frameworkData.put("totalRules", summary.totalRules());
            frameworkData.put("coveredRules", summary.coveredRules());
            frameworkData.put("percentage", Math.round(summary.coveragePercentage() * 10) / 10.0);
            frameworkData.put("status", summary.getStatus());
            compliance.put(entry.getKey().name(), frameworkData);
        }

        return compliance;
    }

    private Map<String, Object> buildOwnershipSummary(ValidationResult result) {
        Map<String, Object> ownership = new LinkedHashMap<>();

        Map<String, List<MatchResult>> byOwner = result.getMatchResults().stream()
                .collect(Collectors.groupingBy(r -> r.getRule().getOwner()));

        for (var entry : byOwner.entrySet()) {
            String owner = entry.getKey();
            List<MatchResult> results = entry.getValue();

            int total = results.size();
            int covered = (int) results.stream().filter(MatchResult::isPassing).count();
            List<String> missing = results.stream()
                    .filter(r -> !r.isPassing())
                    .map(r -> r.getRule().getRuleId())
                    .collect(Collectors.toList());

            Map<String, Object> ownerData = new LinkedHashMap<>();
            ownerData.put("owner", owner);
            ownerData.put("totalRules", total);
            ownerData.put("coveredRules", covered);
            ownerData.put("missingTests", missing);
            ownership.put(owner, ownerData);
        }

        return ownership;
    }

    private List<Map<String, Object>> buildRules(ValidationResult result) {
        List<Map<String, Object>> rules = new ArrayList<>();

        for (MatchResult match : result.getMatchResults()) {
            if (!config.isIncludePassing() && match.isPassing()) {
                continue;
            }

            BusinessRuleInfo rule = match.getRule();
            Map<String, Object> ruleData = new LinkedHashMap<>();

            ruleData.put("ruleId", rule.getRuleId());
            ruleData.put("description", rule.getDescription());
            ruleData.put("owner", rule.getOwner());
            ruleData.put("severity", rule.getSeverity().name());
            ruleData.put("status", match.getStatus().name());

            if (config.isIncludeSourceLocations()) {
                ruleData.put("sourceClass", rule.getSourceClass());
                ruleData.put("sourceMethod", rule.getSourceMethod());
            }

            if (!rule.getComplianceFrameworks().isEmpty()) {
                ruleData.put("complianceFrameworks",
                        rule.getComplianceFrameworks().stream()
                                .map(Enum::name)
                                .collect(Collectors.toList()));
            }

            if (!rule.getRequiredCategories().isEmpty()) {
                ruleData.put("requiredCategories",
                        rule.getRequiredCategories().stream()
                                .map(Enum::name)
                                .collect(Collectors.toList()));
            }

            if (!match.getCoveredCategories().isEmpty()) {
                ruleData.put("coveredCategories",
                        match.getCoveredCategories().stream()
                                .map(Enum::name)
                                .collect(Collectors.toList()));
            }

            if (!match.getMissingCategories().isEmpty()) {
                ruleData.put("missingCategories",
                        match.getMissingCategories().stream()
                                .map(Enum::name)
                                .collect(Collectors.toList()));
            }

            // Tests
            List<Map<String, Object>> tests = new ArrayList<>();
            for (TestRuleInfo test : match.getTests()) {
                Map<String, Object> testData = new LinkedHashMap<>();
                testData.put("testClass", test.getTestClass());
                testData.put("testMethod", test.getTestMethod());
                testData.put("category", test.getCategory().name());
                if (!test.getAuthor().isEmpty()) {
                    testData.put("author", test.getAuthor());
                }
                if (!test.getScenarios().isEmpty()) {
                    testData.put("scenarios", new ArrayList<>(test.getScenarios()));
                }
                if (test.isDisabled()) {
                    testData.put("disabled", true);
                    testData.put("disabledReason", test.getDisabledReason());
                }
                tests.add(testData);
            }
            ruleData.put("tests", tests);

            rules.add(ruleData);
        }

        return rules;
    }

    private List<Map<String, Object>> buildViolations(ValidationResult result) {
        List<Map<String, Object>> violations = new ArrayList<>();

        for (Violation violation : result.getViolations()) {
            Map<String, Object> violationData = new LinkedHashMap<>();
            violationData.put("ruleId", violation.getRuleId());
            violationData.put("type", violation.getViolationType().name());
            violationData.put("severity", violation.getSeverity().name());
            violationData.put("message", violation.getMessage());
            violationData.put("owner", violation.getOwner());

            if (config.isIncludeSourceLocations() && !violation.getSourceLocation().isEmpty()) {
                violationData.put("sourceLocation", violation.getSourceLocation());
            }

            violationData.put("remediation", violation.getRemediation());

            if (!violation.getComplianceImpact().isEmpty()) {
                violationData.put("complianceImpact",
                        violation.getComplianceImpact().stream()
                                .map(Enum::name)
                                .collect(Collectors.toList()));
            }

            violations.add(violationData);
        }

        return violations;
    }
}
