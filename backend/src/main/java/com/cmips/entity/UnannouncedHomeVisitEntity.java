package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Unannounced Home Visit Entity — DSD Section 25, CI-718079
 *
 * AB 19 Section 12305.7(A) — Records unannounced home visits to IHSS recipients.
 * Follow-up required if initial visit is unsuccessful.
 * Final unsuccessful outcome may trigger case termination.
 */
@Entity
@Table(name = "unannounced_home_visits", indexes = {
        @Index(name = "idx_uhv_case", columnList = "case_id"),
        @Index(name = "idx_uhv_visit_date", columnList = "visit_date")
})
public class UnannouncedHomeVisitEntity {

    public enum VisitType { INITIAL, FOLLOWUP, FINAL_PHONE, FINAL_HOME }
    public enum VisitOutcome { SUCCESSFUL, UNSUCCESSFUL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "visit_time", length = 10)
    private String visitTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_type", length = 20, nullable = false)
    private VisitType visitType;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 20)
    private VisitOutcome outcome;

    @Column(name = "reason_for_visit", length = 500)
    private String reasonForVisit;

    @Column(name = "findings", length = 2000)
    private String findings;

    @Column(name = "follow_up_required")
    private Boolean followUpRequired;

    @Column(name = "termination_triggered")
    private Boolean terminationTriggered = false;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }
    public String getVisitTime() { return visitTime; }
    public void setVisitTime(String visitTime) { this.visitTime = visitTime; }
    public VisitType getVisitType() { return visitType; }
    public void setVisitType(VisitType visitType) { this.visitType = visitType; }
    public VisitOutcome getOutcome() { return outcome; }
    public void setOutcome(VisitOutcome outcome) { this.outcome = outcome; }
    public String getReasonForVisit() { return reasonForVisit; }
    public void setReasonForVisit(String reasonForVisit) { this.reasonForVisit = reasonForVisit; }
    public String getFindings() { return findings; }
    public void setFindings(String findings) { this.findings = findings; }
    public Boolean getFollowUpRequired() { return followUpRequired; }
    public void setFollowUpRequired(Boolean followUpRequired) { this.followUpRequired = followUpRequired; }
    public Boolean getTerminationTriggered() { return terminationTriggered; }
    public void setTerminationTriggered(Boolean terminationTriggered) { this.terminationTriggered = terminationTriggered; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
