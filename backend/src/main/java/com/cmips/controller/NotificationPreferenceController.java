package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.NotificationPreference;
import com.cmips.repository.NotificationPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DSD GAP 8 — Notification Preference Controller.
 * Allows users to configure their notification delivery preferences.
 */
@RestController
@RequestMapping("/api/notification-preferences")
@CrossOrigin(origins = "*")
public class NotificationPreferenceController {

    private static final Logger log = LoggerFactory.getLogger(NotificationPreferenceController.class);

    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationPreferenceController(NotificationPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    /**
     * Get all notification preferences for a user.
     * GET /api/notification-preferences/{userId}
     */
    @GetMapping("/{userId}")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<List<NotificationPreference>> getUserPreferences(@PathVariable String userId) {
        List<NotificationPreference> prefs = preferenceRepository.findByUserId(userId);

        // If user has no preferences yet, return defaults for all categories
        if (prefs.isEmpty()) {
            for (NotificationPreference.NotificationCategory category : NotificationPreference.NotificationCategory.values()) {
                NotificationPreference defaultPref = new NotificationPreference();
                defaultPref.setUserId(userId);
                defaultPref.setNotificationCategory(category);
                defaultPref.setInAppEnabled(true);
                defaultPref.setEmailEnabled(false);
                defaultPref.setFrequency(NotificationPreference.Frequency.IMMEDIATE);
                prefs.add(preferenceRepository.save(defaultPref));
            }
        }

        return ResponseEntity.ok(prefs);
    }

    /**
     * Update a specific notification preference.
     * PUT /api/notification-preferences/{userId}/{category}
     * Body: { "inAppEnabled": true, "emailEnabled": false, "frequency": "DAILY_DIGEST" }
     */
    @PutMapping("/{userId}/{category}")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<?> updatePreference(@PathVariable String userId,
                                               @PathVariable String category,
                                               @RequestBody Map<String, Object> request) {
        NotificationPreference.NotificationCategory cat;
        try {
            cat = NotificationPreference.NotificationCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category: " + category));
        }

        NotificationPreference pref = preferenceRepository
                .findByUserIdAndNotificationCategory(userId, cat)
                .orElseGet(() -> {
                    NotificationPreference newPref = new NotificationPreference();
                    newPref.setUserId(userId);
                    newPref.setNotificationCategory(cat);
                    return newPref;
                });

        if (request.containsKey("inAppEnabled")) {
            pref.setInAppEnabled((Boolean) request.get("inAppEnabled"));
        }
        if (request.containsKey("emailEnabled")) {
            pref.setEmailEnabled((Boolean) request.get("emailEnabled"));
        }
        if (request.containsKey("frequency")) {
            pref.setFrequency(NotificationPreference.Frequency.valueOf(
                    ((String) request.get("frequency")).toUpperCase()));
        }

        pref.setUpdatedAt(LocalDateTime.now());
        preferenceRepository.save(pref);

        log.info("Updated notification preference for user {} category {}", userId, category);
        return ResponseEntity.ok(pref);
    }

    /**
     * Bulk update all preferences for a user.
     * PUT /api/notification-preferences/{userId}
     * Body: [ { "notificationCategory": "CASE_MAINTENANCE", "inAppEnabled": true, ... }, ... ]
     */
    @PutMapping("/{userId}")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<?> bulkUpdatePreferences(@PathVariable String userId,
                                                    @RequestBody List<NotificationPreference> preferences) {
        for (NotificationPreference incoming : preferences) {
            NotificationPreference existing = preferenceRepository
                    .findByUserIdAndNotificationCategory(userId, incoming.getNotificationCategory())
                    .orElseGet(() -> {
                        NotificationPreference newPref = new NotificationPreference();
                        newPref.setUserId(userId);
                        newPref.setNotificationCategory(incoming.getNotificationCategory());
                        return newPref;
                    });

            existing.setInAppEnabled(incoming.isInAppEnabled());
            existing.setEmailEnabled(incoming.isEmailEnabled());
            existing.setFrequency(incoming.getFrequency());
            existing.setUpdatedAt(LocalDateTime.now());
            preferenceRepository.save(existing);
        }

        return ResponseEntity.ok(preferenceRepository.findByUserId(userId));
    }
}
