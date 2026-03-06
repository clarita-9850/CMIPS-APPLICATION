package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.FlexibleHoursEntity;
import com.cmips.service.FlexibleHoursService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Flexible Hours Controller — DSD Section 25, CI-67807
 */
@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class FlexibleHoursController {

    private static final Logger log = LoggerFactory.getLogger(FlexibleHoursController.class);

    private final FlexibleHoursService flexibleHoursService;

    public FlexibleHoursController(FlexibleHoursService flexibleHoursService) {
        this.flexibleHoursService = flexibleHoursService;
    }

    @GetMapping("/{caseId}/flexible-hours")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<FlexibleHoursEntity>> getFlexibleHours(@PathVariable Long caseId) {
        return ResponseEntity.ok(flexibleHoursService.getFlexibleHours(caseId));
    }

    @PostMapping("/{caseId}/flexible-hours")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<?> createFlexibleHours(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            FlexibleHoursEntity saved = flexibleHoursService.createFlexibleHours(caseId, request, userId);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/flexible-hours/{id}/approve")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> approveFlexibleHours(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            Integer approvedMinutes = request != null && request.get("approvedMinutes") != null
                    ? ((Number) request.get("approvedMinutes")).intValue() : null;
            return ResponseEntity.ok(flexibleHoursService.approveFlexibleHours(id, approvedMinutes, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/flexible-hours/{id}/deny")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> denyFlexibleHours(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(flexibleHoursService.denyFlexibleHours(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/flexible-hours/{id}/cancel")
    @RequirePermission(resource = "Case Resource", scope = "edit")
    public ResponseEntity<?> cancelFlexibleHours(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            return ResponseEntity.ok(flexibleHoursService.cancelFlexibleHours(id, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
