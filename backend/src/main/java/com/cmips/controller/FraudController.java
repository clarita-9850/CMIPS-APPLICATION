package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.FraudCaseEntity;
import com.cmips.repository.FraudCaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Fraud Case Management Controller — DSD Section 26.
 * Manages fraud investigations, referrals, and outcomes.
 */
@RestController
@RequestMapping("/api/fraud")
@CrossOrigin(origins = "*")
public class FraudController {

    private static final Logger log = LoggerFactory.getLogger(FraudController.class);

    private final FraudCaseRepository fraudCaseRepository;

    public FraudController(FraudCaseRepository fraudCaseRepository) {
        this.fraudCaseRepository = fraudCaseRepository;
    }

    @GetMapping("/cases/{caseId}")
    @RequirePermission(resource = "Fraud Resource", scope = "view")
    public ResponseEntity<?> getFraudCasesByCaseId(@PathVariable Long caseId) {
        try {
            return ResponseEntity.ok(fraudCaseRepository.findByCaseIdOrderByCreatedAtDesc(caseId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/providers/{providerId}")
    @RequirePermission(resource = "Fraud Resource", scope = "view")
    public ResponseEntity<?> getFraudCasesByProvider(@PathVariable Long providerId) {
        try {
            return ResponseEntity.ok(fraudCaseRepository.findByProviderIdOrderByCreatedAtDesc(providerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    @RequirePermission(resource = "Fraud Resource", scope = "view")
    public ResponseEntity<?> getFraudCasesByStatus(@PathVariable String status) {
        try {
            return ResponseEntity.ok(fraudCaseRepository.findByInvestigationStatus(status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "Fraud Resource", scope = "view")
    public ResponseEntity<?> getFraudCase(@PathVariable Long id) {
        try {
            return fraudCaseRepository.findById(id)
                .map(f -> ResponseEntity.ok((Object) f))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @RequirePermission(resource = "Fraud Resource", scope = "edit")
    public ResponseEntity<?> createFraudCase(@RequestBody FraudCaseEntity fraudCase,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            fraudCase.setCreatedBy(userId);
            if (fraudCase.getInvestigationStatus() == null) {
                fraudCase.setInvestigationStatus("REPORTED");
            }
            if (fraudCase.getStatus() == null) {
                fraudCase.setStatus("ACTIVE");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(fraudCaseRepository.save(fraudCase));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "Fraud Resource", scope = "edit")
    public ResponseEntity<?> updateFraudCase(@PathVariable Long id,
            @RequestBody FraudCaseEntity updates,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = fraudCaseRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var f = existing.get();
            if (updates.getInvestigationStatus() != null) f.setInvestigationStatus(updates.getInvestigationStatus());
            if (updates.getInvestigatorName() != null) f.setInvestigatorName(updates.getInvestigatorName());
            if (updates.getInvestigatorId() != null) f.setInvestigatorId(updates.getInvestigatorId());
            if (updates.getInvestigationStartDate() != null) f.setInvestigationStartDate(updates.getInvestigationStartDate());
            if (updates.getFindingSummary() != null) f.setFindingSummary(updates.getFindingSummary());
            if (updates.getAmountInvolved() != null) f.setAmountInvolved(updates.getAmountInvolved());
            if (updates.getRecoveryAmount() != null) f.setRecoveryAmount(updates.getRecoveryAmount());
            f.setUpdatedBy(userId);
            return ResponseEntity.ok(fraudCaseRepository.save(f));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/substantiate")
    @RequirePermission(resource = "Fraud Resource", scope = "edit")
    public ResponseEntity<?> substantiate(@PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = fraudCaseRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var f = existing.get();
            f.setInvestigationStatus("SUBSTANTIATED");
            f.setInvestigationEndDate(LocalDate.now());
            if (body.containsKey("findingSummary")) f.setFindingSummary((String) body.get("findingSummary"));
            f.setUpdatedBy(userId);
            return ResponseEntity.ok(fraudCaseRepository.save(f));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/refer-to-da")
    @RequirePermission(resource = "Fraud Resource", scope = "edit")
    public ResponseEntity<?> referToDA(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = fraudCaseRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var f = existing.get();
            f.setInvestigationStatus("REFERRED_TO_DA");
            f.setReferredToDA(true);
            f.setDaReferralDate(LocalDate.now());
            if (body.containsKey("daCaseNumber")) f.setDaCaseNumber(body.get("daCaseNumber"));
            f.setUpdatedBy(userId);
            return ResponseEntity.ok(fraudCaseRepository.save(f));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/close")
    @RequirePermission(resource = "Fraud Resource", scope = "edit")
    public ResponseEntity<?> closeFraudCase(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = fraudCaseRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var f = existing.get();
            f.setInvestigationStatus("CLOSED");
            f.setStatus("CLOSED");
            f.setInvestigationEndDate(LocalDate.now());
            f.setUpdatedBy(userId);
            return ResponseEntity.ok(fraudCaseRepository.save(f));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/investigator/{investigatorId}")
    @RequirePermission(resource = "Fraud Resource", scope = "view")
    public ResponseEntity<?> getByInvestigator(@PathVariable String investigatorId) {
        try {
            return ResponseEntity.ok(fraudCaseRepository.findByInvestigatorIdAndInvestigationStatusNot(investigatorId, "CLOSED"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
