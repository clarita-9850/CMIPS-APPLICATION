package com.cmips.dto;

import java.time.LocalDate;

/**
 * Request DTO for the "Save" action on Enter Warrant Replacement – Details screen (CI-459401).
 */
public class WarrantReplacementSaveRequest {

    private Long warrantId;
    private LocalDate replacementDate;

    public WarrantReplacementSaveRequest() {}

    public Long getWarrantId() { return warrantId; }
    public void setWarrantId(Long warrantId) { this.warrantId = warrantId; }

    public LocalDate getReplacementDate() { return replacementDate; }
    public void setReplacementDate(LocalDate replacementDate) { this.replacementDate = replacementDate; }
}
