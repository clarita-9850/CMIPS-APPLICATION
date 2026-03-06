package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Notice of Action Entity
 * DSD Case Lifecycle — NOA forms NA 1250–1257
 * NA 1250: Approval
 * NA 1251: Continuation
 * NA 1252: Denial
 * NA 1253: Change in Award
 * NA 1254: Change (Reduction)
 * NA 1255: Termination
 * NA 1256: Share of Cost
 * NA 1257: Multi-Program
 */
@Entity
@Table(name = "notices_of_action")
public class NoticeOfActionEntity {

    public enum NoaType {
        NA_1250, NA_1251, NA_1252, NA_1253, NA_1254, NA_1255, NA_1256, NA_1257
    }

    public enum NoaStatus {
        PENDING, PRINTED, NOT_MAILED, SUPPRESSED
    }

    public enum Language {
        ENGLISH, SPANISH, CHINESE, ARMENIAN, TAGALOG, VIETNAMESE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "noa_type", nullable = false, length = 20)
    private NoaType noaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NoaStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 20)
    private Language language;

    // The lifecycle action that triggered this NOA (e.g. TERMINATE, DENY, APPROVE)
    @Column(name = "trigger_action", length = 50)
    private String triggerAction;

    // Reason code that triggered NOA (e.g. CC514, R0001)
    @Column(name = "trigger_reason_code", length = 20)
    private String triggerReasonCode;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "print_date")
    private LocalDate printDate;

    @Column(name = "mailed_date")
    private LocalDate mailedDate;

    @Column(name = "suppressed_date")
    private LocalDate suppressedDate;

    @Column(name = "suppressed_by", length = 100)
    private String suppressedBy;

    @Column(name = "suppressed_reason", columnDefinition = "TEXT")
    private String suppressedReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Assembled NOA body text (in recipient's language) populated by NoaContentAssemblerService.
     * Contains the full translated text with dynamic variables substituted.
     */
    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    /**
     * Comma-delimited list of Appendix G category codes included in this NOA body.
     * e.g. "DN03,IN01,SH01"
     */
    @Column(name = "assembled_categories", length = 500)
    private String assembledCategories;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public NoticeOfActionEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = NoaStatus.PENDING;
        if (requestDate == null) requestDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public NoaType getNoaType() { return noaType; }
    public void setNoaType(NoaType noaType) { this.noaType = noaType; }

    public NoaStatus getStatus() { return status; }
    public void setStatus(NoaStatus status) { this.status = status; }

    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }

    public String getTriggerAction() { return triggerAction; }
    public void setTriggerAction(String triggerAction) { this.triggerAction = triggerAction; }

    public String getTriggerReasonCode() { return triggerReasonCode; }
    public void setTriggerReasonCode(String triggerReasonCode) { this.triggerReasonCode = triggerReasonCode; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }

    public LocalDate getPrintDate() { return printDate; }
    public void setPrintDate(LocalDate printDate) { this.printDate = printDate; }

    public LocalDate getMailedDate() { return mailedDate; }
    public void setMailedDate(LocalDate mailedDate) { this.mailedDate = mailedDate; }

    public LocalDate getSuppressedDate() { return suppressedDate; }
    public void setSuppressedDate(LocalDate suppressedDate) { this.suppressedDate = suppressedDate; }

    public String getSuppressedBy() { return suppressedBy; }
    public void setSuppressedBy(String suppressedBy) { this.suppressedBy = suppressedBy; }

    public String getSuppressedReason() { return suppressedReason; }
    public void setSuppressedReason(String suppressedReason) { this.suppressedReason = suppressedReason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getMessageContent() { return messageContent; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }

    public String getAssembledCategories() { return assembledCategories; }
    public void setAssembledCategories(String assembledCategories) { this.assembledCategories = assembledCategories; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
