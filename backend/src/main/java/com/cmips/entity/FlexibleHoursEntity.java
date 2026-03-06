package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Flexible Hours Entity — DSD Section 25, CI-67807
 *
 * Allows recipients to flex hours week-to-week beyond authorized maximum.
 * Max approvable: 80 hours (4800 minutes) per service month.
 * Can be backdated up to 3 prior calendar months.
 *
 * Hours stored as minutes (Integer) for HH:MM display consistency.
 */
@Entity
@Table(name = "flexible_hours", indexes = {
        @Index(name = "idx_fh_case", columnList = "case_id"),
        @Index(name = "idx_fh_service_month", columnList = "service_month")
})
public class FlexibleHoursEntity {

    public enum Frequency { ONE_TIME, ONGOING }
    public enum Program { IHSS, WPCS }
    public enum FlexStatus { PENDING, APPROVED, DENIED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 10, nullable = false)
    private Frequency frequency;

    /** First day of the service month (e.g. 2026-03-01) */
    @Column(name = "service_month", nullable = false)
    private LocalDate serviceMonth;

    /** Hours requested — stored as minutes */
    @Column(name = "hours_requested")
    private Integer hoursRequested;

    /** Hours approved — stored as minutes */
    @Column(name = "approved_hours")
    private Integer approvedHours;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private FlexStatus status = FlexStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "program", length = 10)
    private Program program;

    /** End date for ONGOING frequency */
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "approved_date")
    private LocalDate approvedDate;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

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
        if (requestDate == null) requestDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }
    public LocalDate getServiceMonth() { return serviceMonth; }
    public void setServiceMonth(LocalDate serviceMonth) { this.serviceMonth = serviceMonth; }
    public Integer getHoursRequested() { return hoursRequested; }
    public void setHoursRequested(Integer hoursRequested) { this.hoursRequested = hoursRequested; }
    public Integer getApprovedHours() { return approvedHours; }
    public void setApprovedHours(Integer approvedHours) { this.approvedHours = approvedHours; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public FlexStatus getStatus() { return status; }
    public void setStatus(FlexStatus status) { this.status = status; }
    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
    public LocalDate getApprovedDate() { return approvedDate; }
    public void setApprovedDate(LocalDate approvedDate) { this.approvedDate = approvedDate; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
