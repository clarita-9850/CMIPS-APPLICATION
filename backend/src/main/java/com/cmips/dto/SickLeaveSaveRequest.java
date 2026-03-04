package com.cmips.dto;

import java.util.List;

/**
 * Request DTO for Sick Leave Claim Time Entries "Save" action (CI-790532).
 */
public class SickLeaveSaveRequest {

    private Long providerId;
    private Long caseId;
    private String payPeriodBeginDate; // YYYY-MM-DD
    private List<TimeEntry> timeEntries;

    public SickLeaveSaveRequest() {}

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }

    public String getPayPeriodBeginDate() { return payPeriodBeginDate; }
    public void setPayPeriodBeginDate(String payPeriodBeginDate) { this.payPeriodBeginDate = payPeriodBeginDate; }

    public List<TimeEntry> getTimeEntries() { return timeEntries; }
    public void setTimeEntries(List<TimeEntry> timeEntries) { this.timeEntries = timeEntries; }

    public static class TimeEntry {
        private String date;   // YYYY-MM-DD
        private int minutes;   // total minutes for that day

        public TimeEntry() {}

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public int getMinutes() { return minutes; }
        public void setMinutes(int minutes) { this.minutes = minutes; }
    }
}
