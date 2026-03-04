package com.cmips.dto;

/**
 * Request DTO for IRS Live-In Provider Self-Certification Entry "Save" action (CI-718024).
 */
public class LiveInCertSaveRequest {

    private Long providerId;
    private Long caseId;
    private String certificationStatus;

    public LiveInCertSaveRequest() {}

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getCertificationStatus() { return certificationStatus; }
    public void setCertificationStatus(String certificationStatus) { this.certificationStatus = certificationStatus; }
}
