package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * CaseStatusRescind - Tracks case rescind actions
 * Per DSD Section 7.1 - CaseStatusRescind Table
 */
@Entity
@Table(name = "case_status_rescind")
public class CaseStatusRescind {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_status_rescind_id")
    private Long caseStatusRescindId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "before_rescind_case_status", length = 50)
    private String beforeRescindCaseStatus;

    @Column(name = "after_rescind_case_status", length = 50)
    private String afterRescindCaseStatus;

    @Column(name = "rescind_date")
    private LocalDate rescindDate;

    @Column(name = "rescind_reason", length = 10)
    private String rescindReason;

    @Column(name = "last_medi_cal_eligibility_month", length = 20)
    private String lastMediCalEligibilityMonth;

    @Column(name = "medi_cal_eligibility_status", length = 50)
    private String mediCalEligibilityStatus;

    @Column(name = "noa_generated", length = 50)
    private String noaGenerated;

    public CaseStatusRescind() {}

    // Getters and setters
    public Long getCaseStatusRescindId() { return caseStatusRescindId; }
    public void setCaseStatusRescindId(Long id) { this.caseStatusRescindId = id; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public String getBeforeRescindCaseStatus() { return beforeRescindCaseStatus; }
    public void setBeforeRescindCaseStatus(String s) { this.beforeRescindCaseStatus = s; }
    public String getAfterRescindCaseStatus() { return afterRescindCaseStatus; }
    public void setAfterRescindCaseStatus(String s) { this.afterRescindCaseStatus = s; }
    public LocalDate getRescindDate() { return rescindDate; }
    public void setRescindDate(LocalDate d) { this.rescindDate = d; }
    public String getRescindReason() { return rescindReason; }
    public void setRescindReason(String r) { this.rescindReason = r; }
    public String getLastMediCalEligibilityMonth() { return lastMediCalEligibilityMonth; }
    public void setLastMediCalEligibilityMonth(String m) { this.lastMediCalEligibilityMonth = m; }
    public String getMediCalEligibilityStatus() { return mediCalEligibilityStatus; }
    public void setMediCalEligibilityStatus(String s) { this.mediCalEligibilityStatus = s; }
    public String getNoaGenerated() { return noaGenerated; }
    public void setNoaGenerated(String n) { this.noaGenerated = n; }
}
