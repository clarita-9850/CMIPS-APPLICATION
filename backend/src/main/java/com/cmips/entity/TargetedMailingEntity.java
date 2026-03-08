package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Targeted Mailing Entity — DSD Section 26
 *
 * Manages bulk mailings to IHSS recipients and/or providers.
 * Types: NOTICE, REMINDER, INFORMATION, POLICY_UPDATE
 * Supports multi-language: ENGLISH, SPANISH, CHINESE, ARMENIAN
 *
 * Lifecycle: DRAFT -> SCHEDULED -> IN_PROGRESS -> COMPLETED/CANCELLED
 *
 * Target criteria stored as JSON describing the filter conditions
 * used to select mailing recipients.
 */
@Entity
@Table(name = "targeted_mailings", indexes = {
    @Index(name = "idx_tm_status", columnList = "status"),
    @Index(name = "idx_tm_county", columnList = "county_code")
})
public class TargetedMailingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mailing_name", nullable = false, length = 200)
    private String mailingName;

    @Column(name = "mailing_type", nullable = false, length = 50)
    private String mailingType;

    @Column(name = "target_criteria", length = 2000)
    private String targetCriteria;

    @Column(name = "recipient_type", length = 30)
    private String recipientType;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "template_path", length = 500)
    private String templatePath;

    @Column(name = "language", length = 20)
    private String language;

    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "total_recipients")
    private Integer totalRecipients;

    @Column(name = "mailed_count")
    private Integer mailedCount;

    @Column(name = "returned_count")
    private Integer returnedCount;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "mailed_date")
    private LocalDate mailedDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public TargetedMailingEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "DRAFT";
        if (language == null) language = "ENGLISH";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMailingName() { return mailingName; }
    public void setMailingName(String mailingName) { this.mailingName = mailingName; }

    public String getMailingType() { return mailingType; }
    public void setMailingType(String mailingType) { this.mailingType = mailingType; }

    public String getTargetCriteria() { return targetCriteria; }
    public void setTargetCriteria(String targetCriteria) { this.targetCriteria = targetCriteria; }

    public String getRecipientType() { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getTemplatePath() { return templatePath; }
    public void setTemplatePath(String templatePath) { this.templatePath = templatePath; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }

    public Integer getTotalRecipients() { return totalRecipients; }
    public void setTotalRecipients(Integer totalRecipients) { this.totalRecipients = totalRecipients; }

    public Integer getMailedCount() { return mailedCount; }
    public void setMailedCount(Integer mailedCount) { this.mailedCount = mailedCount; }

    public Integer getReturnedCount() { return returnedCount; }
    public void setReturnedCount(Integer returnedCount) { this.returnedCount = returnedCount; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalDate getMailedDate() { return mailedDate; }
    public void setMailedDate(LocalDate mailedDate) { this.mailedDate = mailedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
