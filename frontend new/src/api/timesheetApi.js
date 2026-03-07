import http from './httpClient';

const base = '/ihss-timesheets';

// ── Search & List ──
export const searchTimesheets = (params = {}) => {
  const qs = new URLSearchParams();
  if (params.caseId) qs.append('caseId', params.caseId);
  if (params.recipientId) qs.append('recipientId', params.recipientId);
  if (params.providerId) qs.append('providerId', params.providerId);
  if (params.status) qs.append('status', params.status);
  if (params.programType) qs.append('programType', params.programType);
  if (params.fromDate) qs.append('fromDate', params.fromDate);
  if (params.toDate) qs.append('toDate', params.toDate);
  if (params.countyCode) qs.append('countyCode', params.countyCode);
  return http.get(`${base}/search?${qs.toString()}`).then(r => r.data);
};

export const getTimesheetsByCase = (caseId) =>
  http.get(`${base}/case/${caseId}`).then(r => r.data);

export const getTimesheetsByProvider = (providerId) =>
  http.get(`${base}/provider/${providerId}`).then(r => r.data);

export const getTimesheetsByStatus = (status) =>
  http.get(`${base}/status/${status}`).then(r => r.data);

export const getTimesheetById = (id) =>
  http.get(`${base}/${id}`).then(r => r.data);

// ── Manual Entry ──
export const createManualTimesheet = (data) =>
  http.post(`${base}/manual-entry`, data).then(r => r.data);

// ── Validation ──
export const validateTimesheet = (id) =>
  http.post(`${base}/${id}/validate`).then(r => r.data);

// ── Actions ──
export const releaseTimesheet = (id, releasedBy) =>
  http.post(`${base}/${id}/release`, { releasedBy }).then(r => r.data);

export const rejectTimesheet = (id, reason, rejectedBy) =>
  http.post(`${base}/${id}/reject`, { reason, rejectedBy }).then(r => r.data);

export const voidTimesheet = (id, reason, voidedBy) =>
  http.post(`${base}/${id}/void`, { reason, voidedBy }).then(r => r.data);

export const sendToPayroll = (id) =>
  http.post(`${base}/${id}/send-to-payroll`).then(r => r.data);

// ── FLSA Overtime ──
export const getOvertimeBreakdown = (id) =>
  http.get(`${base}/${id}/overtime`).then(r => r.data);

// ── Random Sampling ──
export const getSamplingPending = () =>
  http.get(`${base}/sampling/pending`).then(r => r.data);

export const verifySampling = (id, data) =>
  http.post(`${base}/${id}/sampling-verify`, data).then(r => r.data);

// ── Flagged Review ──
export const getFlaggedPending = () =>
  http.get(`${base}/flagged/pending`).then(r => r.data);

export const completeFlaggedReview = (id, data) =>
  http.post(`${base}/${id}/flagged-review`, data).then(r => r.data);

// ── Batch ──
export const batchReleaseHeld = () =>
  http.post(`${base}/batch/release-held`).then(r => r.data);

// ── Dashboard ──
export const getDashboard = () =>
  http.get(`${base}/dashboard`).then(r => r.data);

// ── Travel Claims ──
export const searchTravelClaims = (params = {}) => {
  const qs = new URLSearchParams();
  if (params.caseId) qs.append('caseId', params.caseId);
  if (params.providerId) qs.append('providerId', params.providerId);
  if (params.status) qs.append('status', params.status);
  if (params.fromDate) qs.append('fromDate', params.fromDate);
  if (params.toDate) qs.append('toDate', params.toDate);
  return http.get(`${base}/travel-claims/search?${qs.toString()}`).then(r => r.data);
};

export const getTravelClaimsByCase = (caseId) =>
  http.get(`${base}/travel-claims/case/${caseId}`).then(r => r.data);

export const getTravelClaimById = (id) =>
  http.get(`${base}/travel-claims/${id}`).then(r => r.data);

export const createTravelClaim = (data) =>
  http.post(`${base}/travel-claims`, data).then(r => r.data);

export const validateTravelClaim = (id) =>
  http.post(`${base}/travel-claims/${id}/validate`).then(r => r.data);

// ── Legacy compat (old pages still import these) ──
export const getTimesheets = searchTimesheets;
