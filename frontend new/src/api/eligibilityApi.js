import http from './httpClient';

// Base maps to /api/eligibility (controller @RequestMapping("/api/eligibility"))
const base = '/eligibility';

// ── Assessment CRUD ──────────────────────────────────────────────────────────
export const getAssessments = (caseId) =>
  http.get(`${base}/case/${caseId}`).then(r => r.data);

export const getAssessmentById = (id) =>
  http.get(`${base}/${id}`).then(r => r.data);

export const createAssessment = (data) =>
  http.post(`${base}`, data).then(r => r.data);

// ── Home Visit ────────────────────────────────────────────────────────────────
export const recordHomeVisit = (id, homeVisitDate) =>
  http.put(`${base}/${id}/home-visit`, { homeVisitDate }).then(r => r.data);

// ── Service Hours (25 service types) ─────────────────────────────────────────
export const updateServiceHours = (id, data) =>
  http.put(`${base}/${id}/service-hours`, data).then(r => r.data);

export const getTotalHours = (id) =>
  http.get(`${base}/${id}/total-hours`).then(r => r.data);

// ── Functional Ranks (category-level) ────────────────────────────────────────
export const updateFunctionalRanks = (id, data) =>
  http.put(`${base}/${id}/functional-ranks`, data).then(r => r.data);

// ── Share of Cost ─────────────────────────────────────────────────────────────
export const updateShareOfCost = (id, data) =>
  http.put(`${base}/${id}/share-of-cost`, data).then(r => r.data);

// ── Waiver Program ────────────────────────────────────────────────────────────
export const updateWaiverProgram = (id, waiverProgram) =>
  http.put(`${base}/${id}/waiver-program`, { waiverProgram }).then(r => r.data);

// ── Advance Pay ───────────────────────────────────────────────────────────────
export const updateAdvancePay = (id, data) =>
  http.put(`${base}/${id}/advance-pay`, data).then(r => r.data);

// ── Approval Workflow (DSD Section 22) ───────────────────────────────────────
export const submitForApproval = (id) =>
  http.put(`${base}/${id}/submit-for-approval`).then(r => r.data);

export const approveAssessment = (id) =>
  http.put(`${base}/${id}/approve`).then(r => r.data);

export const rejectAssessment = (id, reason) =>
  http.put(`${base}/${id}/reject`, { reason }).then(r => r.data);

// Check Eligibility — preview mode only, no NOA generated (DSD Section 22)
export const checkEligibility = (id) =>
  http.post(`${base}/${id}/check-eligibility`).then(r => r.data);

// ── Health Care Certification (BR SE 28-50) ───────────────────────────────────
export const getHealthCerts = (caseId) =>
  http.get(`${base}/case/${caseId}/health-cert`).then(r => r.data);

export const createHealthCert = (data) =>
  http.post(`${base}/health-cert`, data).then(r => r.data);

export const recordDocumentationReceived = (id, data) =>
  http.put(`${base}/health-cert/${id}/documentation-received`, data).then(r => r.data);

export const requestGoodCauseExtension = (id, data) =>
  http.put(`${base}/health-cert/${id}/good-cause-extension`, data).then(r => r.data);

// ── Legacy aliases (used in CaseDetailPage tabs) ──────────────────────────────
export const getServicePlans = (caseId) =>
  http.get(`${base}/case/${caseId}`).then(r => r.data); // returns assessments; rename tab uses this

export const scheduleReassessment = (caseId, data) =>
  http.post(`${base}/case/${caseId}/schedule-reassessment`, data).then(r => r.data).catch(() => null);
