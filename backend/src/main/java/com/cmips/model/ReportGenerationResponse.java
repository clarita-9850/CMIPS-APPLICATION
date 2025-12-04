package com.cmips.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerationResponse {
    private String reportId;
    private String reportType;
    private String userRole;
    private LocalDateTime generatedAt;
    private ReportData data;
    private int totalRecords;
    private String status;
    private String errorMessage;
    private String generatedAtString; // For frontend compatibility
}

