package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ihss_timesheets")
public class TimesheetEntity {

    // --- Enums ---
    public enum TimesheetType {
        STANDARD,           // SOC 2261
        LARGE_FONT,         // SOC 2261L
        EVV_EXCEPTION,      // SOC 2261EVV
        ADVANCE_PAY,
        SUPPLEMENTAL,
        NEXT_ARREARS,
        LIVE_IN
    }

    public enum TimesheetStatus {
        PENDING_ISSUANCE,
        ISSUED,
        RECEIVED,
        VALIDATING,
        HOLD_EARLY_SUBMISSION,
        HOLD_LATE_SUBMISSION,
        HOLD_BVI_REVIEW,
        HOLD_EXCESSIVE_HOURS,
        HOLD_RANDOM_SAMPLING,
        HOLD_FLAGGED_REVIEW,
        HOLD_USER_REVIEW,
        PENDING_RECIPIENT_REVIEW,
        EXCEPTION,
        APPROVED_FOR_PAYROLL,
        SENT_TO_PAYROLL,
        PROCESSED,
        REJECTED,
        VOID,
        CANCELLED
    }

    public enum SourceType {
        TPF_PAPER,          // Paper scanned via TPF
        ELECTRONIC_ESP,     // Electronic via ESP portal
        MANUAL_ENTRY,       // Staff manual entry
        EVV_MOBILE          // Mobile app EVV
    }

    public enum ProgramType {
        IHSS,
        WPCS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timesheet_number", unique = true, length = 20)
    private String timesheetNumber;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "timesheet_type", length = 30)
    private TimesheetType timesheetType = TimesheetType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40, nullable = false)
    private TimesheetStatus status = TimesheetStatus.PENDING_ISSUANCE;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 30)
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "program_type", length = 10, nullable = false)
    private ProgramType programType = ProgramType.IHSS;

    // --- Pay Period ---
    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;

    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;

    @Column(name = "service_month")
    private String serviceMonth; // e.g. "2026-03"

    // --- Hours ---
    @Column(name = "total_hours_claimed")
    private Double totalHoursClaimed;

    @Column(name = "total_hours_approved")
    private Double totalHoursApproved;

    @Column(name = "authorized_hours_monthly")
    private Double authorizedHoursMonthly;

    @Column(name = "assigned_hours")
    private Double assignedHours;

    @Column(name = "remaining_recipient_hours")
    private Double remainingRecipientHours;

    @Column(name = "remaining_provider_hours")
    private Double remainingProviderHours;

    @Column(name = "overtime_hours")
    private Double overtimeHours;

    @Column(name = "regular_hours")
    private Double regularHours;

    // --- SOC ---
    @Column(name = "soc_deduction_applies")
    private Boolean socDeductionApplies = false;

    @Column(name = "soc_amount")
    private Double socAmount;

    @Column(name = "soc_certified")
    private Boolean socCertified;

    @Column(name = "meds_pos_verified")
    private Boolean medsPosVerified;

    // --- Signatures ---
    @Column(name = "provider_signature_present")
    private Boolean providerSignaturePresent;

    @Column(name = "provider_signature_date")
    private LocalDate providerSignatureDate;

    @Column(name = "recipient_signature_present")
    private Boolean recipientSignaturePresent;

    @Column(name = "recipient_signature_date")
    private LocalDate recipientSignatureDate;

    @Column(name = "recipient_is_bvi")
    private Boolean recipientIsBvi = false;

    // --- Dates ---
    @Column(name = "date_received")
    private LocalDate dateReceived;

    @Column(name = "date_issued")
    private LocalDate dateIssued;

    @Column(name = "date_validated")
    private LocalDateTime dateValidated;

    @Column(name = "date_sent_to_payroll")
    private LocalDateTime dateSentToPayroll;

    @Column(name = "date_processed")
    private LocalDateTime dateProcessed;

    @Column(name = "hold_release_date")
    private LocalDateTime holdReleaseDate;

    @Column(name = "hold_release_by")
    private String holdReleaseBy; // "SYSTEM" or username

    // --- Validation ---
    @Column(name = "has_hard_edit")
    private Boolean hasHardEdit = false;

    @Column(name = "has_soft_edit")
    private Boolean hasSoftEdit = false;

    @Column(name = "has_hold_condition")
    private Boolean hasHoldCondition = false;

    @Column(name = "exception_count")
    private Integer exceptionCount = 0;

    @Column(name = "is_supplemental")
    private Boolean isSupplemental = false;

    @Column(name = "original_timesheet_id")
    private Long originalTimesheetId; // For supplemental timesheets

    // --- Random Sampling ---
    @Column(name = "selected_for_sampling")
    private Boolean selectedForSampling = false;

    @Column(name = "sampling_verified")
    private Boolean samplingVerified;

    @Column(name = "sampling_verified_by")
    private String samplingVerifiedBy;

    // --- Flagged Review ---
    @Column(name = "flagged_for_review")
    private Boolean flaggedForReview = false;

    @Column(name = "review_completed")
    private Boolean reviewCompleted;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    // --- FLSA Overtime ---
    @Column(name = "flsa_overtime_applicable")
    private Boolean flsaOvertimeApplicable = false;

    @Column(name = "flsa_weekly_total_hours")
    private Double flsaWeeklyTotalHours;

    // --- Advance Pay ---
    @Column(name = "is_advance_pay")
    private Boolean isAdvancePay = false;

    @Column(name = "advance_pay_reconciled")
    private Boolean advancePayReconciled;

    // --- TPF fields ---
    @Column(name = "tpf_batch_id", length = 30)
    private String tpfBatchId;

    @Column(name = "tpf_image_id", length = 50)
    private String tpfImageId;

    @Column(name = "mode_of_entry", length = 20)
    private String modeOfEntry; // TPF, ELECTRONIC, MANUAL

    // --- Metadata ---
    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (timesheetNumber == null) {
            timesheetNumber = "TS-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTimesheetNumber() { return timesheetNumber; }
    public void setTimesheetNumber(String timesheetNumber) { this.timesheetNumber = timesheetNumber; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public TimesheetType getTimesheetType() { return timesheetType; }
    public void setTimesheetType(TimesheetType timesheetType) { this.timesheetType = timesheetType; }

    public TimesheetStatus getStatus() { return status; }
    public void setStatus(TimesheetStatus status) { this.status = status; }

    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }

    public ProgramType getProgramType() { return programType; }
    public void setProgramType(ProgramType programType) { this.programType = programType; }

    public LocalDate getPayPeriodStart() { return payPeriodStart; }
    public void setPayPeriodStart(LocalDate payPeriodStart) { this.payPeriodStart = payPeriodStart; }

    public LocalDate getPayPeriodEnd() { return payPeriodEnd; }
    public void setPayPeriodEnd(LocalDate payPeriodEnd) { this.payPeriodEnd = payPeriodEnd; }

    public String getServiceMonth() { return serviceMonth; }
    public void setServiceMonth(String serviceMonth) { this.serviceMonth = serviceMonth; }

    public Double getTotalHoursClaimed() { return totalHoursClaimed; }
    public void setTotalHoursClaimed(Double totalHoursClaimed) { this.totalHoursClaimed = totalHoursClaimed; }

    public Double getTotalHoursApproved() { return totalHoursApproved; }
    public void setTotalHoursApproved(Double totalHoursApproved) { this.totalHoursApproved = totalHoursApproved; }

    public Double getAuthorizedHoursMonthly() { return authorizedHoursMonthly; }
    public void setAuthorizedHoursMonthly(Double authorizedHoursMonthly) { this.authorizedHoursMonthly = authorizedHoursMonthly; }

    public Double getAssignedHours() { return assignedHours; }
    public void setAssignedHours(Double assignedHours) { this.assignedHours = assignedHours; }

    public Double getRemainingRecipientHours() { return remainingRecipientHours; }
    public void setRemainingRecipientHours(Double remainingRecipientHours) { this.remainingRecipientHours = remainingRecipientHours; }

    public Double getRemainingProviderHours() { return remainingProviderHours; }
    public void setRemainingProviderHours(Double remainingProviderHours) { this.remainingProviderHours = remainingProviderHours; }

    public Double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(Double overtimeHours) { this.overtimeHours = overtimeHours; }

    public Double getRegularHours() { return regularHours; }
    public void setRegularHours(Double regularHours) { this.regularHours = regularHours; }

    public Boolean getSocDeductionApplies() { return socDeductionApplies; }
    public void setSocDeductionApplies(Boolean socDeductionApplies) { this.socDeductionApplies = socDeductionApplies; }

    public Double getSocAmount() { return socAmount; }
    public void setSocAmount(Double socAmount) { this.socAmount = socAmount; }

    public Boolean getSocCertified() { return socCertified; }
    public void setSocCertified(Boolean socCertified) { this.socCertified = socCertified; }

    public Boolean getMedsPosVerified() { return medsPosVerified; }
    public void setMedsPosVerified(Boolean medsPosVerified) { this.medsPosVerified = medsPosVerified; }

    public Boolean getProviderSignaturePresent() { return providerSignaturePresent; }
    public void setProviderSignaturePresent(Boolean providerSignaturePresent) { this.providerSignaturePresent = providerSignaturePresent; }

    public LocalDate getProviderSignatureDate() { return providerSignatureDate; }
    public void setProviderSignatureDate(LocalDate providerSignatureDate) { this.providerSignatureDate = providerSignatureDate; }

    public Boolean getRecipientSignaturePresent() { return recipientSignaturePresent; }
    public void setRecipientSignaturePresent(Boolean recipientSignaturePresent) { this.recipientSignaturePresent = recipientSignaturePresent; }

    public LocalDate getRecipientSignatureDate() { return recipientSignatureDate; }
    public void setRecipientSignatureDate(LocalDate recipientSignatureDate) { this.recipientSignatureDate = recipientSignatureDate; }

    public Boolean getRecipientIsBvi() { return recipientIsBvi; }
    public void setRecipientIsBvi(Boolean recipientIsBvi) { this.recipientIsBvi = recipientIsBvi; }

    public LocalDate getDateReceived() { return dateReceived; }
    public void setDateReceived(LocalDate dateReceived) { this.dateReceived = dateReceived; }

    public LocalDate getDateIssued() { return dateIssued; }
    public void setDateIssued(LocalDate dateIssued) { this.dateIssued = dateIssued; }

    public LocalDateTime getDateValidated() { return dateValidated; }
    public void setDateValidated(LocalDateTime dateValidated) { this.dateValidated = dateValidated; }

    public LocalDateTime getDateSentToPayroll() { return dateSentToPayroll; }
    public void setDateSentToPayroll(LocalDateTime dateSentToPayroll) { this.dateSentToPayroll = dateSentToPayroll; }

    public LocalDateTime getDateProcessed() { return dateProcessed; }
    public void setDateProcessed(LocalDateTime dateProcessed) { this.dateProcessed = dateProcessed; }

    public LocalDateTime getHoldReleaseDate() { return holdReleaseDate; }
    public void setHoldReleaseDate(LocalDateTime holdReleaseDate) { this.holdReleaseDate = holdReleaseDate; }

    public String getHoldReleaseBy() { return holdReleaseBy; }
    public void setHoldReleaseBy(String holdReleaseBy) { this.holdReleaseBy = holdReleaseBy; }

    public Boolean getHasHardEdit() { return hasHardEdit; }
    public void setHasHardEdit(Boolean hasHardEdit) { this.hasHardEdit = hasHardEdit; }

    public Boolean getHasSoftEdit() { return hasSoftEdit; }
    public void setHasSoftEdit(Boolean hasSoftEdit) { this.hasSoftEdit = hasSoftEdit; }

    public Boolean getHasHoldCondition() { return hasHoldCondition; }
    public void setHasHoldCondition(Boolean hasHoldCondition) { this.hasHoldCondition = hasHoldCondition; }

    public Integer getExceptionCount() { return exceptionCount; }
    public void setExceptionCount(Integer exceptionCount) { this.exceptionCount = exceptionCount; }

    public Boolean getIsSupplemental() { return isSupplemental; }
    public void setIsSupplemental(Boolean isSupplemental) { this.isSupplemental = isSupplemental; }

    public Long getOriginalTimesheetId() { return originalTimesheetId; }
    public void setOriginalTimesheetId(Long originalTimesheetId) { this.originalTimesheetId = originalTimesheetId; }

    public Boolean getSelectedForSampling() { return selectedForSampling; }
    public void setSelectedForSampling(Boolean selectedForSampling) { this.selectedForSampling = selectedForSampling; }

    public Boolean getSamplingVerified() { return samplingVerified; }
    public void setSamplingVerified(Boolean samplingVerified) { this.samplingVerified = samplingVerified; }

    public String getSamplingVerifiedBy() { return samplingVerifiedBy; }
    public void setSamplingVerifiedBy(String samplingVerifiedBy) { this.samplingVerifiedBy = samplingVerifiedBy; }

    public Boolean getFlaggedForReview() { return flaggedForReview; }
    public void setFlaggedForReview(Boolean flaggedForReview) { this.flaggedForReview = flaggedForReview; }

    public Boolean getReviewCompleted() { return reviewCompleted; }
    public void setReviewCompleted(Boolean reviewCompleted) { this.reviewCompleted = reviewCompleted; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public Boolean getFlsaOvertimeApplicable() { return flsaOvertimeApplicable; }
    public void setFlsaOvertimeApplicable(Boolean flsaOvertimeApplicable) { this.flsaOvertimeApplicable = flsaOvertimeApplicable; }

    public Double getFlsaWeeklyTotalHours() { return flsaWeeklyTotalHours; }
    public void setFlsaWeeklyTotalHours(Double flsaWeeklyTotalHours) { this.flsaWeeklyTotalHours = flsaWeeklyTotalHours; }

    public Boolean getIsAdvancePay() { return isAdvancePay; }
    public void setIsAdvancePay(Boolean isAdvancePay) { this.isAdvancePay = isAdvancePay; }

    public Boolean getAdvancePayReconciled() { return advancePayReconciled; }
    public void setAdvancePayReconciled(Boolean advancePayReconciled) { this.advancePayReconciled = advancePayReconciled; }

    public String getTpfBatchId() { return tpfBatchId; }
    public void setTpfBatchId(String tpfBatchId) { this.tpfBatchId = tpfBatchId; }

    public String getTpfImageId() { return tpfImageId; }
    public void setTpfImageId(String tpfImageId) { this.tpfImageId = tpfImageId; }

    public String getModeOfEntry() { return modeOfEntry; }
    public void setModeOfEntry(String modeOfEntry) { this.modeOfEntry = modeOfEntry; }

    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
