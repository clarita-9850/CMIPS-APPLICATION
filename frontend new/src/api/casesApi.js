import http from './httpClient';

export const getCases = (filters = {}) => {
  const qs = new URLSearchParams(filters).toString();
  return http.get(`/cases${qs ? '?' + qs : ''}`).then(r => r.data);
};

export const getCaseById = (id) =>
  http.get(`/cases/${id}`).then(r => r.data);

export const searchCases = (params) => {
  const qs = new URLSearchParams(params).toString();
  return http.get(`/cases/search?${qs}`).then(r => r.data);
};

export const createCase = (data) =>
  http.post('/cases', data).then(r => r.data);

export const approveCase = (id) =>
  http.put(`/cases/${id}/approve`).then(r => r.data);

export const denyCase = (id, data) =>
  http.put(`/cases/${id}/deny`, data).then(r => r.data);

export const terminateCase = (id, data) =>
  http.put(`/cases/${id}/terminate`, data).then(r => r.data);

export const assignCase = (id, data) =>
  http.put(`/cases/${id}/assign`, data).then(r => r.data);

export const withdrawCase = (id, data) =>
  http.put(`/cases/${id}/withdraw`, data).then(r => r.data);

export const placeOnLeave = (id, data) =>
  http.put(`/cases/${id}/leave`, data).then(r => r.data);

export const getCaseNotes = (id) =>
  http.get(`/cases/${id}/notes`).then(r => r.data);

export const addCaseNote = (id, data) =>
  http.post(`/cases/${id}/notes`, data).then(r => r.data);

export const appendCaseNote = (noteId, data) =>
  http.put(`/cases/notes/${noteId}/append`, data).then(r => r.data);

export const cancelCaseNote = (noteId, data) =>
  http.put(`/cases/notes/${noteId}/cancel`, data).then(r => r.data);

export const getCaseContacts = (id) =>
  http.get(`/cases/${id}/contacts`).then(r => r.data);

export const addCaseContact = (id, data) =>
  http.post(`/cases/${id}/contacts`, data).then(r => r.data);

export const inactivateContact = (contactId) =>
  http.put(`/cases/contacts/${contactId}/inactivate`).then(r => r.data);

export const initiateTransfer = (id, data) =>
  http.post(`/cases/${id}/transfer/initiate`, data).then(r => r.data);

export const completeTransfer = (id, data) =>
  http.post(`/cases/${id}/transfer/complete`, data).then(r => r.data);

export const getCaseStatistics = (countyCode) =>
  http.get(`/cases/statistics/${countyCode}`).then(r => r.data);

export const getDueForReassessment = (date) =>
  http.get(`/cases/due-for-reassessment${date ? '?date=' + date : ''}`).then(r => r.data);

export const getCaseStatusHistory = (id) =>
  http.get(`/cases/${id}/status-history`).then(r => r.data);

export const getCaseCodeTables = () =>
  http.get('/cases/code-tables').then(r => r.data);

// ── Workweek Agreements ──────────────────────────────────────────
export const getWorkweekAgreements = (caseId) =>
  http.get(`/cases/${caseId}/workweek-agreements`).then(r => r.data);

export const getWorkweekAgreementHistory = (caseId) =>
  http.get(`/cases/${caseId}/workweek-agreements/history`).then(r => r.data);

export const createWorkweekAgreement = (caseId, data) =>
  http.post(`/cases/${caseId}/workweek-agreements`, data).then(r => r.data);

export const updateWorkweekAgreement = (id, data) =>
  http.put(`/cases/workweek-agreements/${id}`, data).then(r => r.data);

export const inactivateWorkweekAgreement = (id, reason) =>
  http.put(`/cases/workweek-agreements/${id}/inactivate`, { reason }).then(r => r.data);

// ── Overtime Agreements ──────────────────────────────────────────
export const getOvertimeAgreements = (caseId) =>
  http.get(`/cases/${caseId}/overtime-agreements`).then(r => r.data);

export const createOvertimeAgreement = (caseId, data) =>
  http.post(`/cases/${caseId}/overtime-agreements`, data).then(r => r.data);

export const inactivateOvertimeAgreement = (id) =>
  http.put(`/cases/overtime-agreements/${id}/inactivate`).then(r => r.data);

// ── WPCS Hours ───────────────────────────────────────────────────
export const getWpcsHours = (caseId) =>
  http.get(`/cases/${caseId}/wpcs-hours`).then(r => r.data);

export const createWpcsHours = (caseId, data) =>
  http.post(`/cases/${caseId}/wpcs-hours`, data).then(r => r.data);

export const inactivateWpcsHours = (id) =>
  http.put(`/cases/wpcs-hours/${id}/inactivate`).then(r => r.data);

// ── Workplace Hours ──────────────────────────────────────────────
export const getWorkplaceHours = (caseId) =>
  http.get(`/cases/${caseId}/workplace-hours`).then(r => r.data);

export const createWorkplaceHours = (caseId, data) =>
  http.post(`/cases/${caseId}/workplace-hours`, data).then(r => r.data);

export const inactivateWorkplaceHours = (id) =>
  http.put(`/cases/workplace-hours/${id}/inactivate`).then(r => r.data);

// ── Reassessment ─────────────────────────────────────────────────
export const scheduleReassessment = (id, data) =>
  http.post(`/cases/${id}/schedule-reassessment`, data).then(r => r.data);

// ── Medi-Cal SOC ─────────────────────────────────────────────────
export const getMediCalSoc = (caseId) =>
  http.get(`/cases/${caseId}/medi-cal-soc`).then(r => r.data);

export const updateMediCalSoc = (caseId, data) =>
  http.put(`/cases/${caseId}/medi-cal-soc`, data).then(r => r.data);

export const getMediCalEligibility = (caseId) =>
  http.get(`/cases/${caseId}/medi-cal-eligibility`).then(r => r.data);

// ── Missing lifecycle actions ──────────────────────────────────────
export const rescindCase = (id, data) =>
  http.put(`/cases/${id}/rescind`, data).then(r => r.data);

export const reactivateCase = (id, data) =>
  http.put(`/cases/${id}/reactivate`, data).then(r => r.data);

export const cancelTransfer = (id, data) =>
  http.post(`/cases/${id}/transfer/cancel`, data).then(r => r.data);

// ── Health Care Certification ─────────────────────────────────────
export const getHealthCareCert = (caseId) =>
  http.get(`/cases/${caseId}/health-care-cert`).then(r => r.data);

export const createHealthCareCert = (caseId, data) =>
  http.post(`/cases/${caseId}/health-care-cert`, data).then(r => r.data);

export const updateHealthCareCert = (certId, data) =>
  http.put(`/cases/health-care-cert/${certId}`, data).then(r => r.data);

export const grantGoodCauseExtension = (caseId, data) =>
  http.put(`/cases/${caseId}/health-care-cert/good-cause`, data).then(r => r.data);

// ── NOA (Notice of Action) ────────────────────────────────────────
export const getNoas = (caseId) =>
  http.get(`/cases/${caseId}/noas`).then(r => r.data);

export const generateNoa = (caseId, data) =>
  http.post(`/cases/${caseId}/noas`, data).then(r => r.data);

export const printNoa = (noaId) =>
  http.get(`/cases/noas/${noaId}/print`).then(r => r.data);

export const suppressNoa = (noaId) =>
  http.put(`/cases/noas/${noaId}/suppress`).then(r => r.data);
