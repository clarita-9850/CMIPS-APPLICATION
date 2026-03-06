package com.cmips.entity;

import jakarta.persistence.*;

/**
 * Email Template — DSD Section 31 / CMIPS E-Timesheet & Notifications
 *
 * Stores the 69+ official CDSS/CMIPS email template types with subject,
 * body (Freemarker-compatible with {{VAR}} placeholders), and metadata.
 *
 * Variable placeholders used in subject/body:
 *   {{RECIPIENT_NAME}}  {{CASE_NUMBER}}  {{WORKER_NAME}}  {{WORKER_PHONE}}
 *   {{EFFECTIVE_DATE}}  {{PAY_PERIOD}}   {{AMOUNT}}       {{HOURS}}
 *   {{COUNTY_NAME}}     {{PROVIDER_NAME}} {{DEADLINE_DATE}} {{URL}}
 */
@Entity
@Table(name = "email_templates",
        uniqueConstraints = @UniqueConstraint(columnNames = "template_code"))
public class EmailTemplateEntity {

    public enum TemplateCategory {
        ELIGIBILITY,        // Approval, denial, termination, change, continuation
        PAYMENT,            // Payment issued, direct deposit, warrant
        TIMESHEET,          // E-timesheet submission, approval, rejection, reminder
        PROVIDER,           // Provider enrollment, CORI, orientation
        ACCOUNT,            // ESP account activation, password reset, MFA
        NOTICE,             // NOA distribution by email
        REASSESSMENT,       // Reassessment due, scheduled, completed
        OVERPAYMENT,        // Overpayment notice, collection
        MASS_COMMUNICATION  // County announcements, legislative changes
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique code, e.g. "ET-001", "PAY-003" */
    @Column(name = "template_code", nullable = false, length = 20)
    private String templateCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private TemplateCategory category;

    /** Short display name */
    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    /** Email subject line (may contain {{VARS}}) */
    @Column(name = "subject_line", nullable = false, length = 500)
    private String subjectLine;

    /** HTML body in Freemarker template syntax */
    @Column(name = "body_html", columnDefinition = "TEXT", nullable = false)
    private String bodyHtml;

    /** Plain-text fallback */
    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    /** Comma-delimited list of required variable names */
    @Column(name = "required_vars", length = 500)
    private String requiredVars;

    /** Whether this template should also be sent as an SMS */
    @Column(name = "send_sms_also")
    private Boolean sendSmsAlso = false;

    /** Whether active (disabled templates won't send) */
    @Column(name = "active")
    private Boolean active = true;

    public EmailTemplateEntity() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public TemplateCategory getCategory() { return category; }
    public void setCategory(TemplateCategory category) { this.category = category; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getSubjectLine() { return subjectLine; }
    public void setSubjectLine(String subjectLine) { this.subjectLine = subjectLine; }

    public String getBodyHtml() { return bodyHtml; }
    public void setBodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; }

    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }

    public String getRequiredVars() { return requiredVars; }
    public void setRequiredVars(String requiredVars) { this.requiredVars = requiredVars; }

    public Boolean getSendSmsAlso() { return sendSmsAlso; }
    public void setSendSmsAlso(Boolean sendSmsAlso) { this.sendSmsAlso = sendSmsAlso; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
