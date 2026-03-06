package com.clarita.governance.core.scanner;

import com.clarita.governance.annotations.*;
import com.clarita.governance.core.config.GovernanceConfig;
import com.clarita.governance.core.model.BusinessRuleInfo;
import com.clarita.governance.core.model.TestRuleInfo;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scans the classpath for governance annotations.
 * Uses the Reflections library for efficient class scanning.
 *
 * @since 1.0.0
 */
public class AnnotationScanner {

    private static final Logger log = LoggerFactory.getLogger(AnnotationScanner.class);

    private final GovernanceConfig config;
    private final Map<String, BusinessRuleInfo> businessRuleCache = new ConcurrentHashMap<>();
    private final Map<String, List<TestRuleInfo>> testRuleCache = new ConcurrentHashMap<>();
    private boolean scanned = false;

    public AnnotationScanner(GovernanceConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
    }

    /**
     * Scans all configured packages and returns discovered business rules.
     *
     * @return list of business rules
     */
    public List<BusinessRuleInfo> scanBusinessRules() {
        ensureScanned();
        return new ArrayList<>(businessRuleCache.values());
    }

    /**
     * Scans all configured packages and returns discovered test rules.
     *
     * @return list of test rules
     */
    public List<TestRuleInfo> scanTestRules() {
        ensureScanned();
        return testRuleCache.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Gets test rules for a specific business rule ID.
     *
     * @param ruleId the business rule ID
     * @return list of test rules for that ID
     */
    public List<TestRuleInfo> getTestsForRule(String ruleId) {
        ensureScanned();
        return testRuleCache.getOrDefault(ruleId, List.of());
    }

    /**
     * Gets a business rule by ID.
     *
     * @param ruleId the rule ID
     * @return optional containing the rule if found
     */
    public Optional<BusinessRuleInfo> getBusinessRule(String ruleId) {
        ensureScanned();
        return Optional.ofNullable(businessRuleCache.get(ruleId));
    }

    /**
     * Forces a rescan of all packages.
     */
    public void rescan() {
        businessRuleCache.clear();
        testRuleCache.clear();
        scanned = false;
        ensureScanned();
    }

    private synchronized void ensureScanned() {
        if (scanned && config.isCacheEnabled()) {
            return;
        }

        log.info("Starting annotation scan for packages: {}", config.getBasePackages());
        long startTime = System.currentTimeMillis();

        // Scan for business rules
        scanForBusinessRules();

        // Scan for test rules
        scanForTestRules();

        long duration = System.currentTimeMillis() - startTime;
        log.info("Annotation scan completed in {}ms. Found {} business rules and {} test methods",
                duration, businessRuleCache.size(),
                testRuleCache.values().stream().mapToInt(List::size).sum());

        scanned = true;
    }

    private void scanForBusinessRules() {
        Set<String> packages = config.getBasePackages();
        if (packages.isEmpty()) {
            log.warn("No base packages configured for scanning");
            return;
        }

        Reflections reflections = createReflections(packages);

        // Scan method-level annotations
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(BusinessRule.class);
        for (Method method : annotatedMethods) {
            if (shouldExclude(method.getDeclaringClass())) {
                continue;
            }
            BusinessRule annotation = method.getAnnotation(BusinessRule.class);
            BusinessRuleInfo info = extractBusinessRuleInfo(annotation, method.getDeclaringClass(), method);

            if (businessRuleCache.containsKey(info.getRuleId())) {
                log.warn("Duplicate rule ID found: {} (existing: {}, new: {})",
                        info.getRuleId(),
                        businessRuleCache.get(info.getRuleId()).getSourceLocation(),
                        info.getSourceLocation());
            }
            businessRuleCache.put(info.getRuleId(), info);
        }

        // Scan class-level annotations
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(BusinessRule.class);
        for (Class<?> clazz : annotatedClasses) {
            if (shouldExclude(clazz)) {
                continue;
            }
            BusinessRule annotation = clazz.getAnnotation(BusinessRule.class);
            BusinessRuleInfo info = extractBusinessRuleInfo(annotation, clazz, null);

            if (businessRuleCache.containsKey(info.getRuleId())) {
                log.warn("Duplicate rule ID found: {}", info.getRuleId());
            }
            businessRuleCache.put(info.getRuleId(), info);
        }
    }

    private void scanForTestRules() {
        Set<String> testPackages = config.getTestPackages();
        if (testPackages.isEmpty()) {
            // Default to base packages if test packages not specified
            testPackages = config.getBasePackages();
        }

        if (testPackages.isEmpty()) {
            return;
        }

        Reflections reflections = createReflections(testPackages);

        // Scan for @TestsRule annotations
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(TestsRule.class);
        for (Method method : annotatedMethods) {
            // Handle single annotation
            TestsRule annotation = method.getAnnotation(TestsRule.class);
            if (annotation != null) {
                TestRuleInfo info = extractTestRuleInfo(annotation, method);
                testRuleCache.computeIfAbsent(info.getRuleId(), k -> new ArrayList<>()).add(info);
            }
        }

        // Scan for @TestsRules (repeatable container)
        Set<Method> containerMethods = reflections.getMethodsAnnotatedWith(TestsRules.class);
        for (Method method : containerMethods) {
            TestsRules container = method.getAnnotation(TestsRules.class);
            if (container != null) {
                for (TestsRule annotation : container.value()) {
                    TestRuleInfo info = extractTestRuleInfo(annotation, method);
                    testRuleCache.computeIfAbsent(info.getRuleId(), k -> new ArrayList<>()).add(info);
                }
            }
        }
    }

    private Reflections createReflections(Set<String> packages) {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder()
                .forPackages(packages.toArray(new String[0]))
                .addScanners(Scanners.MethodsAnnotated, Scanners.TypesAnnotated);

        if (config.isParallelScan()) {
            configBuilder.setParallel(true);
        }

        return new Reflections(configBuilder);
    }

    private BusinessRuleInfo extractBusinessRuleInfo(BusinessRule annotation, Class<?> clazz, Method method) {
        return BusinessRuleInfo.builder()
                .ruleId(annotation.ruleId())
                .description(annotation.description())
                .owner(annotation.owner())
                .severity(annotation.severity())
                .requiredCategories(Set.of(annotation.categories()))
                .phases(Set.of(annotation.phases()))
                .minCoverage(annotation.minCoverage())
                .complianceFrameworks(Set.of(annotation.compliance()))
                .requirementRef(annotation.requirementRef())
                .since(annotation.since())
                .deprecated(annotation.deprecated())
                .deprecationReason(annotation.deprecationReason())
                .tags(Set.of(annotation.tags()))
                .sourceClass(clazz.getName())
                .sourceMethod(method != null ? method.getName() : null)
                .sourceLineNumber(0) // Line number requires source parsing, skip for now
                .build();
    }

    private TestRuleInfo extractTestRuleInfo(TestsRule annotation, Method method) {
        return TestRuleInfo.builder()
                .ruleId(annotation.ruleId())
                .scenarios(Set.of(annotation.scenarios()))
                .author(annotation.author())
                .lastReviewed(annotation.lastReviewed())
                .category(annotation.category())
                .notes(annotation.notes())
                .priority(annotation.priority())
                .disabled(annotation.disabled())
                .disabledReason(annotation.disabledReason())
                .testClass(method.getDeclaringClass().getName())
                .testMethod(method.getName())
                .testLineNumber(0)
                .build();
    }

    private boolean shouldExclude(Class<?> clazz) {
        String className = clazz.getName();

        // Check excluded packages
        for (String excludedPkg : config.getExcludePackages()) {
            if (className.startsWith(excludedPkg)) {
                return true;
            }
        }

        // Check excluded patterns
        for (var pattern : config.getExcludePatterns()) {
            if (pattern.matcher(className).matches()) {
                return true;
            }
        }

        // Check for @GovernanceIgnore
        if (clazz.isAnnotationPresent(GovernanceIgnore.class)) {
            GovernanceIgnore ignore = clazz.getAnnotation(GovernanceIgnore.class);
            log.debug("Class {} excluded: {}", className, ignore.reason());
            return true;
        }

        return false;
    }

    /**
     * Gets statistics about the scan.
     *
     * @return scan statistics
     */
    public ScanStatistics getStatistics() {
        ensureScanned();
        return new ScanStatistics(
                businessRuleCache.size(),
                testRuleCache.values().stream().mapToInt(List::size).sum(),
                (int) testRuleCache.keySet().stream()
                        .filter(id -> !businessRuleCache.containsKey(id))
                        .count()
        );
    }

    /**
     * Statistics from the annotation scan.
     */
    public record ScanStatistics(
            int businessRuleCount,
            int testRuleCount,
            int orphanedTestCount
    ) {}
}
