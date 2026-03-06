package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.WarrantEntity;
import com.cmips.repository.WarrantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/warrants")
public class WarrantController {

    private final WarrantRepository warrantRepository;

    public WarrantController(WarrantRepository warrantRepository) {
        this.warrantRepository = warrantRepository;
    }

    @GetMapping
    @RequirePermission(resource = "Warrant Resource", scope = "view")
    public ResponseEntity<?> searchWarrants(
            @RequestParam(required = false) String warrantNumber,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) String caseNumber,
            @RequestParam(required = false) String countyCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<WarrantEntity> results;

        if (warrantNumber != null && !warrantNumber.isBlank()) {
            Optional<WarrantEntity> opt = warrantRepository.findByWarrantNumber(warrantNumber);
            results = opt.map(List::of).orElse(List.of());
        } else if (providerId != null && !providerId.isBlank()) {
            if (startDate != null && endDate != null) {
                results = warrantRepository.findByProviderAndDateRange(
                        providerId, LocalDate.parse(startDate), LocalDate.parse(endDate));
            } else {
                results = warrantRepository.findByProviderIdOrderByIssueDateDesc(providerId);
            }
        } else if (caseNumber != null && !caseNumber.isBlank()) {
            results = warrantRepository.findByCaseNumberOrderByIssueDateDesc(caseNumber);
        } else if (countyCode != null && !countyCode.isBlank()) {
            if (startDate != null && endDate != null) {
                results = warrantRepository.findByCountyCodeAndIssueDateBetweenOrderByIssueDateDesc(
                        countyCode, LocalDate.parse(startDate), LocalDate.parse(endDate));
            } else {
                results = warrantRepository.findByCountyCodeOrderByIssueDateDesc(countyCode);
            }
        } else if (startDate != null && endDate != null) {
            results = warrantRepository.findByIssueDateBetweenOrderByIssueDateDesc(
                    LocalDate.parse(startDate), LocalDate.parse(endDate));
        } else {
            results = List.of();
        }

        // Filter by status if provided
        if (status != null && !status.isBlank()) {
            results = results.stream()
                    .filter(w -> status.equalsIgnoreCase(w.getStatus().name()))
                    .toList();
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/{warrantNumber}")
    @RequirePermission(resource = "Warrant Resource", scope = "view")
    public ResponseEntity<?> getByWarrantNumber(@PathVariable String warrantNumber) {
        return warrantRepository.findByWarrantNumber(warrantNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{warrantNumber}/replace")
    @RequirePermission(resource = "Warrant Resource", scope = "create")
    public ResponseEntity<?> requestReplacement(
            @PathVariable String warrantNumber,
            @RequestBody Map<String, String> body) {
        Optional<WarrantEntity> opt = warrantRepository.findByWarrantNumber(warrantNumber);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        WarrantEntity warrant = opt.get();
        // Only ISSUED or STALE warrants can be replaced
        String currentStatus = warrant.getStatus().name();
        if (!"ISSUED".equals(currentStatus) && !"STALE".equals(currentStatus)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only ISSUED or STALE warrants can be replaced."));
        }
        // Mark original as voided
        warrant.setStatus(WarrantEntity.WarrantStatus.VOIDED);
        warrantRepository.save(warrant);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Replacement requested for warrant " + warrantNumber);
        response.put("originalWarrantNumber", warrantNumber);
        response.put("reason", body.getOrDefault("reason", ""));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{warrantNumber}/history")
    @RequirePermission(resource = "Warrant Resource", scope = "view")
    public ResponseEntity<?> getHistory(@PathVariable String warrantNumber) {
        Optional<WarrantEntity> opt = warrantRepository.findByWarrantNumber(warrantNumber);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        WarrantEntity w = opt.get();
        // Return the warrant's details as its history entry
        Map<String, Object> history = new HashMap<>();
        history.put("warrantNumber", w.getWarrantNumber());
        history.put("status", w.getStatus().name());
        history.put("issueDate", w.getIssueDate());
        history.put("paidDate", w.getPaidDate());
        history.put("amount", w.getAmount());
        history.put("statusUpdatedAt", w.getStatusUpdatedAt());
        return ResponseEntity.ok(List.of(history));
    }

    @GetMapping("/stats")
    @RequirePermission(resource = "Warrant Resource", scope = "view")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("issuedCount", warrantRepository.countByStatus(WarrantEntity.WarrantStatus.ISSUED));
        stats.put("paidCount", warrantRepository.countByStatus(WarrantEntity.WarrantStatus.PAID));
        stats.put("voidedCount", warrantRepository.countByStatus(WarrantEntity.WarrantStatus.VOIDED));
        stats.put("staleCount", warrantRepository.countByStatus(WarrantEntity.WarrantStatus.STALE));
        stats.put("distinctCounties", warrantRepository.findDistinctCountyCodes());
        return ResponseEntity.ok(stats);
    }
}
