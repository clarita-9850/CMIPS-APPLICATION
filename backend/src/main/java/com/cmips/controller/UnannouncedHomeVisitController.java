package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.UnannouncedHomeVisitEntity;
import com.cmips.service.UnannouncedHomeVisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Unannounced Home Visit Controller — DSD Section 25, CI-718079
 * AB 19 Section 12305.7(A) — Records and tracks unannounced home visits.
 */
@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class UnannouncedHomeVisitController {

    private static final Logger log = LoggerFactory.getLogger(UnannouncedHomeVisitController.class);

    private final UnannouncedHomeVisitService visitService;

    public UnannouncedHomeVisitController(UnannouncedHomeVisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping("/{caseId}/home-visits")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<List<UnannouncedHomeVisitEntity>> getVisits(@PathVariable Long caseId) {
        return ResponseEntity.ok(visitService.getVisits(caseId));
    }

    @PostMapping("/{caseId}/home-visits")
    @RequirePermission(resource = "Case Resource", scope = "create")
    public ResponseEntity<UnannouncedHomeVisitEntity> createVisit(
            @PathVariable Long caseId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            UnannouncedHomeVisitEntity saved = visitService.createVisit(caseId, request, userId);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/home-visits/{id}")
    @RequirePermission(resource = "Case Resource", scope = "view")
    public ResponseEntity<UnannouncedHomeVisitEntity> getVisit(@PathVariable Long id) {
        return ResponseEntity.ok(visitService.getVisit(id));
    }
}
