package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Disaster Preparedness Contact Entity — DSD Section 21
 * (DisasterPreparedness.* permission group)
 *
 * Emergency contacts for IHSS recipients — used by counties for disaster
 * response. Tracks who to notify and whether the recipient can self-evacuate.
 */
@Entity
@Table(name = "disaster_preparedness_contact", indexes = {
        @Index(name = "idx_dp_case", columnList = "case_id"),
        @Index(name = "idx_dp_status", columnList = "status")
})
public class DisasterPreparednessContactEntity {

    public enum Relationship {
        SPOUSE_PARTNER,
        CHILD,
        PARENT,
        SIBLING,
        OTHER_FAMILY,
        FRIEND,
        NEIGHBOR,
        PROVIDER,
        SOCIAL_WORKER,
        OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "contact_name", length = 100, nullable = false)
    private String contactName;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", length = 20)
    private Relationship relationship;

    @Column(name = "relationship_other", length = 50)
    private String relationshipOther;

    @Column(name = "primary_phone", length = 20)
    private String primaryPhone;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    @Column(name = "street_address", length = 200)
    private String streetAddress;

    @Column(name = "city", length = 60)
    private String city;

    @Column(name = "state", length = 2)
    private String state;

    @Column(name = "zip", length = 10)
    private String zip;

    /** Can the recipient evacuate independently without assistance? */
    @Column(name = "can_evacuate_independently")
    private Boolean canEvacuateIndependently;

    /** Does the recipient require specialized transportation (wheelchair, etc.)? */
    @Column(name = "requires_specialized_transport")
    private Boolean requiresSpecializedTransport;

    /** Special needs or instructions for emergency responders */
    @Column(name = "special_needs_notes", columnDefinition = "TEXT")
    private String specialNeedsNotes;

    @Column(name = "status", length = 10)
    private String status; // ACTIVE, INACTIVE

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
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public Relationship getRelationship() { return relationship; }
    public void setRelationship(Relationship relationship) { this.relationship = relationship; }

    public String getRelationshipOther() { return relationshipOther; }
    public void setRelationshipOther(String relationshipOther) { this.relationshipOther = relationshipOther; }

    public String getPrimaryPhone() { return primaryPhone; }
    public void setPrimaryPhone(String primaryPhone) { this.primaryPhone = primaryPhone; }

    public String getAlternatePhone() { return alternatePhone; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZip() { return zip; }
    public void setZip(String zip) { this.zip = zip; }

    public Boolean getCanEvacuateIndependently() { return canEvacuateIndependently; }
    public void setCanEvacuateIndependently(Boolean canEvacuateIndependently) { this.canEvacuateIndependently = canEvacuateIndependently; }

    public Boolean getRequiresSpecializedTransport() { return requiresSpecializedTransport; }
    public void setRequiresSpecializedTransport(Boolean requiresSpecializedTransport) { this.requiresSpecializedTransport = requiresSpecializedTransport; }

    public String getSpecialNeedsNotes() { return specialNeedsNotes; }
    public void setSpecialNeedsNotes(String specialNeedsNotes) { this.specialNeedsNotes = specialNeedsNotes; }

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
