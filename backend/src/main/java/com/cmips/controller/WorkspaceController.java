package com.cmips.controller;

import com.cmips.annotation.RequirePermission;
import com.cmips.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workspace")
@CrossOrigin(origins = "*")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    private String resolveUsername(String username) {
        if (username != null && !username.isBlank()) return username;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "anonymous";
    }

    /**
     * Get dashboard stats for a user (task count, case count, notification count, approval count)
     */
    @GetMapping("/stats")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestParam(required = false) String username) {
        Map<String, Object> stats = workspaceService.getDashboardStats(resolveUsername(username));
        return ResponseEntity.ok(stats);
    }

    /**
     * Get items pending approval (supervisor only)
     */
    @GetMapping("/approvals")
    @RequirePermission(resource = "Case Management Resource", scope = "approve")
    public ResponseEntity<Map<String, Object>> getPendingApprovals(
            @RequestParam(required = false) String username) {
        Map<String, Object> approvals = workspaceService.getPendingApprovals(resolveUsername(username));
        return ResponseEntity.ok(approvals);
    }

    /**
     * Get team workload info (supervisor only)
     */
    @GetMapping("/team")
    @RequirePermission(resource = "Case Management Resource", scope = "supervise")
    public ResponseEntity<Map<String, Object>> getTeamWorkloads(
            @RequestParam(required = false) String supervisorId) {
        Map<String, Object> team = workspaceService.getTeamWorkloads(resolveUsername(supervisorId));
        return ResponseEntity.ok(team);
    }

    /**
     * Get My Tasks — open tasks assigned to the user within next 7 days (max 25).
     * Implements DSD My Workspace Phase 1.
     */
    @GetMapping("/my-tasks")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> getMyTasks(
            @RequestParam(required = false) String username) {
        Map<String, Object> tasks = workspaceService.getMyTasks(resolveUsername(username));
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get My Shortcuts — quick-access page links for the authenticated user.
     * Implements DSD My Workspace Phase 1.
     */
    @GetMapping("/my-shortcuts")
    @RequirePermission(resource = "Normal Login Resource", scope = "view")
    public ResponseEntity<Map<String, Object>> getMyShortcuts(
            @RequestParam(required = false, defaultValue = "") String username) {
        Map<String, Object> shortcuts = workspaceService.getMyShortcuts(resolveUsername(username));
        return ResponseEntity.ok(shortcuts);
    }
}
