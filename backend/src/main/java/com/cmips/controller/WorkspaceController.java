package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workspace")
@CrossOrigin(origins = "*")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * Get dashboard stats for a user (task count, case count, notification count, approval count)
     */
    @GetMapping("/stats")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> getDashboardStats(@RequestParam String username) {
        Map<String, Object> stats = workspaceService.getDashboardStats(username);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get items pending approval (supervisor only)
     */
    @GetMapping("/approvals")
    @RequirePermission(resource = "Case Management Resource", scope = "approve")
    public ResponseEntity<Map<String, Object>> getPendingApprovals(@RequestParam String username) {
        Map<String, Object> approvals = workspaceService.getPendingApprovals(username);
        return ResponseEntity.ok(approvals);
    }

    /**
     * Get team workload info (supervisor only)
     */
    @GetMapping("/team")
    @RequirePermission(resource = "Case Management Resource", scope = "supervise")
    public ResponseEntity<Map<String, Object>> getTeamWorkloads(@RequestParam String supervisorId) {
        Map<String, Object> team = workspaceService.getTeamWorkloads(supervisorId);
        return ResponseEntity.ok(team);
    }
}
