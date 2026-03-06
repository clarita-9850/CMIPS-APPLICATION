package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Household Evidence Entity — DSD Section 21, BR SE 26-27
 *
 * Tracks household composition, living arrangements, and companion case links
 * for IHSS eligibility determination. Companion cases are identified when
 * another active case shares the same address (BR SE 26-27).
 */
@Entity
@Table(name = "household_evidence", indexes = {
        @Index(name = "idx_he_case", columnList = "case_id"),
        @Index(name = "idx_he_status", columnList = "status")
})
public class HouseholdEvidenceEntity {

    public enum LivingArrangement {
        LIVES_ALONE,
        WITH_SPOUSE_PARTNER,
        WITH_FAMILY_MEMBER,
        WITH_NON_FAMILY,
        BOARD_AND_CARE,
        ASSISTED_LIVING,
        OTHER
    }

    public enum HousingType {
        OWN_HOME,
        RENTS,
        LIVES_WITH_OTHERS_FREE,
        BOARD_AND_CARE_FACILITY,
        OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "recipient_id")
    private Long recipientId;

    /** Total number of people in the household including recipient */
    @Column(name = "number_of_members")
    private Integer numberOfMembers;

    @Enumerated(EnumType.STRING)
    @Column(name = "living_arrangement", length = 30)
    private LivingArrangement livingArrangement;

    @Enumerated(EnumType.STRING)
    @Column(name = "housing_type", length = 30)
    private HousingType housingType;

    /** BR SE 26: Companion case flag — another active IHSS case at the same address */
    @Column(name = "has_companion_case")
    private Boolean hasCompanionCase;

    /** BR SE 27: Companion case ID (if hasCompanionCase = true) */
    @Column(name = "companion_case_id")
    private Long companionCaseId;

    /** Address used for companion-case detection (stored for audit) */
    @Column(name = "shared_address", length = 200)
    private String sharedAddress;

    /** Whether recipient has live-in provider (affects OT rules) */
    @Column(name = "has_live_in_provider")
    private Boolean hasLiveInProvider;

    /** Recipient lives alone — no one else in household */
    @Column(name = "lives_alone")
    private Boolean livesAlone;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "evidence_status", length = 20)
    private String status; // PENDING, ACTIVE, INACTIVE

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

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

    public Integer getNumberOfMembers() { return numberOfMembers; }
    public void setNumberOfMembers(Integer numberOfMembers) { this.numberOfMembers = numberOfMembers; }

    public LivingArrangement getLivingArrangement() { return livingArrangement; }
    public void setLivingArrangement(LivingArrangement livingArrangement) { this.livingArrangement = livingArrangement; }

    public HousingType getHousingType() { return housingType; }
    public void setHousingType(HousingType housingType) { this.housingType = housingType; }

    public Boolean getHasCompanionCase() { return hasCompanionCase; }
    public void setHasCompanionCase(Boolean hasCompanionCase) { this.hasCompanionCase = hasCompanionCase; }

    public Long getCompanionCaseId() { return companionCaseId; }
    public void setCompanionCaseId(Long companionCaseId) { this.companionCaseId = companionCaseId; }

    public String getSharedAddress() { return sharedAddress; }
    public void setSharedAddress(String sharedAddress) { this.sharedAddress = sharedAddress; }

    public Boolean getHasLiveInProvider() { return hasLiveInProvider; }
    public void setHasLiveInProvider(Boolean hasLiveInProvider) { this.hasLiveInProvider = hasLiveInProvider; }

    public Boolean getLivesAlone() { return livesAlone; }
    public void setLivesAlone(Boolean livesAlone) { this.livesAlone = livesAlone; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
