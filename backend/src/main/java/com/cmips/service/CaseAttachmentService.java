package com.cmips.service;

import com.cmips.entity.CaseAttachmentEntity;
import com.cmips.entity.CaseAttachmentEntity.AttachmentStatus;
import com.cmips.entity.CaseAttachmentEntity.DocumentType;
import com.cmips.repository.CaseAttachmentRepository;
import com.cmips.repository.CaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Case Attachment Service — DSD Section 25, CI-71055 / CI-67898
 *
 * Handles upload, archive, restore, and download of case documents.
 * Mirrors ProviderAttachmentService pattern.
 * Allowed types: PDF, DOC, DOCX, TIF, TIFF, GIF, JPG, JPEG (max 5 MB)
 */
@Service
public class CaseAttachmentService {

    private static final Logger log = LoggerFactory.getLogger(CaseAttachmentService.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/tiff", "image/gif", "image/jpeg"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "tif", "tiff", "gif", "jpg", "jpeg"
    );

    private final CaseAttachmentRepository attachmentRepository;
    private final CaseRepository caseRepository;

    public CaseAttachmentService(CaseAttachmentRepository attachmentRepository,
                                  CaseRepository caseRepository) {
        this.attachmentRepository = attachmentRepository;
        this.caseRepository = caseRepository;
    }

    public List<CaseAttachmentEntity> listAttachments(Long caseId) {
        return attachmentRepository.findByCaseIdOrderByUploadDateDesc(caseId);
    }

    @Transactional
    public CaseAttachmentEntity uploadAttachment(Long caseId, MultipartFile file,
                                                  String documentTypeStr, String description,
                                                  String uploadedBy) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5 MB limit.");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String extension = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed: PDF, DOC, DOCX, TIF, TIFF, GIF, JPG, JPEG.");
        }

        String caseNumber = caseRepository.findById(caseId)
                .map(c -> c.getCaseNumber())
                .orElse(null);

        DocumentType documentType = DocumentType.OTHER;
        if (documentTypeStr != null) {
            try { documentType = DocumentType.valueOf(documentTypeStr); } catch (Exception ignored) {}
        }

        // Mock storage key — in production this would be an S3/filesystem path
        String storageKey = "cases/" + caseId + "/" + System.currentTimeMillis() + "_" + originalName;

        CaseAttachmentEntity attachment = new CaseAttachmentEntity();
        attachment.setCaseId(caseId);
        attachment.setCaseNumber(caseNumber);
        attachment.setDocumentType(documentType);
        attachment.setDescription(description);
        attachment.setOriginalFileName(originalName);
        attachment.setFileSizeBytes(file.getSize());
        attachment.setContentType(file.getContentType());
        attachment.setStorageKey(storageKey);
        attachment.setStatus(AttachmentStatus.ACTIVE);
        attachment.setUploadDate(LocalDate.now());
        attachment.setCreatedBy(uploadedBy);
        attachment.setUpdatedBy(uploadedBy);

        CaseAttachmentEntity saved = attachmentRepository.save(attachment);
        log.info("[CaseAttachment] Uploaded: caseId={}, id={}, file={}", caseId, saved.getId(), originalName);
        return saved;
    }

    @Transactional
    public CaseAttachmentEntity archiveAttachment(Long id, String archivedBy) {
        CaseAttachmentEntity attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found: " + id));

        if (attachment.getStatus() == AttachmentStatus.ARCHIVED) {
            throw new RuntimeException("Attachment is already archived.");
        }
        if (attachment.getStatus() == AttachmentStatus.DELETED) {
            throw new RuntimeException("Deleted attachments cannot be archived.");
        }

        attachment.setStatus(AttachmentStatus.ARCHIVED);
        attachment.setArchivedDate(LocalDate.now());
        attachment.setArchivedBy(archivedBy);
        attachment.setUpdatedBy(archivedBy);

        log.info("[CaseAttachment] Archived: id={}", id);
        return attachmentRepository.save(attachment);
    }

    @Transactional
    public CaseAttachmentEntity restoreAttachment(Long id, String restoredBy) {
        CaseAttachmentEntity attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found: " + id));

        if (attachment.getStatus() != AttachmentStatus.ARCHIVED) {
            throw new RuntimeException("Only archived attachments can be restored.");
        }

        attachment.setStatus(AttachmentStatus.RESTORED);
        attachment.setRestoredDate(LocalDate.now());
        attachment.setRestoredBy(restoredBy);
        attachment.setUpdatedBy(restoredBy);

        log.info("[CaseAttachment] Restored: id={}", id);
        return attachmentRepository.save(attachment);
    }

    public CaseAttachmentEntity getAttachment(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found: " + id));
    }
}
