package com.cmips.service;

import com.cmips.entity.EmailTemplateEntity;
import com.cmips.repository.EmailTemplateRepository;
import freemarker.template.Configuration;
import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Email Service — DSD Section 31
 *
 * Sends CMIPS transactional emails using a template code + variable map.
 * Templates are stored in the email_templates table (seeded by EmailTemplateDataLoader).
 *
 * Usage:
 *   emailService.send("ET-003", toAddress, Map.of(
 *       "RECIPIENT_NAME", "Jane Doe",
 *       "PROVIDER_NAME",  "John Smith",
 *       "PAY_PERIOD",     "01/01/2026 - 01/15/2026",
 *       "DEADLINE_DATE",  "01/20/2026",
 *       "URL",            "https://cmips.example.com/timesheets"
 *   ));
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final EmailTemplateRepository templateRepo;
    private final Configuration freemarkerConfig;

    public EmailService(JavaMailSender mailSender,
                        EmailTemplateRepository templateRepo,
                        Configuration freemarkerConfig) {
        this.mailSender = mailSender;
        this.templateRepo = templateRepo;
        this.freemarkerConfig = freemarkerConfig;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Public API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Send a templated email.
     *
     * @param templateCode e.g. "ET-003"
     * @param toAddress    recipient email address
     * @param vars         map of variable values (keys match {{VAR}} placeholders in template)
     */
    public void send(String templateCode, String toAddress, Map<String, Object> vars) {
        EmailTemplateEntity tmpl = templateRepo.findByTemplateCode(templateCode).orElse(null);
        if (tmpl == null) {
            log.warn("[EMAIL] Template not found: {}", templateCode);
            return;
        }
        if (Boolean.FALSE.equals(tmpl.getActive())) {
            log.debug("[EMAIL] Template {} is inactive, skipping", templateCode);
            return;
        }
        if (toAddress == null || toAddress.isBlank()) {
            log.warn("[EMAIL] No recipient address for template {}", templateCode);
            return;
        }
        try {
            String subject = renderInline(tmpl.getSubjectLine(), vars);
            String htmlBody = renderInline(tmpl.getBodyHtml(), vars);
            String textBody = tmpl.getBodyText() != null ? renderInline(tmpl.getBodyText(), vars) : null;

            sendMime(toAddress, subject, htmlBody, textBody);
            log.info("[EMAIL] Sent {} → {} (template={})", templateCode, toAddress, tmpl.getTemplateName());

        } catch (Exception ex) {
            log.error("[EMAIL] Failed to send {} to {}: {}", templateCode, toAddress, ex.getMessage());
        }
    }

    /**
     * Resolve a template by code and return its name for logging/display.
     */
    public String getTemplateName(String templateCode) {
        return templateRepo.findByTemplateCode(templateCode)
                .map(EmailTemplateEntity::getTemplateName)
                .orElse(templateCode);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Simple {{VAR}} substitution using Freemarker inline template rendering.
     * Falls back to direct string replacement if Freemarker fails.
     */
    private String renderInline(String template, Map<String, Object> vars) {
        if (template == null) return "";
        try {
            Template ftl = new Template("inline", new StringReader(
                    template.replace("{{", "${").replace("}}", "}")), freemarkerConfig);
            StringWriter out = new StringWriter();
            ftl.process(vars, out);
            return out.toString();
        } catch (Exception ex) {
            // Fallback: manual replace
            String result = template;
            for (Map.Entry<String, Object> e : vars.entrySet()) {
                result = result.replace("{{" + e.getKey() + "}}", String.valueOf(e.getValue()));
            }
            return result;
        }
    }

    private void sendMime(String to, String subject, String htmlBody, String textBody) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setFrom("noreply@ihss.ca.gov");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(textBody != null ? textBody : "", htmlBody);
        mailSender.send(msg);
    }
}
