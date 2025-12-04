package com.cmips.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResult {
    private String jobId;
    private String status;
    private String resultPath;
    private Long totalRecords;
    private Long processedRecords;
    private String dataFormat;
    private LocalDateTime completedAt;
}

