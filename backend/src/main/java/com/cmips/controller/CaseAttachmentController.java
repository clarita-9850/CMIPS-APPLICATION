package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.CaseAttachmentEntity;
import com.cmips.service.CaseAttachmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Case Attachment Controller — DSD Section 25, CI-71055 / CI-67898
 */
@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class CaseAttachmentController {

    private static final Logger log = LoggerFactory.getLogger(CaseAttachmentController.class);

    private final CaseAttachmentService attachmentService;

    public CaseAttachmentController(CaseAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping("/{caseId}/attachments")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<CaseAttachmentEntity>> listAttachments(@PathVariable Long caseId) {
        return ResponseEntity.ok(attachmentService.listAttachments(caseId));
    }

    @PostMapping("/{caseId}/attachments")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<CaseAttachmentEntity> uploadAttachment(
            @PathVariable Long caseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        CaseAttachmentEntity saved = attachmentService.uploadAttachment(
                caseId, file, documentType, description, userId);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/attachments/{id}/archive")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<CaseAttachmentEntity> archiveAttachment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.ok(attachmentService.archiveAttachment(id, userId));
    }

    @PutMapping("/attachments/{id}/restore")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<CaseAttachmentEntity> restoreAttachment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        return ResponseEntity.ok(attachmentService.restoreAttachment(id, userId));
    }

    @GetMapping("/attachments/{id}/download")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<?> downloadAttachment(@PathVariable Long id) {
        CaseAttachmentEntity attachment = attachmentService.getAttachment(id);
        // In production: return file bytes from storage. Mock: return metadata.
        return ResponseEntity.ok(Map.of(
                "id", attachment.getId(),
                "fileName", attachment.getOriginalFileName() != null ? attachment.getOriginalFileName() : "",
                "contentType", attachment.getContentType() != null ? attachment.getContentType() : "application/octet-stream",
                "storageKey", attachment.getStorageKey() != null ? attachment.getStorageKey() : "",
                "message", "File download endpoint — connect to document store for production use."
        ));
    }
}
