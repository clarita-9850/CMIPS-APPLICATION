package com.cmips.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportData {
    private String reportType;
    private String userRole;
    private LocalDateTime generatedAt;
    private List<Map<String, Object>> records;
    private Integer totalRecords;
    private Long totalCount; // For pagination
    private Map<String, Object> fieldVisibility;
    private Map<String, Object> statusDistribution;
}

