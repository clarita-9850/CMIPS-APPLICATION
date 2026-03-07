package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.entity.TimesheetEntity;
import com.cmips.service.TpfEspAdapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * DSD Section 10/24 — TPF and ESP REST Endpoints
 *
 * TPF: Batch ingest of paper timesheets from Timesheet Processing Facility
 * ESP: Real-time ingest of electronic timesheets from Electronic Services Portal
 * TTS: Telephonic timesheet processing via Telephone Timesheet System
 */
@RestController
@RequestMapping("/api/tpf-esp")
public class TpfEspController {

    @Autowired private TpfEspAdapterService adapterService;

    /**
     * TPF Batch Ingest — receive pre-validated timesheet records from TPF.
     * POST body: { "records": [ { timesheetNumber, providerId, recipientId, caseId, payPeriodStart,
     *              payPeriodEnd, programType, dailyHours: { "2026-03-01": 8.0, ... },
     *              providerSignature: true, recipientSignature: true } ] }
     */
    @PostMapping("/tpf/batch")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<Map<String, Object>> receiveTpfBatch(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> records = (List<Map<String, Object>>) request.getOrDefault("records", List.of());
        if (records.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "records list is required"));
        }

        List<Long> createdIds = adapterService.receiveTpfBatch(records);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("source", "TPF");
        result.put("recordsReceived", records.size());
        result.put("timesheetsCreated", createdIds.size());
        result.put("timesheetIds", createdIds);
        result.put("errors", records.size() - createdIds.size());
        return ResponseEntity.ok(result);
    }

    /**
     * ESP Electronic Timesheet — receive real-time e-timesheet from ESP portal.
     * POST body: { providerId, recipientId, caseId, payPeriodStart, payPeriodEnd,
     *              programType, modeOfEntry: "ELECTRONIC"|"TELEPHONIC",
     *              dailyHours: { "2026-03-01": 8.0, ... } }
     */
    @PostMapping("/esp/timesheet")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<TimesheetEntity> receiveEspTimesheet(@RequestBody Map<String, Object> espData) {
        TimesheetEntity ts = adapterService.receiveEspTimesheet(espData);
        return ResponseEntity.ok(ts);
    }

    /**
     * TTS Recipient Approval — recipient approves timesheet via TTS.
     * PUT /api/tpf-esp/tts/{timesheetId}/approve?confirmationCode=TTS-123
     */
    @PutMapping("/tts/{timesheetId}/approve")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<TimesheetEntity> ttsRecipientApprove(
            @PathVariable Long timesheetId,
            @RequestParam String confirmationCode) {
        TimesheetEntity ts = adapterService.processRecipientApproval(timesheetId, confirmationCode);
        return ResponseEntity.ok(ts);
    }

    /**
     * TTS Recipient Rejection — recipient rejects timesheet via TTS/ESP.
     * PUT /api/tpf-esp/tts/{timesheetId}/reject?reason=Hours+not+correct
     */
    @PutMapping("/tts/{timesheetId}/reject")
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<TimesheetEntity> ttsRecipientReject(
            @PathVariable Long timesheetId,
            @RequestParam String reason) {
        TimesheetEntity ts = adapterService.processRecipientRejection(timesheetId, reason);
        return ResponseEntity.ok(ts);
    }
}
