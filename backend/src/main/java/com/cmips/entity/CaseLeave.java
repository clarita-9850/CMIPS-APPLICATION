package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * CaseLeave - Tracks case leave details
 * Per DSD Section 7.2 - CaseLeave Table
 */
@Entity
@Table(name = "case_leave")
public class CaseLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_leave_id")
    private Long caseLeaveId;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "case_status_id")
    private Long caseStatusId;

    @Column(name = "authorization_end_date")
    private LocalDate authorizationEndDate;

    @Column(name = "resource_suspension_end_date")
    private LocalDate resourceSuspensionEndDate;

    @Column(name = "leave_reason", length = 10)
    private String leaveReason;

    @Column(name = "leave_date")
    private LocalDate leaveDate;

    public CaseLeave() {}

    // Getters and setters
    public Long getCaseLeaveId() { return caseLeaveId; }
    public void setCaseLeaveId(Long id) { this.caseLeaveId = id; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public Long getCaseStatusId() { return caseStatusId; }
    public void setCaseStatusId(Long caseStatusId) { this.caseStatusId = caseStatusId; }
    public LocalDate getAuthorizationEndDate() { return authorizationEndDate; }
    public void setAuthorizationEndDate(LocalDate d) { this.authorizationEndDate = d; }
    public LocalDate getResourceSuspensionEndDate() { return resourceSuspensionEndDate; }
    public void setResourceSuspensionEndDate(LocalDate d) { this.resourceSuspensionEndDate = d; }
    public String getLeaveReason() { return leaveReason; }
    public void setLeaveReason(String r) { this.leaveReason = r; }
    public LocalDate getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDate d) { this.leaveDate = d; }
}
