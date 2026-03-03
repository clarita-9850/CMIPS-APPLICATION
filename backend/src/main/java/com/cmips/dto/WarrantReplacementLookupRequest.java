package com.cmips.dto;

import java.time.LocalDate;

/**
 * Request DTO for the "Continue" action on Enter Warrant Replacement screen (CI-459400).
 */
public class WarrantReplacementLookupRequest {

    private LocalDate replacementDate;
    private String warrantNumber;
    private LocalDate issueDate;

    public WarrantReplacementLookupRequest() {}

    public LocalDate getReplacementDate() { return replacementDate; }
    public void setReplacementDate(LocalDate replacementDate) { this.replacementDate = replacementDate; }

    public String getWarrantNumber() { return warrantNumber; }
    public void setWarrantNumber(String warrantNumber) { this.warrantNumber = warrantNumber; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
}
