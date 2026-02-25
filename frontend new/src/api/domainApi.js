/**
 * Domain API Registry
 *
 * Maps each feature domain (from uim-graph.json) to its backend API endpoints.
 * Used by generated stub pages to fetch real data from the backend.
 *
 * Each domain provides:
 *   - list(params)     — Fetch a paged list of records
 *   - get(id)          — Fetch a single record by ID
 *   - search(params)   — Search with filters
 *   - create(data)     — Create a new record
 *   - update(id, data) — Update an existing record
 *
 * Not all domains support all operations.
 */

import http from './httpClient';

// ---------------------------------------------------------------------------
// Helper: build query string from params object
// ---------------------------------------------------------------------------
function qs(params = {}) {
  const s = new URLSearchParams(params).toString();
  return s ? '?' + s : '';
}

// ---------------------------------------------------------------------------
// Domain-specific API functions
// ---------------------------------------------------------------------------

const caseApi = {
  list:   (params = {}) => http.get(`/cases${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/cases/${id}`).then(r => r.data),
  search: (params) => http.get(`/cases/search${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/cases', data).then(r => r.data),
  update: (id, data) => http.put(`/cases/${id}`, data).then(r => r.data),
  getNotes: (id) => http.get(`/cases/${id}/notes`).then(r => r.data),
  getContacts: (id) => http.get(`/cases/${id}/contacts`).then(r => r.data),
  getStatistics: (countyCode) => http.get(`/cases/statistics/${countyCode}`).then(r => r.data),
};

const personApi = {
  list:   (params = {}) => http.get(`/recipients${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/recipients/${id}`).then(r => r.data),
  search: (params) => http.get(`/recipients/search${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/recipients', data).then(r => r.data),
  update: (id, data) => http.put(`/recipients/${id}`, data).then(r => r.data),
};

const providerApi = {
  list:   (params = {}) => http.get(`/providers${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/providers/${id}`).then(r => r.data),
  search: (params) => http.get(`/providers/search${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/providers', data).then(r => r.data),
  update: (id, data) => http.put(`/providers/${id}`, data).then(r => r.data),
  getAssignments: (id) => http.get(`/providers/${id}/assignments`).then(r => r.data),
  getViolations: (id) => http.get(`/providers/${id}/violations`).then(r => r.data),
  getCori: (id) => http.get(`/providers/${id}/cori`).then(r => r.data),
};

const evidenceApi = {
  list:   (params = {}) => http.get(`/eligibility${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/eligibility/${id}`).then(r => r.data),
  search: (params) => http.get(`/eligibility/due-for-reassessment${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/eligibility', data).then(r => r.data),
  update: (id, data) => http.put(`/eligibility/${id}/service-hours`, data).then(r => r.data),
  getForCase: (caseId) => http.get(`/eligibility/case/${caseId}`).then(r => r.data),
  getHealthCerts: (caseId) => http.get(`/eligibility/case/${caseId}/health-cert`).then(r => r.data),
};

const paymentApi = {
  list:   (params = {}) => http.get(`/timesheets${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/timesheets/${id}`).then(r => r.data),
  search: (params) => http.get(`/warrants${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/timesheets', data).then(r => r.data),
  update: (id, data) => http.put(`/timesheets/${id}`, data).then(r => r.data),
};

const supervisorApi = {
  list:   (params = {}) => http.get(`/workspace/team${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/tasks/${id}`).then(r => r.data),
  search: (params) => http.get(`/tasks${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/tasks', data).then(r => r.data),
  update: (id, data) => http.put(`/tasks/${id}`, data).then(r => r.data),
  getApprovals: (params = {}) => http.get(`/workspace/approvals${qs(params)}`).then(r => r.data),
  getStats: () => http.get('/workspace/stats').then(r => r.data),
};

const taskManagementApi = {
  list:   (params = {}) => http.get(`/tasks${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/tasks/${id}`).then(r => r.data),
  search: (params) => http.get(`/tasks${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/tasks', data).then(r => r.data),
  update: (id, data) => http.put(`/tasks/${id}`, data).then(r => r.data),
  getQueues: () => http.get('/work-queues').then(r => r.data),
  reserve: (id) => http.post(`/tasks/${id}/reserve`).then(r => r.data),
  forward: (id, data) => http.post(`/tasks/${id}/forward`, data).then(r => r.data),
  defer: (id, data) => http.post(`/tasks/${id}/defer`, data).then(r => r.data),
  close: (id) => http.post(`/tasks/${id}/close`).then(r => r.data),
  reallocate: (id, data) => http.post(`/tasks/${id}/reallocate`, data).then(r => r.data),
};

const organizationApi = {
  list:   (params = {}) => http.get(`/admin/keycloak/users${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/admin/keycloak/users/${id}`).then(r => r.data),
  search: (params) => http.get(`/admin/keycloak/users${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/admin/keycloak/users', data).then(r => r.data),
  update: (id, data) => http.put(`/admin/keycloak/users/${id}`, data).then(r => r.data),
  getRoles: () => http.get('/admin/keycloak/roles').then(r => r.data),
  getGroups: () => http.get('/admin/keycloak/groups').then(r => r.data),
  getResources: () => http.get('/admin/keycloak/resources').then(r => r.data),
  getPolicies: () => http.get('/admin/keycloak/policies').then(r => r.data),
  getPermissions: () => http.get('/admin/keycloak/permissions').then(r => r.data),
};

const countyApi = {
  list:   (params = {}) => http.get(`/providers${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/providers/${id}`).then(r => r.data),
  search: (params) => http.get(`/providers/search${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/providers', data).then(r => r.data),
  update: (id, data) => http.put(`/providers/${id}`, data).then(r => r.data),
};

const homemakerApi = {
  list:   (params = {}) => http.get(`/providers${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/providers/${id}`).then(r => r.data),
  search: (params) => http.get(`/providers/search${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/providers', data).then(r => r.data),
  update: (id, data) => http.put(`/providers/${id}`, data).then(r => r.data),
};

const helpDeskApi = {
  list:   (params = {}) => http.get(`/recipients${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/recipients/${id}`).then(r => r.data),
  search: (params) => http.get(`/recipients/search${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/recipients', data).then(r => r.data),
  update: (id, data) => http.put(`/recipients/${id}`, data).then(r => r.data),
};

const backOfficeApi = {
  list:   (params = {}) => http.get(`/pipeline/status${qs(params)}`).then(r => r.data),
  get:    () => http.get(`/pipeline/status`).then(r => r.data),
  search: (params) => http.get(`/pipeline/status${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/batch/trigger/start', data).then(r => r.data),
  getAnalytics: () => http.get('/analytics/realtime-metrics').then(r => r.data),
  generateReport: (data) => http.post('/bi/reports/generate', data).then(r => r.data),
  getReportStatus: (jobId) => http.get(`/bi/jobs/${jobId}/status`).then(r => r.data),
};

// Misc domain: routes to generic person/task endpoints as catch-all
const miscApi = {
  list:   (params = {}) => http.get(`/recipients${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/recipients/${id}`).then(r => r.data),
  search: (params) => http.get(`/recipients/search${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/recipients', data).then(r => r.data),
  update: (id, data) => http.put(`/recipients/${id}`, data).then(r => r.data),
};

// ---------------------------------------------------------------------------
// Additional domain APIs for orphaned backend controllers
// ---------------------------------------------------------------------------

const applicationApi = {
  list:   (params = {}) => http.get(`/applications${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/applications/${id}`).then(r => r.data),
  search: (params) => http.get(`/applications/search${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/applications', data).then(r => r.data),
  update: (id, data) => http.patch(`/applications/${id}/status`, data).then(r => r.data),
  approve: (id) => http.post(`/applications/${id}/approve`).then(r => r.data),
  deny: (id) => http.post(`/applications/${id}/deny`).then(r => r.data),
  getPending: () => http.get('/applications/pending').then(r => r.data),
  getOverdue: () => http.get('/applications/overdue').then(r => r.data),
};

const referralApi = {
  list:   (params = {}) => http.get(`/referrals${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/referrals/${id}`).then(r => r.data),
  search: (params) => http.get(`/referrals/search${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/referrals', data).then(r => r.data),
  update: (id, data) => http.patch(`/referrals/${id}/status`, data).then(r => r.data),
  close: (id) => http.post(`/referrals/${id}/close`).then(r => r.data),
  reopen: (id) => http.post(`/referrals/${id}/reopen`).then(r => r.data),
  convert: (id) => http.post(`/referrals/${id}/convert`).then(r => r.data),
  getOpen: () => http.get('/referrals/open').then(r => r.data),
  getUrgent: () => http.get('/referrals/urgent').then(r => r.data),
};

const notificationApi = {
  list:   (userId) => http.get(`/notifications/user/${userId}`).then(r => r.data),
  get:    (id) => http.get(`/notifications/${id}`).then(r => r.data),
  search: (userId) => http.get(`/notifications/user/${userId}`).then(r => r.data),
  create: (data) => http.post('/notifications', data).then(r => r.data),
  markRead: (id) => http.put(`/notifications/${id}/read`).then(r => r.data),
  getUnreadCount: (userId) => http.get(`/notifications/unread-count/${userId}`).then(r => r.data),
};

const waiverApi = {
  list:   (params = {}) => http.get(`/waivers/active${qs(params)}`).then(r => r.data),
  get:    (id) => http.get(`/waivers/${id}`).then(r => r.data),
  search: (params) => http.get(`/waivers/search${qs(params)}`).then(r => r.data),
  create: (data) => http.post('/waivers', data).then(r => r.data),
  update: (id, data) => http.post(`/waivers/${id}/submit-for-review`, data).then(r => r.data),
  getPending: () => http.get('/waivers/pending').then(r => r.data),
};

const analyticsApi = {
  list:   () => http.get('/analytics/realtime-metrics').then(r => r.data),
  get:    (type) => http.get(`/analytics/demographics/${type}`).then(r => r.data),
  search: (params) => http.get(`/analytics/adhoc-data${qs(params)}`).then(r => r.data),
};

const personNoteApi = {
  list:   (personId) => http.get(`/notes/person/${personId}`).then(r => r.data),
  get:    (id) => http.get(`/notes/${id}`).then(r => r.data),
  search: (params) => http.get(`/notes/search${qs(params)}`).then(r => r.data),
  create: (data) => http.post(`/notes/recipient/${data.recipientId || 'unknown'}`, data).then(r => r.data),
  update: (id, data) => http.put(`/notes/${id}`, data).then(r => r.data),
};

const espApi = {
  startRecipient: (data) => http.post('/esp/register/recipient/start', data).then(r => r.data),
  startProvider: (data) => http.post('/esp/register/provider/start', data).then(r => r.data),
  verifyEmail: (regId, data) => http.post(`/esp/register/${regId}/verify-email`, data).then(r => r.data),
  complete: (regId) => http.post(`/esp/register/${regId}/complete`).then(r => r.data),
  checkUsername: (params) => http.get(`/esp/register/username/check${qs(params)}`).then(r => r.data),
};

// ---------------------------------------------------------------------------
// Registry: domain name -> API module
// ---------------------------------------------------------------------------

export const DOMAIN_API = {
  'case':            caseApi,
  'person':          personApi,
  'provider':        providerApi,
  'evidence':        evidenceApi,
  'payment':         paymentApi,
  'supervisor':      supervisorApi,
  'task-management': taskManagementApi,
  'organization':    organizationApi,
  'county':          countyApi,
  'homemaker':       homemakerApi,
  'help-desk':       helpDeskApi,
  'back-office':     backOfficeApi,
  'misc':            miscApi,
  // Additional domain APIs
  'application':     applicationApi,
  'referral':        referralApi,
  'notification':    notificationApi,
  'waiver':          waiverApi,
  'analytics':       analyticsApi,
  'person-note':     personNoteApi,
  'esp':             espApi,
};

/**
 * Get the API module for a given domain.
 * @param {string} domain - Domain name (e.g. 'case', 'person', 'provider')
 * @returns {Object} API module with list/get/search/create/update methods
 */
export function getDomainApi(domain) {
  return DOMAIN_API[domain] || miscApi;
}

export default DOMAIN_API;
