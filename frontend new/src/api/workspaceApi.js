import http from './httpClient';

/**
 * Fetch dashboard stats (task count, case count, notification count, approval count)
 */
export async function fetchDashboardStats(username) {
  try {
    const res = await http.get(`/workspace/stats?username=${encodeURIComponent(username)}`);
    return res.data;
  } catch (err) {
    console.warn('[workspaceApi] fetchDashboardStats failed:', err?.message);
    return { taskCount: 0, caseCount: 0, notificationCount: 0, approvalCount: 0 };
  }
}

/**
 * Fetch pending approvals (supervisor)
 */
export async function fetchPendingApprovals(username) {
  try {
    const res = await http.get(`/workspace/approvals?username=${encodeURIComponent(username)}`);
    return res.data;
  } catch (err) {
    console.warn('[workspaceApi] fetchPendingApprovals failed:', err?.message);
    return { timesheets: [], totalCount: 0 };
  }
}

/**
 * Fetch team workloads (supervisor)
 */
export async function fetchTeamWorkloads(supervisorId) {
  try {
    const res = await http.get(`/workspace/team?supervisorId=${encodeURIComponent(supervisorId)}`);
    return res.data;
  } catch (err) {
    console.warn('[workspaceApi] fetchTeamWorkloads failed:', err?.message);
    return { teamMembers: [], teamSize: 0 };
  }
}
