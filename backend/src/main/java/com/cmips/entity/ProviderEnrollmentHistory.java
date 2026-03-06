package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Provider Enrollment History Entity (BR-87)
 * Records all changes to provider enrollment status for audit trail.
 */
@Entity
@Table(name = "provider_enrollment_history")
public class ProviderEnrollmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "eligible", length = 30)
    private String eligible;

    @Column(name = "ineligible_reason", length = 100)
    private String ineligibleReason;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "soc426_completed")
    private Boolean soc426Completed;

    @Column(name = "soc846_completed")
    private Boolean soc846Completed;

    @Column(name = "orientation_completed")
    private Boolean orientationCompleted;

    @Column(name = "background_check_completed")
    private Boolean backgroundCheckCompleted;

    @Column(name = "ssn_verification_status", length = 50)
    private String ssnVerificationStatus;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "change_description", length = 500)
    private String changeDescription;

    public ProviderEnrollmentHistory() {}

    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getEligible() { return eligible; }
    public void setEligible(String eligible) { this.eligible = eligible; }

    public String getIneligibleReason() { return ineligibleReason; }
    public void setIneligibleReason(String ineligibleReason) { this.ineligibleReason = ineligibleReason; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public Boolean getSoc426Completed() { return soc426Completed; }
    public void setSoc426Completed(Boolean soc426Completed) { this.soc426Completed = soc426Completed; }

    public Boolean getSoc846Completed() { return soc846Completed; }
    public void setSoc846Completed(Boolean soc846Completed) { this.soc846Completed = soc846Completed; }

    public Boolean getOrientationCompleted() { return orientationCompleted; }
    public void setOrientationCompleted(Boolean orientationCompleted) { this.orientationCompleted = orientationCompleted; }

    public Boolean getBackgroundCheckCompleted() { return backgroundCheckCompleted; }
    public void setBackgroundCheckCompleted(Boolean backgroundCheckCompleted) { this.backgroundCheckCompleted = backgroundCheckCompleted; }

    public String getSsnVerificationStatus() { return ssnVerificationStatus; }
    public void setSsnVerificationStatus(String ssnVerificationStatus) { this.ssnVerificationStatus = ssnVerificationStatus; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }
}
