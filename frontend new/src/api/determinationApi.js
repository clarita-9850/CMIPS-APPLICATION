import http from './httpClient';

const base = '/determination';

// ── Authorization ────────────────────────────────────────────────────────────
export const createAuthorization = (assessmentId) =>
  http.post(`${base}/authorize`, { assessmentId }).then(r => r.data);

export const getActiveAuthorization = (caseId) =>
  http.get(`${base}/cases/${caseId}/authorization`).then(r => r.data);

export const getAuthorizationHistory = (caseId) =>
  http.get(`${base}/cases/${caseId}/authorizations`).then(r => r.data);

export const getAuthorizedServices = (authId) =>
  http.get(`${base}/authorizations/${authId}/services`).then(r => r.data);

// ── Modes of Service ─────────────────────────────────────────────────────────
export const assignModesOfService = (data) =>
  http.post(`${base}/modes-of-service`, data).then(r => r.data);

export const getModesOfService = (caseId) =>
  http.get(`${base}/cases/${caseId}/modes-of-service`).then(r => r.data);

export const getActiveModeOfService = (caseId) =>
  http.get(`${base}/cases/${caseId}/modes-of-service/active`).then(r => r.data);

export const getMOSHistory = (mosId) =>
  http.get(`${base}/modes-of-service/${mosId}/history`).then(r => r.data);

// ── SOC Spend Down ───────────────────────────────────────────────────────────
export const calculateSOCSpendDown = (caseId, serviceMonth) =>
  http.post(`${base}/soc-spend-down`, { caseId, serviceMonth }).then(r => r.data);

export const recalculateSOCSpendDown = (caseId) =>
  http.post(`${base}/cases/${caseId}/soc-spend-down/recalculate`).then(r => r.data);

export const getSOCHours = (caseId) =>
  http.get(`${base}/cases/${caseId}/soc-hours`).then(r => r.data);

export const getSpendDownTriggers = (caseId) =>
  http.get(`${base}/cases/${caseId}/spend-down-triggers`).then(r => r.data);

// ── Case Service Months ──────────────────────────────────────────────────────
export const getCaseServiceMonths = (caseId) =>
  http.get(`${base}/cases/${caseId}/service-months`).then(r => r.data);

export const getCaseServiceMonth = (caseId, serviceMonth) =>
  http.get(`${base}/cases/${caseId}/service-months/${serviceMonth}`).then(r => r.data);

// ── Full Determination Workflow ──────────────────────────────────────────────
export const executeFinalDetermination = (assessmentId) =>
  http.post(`${base}/execute`, { assessmentId }).then(r => r.data);
