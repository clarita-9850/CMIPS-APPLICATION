package com.cmips.controller;

import com.cmips.entity.BviReviewEntity;
import com.cmips.annotation.RequirePermission;
import com.cmips.service.BviReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * DSD Section 24 — BVI Queue Controller
 * TVP Rules 62, 63, 64, 74.
 */
@RestController
@RequestMapping("/api/bvi-reviews")
public class BviReviewController {

    @Autowired private BviReviewService bviService;

    @GetMapping("/pending")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<BviReviewEntity>> listPending(
            @RequestParam(required = false) String countyCode) {
        if (countyCode != null) {
            return ResponseEntity.ok(bviService.listPendingByCounty(countyCode));
        }
        return ResponseEntity.ok(bviService.listPendingQueue());
    }

    @GetMapping("/recipient/{recipientId}")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<BviReviewEntity>> listByRecipient(@PathVariable Long recipientId) {
        return ResponseEntity.ok(bviService.listByRecipient(recipientId));
    }

    @GetMapping("/{reviewNumber}")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<?> getByNumber(@PathVariable String reviewNumber) {
        return bviService.getByReviewNumber(reviewNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/approve")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<BviReviewEntity> approve(@PathVariable Long id,
            @RequestParam(required = false, defaultValue = "TTS-AUTO") String confirmationCode) {
        return ResponseEntity.ok(bviService.approveBviReview(id, confirmationCode));
    }

    @PutMapping("/{id}/reject")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<BviReviewEntity> reject(@PathVariable Long id,
            @RequestParam String rejectionReason) {
        return ResponseEntity.ok(bviService.rejectBviReview(id, rejectionReason));
    }

    @PostMapping("/expire-overdue")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<Map<String, Object>> expireOverdue() {
        int count = bviService.expireOverdueReviews();
        return ResponseEntity.ok(Map.of("expiredCount", count));
    }
}
