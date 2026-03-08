package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Report Definition Entity — DSD Section 28
 *
 * Defines available reports in the CMIPS reporting system.
 * Categories: CASE_MANAGEMENT, PAYROLL, PROVIDER, TIMESHEET, ADMINISTRATIVE, FRAUD, STATISTICAL
 * Supports scheduled execution via cron expressions and on-demand runs.
 * Output formats: PDF, CSV, EXCEL
 */
@Entity
@Table(name = "report_definitions",
    indexes = {
        @Index(name = "idx_rd_category", columnList = "report_category")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_rd_code", columnNames = "report_code")
    }
)
public class ReportDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_code", nullable = false, length = 30, unique = true)
    private String reportCode;

    @Column(name = "report_name", nullable = false, length = 200)
    private String reportName;

    @Column(name = "report_category", nullable = false, length = 50)
    private String reportCategory;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "frequency", length = 30)
    private String frequency;

    @Column(name = "output_format", length = 20)
    private String outputFormat;

    @Column(name = "template_path", length = 500)
    private String templatePath;

    @Column(name = "parameters", length = 4000)
    private String parameters;

    @Column(name = "last_run_date")
    private LocalDateTime lastRunDate;

    @Column(name = "last_run_by", length = 100)
    private String lastRunBy;

    @Column(name = "last_run_status", length = 20)
    private String lastRunStatus;

    @Column(name = "schedule_enabled")
    private Boolean scheduleEnabled;

    @Column(name = "schedule_cron", length = 100)
    private String scheduleCron;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public ReportDefinitionEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "ACTIVE";
        if (scheduleEnabled == null) scheduleEnabled = Boolean.FALSE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReportCode() { return reportCode; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public String getReportCategory() { return reportCategory; }
    public void setReportCategory(String reportCategory) { this.reportCategory = reportCategory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    public String getTemplatePath() { return templatePath; }
    public void setTemplatePath(String templatePath) { this.templatePath = templatePath; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }

    public LocalDateTime getLastRunDate() { return lastRunDate; }
    public void setLastRunDate(LocalDateTime lastRunDate) { this.lastRunDate = lastRunDate; }

    public String getLastRunBy() { return lastRunBy; }
    public void setLastRunBy(String lastRunBy) { this.lastRunBy = lastRunBy; }

    public String getLastRunStatus() { return lastRunStatus; }
    public void setLastRunStatus(String lastRunStatus) { this.lastRunStatus = lastRunStatus; }

    public Boolean getScheduleEnabled() { return scheduleEnabled; }
    public void setScheduleEnabled(Boolean scheduleEnabled) { this.scheduleEnabled = scheduleEnabled; }

    public String getScheduleCron() { return scheduleCron; }
    public void setScheduleCron(String scheduleCron) { this.scheduleCron = scheduleCron; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
