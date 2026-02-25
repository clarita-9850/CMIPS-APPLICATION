package com.clarita.governance.core.report;

import com.clarita.governance.annotations.Severity;
import com.clarita.governance.core.config.GovernanceConfig;
import com.clarita.governance.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Generates HTML reports from validation results.
 * Produces human-readable reports with styling and interactivity.
 *
 * @since 1.0.0
 */
public class HtmlReportGenerator implements ReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(HtmlReportGenerator.class);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

    private final GovernanceConfig config;

    public HtmlReportGenerator() {
        this(GovernanceConfig.defaults());
    }

    public HtmlReportGenerator(GovernanceConfig config) {
        this.config = config;
    }

    @Override
    public GovernanceConfig.ReportFormat getFormat() {
        return GovernanceConfig.ReportFormat.HTML;
    }

    @Override
    public void generate(ValidationResult result, Path outputPath) throws IOException {
        log.info("Generating HTML report to: {}", outputPath);

        Files.createDirectories(outputPath.getParent());
        String html = generateToString(result);
        Files.writeString(outputPath, html);

        log.info("HTML report generated successfully: {} bytes", Files.size(outputPath));
    }

    @Override
    public String generateToString(ValidationResult result) {
        StringBuilder html = new StringBuilder();

        html.append(generateHead(result));
        html.append("<body>\n");
        html.append(generateHeader(result));
        html.append("<main class=\"container\">\n");
        html.append(generateSummaryCards(result));
        html.append(generateComplianceSection(result));
        html.append(generateViolationsSection(result));
        html.append(generateRulesSection(result));
        html.append(generateOwnershipSection(result));
        html.append("</main>\n");
        html.append(generateFooter());
        html.append("</body>\n</html>");

        return html.toString();
    }

    private String generateHead(ValidationResult result) {
        String title = config.getHtmlTitle().isEmpty() ?
                "Test Governance Report" : config.getHtmlTitle();

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        :root {
                            --primary: #2563eb;
                            --success: #16a34a;
                            --warning: #ca8a04;
                            --danger: #dc2626;
                            --muted: #64748b;
                            --bg: #f8fafc;
                            --card: #ffffff;
                            --border: #e2e8f0;
                        }
                        * { box-sizing: border-box; margin: 0; padding: 0; }
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: var(--bg); color: #1e293b; line-height: 1.6; }
                        .container { max-width: 1200px; margin: 0 auto; padding: 2rem; }
                        header { background: linear-gradient(135deg, #1e3a8a 0%%, #3b82f6 100%%); color: white; padding: 2rem 0; }
                        header .container { display: flex; justify-content: space-between; align-items: center; }
                        h1 { font-size: 1.75rem; font-weight: 600; }
                        h2 { font-size: 1.25rem; font-weight: 600; margin-bottom: 1rem; color: #334155; }
                        h3 { font-size: 1rem; font-weight: 500; margin-bottom: 0.5rem; }
                        .badge { display: inline-block; padding: 0.25rem 0.75rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; }
                        .badge-success { background: #dcfce7; color: #166534; }
                        .badge-warning { background: #fef3c7; color: #92400e; }
                        .badge-danger { background: #fee2e2; color: #991b1b; }
                        .badge-info { background: #dbeafe; color: #1e40af; }
                        .card { background: var(--card); border-radius: 0.5rem; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 1.5rem; margin-bottom: 1.5rem; }
                        .card-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 2rem; }
                        .stat-card { text-align: center; }
                        .stat-value { font-size: 2rem; font-weight: 700; }
                        .stat-label { color: var(--muted); font-size: 0.875rem; }
                        .stat-value.success { color: var(--success); }
                        .stat-value.warning { color: var(--warning); }
                        .stat-value.danger { color: var(--danger); }
                        table { width: 100%%; border-collapse: collapse; font-size: 0.875rem; }
                        th, td { padding: 0.75rem; text-align: left; border-bottom: 1px solid var(--border); }
                        th { background: #f1f5f9; font-weight: 600; color: #475569; }
                        tr:hover { background: #f8fafc; }
                        .severity-critical { color: var(--danger); font-weight: 600; }
                        .severity-high { color: #ea580c; font-weight: 600; }
                        .severity-medium { color: var(--warning); }
                        .severity-low { color: var(--muted); }
                        .status-passed, .status-covered { color: var(--success); }
                        .status-failed, .status-uncovered { color: var(--danger); }
                        .status-warning, .status-partially_covered { color: var(--warning); }
                        .collapsible { cursor: pointer; }
                        .collapsible:after { content: ' ▼'; font-size: 0.75rem; }
                        .collapsible.active:after { content: ' ▲'; }
                        .content { display: none; padding: 1rem 0; }
                        .content.show { display: block; }
                        .test-list { list-style: none; padding-left: 1rem; }
                        .test-list li { padding: 0.25rem 0; color: var(--muted); }
                        .meta { color: var(--muted); font-size: 0.75rem; }
                        footer { text-align: center; padding: 2rem; color: var(--muted); font-size: 0.875rem; }
                        @media print { .card { break-inside: avoid; } }
                    </style>
                </head>
                """.formatted(title);
    }

    private String generateHeader(ValidationResult result) {
        String statusClass = switch (result.getOverallStatus()) {
            case PASSED -> "badge-success";
            case WARNING -> "badge-warning";
            case FAILED -> "badge-danger";
        };

        return """
                <header>
                    <div class="container">
                        <div>
                            <h1>%s</h1>
                            <div class="meta">Generated: %s | Phase: %s</div>
                        </div>
                        <span class="badge %s">%s</span>
                    </div>
                </header>
                """.formatted(
                config.getHtmlTitle().isEmpty() ? "Test Governance Report" : config.getHtmlTitle(),
                DATE_FORMATTER.format(result.getTimestamp()),
                result.getPhase().name(),
                statusClass,
                result.getOverallStatus().getDisplayName()
        );
    }

    private String generateSummaryCards(ValidationResult result) {
        String coverageClass = result.getCoveragePercentage() >= 90 ? "success" :
                result.getCoveragePercentage() >= 70 ? "warning" : "danger";

        return """
                <div class="card-grid">
                    <div class="card stat-card">
                        <div class="stat-value">%d</div>
                        <div class="stat-label">Total Rules</div>
                    </div>
                    <div class="card stat-card">
                        <div class="stat-value success">%d</div>
                        <div class="stat-label">Covered</div>
                    </div>
                    <div class="card stat-card">
                        <div class="stat-value %s">%.1f%%</div>
                        <div class="stat-label">Coverage</div>
                    </div>
                    <div class="card stat-card">
                        <div class="stat-value danger">%d</div>
                        <div class="stat-label">Violations</div>
                    </div>
                </div>
                """.formatted(
                result.getTotalRules(),
                result.getCoveredRules(),
                coverageClass,
                result.getCoveragePercentage(),
                result.getViolations().size()
        );
    }

    private String generateComplianceSection(ValidationResult result) {
        if (result.getComplianceSummary().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"card\"><h2>Compliance Summary</h2><table><thead><tr>");
        sb.append("<th>Framework</th><th>Rules</th><th>Covered</th><th>Coverage</th><th>Status</th>");
        sb.append("</tr></thead><tbody>");

        for (var entry : result.getComplianceSummary().entrySet()) {
            var summary = entry.getValue();
            String statusClass = summary.isPassing() ? "status-passed" : "status-failed";

            sb.append("<tr>");
            sb.append("<td><strong>").append(entry.getKey().getCode()).append("</strong><br>");
            sb.append("<span class=\"meta\">").append(entry.getKey().getFullName()).append("</span></td>");
            sb.append("<td>").append(summary.totalRules()).append("</td>");
            sb.append("<td>").append(summary.coveredRules()).append("</td>");
            sb.append("<td>").append(String.format("%.1f%%", summary.coveragePercentage())).append("</td>");
            sb.append("<td class=\"").append(statusClass).append("\">").append(summary.getStatus()).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</tbody></table></div>");
        return sb.toString();
    }

    private String generateViolationsSection(ValidationResult result) {
        if (result.getViolations().isEmpty()) {
            return "<div class=\"card\"><h2>Violations</h2><p>No violations found.</p></div>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"card\"><h2>Violations (").append(result.getViolations().size()).append(")</h2>");
        sb.append("<table><thead><tr>");
        sb.append("<th>Severity</th><th>Rule ID</th><th>Type</th><th>Message</th><th>Owner</th>");
        sb.append("</tr></thead><tbody>");

        for (Violation violation : result.getViolations()) {
            String severityClass = "severity-" + violation.getSeverity().name().toLowerCase();

            sb.append("<tr>");
            sb.append("<td class=\"").append(severityClass).append("\">")
                    .append(violation.getSeverity().name()).append("</td>");
            sb.append("<td><code>").append(violation.getRuleId()).append("</code></td>");
            sb.append("<td>").append(violation.getViolationType().getDisplayName()).append("</td>");
            sb.append("<td>").append(escapeHtml(violation.getMessage())).append("</td>");
            sb.append("<td>").append(violation.getOwner()).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</tbody></table></div>");
        return sb.toString();
    }

    private String generateRulesSection(ValidationResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"card\"><h2>Business Rules</h2>");
        sb.append("<table><thead><tr>");
        sb.append("<th>Rule ID</th><th>Description</th><th>Severity</th><th>Status</th><th>Tests</th><th>Owner</th>");
        sb.append("</tr></thead><tbody>");

        for (MatchResult match : result.getMatchResults()) {
            if (!config.isIncludePassing() && match.isPassing()) {
                continue;
            }

            BusinessRuleInfo rule = match.getRule();
            String severityClass = "severity-" + rule.getSeverity().name().toLowerCase();
            String statusClass = "status-" + match.getStatus().name().toLowerCase();

            sb.append("<tr>");
            sb.append("<td><code>").append(rule.getRuleId()).append("</code></td>");
            sb.append("<td>").append(escapeHtml(truncate(rule.getDescription(), 60))).append("</td>");
            sb.append("<td class=\"").append(severityClass).append("\">")
                    .append(rule.getSeverity().name()).append("</td>");
            sb.append("<td class=\"").append(statusClass).append("\">")
                    .append(match.getStatus().getDisplayName()).append("</td>");
            sb.append("<td>").append(match.getActiveTestCount()).append("</td>");
            sb.append("<td>").append(rule.getOwner()).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</tbody></table></div>");
        return sb.toString();
    }

    private String generateOwnershipSection(ValidationResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"card\"><h2>Ownership Summary</h2>");
        sb.append("<table><thead><tr>");
        sb.append("<th>Owner</th><th>Total</th><th>Covered</th><th>Coverage</th>");
        sb.append("</tr></thead><tbody>");

        var byOwner = result.getMatchResults().stream()
                .collect(Collectors.groupingBy(r -> r.getRule().getOwner()));

        for (var entry : byOwner.entrySet()) {
            int total = entry.getValue().size();
            int covered = (int) entry.getValue().stream().filter(MatchResult::isPassing).count();
            double pct = total > 0 ? (double) covered / total * 100 : 100;

            sb.append("<tr>");
            sb.append("<td><strong>").append(entry.getKey()).append("</strong></td>");
            sb.append("<td>").append(total).append("</td>");
            sb.append("<td>").append(covered).append("</td>");
            sb.append("<td>").append(String.format("%.1f%%", pct)).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</tbody></table></div>");
        return sb.toString();
    }

    private String generateFooter() {
        return """
                <footer>
                    <p>Generated by Test Governance Framework v1.0.0</p>
                    <p>Report format compliant with NIST 800-53, CMS SMC, and FISMA requirements</p>
                </footer>
                <script>
                    document.querySelectorAll('.collapsible').forEach(item => {
                        item.addEventListener('click', function() {
                            this.classList.toggle('active');
                            const content = this.nextElementSibling;
                            content.classList.toggle('show');
                        });
                    });
                </script>
                """;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
