package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.PayrollBatchRunEntity;
import com.cmips.repository.PayrollBatchRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;

/**
 * Payroll Batch Controller — DSD Section 17
 *
 * Manages payroll batch runs: creation, processing lifecycle,
 * completion, failure tracking, and cancellation.
 *
 * GET    /api/payroll-batch/recent                         — recent batch runs
 * GET    /api/payroll-batch/{id}                           — get by id
 * GET    /api/payroll-batch/batch-number/{batchNumber}     — find by batch number
 * GET    /api/payroll-batch/status/{status}                — find by status
 * POST   /api/payroll-batch                                — create batch run
 * PUT    /api/payroll-batch/{id}/process                   — start processing
 * PUT    /api/payroll-batch/{id}/complete                  — mark completed
 * PUT    /api/payroll-batch/{id}/fail                      — mark failed
 * PUT    /api/payroll-batch/{id}/cancel                    — cancel batch run
 */
@RestController
@RequestMapping("/api/payroll-batch")
@CrossOrigin(origins = "*")
public class PayrollBatchController {

    private static final Logger logger = LoggerFactory.getLogger(PayrollBatchController.class);
    private static final DateTimeFormatter BATCH_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Random random = new Random();

    private final PayrollBatchRunRepository payrollBatchRunRepository;

    public PayrollBatchController(PayrollBatchRunRepository payrollBatchRunRepository) {
        this.payrollBatchRunRepository = payrollBatchRunRepository;
    }

    // ─── Recent Batch Runs ──────────────────────────────────────────────────────

    @GetMapping("/recent")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> listRecent() {
        try {
            return ResponseEntity.ok(payrollBatchRunRepository.findTop10ByOrderByCreatedAtDesc());
        } catch (Exception e) {
            logger.error("Error listing recent batch runs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Get by ID ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return payrollBatchRunRepository.findById(id)
                    .map(batch -> ResponseEntity.ok((Object) batch))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching batch run {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Find by Batch Number ───────────────────────────────────────────────────

    @GetMapping("/batch-number/{batchNumber}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> findByBatchNumber(@PathVariable String batchNumber) {
        try {
            return payrollBatchRunRepository.findByBatchNumber(batchNumber)
                    .map(batch -> ResponseEntity.ok((Object) batch))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching batch run by number {}: {}", batchNumber, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Find by Status ─────────────────────────────────────────────────────────

    @GetMapping("/status/{status}")
    @RequirePermission(resource = "Payment Resource", scope = "view")
    public ResponseEntity<?> findByStatus(@PathVariable String status) {
        try {
            return ResponseEntity.ok(
                    payrollBatchRunRepository.findByStatusOrderByCreatedAtDesc(status));
        } catch (Exception e) {
            logger.error("Error listing batch runs by status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Create Batch Run ───────────────────────────────────────────────────────

    @PostMapping
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> create(
            @RequestBody PayrollBatchRunEntity batchRun,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            String batchNumber = "PBR-" + LocalDate.now().format(BATCH_DATE_FMT)
                    + "-" + String.format("%05d", random.nextInt(100000));
            batchRun.setBatchNumber(batchNumber);
            batchRun.setStatus("PENDING");
            batchRun.setCreatedBy(userId);
            batchRun.setCreatedAt(LocalDateTime.now());
            PayrollBatchRunEntity saved = payrollBatchRunRepository.save(batchRun);
            logger.info("Created batch run {} ({}) by user {}", saved.getId(), batchNumber, userId);
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            logger.error("Error creating batch run: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Process ────────────────────────────────────────────────────────────────

    @PutMapping("/{id}/process")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> process(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            PayrollBatchRunEntity batch = payrollBatchRunRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Batch run not found: " + id));
            batch.setStatus("PROCESSING");
            batch.setSubmittedDate(LocalDateTime.now());
            batch.setUpdatedBy(userId);
            batch.setUpdatedAt(LocalDateTime.now());
            payrollBatchRunRepository.save(batch);
            logger.info("Started processing batch run {} by user {}", id, userId);
            return ResponseEntity.ok(batch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing batch run {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Complete ───────────────────────────────────────────────────────────────

    @PutMapping("/{id}/complete")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> complete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            PayrollBatchRunEntity batch = payrollBatchRunRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Batch run not found: " + id));
            batch.setStatus("COMPLETED");
            batch.setCompletedDate(LocalDateTime.now());
            batch.setUpdatedBy(userId);
            batch.setUpdatedAt(LocalDateTime.now());
            payrollBatchRunRepository.save(batch);
            logger.info("Completed batch run {} by user {}", id, userId);
            return ResponseEntity.ok(batch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error completing batch run {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Fail ───────────────────────────────────────────────────────────────────

    @PutMapping("/{id}/fail")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> fail(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            PayrollBatchRunEntity batch = payrollBatchRunRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Batch run not found: " + id));
            batch.setStatus("FAILED");
            batch.setErrorDetails(body.getOrDefault("errorDetails", ""));
            batch.setUpdatedBy(userId);
            batch.setUpdatedAt(LocalDateTime.now());
            payrollBatchRunRepository.save(batch);
            logger.info("Marked batch run {} as FAILED by user {}", id, userId);
            return ResponseEntity.ok(batch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error failing batch run {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Cancel ─────────────────────────────────────────────────────────────────

    @PutMapping("/{id}/cancel")
    @RequirePermission(resource = "Payment Resource", scope = "edit")
    public ResponseEntity<?> cancel(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        try {
            PayrollBatchRunEntity batch = payrollBatchRunRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Batch run not found: " + id));
            batch.setStatus("CANCELLED");
            batch.setUpdatedBy(userId);
            batch.setUpdatedAt(LocalDateTime.now());
            payrollBatchRunRepository.save(batch);
            logger.info("Cancelled batch run {} by user {}", id, userId);
            return ResponseEntity.ok(batch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error cancelling batch run {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
