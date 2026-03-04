package com.cmips.dto;

/**
 * Request DTO for Sick Leave Claim Manual Entry "Continue" action (CI-790531).
 */
public class SickLeaveLookupRequest {

    private String providerNumber;
    private String caseNumber;
    private String payPeriodBeginDate; // MM/DD/YYYY or YYYY-MM-DD

    public SickLeaveLookupRequest() {}

    public String getProviderNumber() { return providerNumber; }
    public void setProviderNumber(String providerNumber) { this.providerNumber = providerNumber; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }

    public String getPayPeriodBeginDate() { return payPeriodBeginDate; }
    public void setPayPeriodBeginDate(String payPeriodBeginDate) { this.payPeriodBeginDate = payPeriodBeginDate; }
}
