package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Provider Attachment Entity — DSD Section 23 CI-117642 through CI-117650
 *
 * Tracks document uploads for providers (SOC 426, 846, 2305, 2303, 2313 etc.)
 * Supports upload, archive, and restore workflow with nightly batch archival.
 *
 * Allowed file types: PDF, DOC, DOCX, TIF, TIFF, GIF, JPG, JPEG (max 5 MB)
 */
@Entity
@Table(name = "provider_attachments", indexes = {
        @Index(name = "idx_pa_provider", columnList = "provider_id"),
        @Index(name = "idx_pa_status", columnList = "status")
})
public class ProviderAttachmentEntity {

    public enum AttachmentStatus { ACTIVE, ARCHIVED, RESTORED, DELETED }
    public enum DocumentType {
        SOC_426, SOC_426A, SOC_846, SOC_2305, SOC_2303, SOC_2313, OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @Column(name = "document_type", length = 20)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "content_type", length = 50)
    private String contentType;

    /** Stored path/key in document store (e.g. S3 key or filesystem path) */
    @Column(name = "storage_key", length = 500)
    private String storageKey;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private AttachmentStatus status;

    @Column(name = "upload_date")
    private LocalDate uploadDate;

    /** Date archived (by batch or manual) — CI-117644 */
    @Column(name = "archived_date")
    private LocalDate archivedDate;

    @Column(name = "archived_by", length = 100)
    private String archivedBy;

    /** Date restored — CI-117645; only same-day user request allowed */
    @Column(name = "restored_date")
    private LocalDate restoredDate;

    @Column(name = "restored_by", length = 100)
    private String restoredBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public ProviderAttachmentEntity() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = AttachmentStatus.ACTIVE;
        if (uploadDate == null) uploadDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public DocumentType getDocumentType() { return documentType; }
    public void setDocumentType(DocumentType documentType) { this.documentType = documentType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }

    public AttachmentStatus getStatus() { return status; }
    public void setStatus(AttachmentStatus status) { this.status = status; }

    public LocalDate getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDate uploadDate) { this.uploadDate = uploadDate; }

    public LocalDate getArchivedDate() { return archivedDate; }
    public void setArchivedDate(LocalDate archivedDate) { this.archivedDate = archivedDate; }

    public String getArchivedBy() { return archivedBy; }
    public void setArchivedBy(String archivedBy) { this.archivedBy = archivedBy; }

    public LocalDate getRestoredDate() { return restoredDate; }
    public void setRestoredDate(LocalDate restoredDate) { this.restoredDate = restoredDate; }

    public String getRestoredBy() { return restoredBy; }
    public void setRestoredBy(String restoredBy) { this.restoredBy = restoredBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
