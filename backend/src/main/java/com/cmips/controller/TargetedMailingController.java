package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.TargetedMailingEntity;
import com.cmips.repository.TargetedMailingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Targeted Mailing Controller — DSD Section 26.
 * Manages bulk mailing campaigns to recipients and providers.
 */
@RestController
@RequestMapping("/api/targeted-mailings")
@CrossOrigin(origins = "*")
public class TargetedMailingController {

    private static final Logger log = LoggerFactory.getLogger(TargetedMailingController.class);

    private final TargetedMailingRepository mailingRepository;

    public TargetedMailingController(TargetedMailingRepository mailingRepository) {
        this.mailingRepository = mailingRepository;
    }

    @GetMapping
    @RequirePermission(resource = "Mailing Resource", scope = "view")
    public ResponseEntity<?> getAllMailings(@RequestParam(required = false) String countyCode) {
        try {
            if (countyCode != null) {
                return ResponseEntity.ok(mailingRepository.findByCountyCodeOrderByCreatedAtDesc(countyCode));
            }
            return ResponseEntity.ok(mailingRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    @RequirePermission(resource = "Mailing Resource", scope = "view")
    public ResponseEntity<?> getByStatus(@PathVariable String status) {
        try {
            return ResponseEntity.ok(mailingRepository.findByStatus(status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "Mailing Resource", scope = "view")
    public ResponseEntity<?> getMailing(@PathVariable Long id) {
        try {
            return mailingRepository.findById(id)
                .map(m -> ResponseEntity.ok((Object) m))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @RequirePermission(resource = "Mailing Resource", scope = "edit")
    public ResponseEntity<?> createMailing(@RequestBody TargetedMailingEntity mailing,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            mailing.setCreatedBy(userId);
            if (mailing.getStatus() == null) mailing.setStatus("DRAFT");
            return ResponseEntity.status(HttpStatus.CREATED).body(mailingRepository.save(mailing));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "Mailing Resource", scope = "edit")
    public ResponseEntity<?> updateMailing(@PathVariable Long id,
            @RequestBody TargetedMailingEntity updates,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = mailingRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var m = existing.get();
            if (updates.getMailingName() != null) m.setMailingName(updates.getMailingName());
            if (updates.getTargetCriteria() != null) m.setTargetCriteria(updates.getTargetCriteria());
            if (updates.getDocumentType() != null) m.setDocumentType(updates.getDocumentType());
            if (updates.getLanguage() != null) m.setLanguage(updates.getLanguage());
            if (updates.getScheduledDate() != null) m.setScheduledDate(updates.getScheduledDate());
            m.setUpdatedBy(userId);
            return ResponseEntity.ok(mailingRepository.save(m));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/schedule")
    @RequirePermission(resource = "Mailing Resource", scope = "edit")
    public ResponseEntity<?> scheduleMailing(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = mailingRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var m = existing.get();
            m.setStatus("SCHEDULED");
            if (body.containsKey("scheduledDate")) {
                m.setScheduledDate(LocalDate.parse(body.get("scheduledDate")));
            }
            m.setUpdatedBy(userId);
            return ResponseEntity.ok(mailingRepository.save(m));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/execute")
    @RequirePermission(resource = "Mailing Resource", scope = "edit")
    public ResponseEntity<?> executeMailing(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = mailingRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var m = existing.get();
            m.setStatus("COMPLETED");
            m.setMailedDate(LocalDate.now());
            m.setMailedCount(m.getTotalRecipients() != null ? m.getTotalRecipients() : 0);
            m.setUpdatedBy(userId);
            return ResponseEntity.ok(mailingRepository.save(m));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancel")
    @RequirePermission(resource = "Mailing Resource", scope = "edit")
    public ResponseEntity<?> cancelMailing(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = mailingRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var m = existing.get();
            m.setStatus("CANCELLED");
            m.setUpdatedBy(userId);
            return ResponseEntity.ok(mailingRepository.save(m));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
