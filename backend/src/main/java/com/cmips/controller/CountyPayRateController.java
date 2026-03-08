package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.CountyPayRateEntity;
import com.cmips.repository.CountyPayRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * County Pay Rate Controller — DSD Section 21/22
 *
 * Manages county-specific IP pay rates (Standard, WPCS, Enhanced, Homemaker).
 */
@RestController
@RequestMapping("/api/county-pay-rates")
@CrossOrigin(origins = "*")
public class CountyPayRateController {

    private static final Logger log = LoggerFactory.getLogger(CountyPayRateController.class);

    private final CountyPayRateRepository countyPayRateRepository;

    public CountyPayRateController(CountyPayRateRepository countyPayRateRepository) {
        this.countyPayRateRepository = countyPayRateRepository;
    }

    @GetMapping
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listAll() {
        try {
            return ResponseEntity.ok(countyPayRateRepository.findAllByOrderByCountyCodeAscEffectiveDateDesc());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/active")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listActive() {
        try {
            return ResponseEntity.ok(countyPayRateRepository.findByStatusOrderByCountyCodeAscEffectiveDateDesc("ACTIVE"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{countyCode}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listByCounty(@PathVariable String countyCode) {
        try {
            return ResponseEntity.ok(countyPayRateRepository.findByCountyCodeOrderByEffectiveDateDesc(countyCode));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{countyCode}/{rateType}/current")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> getCurrentRate(
            @PathVariable String countyCode,
            @PathVariable String rateType) {
        try {
            CountyPayRateEntity.RateType rt = CountyPayRateEntity.RateType.valueOf(rateType);
            return countyPayRateRepository.findCurrentRate(countyCode, rt, LocalDate.now())
                    .map(r -> ResponseEntity.ok((Object) r))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid rate type: " + rateType));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> create(
            @RequestBody CountyPayRateEntity rate,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            rate.setCreatedBy(userId);
            if (rate.getStatus() == null) rate.setStatus("ACTIVE");
            CountyPayRateEntity saved = countyPayRateRepository.save(rate);
            log.info("Created county pay rate {} for county {} by {}", saved.getId(), saved.getCountyCode(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody CountyPayRateEntity updates,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = countyPayRateRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var rate = existing.get();
            if (updates.getHourlyRate() != null) rate.setHourlyRate(updates.getHourlyRate());
            if (updates.getOvertimeRate() != null) rate.setOvertimeRate(updates.getOvertimeRate());
            if (updates.getEffectiveDate() != null) rate.setEffectiveDate(updates.getEffectiveDate());
            if (updates.getEndDate() != null) rate.setEndDate(updates.getEndDate());
            if (updates.getNotes() != null) rate.setNotes(updates.getNotes());
            rate.setUpdatedBy(userId);
            return ResponseEntity.ok(countyPayRateRepository.save(rate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/inactivate")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> inactivate(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            var existing = countyPayRateRepository.findById(id);
            if (existing.isEmpty()) return ResponseEntity.notFound().build();
            var rate = existing.get();
            rate.setStatus("INACTIVE");
            rate.setEndDate(LocalDate.now());
            rate.setUpdatedBy(userId);
            log.info("Inactivated county pay rate {} by {}", id, userId);
            return ResponseEntity.ok(countyPayRateRepository.save(rate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
