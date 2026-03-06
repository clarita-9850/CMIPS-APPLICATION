package com.ihss.scheduler.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_dependency")
public class JobDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobDefinition job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on_job_id", nullable = false)
    private JobDefinition dependsOnJob;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type")
    private DependencyType dependencyType = DependencyType.SUCCESS;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobDefinition getJob() {
        return job;
    }

    public void setJob(JobDefinition job) {
        this.job = job;
    }

    public JobDefinition getDependsOnJob() {
        return dependsOnJob;
    }

    public void setDependsOnJob(JobDefinition dependsOnJob) {
        this.dependsOnJob = dependsOnJob;
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
