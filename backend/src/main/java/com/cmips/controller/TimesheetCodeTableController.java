package com.cmips.controller;

import com.cmips.entity.TimesheetCodeTableEntity;
import com.cmips.repository.TimesheetCodeTableRepository;
import com.cmips.annotation.RequirePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * DSD Section 24 — Timesheet Code Tables Controller (21 tables)
 */
@RestController
@RequestMapping("/api/timesheet-code-tables")
public class TimesheetCodeTableController {

    @Autowired private TimesheetCodeTableRepository codeTableRepo;

    @GetMapping("/{tableType}")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<TimesheetCodeTableEntity>> getByType(
            @PathVariable String tableType,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        TimesheetCodeTableEntity.TableType type;
        try {
            type = TimesheetCodeTableEntity.TableType.valueOf(tableType);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        if (activeOnly) {
            return ResponseEntity.ok(codeTableRepo.findByTableTypeAndActiveTrueOrderByDisplayOrderAsc(type));
        }
        return ResponseEntity.ok(codeTableRepo.findByTableTypeOrderByDisplayOrderAsc(type));
    }

    @GetMapping("/{tableType}/{code}")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<?> getByTypeAndCode(@PathVariable String tableType, @PathVariable String code) {
        try {
            TimesheetCodeTableEntity.TableType type = TimesheetCodeTableEntity.TableType.valueOf(tableType);
            TimesheetCodeTableEntity entry = codeTableRepo.findByTableTypeAndCode(type, code);
            if (entry == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(entry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/types")
    @RequirePermission(resource = "Timesheet Resource", scope = "view")
    public ResponseEntity<List<String>> listTypes() {
        List<String> types = new ArrayList<>();
        for (TimesheetCodeTableEntity.TableType t : TimesheetCodeTableEntity.TableType.values()) {
            types.add(t.name());
        }
        return ResponseEntity.ok(types);
    }

    @PostMapping
    @RequirePermission(resource = "Timesheet Resource", scope = "edit")
    public ResponseEntity<TimesheetCodeTableEntity> create(@RequestBody TimesheetCodeTableEntity entry) {
        return ResponseEntity.ok(codeTableRepo.save(entry));
    }
}
