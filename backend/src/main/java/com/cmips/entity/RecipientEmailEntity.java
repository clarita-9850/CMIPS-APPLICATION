package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Recipient Email Entity — BR OS 46
 *
 * Manages multiple email addresses per recipient.
 * Supports primary flag, email types, and ACTIVE/INACTIVE lifecycle.
 *
 * On create/update: triggers ESP enrollment notification event
 * (AMQP channel in production; logged via NotificationService in MVP).
 *
 * Email types:
 *   HOME   — personal/home address
 *   WORK   — workplace address
 *   OTHER  — any other address
 *
 * Status:
 *   ACTIVE   — in use
 *   INACTIVE — deactivated by worker or superseded
 */
@Entity
@Table(name = "recipient_emails")
public class RecipientEmailEntity {

    public enum EmailType {
        HOME, WORK, OTHER
    }

    public enum EmailStatus {
        ACTIVE, INACTIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "email_address", nullable = false, length = 255)
    private String emailAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 20)
    private EmailType emailType;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmailStatus status;

    @Column(name = "inactivated_date")
    private LocalDateTime inactivatedDate;

    @Column(name = "inactivated_by", length = 100)
    private String inactivatedBy;

    @Column(name = "notes", length = 500)
    private String notes;

    // Audit
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null)    status    = EmailStatus.ACTIVE;
        if (isPrimary == null) isPrimary = Boolean.FALSE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public RecipientEmailEntity() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public EmailType getEmailType() { return emailType; }
    public void setEmailType(EmailType emailType) { this.emailType = emailType; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }

    public EmailStatus getStatus() { return status; }
    public void setStatus(EmailStatus status) { this.status = status; }

    public LocalDateTime getInactivatedDate() { return inactivatedDate; }
    public void setInactivatedDate(LocalDateTime inactivatedDate) { this.inactivatedDate = inactivatedDate; }

    public String getInactivatedBy() { return inactivatedBy; }
    public void setInactivatedBy(String inactivatedBy) { this.inactivatedBy = inactivatedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
