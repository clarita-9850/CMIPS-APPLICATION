package com.clarita.governance.core.report;

import com.clarita.governance.core.config.GovernanceConfig;
import com.clarita.governance.core.model.ValidationResult;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface for report generators.
 * Implementations generate reports in different formats (JSON, HTML, PDF, etc.)
 *
 * @since 1.0.0
 */
public interface ReportGenerator {

    /**
     * Gets the format this generator produces.
     *
     * @return report format
     */
    GovernanceConfig.ReportFormat getFormat();

    /**
     * Generates a report from validation results.
     *
     * @param result the validation result
     * @param outputPath path to write the report
     * @throws IOException if writing fails
     */
    void generate(ValidationResult result, Path outputPath) throws IOException;

    /**
     * Generates a report and returns it as a string.
     *
     * @param result the validation result
     * @return report content as string
     */
    String generateToString(ValidationResult result);

    /**
     * Gets the default filename for this report type.
     *
     * @return default filename
     */
    default String getDefaultFilename() {
        return "governance-report." + getFormat().name().toLowerCase();
    }
}
