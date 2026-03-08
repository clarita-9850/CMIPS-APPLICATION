package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.TaxContributionEntity;
import com.cmips.repository.TaxContributionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Tax Contribution Controller — DSD Section 18
 *
 * Manages provider tax records, W-4/DE-4 withholding elections,
 * quarterly tax contributions, and W-2 generation tracking.
 *
 * GET    /api/tax/providers/{providerId}                        — list by provider
 * GET    /api/tax/providers/{providerId}/{year}                 — by provider + year
 * GET    /api/tax/providers/{providerId}/{year}/{quarter}       — by provider + year + quarter
 * POST   /api/tax/providers/{providerId}                        — create tax record
 * PUT    /api/tax/{id}                                          — update tax record
 * PUT    /api/tax/{id}/generate-w2                              — mark W-2 generated
 * GET    /api/tax/w2-pending/{year}                             — W-2 pending for year
 */
@RestController
@RequestMapping("/api/tax")
@CrossOrigin(origins = "*")
public class TaxContributionController {

    private static final Logger logger = LoggerFactory.getLogger(TaxContributionController.class);

    private final TaxContributionRepository taxContributionRepository;

    public TaxContributionController(TaxContributionRepository taxContributionRepository) {
        this.taxContributionRepository = taxContributionRepository;
    }

    // ─── List by Provider ───────────────────────────────────────────────────────

    @GetMapping("/providers/{providerId}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listByProviderId(@PathVariable Long providerId) {
        try {
            return ResponseEntity.ok(
                    taxContributionRepository.findByProviderIdOrderByTaxYearDescTaxQuarterDesc(providerId));
        } catch (Exception e) {
            logger.error("Error listing tax records for provider {}: {}", providerId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── By Provider + Year ─────────────────────────────────────────────────────

    @GetMapping("/providers/{providerId}/{year}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listByProviderAndYear(
            @PathVariable Long providerId,
            @PathVariable Integer year) {
        try {
            return ResponseEntity.ok(
                    taxContributionRepository.findByProviderIdAndTaxYear(providerId, year));
        } catch (Exception e) {
            logger.error("Error listing tax records for provider {} year {}: {}", providerId, year, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── By Provider + Year + Quarter ───────────────────────────────────────────

    @GetMapping("/providers/{providerId}/{year}/{quarter}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> getByProviderYearQuarter(
            @PathVariable Long providerId,
            @PathVariable Integer year,
            @PathVariable Integer quarter) {
        try {
            return ResponseEntity.ok(
                    taxContributionRepository.findByProviderIdAndTaxYearAndTaxQuarter(providerId, year, quarter));
        } catch (Exception e) {
            logger.error("Error fetching tax record for provider {} year {} Q{}: {}",
                    providerId, year, quarter, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Create ─────────────────────────────────────────────────────────────────

    @PostMapping("/providers/{providerId}")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> create(
            @PathVariable Long providerId,
            @RequestBody TaxContributionEntity taxRecord,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            taxRecord.setProviderId(providerId);
            taxRecord.setCreatedBy(userId);
            taxRecord.setCreatedAt(LocalDateTime.now());
            TaxContributionEntity saved = taxContributionRepository.save(taxRecord);
            logger.info("Created tax record {} for provider {} by user {}", saved.getId(), providerId, userId);
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            logger.error("Error creating tax record for provider {}: {}", providerId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Update ─────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody TaxContributionEntity updates,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            TaxContributionEntity existing = taxContributionRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tax record not found: " + id));
            if (updates.getFederalAllowances() != null)
                existing.setFederalAllowances(updates.getFederalAllowances());
            if (updates.getStateAllowances() != null)
                existing.setStateAllowances(updates.getStateAllowances());
            if (updates.getAdditionalFederalWithholding() != null)
                existing.setAdditionalFederalWithholding(updates.getAdditionalFederalWithholding());
            if (updates.getAdditionalStateWithholding() != null)
                existing.setAdditionalStateWithholding(updates.getAdditionalStateWithholding());
            if (updates.getFederalFilingStatus() != null) existing.setFederalFilingStatus(updates.getFederalFilingStatus());
            if (updates.getStateFilingStatus() != null) existing.setStateFilingStatus(updates.getStateFilingStatus());
            existing.setUpdatedBy(userId);
            existing.setUpdatedAt(LocalDateTime.now());
            taxContributionRepository.save(existing);
            logger.info("Updated tax record {} by user {}", id, userId);
            return ResponseEntity.ok(existing);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating tax record {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Generate W-2 ───────────────────────────────────────────────────────────

    @PutMapping("/{id}/generate-w2")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> generateW2(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            TaxContributionEntity record = taxContributionRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tax record not found: " + id));
            record.setW2Generated(true);
            record.setW2GeneratedDate(LocalDate.now());
            record.setUpdatedBy(userId);
            record.setUpdatedAt(LocalDateTime.now());
            taxContributionRepository.save(record);
            logger.info("Generated W-2 for tax record {} by user {}", id, userId);
            return ResponseEntity.ok(record);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error generating W-2 for tax record {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── W-2 Pending for Year ───────────────────────────────────────────────────

    @GetMapping("/w2-pending/{year}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listW2Pending(@PathVariable Integer year) {
        try {
            return ResponseEntity.ok(
                    taxContributionRepository.findByTaxYearAndW2Generated(year, false));
        } catch (Exception e) {
            logger.error("Error listing W-2 pending for year {}: {}", year, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
