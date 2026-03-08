package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.DeathMatchEntity;
import com.cmips.repository.DeathMatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Death Match Controller — DSD Section 26.
 * Manages death match records from CDPH, SCO, SSA, and MEDS interfaces.
 */
@RestController
@RequestMapping("/api/death-match")
@CrossOrigin(origins = "*")
public class DeathMatchController {

    private static final Logger log = LoggerFactory.getLogger(DeathMatchController.class);

    private final DeathMatchRepository deathMatchRepository;

    public DeathMatchController(DeathMatchRepository deathMatchRepository) {
        this.deathMatchRepository = deathMatchRepository;
    }

    @GetMapping("/pending")
    @RequirePermission(resource = "Death Match Resource", scope = "view")
    public ResponseEntity<?> getPendingMatches() {
        try {
            return ResponseEntity.ok(deathMatchRepository.findByVerificationStatusOrderByMatchDateDesc("PENDING_VERIFICATION"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/person/{personId}/{personType}")
    @RequirePermission(resource = "Death Match Resource", scope = "view")
    public ResponseEntity<?> getByPerson(@PathVariable Long personId, @PathVariable String personType) {
        try {
            return ResponseEntity.ok(deathMatchRepository.findByPersonIdAndPersonType(personId, personType));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/source/{matchSource}")
    @RequirePermission(resource = "Death Match Resource", scope = "view")
    public ResponseEntity<?> getBySource(@PathVariable String matchSource,
            @RequestParam(defaultValue = "PENDING_VERIFICATION") String status) {
        try {
            return ResponseEntity.ok(deathMatchRepository.findByMatchSourceAndVerificationStatus(matchSource, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "Death Match Resource", scope = "view")
    public ResponseEntity<?> getDeathMatch(@PathVariable Long id) {
        try {
            return deathMatchRepository.findById(id)
                .map(d -> ResponseEntity.ok((Object) d))
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @RequirePermission(resource = "Death Match Resource", scope = "edit")
    public ResponseEntity<?> createDeathMatch(@RequestBody DeathMatchEntity match,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            match.setCreatedBy(userId);
            if (match.getVerificationStatus() == null) {
                match.setVerificationStatus("PENDING_VERIFICATION");
            }
            if (match.getActionTaken() == null) {
                match.setActionTaken("PENDING");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(deathMatchRepository.save(match));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/verify")
    @RequirePermission(resource = "Death Match Resource", scope = "edit")
    public ResponseEntity<?> verifyDeathMatch(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = deathMatchRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var d = existing.get();
            d.setVerificationStatus("VERIFIED");
            d.setVerifiedBy(userId);
            d.setVerifiedDate(LocalDate.now());
            if (body.containsKey("notes")) d.setNotes(body.get("notes"));
            d.setUpdatedBy(userId);
            return ResponseEntity.ok(deathMatchRepository.save(d));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/false-match")
    @RequirePermission(resource = "Death Match Resource", scope = "edit")
    public ResponseEntity<?> markFalseMatch(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = deathMatchRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var d = existing.get();
            d.setVerificationStatus("FALSE_MATCH");
            d.setVerifiedBy(userId);
            d.setVerifiedDate(LocalDate.now());
            d.setActionTaken("NO_ACTION");
            if (body.containsKey("notes")) d.setNotes(body.get("notes"));
            d.setUpdatedBy(userId);
            return ResponseEntity.ok(deathMatchRepository.save(d));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/action")
    @RequirePermission(resource = "Death Match Resource", scope = "edit")
    public ResponseEntity<?> recordAction(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = deathMatchRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var d = existing.get();
            if (body.containsKey("actionTaken")) d.setActionTaken(body.get("actionTaken"));
            d.setActionDate(LocalDate.now());
            if (body.containsKey("notes")) d.setNotes(body.get("notes"));
            d.setUpdatedBy(userId);
            return ResponseEntity.ok(deathMatchRepository.save(d));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
