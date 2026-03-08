package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Report Execution Entity — DSD Section 28
 *
 * Tracks individual executions of report definitions.
 * Records execution parameters, timing, output location, and status.
 * Supports audit trail of all report runs by user and system schedules.
 */
@Entity
@Table(name = "report_executions", indexes = {
    @Index(name = "idx_re_report", columnList = "report_definition_id"),
    @Index(name = "idx_re_status", columnList = "status")
})
public class ReportExecutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_definition_id", nullable = false)
    private Long reportDefinitionId;

    @Column(name = "report_code", length = 30)
    private String reportCode;

    @Column(name = "report_name", length = 200)
    private String reportName;

    @Column(name = "parameters", length = 4000)
    private String parameters;

    @Column(name = "county_code", length = 10)
    private String countyCode;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "output_file_path", length = 500)
    private String outputFilePath;

    @Column(name = "output_file_size")
    private Long outputFileSize;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ReportExecutionEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "RUNNING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReportDefinitionId() { return reportDefinitionId; }
    public void setReportDefinitionId(Long reportDefinitionId) { this.reportDefinitionId = reportDefinitionId; }

    public String getReportCode() { return reportCode; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public String getCountyCode() { return countyCode; }
    public void setCountyCode(String countyCode) { this.countyCode = countyCode; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public Integer getRowCount() { return rowCount; }
    public void setRowCount(Integer rowCount) { this.rowCount = rowCount; }

    public String getOutputFilePath() { return outputFilePath; }
    public void setOutputFilePath(String outputFilePath) { this.outputFilePath = outputFilePath; }

    public Long getOutputFileSize() { return outputFileSize; }
    public void setOutputFileSize(Long outputFileSize) { this.outputFileSize = outputFileSize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
