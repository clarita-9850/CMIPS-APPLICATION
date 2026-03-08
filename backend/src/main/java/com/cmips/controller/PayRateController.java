package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.PayRateEntity;
import com.cmips.repository.PayRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Pay Rate Controller — DSD Section 16
 *
 * Manages county-level IHSS pay rates by rate type (regular, overtime,
 * wait time, travel, etc.).
 *
 * GET    /api/pay-rates/{countyCode}                        — active rates by county
 * GET    /api/pay-rates/{countyCode}/{rateType}             — active rates by county + type
 * GET    /api/pay-rates/{countyCode}/{rateType}/current     — current (latest) rate
 * POST   /api/pay-rates                                     — create pay rate
 * PUT    /api/pay-rates/{id}                                — update pay rate
 * PUT    /api/pay-rates/{id}/inactivate                     — inactivate pay rate
 */
@RestController
@RequestMapping("/api/pay-rates")
@CrossOrigin(origins = "*")
public class PayRateController {

    private static final Logger logger = LoggerFactory.getLogger(PayRateController.class);

    private final PayRateRepository payRateRepository;

    public PayRateController(PayRateRepository payRateRepository) {
        this.payRateRepository = payRateRepository;
    }

    // ─── List Active Rates by County ────────────────────────────────────────────

    @GetMapping("/{countyCode}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listByCounty(@PathVariable String countyCode) {
        try {
            return ResponseEntity.ok(
                    payRateRepository.findByCountyCodeAndStatusOrderByEffectiveDateDesc(countyCode, "ACTIVE"));
        } catch (Exception e) {
            logger.error("Error listing pay rates for county {}: {}", countyCode, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── List Active Rates by County + Rate Type ────────────────────────────────

    @GetMapping("/{countyCode}/{rateType}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listByCountyAndRateType(
            @PathVariable String countyCode,
            @PathVariable String rateType) {
        try {
            return ResponseEntity.ok(
                    payRateRepository.findByCountyCodeAndRateTypeAndStatus(countyCode, rateType, "ACTIVE"));
        } catch (Exception e) {
            logger.error("Error listing pay rates for county {}, type {}: {}", countyCode, rateType, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Current (Latest Effective) Rate ────────────────────────────────────────

    @GetMapping("/{countyCode}/{rateType}/current")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> getCurrentRate(
            @PathVariable String countyCode,
            @PathVariable String rateType) {
        try {
            return payRateRepository
                    .findFirstByCountyCodeAndRateTypeAndStatusOrderByEffectiveDateDesc(countyCode, rateType, "ACTIVE")
                    .map(rate -> ResponseEntity.ok((Object) rate))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching current pay rate for county {}, type {}: {}", countyCode, rateType, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Create ─────────────────────────────────────────────────────────────────

    @PostMapping
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> create(
            @RequestBody PayRateEntity payRate,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            payRate.setStatus("ACTIVE");
            payRate.setCreatedBy(userId);
            payRate.setCreatedAt(LocalDateTime.now());
            PayRateEntity saved = payRateRepository.save(payRate);
            logger.info("Created pay rate {} for county {} type {} by user {}",
                    saved.getId(), saved.getCountyCode(), saved.getRateType(), userId);
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            logger.error("Error creating pay rate: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Update ─────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody PayRateEntity updates,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            PayRateEntity existing = payRateRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Pay rate not found: " + id));
            if (updates.getHourlyRate() != null) existing.setHourlyRate(updates.getHourlyRate());
            if (updates.getEffectiveDate() != null) existing.setEffectiveDate(updates.getEffectiveDate());
            if (updates.getEndDate() != null) existing.setEndDate(updates.getEndDate());
            if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
            existing.setUpdatedBy(userId);
            existing.setUpdatedAt(LocalDateTime.now());
            payRateRepository.save(existing);
            logger.info("Updated pay rate {} by user {}", id, userId);
            return ResponseEntity.ok(existing);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating pay rate {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Inactivate ─────────────────────────────────────────────────────────────

    @PutMapping("/{id}/inactivate")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> inactivate(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            PayRateEntity payRate = payRateRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Pay rate not found: " + id));
            payRate.setStatus("INACTIVE");
            payRate.setUpdatedBy(userId);
            payRate.setUpdatedAt(LocalDateTime.now());
            payRateRepository.save(payRate);
            logger.info("Inactivated pay rate {} by user {}", id, userId);
            return ResponseEntity.ok(payRate);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inactivating pay rate {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
