package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_queues")
public class WorkQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "queue_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private QueueCategory queueCategory;

    @Column
    private String county;

    @Column(name = "district_office")
    private String districtOffice;

    @Column(name = "organizational_unit")
    private String organizationalUnit;

    @Column(name = "sensitivity_level")
    private int sensitivityLevel = 1;

    @Column(name = "supervisor_only")
    private boolean supervisorOnly = false;

    @Column(name = "administrator")
    private String administrator;

    @Column(name = "subscription_allowed")
    private boolean subscriptionAllowed = true;

    @Column
    private boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public WorkQueue() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public QueueCategory getQueueCategory() { return queueCategory; }
    public void setQueueCategory(QueueCategory queueCategory) { this.queueCategory = queueCategory; }

    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }

    public String getDistrictOffice() { return districtOffice; }
    public void setDistrictOffice(String districtOffice) { this.districtOffice = districtOffice; }

    public String getOrganizationalUnit() { return organizationalUnit; }
    public void setOrganizationalUnit(String organizationalUnit) { this.organizationalUnit = organizationalUnit; }

    public int getSensitivityLevel() { return sensitivityLevel; }
    public void setSensitivityLevel(int sensitivityLevel) { this.sensitivityLevel = sensitivityLevel; }

    public boolean isSupervisorOnly() { return supervisorOnly; }
    public void setSupervisorOnly(boolean supervisorOnly) { this.supervisorOnly = supervisorOnly; }

    public String getAdministrator() { return administrator; }
    public void setAdministrator(String administrator) { this.administrator = administrator; }

    public boolean isSubscriptionAllowed() { return subscriptionAllowed; }
    public void setSubscriptionAllowed(boolean subscriptionAllowed) { this.subscriptionAllowed = subscriptionAllowed; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum QueueCategory {
        CASE_MGMT,
        QA,
        PAYROLL,
        TRAINING,
        PROVIDER,
        INTERNAL_OPS,
        SUPERVISOR
    }
}
