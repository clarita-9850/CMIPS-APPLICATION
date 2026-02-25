package com.clarita.governance.maven;

import com.clarita.governance.annotations.ExecutionPhase;
import com.clarita.governance.core.GovernanceEngine;
import com.clarita.governance.core.config.GovernanceConfig;
import com.clarita.governance.core.model.ValidationResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Maven goal to validate governance rules.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * <plugin>
 *     <groupId>com.clarita</groupId>
 *     <artifactId>test-governance-maven-plugin</artifactId>
 *     <version>1.0.0</version>
 *     <executions>
 *         <execution>
 *             <goals>
 *                 <goal>validate</goal>
 *             </goals>
 *         </execution>
 *     </executions>
 *     <configuration>
 *         <basePackages>
 *             <package>com.mycompany.myapp</package>
 *         </basePackages>
 *         <failOnCritical>true</failOnCritical>
 *         <failOnHigh>true</failOnHigh>
 *     </configuration>
 * </plugin>
 * }</pre>
 *
 * @since 1.0.0
 */
@Mojo(name = "validate", defaultPhase = LifecyclePhase.VERIFY)
public class ValidateMojo extends AbstractMojo {

    /**
     * Packages to scan for business rules.
     */
    @Parameter(required = true)
    private List<String> basePackages;

    /**
     * Packages to scan for tests.
     */
    @Parameter
    private List<String> testPackages;

    /**
     * Packages to exclude from scanning.
     */
    @Parameter
    private List<String> excludePackages;

    /**
     * Execution phase.
     */
    @Parameter(defaultValue = "PR")
    private String phase;

    /**
     * Fail build on CRITICAL violations.
     */
    @Parameter(defaultValue = "true")
    private boolean failOnCritical;

    /**
     * Fail build on HIGH violations.
     */
    @Parameter(defaultValue = "true")
    private boolean failOnHigh;

    /**
     * Fail build on MEDIUM violations.
     */
    @Parameter(defaultValue = "false")
    private boolean failOnMedium;

    /**
     * Report output directory.
     */
    @Parameter(defaultValue = "${project.build.directory}/governance-reports")
    private File reportDirectory;

    /**
     * Report formats to generate.
     */
    @Parameter
    private List<String> reportFormats;

    /**
     * Skip governance validation.
     */
    @Parameter(property = "governance.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Path to JaCoCo XML report for coverage integration.
     */
    @Parameter(defaultValue = "${project.build.directory}/site/jacoco/jacoco.xml")
    private File jacocoReportPath;

    /**
     * Enable JaCoCo coverage integration.
     */
    @Parameter(defaultValue = "false")
    private boolean coverageEnabled;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Governance validation skipped");
            return;
        }

        getLog().info("Running Test Governance validation...");
        getLog().info("Base packages: " + basePackages);

        try {
            GovernanceConfig config = buildConfig();
            GovernanceEngine engine = new GovernanceEngine(config);

            ValidationResult result = engine.validate();
            getLog().info("Validation result: " + result.getSummary());

            // Generate reports
            engine.generateReports(result);
            getLog().info("Reports generated in: " + reportDirectory.getAbsolutePath());

            // Check for failure
            if (result.shouldFailBuild()) {
                throw new MojoFailureException(
                        "Governance validation failed: " + result.getSummary() +
                                "\nSee report at: " + reportDirectory.getAbsolutePath()
                );
            }

            getLog().info("Governance validation passed!");

        } catch (MojoFailureException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("Governance validation error", e);
        }
    }

    private GovernanceConfig buildConfig() {
        GovernanceConfig config = new GovernanceConfig();

        // Packages
        config.setBasePackages(Set.copyOf(basePackages));
        if (testPackages != null && !testPackages.isEmpty()) {
            config.setTestPackages(Set.copyOf(testPackages));
        } else {
            config.setTestPackages(Set.copyOf(basePackages));
        }
        if (excludePackages != null) {
            config.setExcludePackages(Set.copyOf(excludePackages));
        }

        // Phase
        config.setCurrentPhase(ExecutionPhase.valueOf(phase.toUpperCase()));

        // Failure
        config.setFailOnCritical(failOnCritical);
        config.setFailOnHigh(failOnHigh);
        config.setFailOnMedium(failOnMedium);

        // Reports
        config.setReportEnabled(true);
        config.setReportOutputDir(reportDirectory.toPath());

        if (reportFormats != null && !reportFormats.isEmpty()) {
            EnumSet<GovernanceConfig.ReportFormat> formats = EnumSet.noneOf(GovernanceConfig.ReportFormat.class);
            for (String format : reportFormats) {
                formats.add(GovernanceConfig.ReportFormat.valueOf(format.toUpperCase()));
            }
            config.setReportFormats(formats);
        }

        // Coverage
        config.setCoverageEnabled(coverageEnabled);
        if (jacocoReportPath != null) {
            config.setJacocoReportPath(jacocoReportPath.toPath());
        }

        return config;
    }
}
