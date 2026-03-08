package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.GarnishmentEntity;
import com.cmips.entity.DirectDepositEntity;
import com.cmips.repository.GarnishmentRepository;
import com.cmips.repository.DirectDepositRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Internal Operations Controller — DSD Section 32.
 * Manages garnishment processing and direct deposit support.
 */
@RestController
@RequestMapping("/api/internal-ops")
@CrossOrigin(origins = "*")
public class InternalOperationsController {

    private static final Logger log = LoggerFactory.getLogger(InternalOperationsController.class);

    private final GarnishmentRepository garnishmentRepository;
    private final DirectDepositRepository directDepositRepository;

    public InternalOperationsController(GarnishmentRepository garnishmentRepository,
                                       DirectDepositRepository directDepositRepository) {
        this.garnishmentRepository = garnishmentRepository;
        this.directDepositRepository = directDepositRepository;
    }

    // ==================== GARNISHMENTS (DSD Section 32) ====================

    @GetMapping("/garnishments/providers/{providerId}")
    @RequirePermission(resource = "Internal Ops Resource", scope = "view")
    public ResponseEntity<?> getProviderGarnishments(@PathVariable Long providerId) {
        try {
            return ResponseEntity.ok(garnishmentRepository.findByProviderIdOrderByPriorityAsc(providerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/garnishments/providers/{providerId}/active")
    @RequirePermission(resource = "Internal Ops Resource", scope = "view")
    public ResponseEntity<?> getActiveGarnishments(@PathVariable Long providerId) {
        try {
            return ResponseEntity.ok(garnishmentRepository.findByProviderIdAndStatus(providerId, "ACTIVE"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/garnishments/{id}")
    @RequirePermission(resource = "Internal Ops Resource", scope = "view")
    public ResponseEntity<?> getGarnishment(@PathVariable Long id) {
        try {
            return garnishmentRepository.findById(id)
                .map(g -> ResponseEntity.ok((Object) g))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/garnishments/providers/{providerId}")
    @RequirePermission(resource = "Internal Ops Resource", scope = "edit")
    public ResponseEntity<?> createGarnishment(@PathVariable Long providerId,
            @RequestBody GarnishmentEntity garnishment,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            garnishment.setProviderId(providerId);
            garnishment.setCreatedBy(userId);
            if (garnishment.getStatus() == null) garnishment.setStatus("ACTIVE");
            return ResponseEntity.status(HttpStatus.CREATED).body(garnishmentRepository.save(garnishment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/garnishments/{id}")
    @RequirePermission(resource = "Internal Ops Resource", scope = "edit")
    public ResponseEntity<?> updateGarnishment(@PathVariable Long id,
            @RequestBody GarnishmentEntity updates,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = garnishmentRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var g = existing.get();
            if (updates.getGarnishmentAmount() != null) g.setGarnishmentAmount(updates.getGarnishmentAmount());
            if (updates.getGarnishmentPercentage() != null) g.setGarnishmentPercentage(updates.getGarnishmentPercentage());
            if (updates.getMaxPerPayPeriod() != null) g.setMaxPerPayPeriod(updates.getMaxPerPayPeriod());
            if (updates.getEndDate() != null) g.setEndDate(updates.getEndDate());
            if (updates.getPayeeInfo() != null) g.setPayeeInfo(updates.getPayeeInfo());
            if (updates.getNotes() != null) g.setNotes(updates.getNotes());
            g.setUpdatedBy(userId);
            return ResponseEntity.ok(garnishmentRepository.save(g));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/garnishments/{id}/suspend")
    @RequirePermission(resource = "Internal Ops Resource", scope = "edit")
    public ResponseEntity<?> suspendGarnishment(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = garnishmentRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var g = existing.get();
            g.setStatus("SUSPENDED");
            g.setSuspendedReason(body.get("reason"));
            g.setUpdatedBy(userId);
            return ResponseEntity.ok(garnishmentRepository.save(g));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/garnishments/{id}/satisfy")
    @RequirePermission(resource = "Internal Ops Resource", scope = "edit")
    public ResponseEntity<?> satisfyGarnishment(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = garnishmentRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var g = existing.get();
            g.setStatus("SATISFIED");
            g.setEndDate(LocalDate.now());
            g.setUpdatedBy(userId);
            return ResponseEntity.ok(garnishmentRepository.save(g));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/garnishments/{id}/terminate")
    @RequirePermission(resource = "Internal Ops Resource", scope = "edit")
    public ResponseEntity<?> terminateGarnishment(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = garnishmentRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var g = existing.get();
            g.setStatus("TERMINATED");
            g.setEndDate(LocalDate.now());
            g.setUpdatedBy(userId);
            return ResponseEntity.ok(garnishmentRepository.save(g));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== DIRECT DEPOSIT (DSD Section 32) ====================

    @GetMapping("/direct-deposit/providers/{providerId}")
    @RequirePermission(resource = "Internal Ops Resource", scope = "view")
    public ResponseEntity<?> getProviderDirectDeposits(@PathVariable Long providerId) {
        try {
            return ResponseEntity.ok(directDepositRepository.findByProviderIdOrderByCreatedAtDesc(providerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/direct-deposit/providers/{providerId}/active")
    @RequirePermission(resource = "Internal Ops Resource", scope = "view")
    public ResponseEntity<?> getActiveDirectDeposit(@PathVariable Long providerId) {
        try {
            return ResponseEntity.ok(directDepositRepository.findByProviderIdAndStatus(providerId, "ACTIVE"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/direct-deposit/{id}")
    @RequirePermission(resource = "Internal Ops Resource", scope = "view")
    public ResponseEntity<?> getDirectDeposit(@PathVariable Long id) {
        try {
            return directDepositRepository.findById(id)
                .map(d -> ResponseEntity.ok((Object) d))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/direct-deposit/providers/{providerId}")
    @RequirePermission(resource = "Internal Ops Resource", scope = "edit")
    public ResponseEntity<?> createDirectDeposit(@PathVariable Long providerId,
            @RequestBody DirectDepositEntity deposit,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            deposit.setProviderId(providerId);
            deposit.setCreatedBy(userId);
            if (deposit.getStatus() == null) deposit.setStatus("PENDING_VERIFICATION");
            if (deposit.getPrenoteStatus() == null) deposit.setPrenoteStatus("PENDING");
            return ResponseEntity.status(HttpStatus.CREATED).body(directDepositRepository.save(deposit));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/direct-deposit/{id}/verify-prenote")
    @RequirePermission(resource = "Internal Ops Resource", scope = "edit")
    public ResponseEntity<?> verifyPrenote(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = directDepositRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var d = existing.get();
            d.setPrenoteStatus("VERIFIED");
            d.setPrenoteVerifiedDate(LocalDate.now());
            d.setStatus("ACTIVE");
            d.setUpdatedBy(userId);
            return ResponseEntity.ok(directDepositRepository.save(d));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/direct-deposit/{id}/inactivate")
    @RequirePermission(resource = "Internal Ops Resource", scope = "edit")
    public ResponseEntity<?> inactivateDirectDeposit(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = directDepositRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var d = existing.get();
            d.setStatus("INACTIVE");
            d.setInactivatedDate(LocalDate.now());
            d.setInactivatedBy(userId);
            d.setInactivatedReason(body.get("reason"));
            d.setUpdatedBy(userId);
            return ResponseEntity.ok(directDepositRepository.save(d));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/direct-deposit/pending-prenote")
    @RequirePermission(resource = "Internal Ops Resource", scope = "view")
    public ResponseEntity<?> getPendingPrenotes() {
        try {
            return ResponseEntity.ok(directDepositRepository.findByPrenoteStatus("PENDING"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
