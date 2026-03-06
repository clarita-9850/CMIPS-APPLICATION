package com.cmips.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Case Attachment Entity — DSD Section 25, CI-71055 / CI-67898
 *
 * Tracks document uploads for case records.
 * Allowed file types: PDF, DOC, DOCX, TIF, TIFF, GIF, JPG, JPEG (max 5 MB)
 * Status lifecycle: ACTIVE -> ARCHIVED -> RESTORED -> ARCHIVED
 */
@Entity
@Table(name = "case_attachments", indexes = {
        @Index(name = "idx_ca_case", columnList = "case_id"),
        @Index(name = "idx_ca_status", columnList = "status")
})
public class CaseAttachmentEntity {

    public enum AttachmentStatus { ACTIVE, ARCHIVED, RESTORED, DELETED }
    public enum DocumentType {
        SOC_426, SOC_426A, SOC_846, SOC_2305, SOC_2303, SOC_2313, OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "case_number", length = 20)
    private String caseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 20)
    private DocumentType documentType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "storage_key", length = 500)
    private String storageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private AttachmentStatus status;

    @Column(name = "upload_date")
    private LocalDate uploadDate;

    @Column(name = "archived_date")
    private LocalDate archivedDate;

    @Column(name = "archived_by", length = 100)
    private String archivedBy;

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCaseId() { return caseId; }
    public void setCaseId(Long caseId) { this.caseId = caseId; }
    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
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
