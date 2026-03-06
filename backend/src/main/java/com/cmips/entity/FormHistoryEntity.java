package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Form History Entity — DSD Section 31, CI-116615
 *
 * Tracks correspondence history (comments, status changes, events) for each
 * NOA or Electronic Form. Provides a per-document audit trail visible to county staff.
 *
 * Linked to either a NOA (noaId) or an Electronic Form (formId) — exactly one must be set.
 */
@Entity
@Table(name = "form_history", indexes = {
        @Index(name = "idx_fh_noa", columnList = "noa_id"),
        @Index(name = "idx_fh_form", columnList = "form_id"),
        @Index(name = "idx_fh_created", columnList = "created_at")
})
public class FormHistoryEntity {

    public enum EventType {
        CREATED,        // Initial form/NOA creation
        STATUS_CHANGE,  // Status transition (e.g. PENDING → PRINTED)
        COMMENT,        // Free-text note added by staff
        PRINTED,        // Form physically printed
        MAILED,         // Form mailed to recipient
        SUPPRESSED,     // Form suppressed (no mail)
        INACTIVATED,    // Form inactivated
        DOWNLOADED,     // PDF downloaded by staff
        REISSUED,       // Form reissued/reprinted
        BVI_REQUESTED,  // BVI alternate format requested
        HEARING_FILED,  // State hearing requested (NOAs only)
        HEARING_WITHDRAWN // State hearing withdrawn
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Set when this history entry is for a Notice of Action */
    @Column(name = "noa_id")
    private Long noaId;

    /** Set when this history entry is for an Electronic Form */
    @Column(name = "form_id")
    private Long formId;

    /** Case context (always set for traceability) */
    @Column(name = "case_id")
    private Long caseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 30, nullable = false)
    private EventType eventType;

    /** Short summary of the event, e.g. "Status changed from PENDING to PRINTED" */
    @Column(name = "event_summary", length = 500)
    private String eventSummary;

    /** Staff-entered free-text comment (only for EventType.COMMENT) */
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    /** Previous status value before the change (for STATUS_CHANGE events) */
    @Column(name = "previous_status", length = 50)
    private String previousStatus;

    /** New status value after the change (for STATUS_CHANGE events) */
    @Column(name = "new_status", length = 50)
    private String newStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNoaId() { return noaId; }
    public void setNoaId(Long noaId) { this.noaId = noaId; }

    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public String getEventSummary() { return eventSummary; }
    public void setEventSummary(String eventSummary) { this.eventSummary = eventSummary; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
