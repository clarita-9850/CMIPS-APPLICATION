package com.cmips.service;

import com.cmips.entity.CaseEntity;
import com.cmips.entity.Task;
import com.cmips.entity.Notification;
import com.cmips.entity.Timesheet;
import com.cmips.entity.TimesheetStatus;
import com.cmips.repository.TaskRepository;
import com.cmips.repository.CaseRepository;
import com.cmips.repository.NotificationRepository;
import com.cmips.repository.TimesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkspaceService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TimesheetRepository timesheetRepository;

    /**
     * Aggregate dashboard stats for a user
     */
    public Map<String, Object> getDashboardStats(String username) {
        Map<String, Object> stats = new HashMap<>();
        try {
            // Task count: assigned to user, not closed
            long taskCount = taskRepository.findByAssignedTo(username).stream()
                .filter(t -> t.getStatus() != Task.TaskStatus.CLOSED)
                .count();

            // Case count: created by user or assigned to user
            long caseCount = caseRepository.findAll().stream()
                .filter(c -> username.equalsIgnoreCase(c.getCreatedBy()) || username.equalsIgnoreCase(c.getCaseOwnerId()))
                .filter(c -> c.getCaseStatus() != null
                    && c.getCaseStatus() != com.cmips.entity.CaseEntity.CaseStatus.TERMINATED
                    && c.getCaseStatus() != com.cmips.entity.CaseEntity.CaseStatus.DENIED)
                .count();

            // Notification count
            long notificationCount = notificationRepository.findByUserId(username).size();

            // Approval count: timesheets in SUBMITTED status (for supervisor view)
            long approvalCount = timesheetRepository.findAll().stream()
                .filter(t -> t.getStatus() == TimesheetStatus.SUBMITTED)
                .count();

            stats.put("taskCount", taskCount);
            stats.put("caseCount", caseCount);
            stats.put("notificationCount", notificationCount);
            stats.put("approvalCount", approvalCount);
            stats.put("status", "SUCCESS");
        } catch (Exception e) {
            stats.put("taskCount", 0);
            stats.put("caseCount", 0);
            stats.put("notificationCount", 0);
            stats.put("approvalCount", 0);
            stats.put("status", "ERROR");
            stats.put("message", e.getMessage());
        }
        return stats;
    }

    /**
     * Get items pending supervisor approval
     */
    public Map<String, Object> getPendingApprovals(String username) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Timesheets awaiting approval
            List<Map<String, Object>> pendingTimesheets = timesheetRepository.findAll().stream()
                .filter(t -> t.getStatus() == TimesheetStatus.SUBMITTED || t.getStatus() == TimesheetStatus.REVISION_REQUESTED)
                .limit(20)
                .map(t -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", t.getId());
                    m.put("type", "TIMESHEET");
                    m.put("employeeName", t.getEmployeeName());
                    m.put("status", t.getStatus().name());
                    m.put("submittedAt", t.getSubmittedAt());
                    m.put("payPeriodStart", t.getPayPeriodStart());
                    m.put("payPeriodEnd", t.getPayPeriodEnd());
                    m.put("totalHours", t.getTotalHours());
                    return m;
                })
                .collect(Collectors.toList());

            result.put("timesheets", pendingTimesheets);
            result.put("totalCount", pendingTimesheets.size());
            result.put("status", "SUCCESS");
        } catch (Exception e) {
            result.put("timesheets", Collections.emptyList());
            result.put("totalCount", 0);
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * Get team workload data for supervisor
     */
    public Map<String, Object> getTeamWorkloads(String supervisorId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Group tasks by assigned worker
            Map<String, Long> workerTaskCounts = taskRepository.findAll().stream()
                .filter(t -> t.getAssignedTo() != null && t.getStatus() != Task.TaskStatus.CLOSED)
                .collect(Collectors.groupingBy(Task::getAssignedTo, Collectors.counting()));

            List<Map<String, Object>> teamMembers = new ArrayList<>();
            for (Map.Entry<String, Long> entry : workerTaskCounts.entrySet()) {
                Map<String, Object> member = new HashMap<>();
                member.put("username", entry.getKey());
                member.put("openTasks", entry.getValue());
                teamMembers.add(member);
            }
            teamMembers.sort((a, b) -> Long.compare((Long) b.get("openTasks"), (Long) a.get("openTasks")));

            result.put("teamMembers", teamMembers);
            result.put("teamSize", teamMembers.size());
            result.put("status", "SUCCESS");
        } catch (Exception e) {
            result.put("teamMembers", Collections.emptyList());
            result.put("teamSize", 0);
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
        }
        return result;
    }
}
