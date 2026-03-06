package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * WPCS Hours Entity — DSD Section 25, CI-67731
 *
 * Waiver Personal Care Services authorized hours, additional to IHSS.
 * Funded by DHCS waiver: IFO (In-Home Supportive Services Plus Option),
 * PCSP (Personal Care Services Program), or CFCO (Community First Choice Option).
 * Hours stored as minutes (Integer) for HH:MM display.
 */
@Entity
@Table(name = "wpcs_hours", indexes = {
        @Index(name = "idx_wpcs_case", columnList = "case_id")
})
public class WPCSHoursEntity {

    public enum FundingSource { IFO, PCSP, CFCO }
    public enum HoursStatus { ACTIVE, INACTIVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "begin_date", nullable = false)
    private LocalDate beginDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** Authorized hours — stored as minutes */
    @Column(name = "authorized_hours")
    private Integer authorizedHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "funding_source", length = 10)
    private FundingSource fundingSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private HoursStatus status = HoursStatus.ACTIVE;

    @Column(name = "inactivated_date")
    private LocalDate inactivatedDate;

    @Column(name = "inactivated_by", length = 100)
    private String inactivatedBy;

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
    public LocalDate getBeginDate() { return beginDate; }
    public void setBeginDate(LocalDate beginDate) { this.beginDate = beginDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getAuthorizedHours() { return authorizedHours; }
    public void setAuthorizedHours(Integer authorizedHours) { this.authorizedHours = authorizedHours; }
    public FundingSource getFundingSource() { return fundingSource; }
    public void setFundingSource(FundingSource fundingSource) { this.fundingSource = fundingSource; }
    public HoursStatus getStatus() { return status; }
    public void setStatus(HoursStatus status) { this.status = status; }
    public LocalDate getInactivatedDate() { return inactivatedDate; }
    public void setInactivatedDate(LocalDate inactivatedDate) { this.inactivatedDate = inactivatedDate; }
    public String getInactivatedBy() { return inactivatedBy; }
    public void setInactivatedBy(String inactivatedBy) { this.inactivatedBy = inactivatedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
