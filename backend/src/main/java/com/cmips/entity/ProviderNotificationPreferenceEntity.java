package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Provider Notification Preference — DSD Section 23, Section 10.
 *
 * Tracks provider communication preferences for timesheet reminders,
 * payment confirmations, case changes, and policy updates.
 */
@Entity
@Table(name = "provider_notification_preferences", indexes = {
        @Index(name = "idx_pnp_provider", columnList = "provider_id")
})
public class ProviderNotificationPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    /** EMAIL, TEXT_SMS, PHONE, MAIL */
    @Column(name = "preferred_contact_method", length = 20)
    private String preferredContactMethod;

    @Column(name = "email_notifications_enabled")
    private Boolean emailNotificationsEnabled;

    @Column(name = "sms_notifications_enabled")
    private Boolean smsNotificationsEnabled;

    @Column(name = "phone_notifications_enabled")
    private Boolean phoneNotificationsEnabled;

    @Column(name = "mail_notifications_enabled")
    private Boolean mailNotificationsEnabled;

    @Column(name = "timesheet_reminders")
    private Boolean timesheetReminders;

    @Column(name = "payment_confirmations")
    private Boolean paymentConfirmations;

    @Column(name = "case_assignment_changes")
    private Boolean caseAssignmentChanges;

    @Column(name = "policy_updates")
    private Boolean policyUpdates;

    @Column(name = "training_opportunities")
    private Boolean trainingOpportunities;

    @Column(name = "cell_phone_verified")
    private Boolean cellPhoneVerified;

    @Column(name = "cell_phone_verified_date")
    private LocalDateTime cellPhoneVerifiedDate;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "email_verified_date")
    private LocalDateTime emailVerifiedDate;

    /** TTS, ESP, PAPER */
    @Column(name = "timesheet_method", length = 20)
    private String timesheetMethod;

    @Column(name = "e_timesheet_indicator")
    private Boolean eTimesheetIndicator;

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
        if (emailNotificationsEnabled == null) emailNotificationsEnabled = false;
        if (smsNotificationsEnabled == null) smsNotificationsEnabled = false;
        if (phoneNotificationsEnabled == null) phoneNotificationsEnabled = false;
        if (mailNotificationsEnabled == null) mailNotificationsEnabled = true;
        if (timesheetReminders == null) timesheetReminders = true;
        if (paymentConfirmations == null) paymentConfirmations = true;
        if (caseAssignmentChanges == null) caseAssignmentChanges = true;
        if (policyUpdates == null) policyUpdates = false;
        if (trainingOpportunities == null) trainingOpportunities = false;
        if (cellPhoneVerified == null) cellPhoneVerified = false;
        if (emailVerified == null) emailVerified = false;
        if (eTimesheetIndicator == null) eTimesheetIndicator = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }

    public Boolean getEmailNotificationsEnabled() { return emailNotificationsEnabled; }
    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) { this.emailNotificationsEnabled = emailNotificationsEnabled; }

    public Boolean getSmsNotificationsEnabled() { return smsNotificationsEnabled; }
    public void setSmsNotificationsEnabled(Boolean smsNotificationsEnabled) { this.smsNotificationsEnabled = smsNotificationsEnabled; }

    public Boolean getPhoneNotificationsEnabled() { return phoneNotificationsEnabled; }
    public void setPhoneNotificationsEnabled(Boolean phoneNotificationsEnabled) { this.phoneNotificationsEnabled = phoneNotificationsEnabled; }

    public Boolean getMailNotificationsEnabled() { return mailNotificationsEnabled; }
    public void setMailNotificationsEnabled(Boolean mailNotificationsEnabled) { this.mailNotificationsEnabled = mailNotificationsEnabled; }

    public Boolean getTimesheetReminders() { return timesheetReminders; }
    public void setTimesheetReminders(Boolean timesheetReminders) { this.timesheetReminders = timesheetReminders; }

    public Boolean getPaymentConfirmations() { return paymentConfirmations; }
    public void setPaymentConfirmations(Boolean paymentConfirmations) { this.paymentConfirmations = paymentConfirmations; }

    public Boolean getCaseAssignmentChanges() { return caseAssignmentChanges; }
    public void setCaseAssignmentChanges(Boolean caseAssignmentChanges) { this.caseAssignmentChanges = caseAssignmentChanges; }

    public Boolean getPolicyUpdates() { return policyUpdates; }
    public void setPolicyUpdates(Boolean policyUpdates) { this.policyUpdates = policyUpdates; }

    public Boolean getTrainingOpportunities() { return trainingOpportunities; }
    public void setTrainingOpportunities(Boolean trainingOpportunities) { this.trainingOpportunities = trainingOpportunities; }

    public Boolean getCellPhoneVerified() { return cellPhoneVerified; }
    public void setCellPhoneVerified(Boolean cellPhoneVerified) { this.cellPhoneVerified = cellPhoneVerified; }

    public LocalDateTime getCellPhoneVerifiedDate() { return cellPhoneVerifiedDate; }
    public void setCellPhoneVerifiedDate(LocalDateTime cellPhoneVerifiedDate) { this.cellPhoneVerifiedDate = cellPhoneVerifiedDate; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public LocalDateTime getEmailVerifiedDate() { return emailVerifiedDate; }
    public void setEmailVerifiedDate(LocalDateTime emailVerifiedDate) { this.emailVerifiedDate = emailVerifiedDate; }

    public String getTimesheetMethod() { return timesheetMethod; }
    public void setTimesheetMethod(String timesheetMethod) { this.timesheetMethod = timesheetMethod; }

    public Boolean getETimesheetIndicator() { return eTimesheetIndicator; }
    public void setETimesheetIndicator(Boolean eTimesheetIndicator) { this.eTimesheetIndicator = eTimesheetIndicator; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
