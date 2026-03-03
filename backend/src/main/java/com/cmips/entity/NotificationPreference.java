package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * DSD GAP 8 — Notification Preference entity.
 * <p>
 * Per DSD Section 5, users can configure:
 * - Which notification types they want to see
 * - Delivery channel (in-app, email)
 * - Alert frequency (immediate, daily digest, weekly digest)
 */
@Entity
@Table(name = "notification_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "notification_category"}))
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "notification_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationCategory notificationCategory;

    @Column(name = "in_app_enabled")
    private boolean inAppEnabled = true;

    @Column(name = "email_enabled")
    private boolean emailEnabled = false;

    @Column(name = "frequency")
    @Enumerated(EnumType.STRING)
    private Frequency frequency = Frequency.IMMEDIATE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public NotificationPreference() {}

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public NotificationCategory getNotificationCategory() { return notificationCategory; }
    public void setNotificationCategory(NotificationCategory notificationCategory) { this.notificationCategory = notificationCategory; }

    public boolean isInAppEnabled() { return inAppEnabled; }
    public void setInAppEnabled(boolean inAppEnabled) { this.inAppEnabled = inAppEnabled; }

    public boolean isEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }

    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum NotificationCategory {
        CASE_MAINTENANCE,
        TASK_ASSIGNMENT,
        TASK_ESCALATION,
        TASK_FORWARDED,
        CASE_STATUS_CHANGE,
        INTERFACE_UPDATE,
        SUPERVISOR_ALERT,
        SYSTEM
    }

    public enum Frequency {
        IMMEDIATE,
        DAILY_DIGEST,
        WEEKLY_DIGEST
    }
}
