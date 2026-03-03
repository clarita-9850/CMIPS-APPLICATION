package com.cmips.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for Enter Warrant Replacement – Details screen (CI-459401).
 * Contains all sections: Payee, Case, Payment/Warrant Info, Pay Event.
 */
public class WarrantReplacementDetailResponse {

    private Long warrantId;

    // Payee section
    private String payeeNumber;
    private String payeeName;

    // Case section
    private String caseNumber;
    private String recipientName;
    private String county;

    // Payment / Warrant Information
    private String warrantNumber;
    private BigDecimal netAmount;
    private LocalDate issueDate;
    private String fundingSource;
    private LocalDate replacementDate;

    // Pay Event
    private String payType;
    private LocalDate payPeriodFrom;
    private LocalDate payPeriodTo;

    // Status info
    private String status;
    private String voidReplacementType;
    private String voidReplacementReason;

    public WarrantReplacementDetailResponse() {}

    public Long getWarrantId() { return warrantId; }
    public void setWarrantId(Long warrantId) { this.warrantId = warrantId; }

    public String getPayeeNumber() { return payeeNumber; }
    public void setPayeeNumber(String payeeNumber) { this.payeeNumber = payeeNumber; }

    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }

    public String getWarrantNumber() { return warrantNumber; }
    public void setWarrantNumber(String warrantNumber) { this.warrantNumber = warrantNumber; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public String getFundingSource() { return fundingSource; }
    public void setFundingSource(String fundingSource) { this.fundingSource = fundingSource; }

    public LocalDate getReplacementDate() { return replacementDate; }
    public void setReplacementDate(LocalDate replacementDate) { this.replacementDate = replacementDate; }

    public String getPayType() { return payType; }
    public void setPayType(String payType) { this.payType = payType; }

    public LocalDate getPayPeriodFrom() { return payPeriodFrom; }
    public void setPayPeriodFrom(LocalDate payPeriodFrom) { this.payPeriodFrom = payPeriodFrom; }

    public LocalDate getPayPeriodTo() { return payPeriodTo; }
    public void setPayPeriodTo(LocalDate payPeriodTo) { this.payPeriodTo = payPeriodTo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVoidReplacementType() { return voidReplacementType; }
    public void setVoidReplacementType(String voidReplacementType) { this.voidReplacementType = voidReplacementType; }

    public String getVoidReplacementReason() { return voidReplacementReason; }
    public void setVoidReplacementReason(String voidReplacementReason) { this.voidReplacementReason = voidReplacementReason; }
}
