import http from './httpClient';

export const getProviders = (page = 0, size = 20, filters = {}) => {
  const params = new URLSearchParams({ page, size, ...filters });
  return http.get(`/providers?${params}`).then(r => r.data);
};

export const getProviderById = (id) =>
  http.get(`/providers/${id}`).then(r => r.data);

export const searchProviders = (params) => {
  const qs = new URLSearchParams(params).toString();
  return http.get(`/providers/search?${qs}`).then(r => r.data);
};

export const createProvider = (data) =>
  http.post('/providers', data).then(r => r.data);

export const approveEnrollment = (id) =>
  http.put(`/providers/${id}/approve-enrollment`).then(r => r.data);

export const setIneligible = (id, data) =>
  http.put(`/providers/${id}/set-ineligible`, data).then(r => r.data);

export const reinstateProvider = (id) =>
  http.put(`/providers/${id}/reinstate`).then(r => r.data);

export const reEnrollProvider = (id) =>
  http.put(`/providers/${id}/re-enroll`).then(r => r.data);

export const getProviderAssignments = (id) =>
  http.get(`/providers/${id}/assignments`).then(r => r.data);

export const assignProviderToCase = (data) =>
  http.post('/providers/assignments', data).then(r => r.data);

export const terminateAssignment = (id, data) =>
  http.put(`/providers/assignments/${id}/terminate`, data).then(r => r.data);

export const placeAssignmentOnLeave = (id, data) =>
  http.put(`/providers/assignments/${id}/leave`, data).then(r => r.data);

export const getProviderCori = (id) =>
  http.get(`/providers/${id}/cori`).then(r => r.data);

export const createCoriRecord = (id, data) =>
  http.post(`/providers/${id}/cori`, data).then(r => r.data);

export const getProviderViolations = (id) =>
  http.get(`/providers/${id}/violations`).then(r => r.data);

export const createViolation = (id, data) =>
  http.post(`/providers/${id}/violations`, data).then(r => r.data);

export const getEligibleProviders = (countyCode) =>
  http.get(`/providers/eligible${countyCode ? '?countyCode=' + countyCode : ''}`).then(r => r.data);

// IRS Live-In Provider Self-Certification (DSD Section 32)
export const lookupLiveInCert = (data) => http.post('/providers/live-in-cert/lookup', data).then(r => r.data);
export const saveLiveInCert = (data) => http.post('/providers/live-in-cert/save', data).then(r => r.data);

// ── Overtime Violation Review Workflow (DSD Section 23) ──────────────────────
export const countyReviewViolation = (violationId, data) =>
  http.put(`/providers/violations/${violationId}/county-review`, data).then(r => r.data);
export const supervisorReviewViolation = (violationId, data) =>
  http.put(`/providers/violations/${violationId}/supervisor-review`, data).then(r => r.data);
export const recordTrainingCompletion = (violationId) =>
  http.put(`/providers/violations/${violationId}/training-completed`).then(r => r.data);
export const fileCountyDispute = (violationId, data) =>
  http.put(`/providers/violations/${violationId}/county-dispute`, data).then(r => r.data);
export const resolveCountyDispute = (violationId, data) =>
  http.put(`/providers/violations/${violationId}/county-dispute/resolve`, data).then(r => r.data);
export const requestCdssReview = (violationId) =>
  http.put(`/providers/violations/${violationId}/cdss-review`).then(r => r.data);
export const recordCdssOutcome = (violationId, data) =>
  http.put(`/providers/violations/${violationId}/cdss-review/outcome`, data).then(r => r.data);

// ── Overtime Exemptions (CI-668111) ─────────────────────────────────────────
export const getProviderExemptions = (id) =>
  http.get(`/providers/${id}/exemptions`).then(r => r.data);
export const getActiveExemption = (id) =>
  http.get(`/providers/${id}/exemptions/active`).then(r => r.data);
export const createExemption = (id, data) =>
  http.post(`/providers/${id}/exemptions`, data).then(r => r.data);
export const modifyExemption = (exemptionId, data) =>
  http.put(`/providers/exemptions/${exemptionId}`, data).then(r => r.data);
export const inactivateExemption = (exemptionId, data) =>
  http.put(`/providers/exemptions/${exemptionId}/inactivate`, data).then(r => r.data);

// ── Workweek Agreements (CI-480910) ─────────────────────────────────────────
export const getWorkweekAgreements = (id) =>
  http.get(`/providers/${id}/workweek-agreements`).then(r => r.data);
export const createWorkweekAgreement = (id, data) =>
  http.post(`/providers/${id}/workweek-agreements`, data).then(r => r.data);
export const modifyWorkweekAgreement = (agreementId, data) =>
  http.put(`/providers/workweek-agreements/${agreementId}`, data).then(r => r.data);
export const inactivateWorkweekAgreement = (agreementId, data) =>
  http.put(`/providers/workweek-agreements/${agreementId}/inactivate`, data).then(r => r.data);

// ── Travel Time (CI-480867) ──────────────────────────────────────────────────
export const getTravelTimes = (id) =>
  http.get(`/providers/${id}/travel-times`).then(r => r.data);
export const createTravelTime = (id, data) =>
  http.post(`/providers/${id}/travel-times`, data).then(r => r.data);
export const modifyTravelTime = (travelTimeId, data) =>
  http.put(`/providers/travel-times/${travelTimeId}`, data).then(r => r.data);
export const inactivateTravelTime = (travelTimeId) =>
  http.put(`/providers/travel-times/${travelTimeId}/inactivate`).then(r => r.data);

// ── Provider Benefits / Deductions (CI-117534) ───────────────────────────────
export const getProviderBenefits = (id) =>
  http.get(`/providers/${id}/benefits`).then(r => r.data);
export const createProviderBenefit = (id, data) =>
  http.post(`/providers/${id}/benefits`, data).then(r => r.data);
export const modifyProviderBenefit = (benefitId, data) =>
  http.put(`/providers/benefits/${benefitId}`, data).then(r => r.data);
export const terminateProviderBenefit = (benefitId, data) =>
  http.put(`/providers/benefits/${benefitId}/terminate`, data).then(r => r.data);

// ── CORI Modify / Inactivate (CI-117566/117567) ───────────────────────────────
export const modifyCoriRecord = (coriId, data) =>
  http.put(`/providers/cori/${coriId}`, data).then(r => r.data);
export const inactivateCoriRecord = (coriId, data) =>
  http.put(`/providers/cori/${coriId}/inactivate`, data).then(r => r.data);

// ── Provider Attachments (CI-117642-117650) ───────────────────────────────────
export const getProviderAttachments = (id) =>
  http.get(`/providers/${id}/attachments`).then(r => r.data);
export const uploadAttachment = (id, data) =>
  http.post(`/providers/${id}/attachments`, data).then(r => r.data);
export const updateAttachmentDescription = (attachmentId, data) =>
  http.put(`/providers/attachments/${attachmentId}/description`, data).then(r => r.data);
export const archiveAttachment = (attachmentId) =>
  http.put(`/providers/attachments/${attachmentId}/archive`).then(r => r.data);
export const restoreAttachment = (attachmentId) =>
  http.put(`/providers/attachments/${attachmentId}/restore`).then(r => r.data);

// ── Backup Provider Hours (CI-117646/117647) ──────────────────────────────────
export const getBackupProviderHours = (id) =>
  http.get(`/providers/${id}/backup-hours`).then(r => r.data);
export const createBackupProviderHours = (id, data) =>
  http.post(`/providers/${id}/backup-hours`, data).then(r => r.data);
export const modifyBackupProviderHours = (hoursId, data) =>
  http.put(`/providers/backup-hours/${hoursId}`, data).then(r => r.data);
export const terminateBackupProviderHours = (hoursId, data) =>
  http.put(`/providers/backup-hours/${hoursId}/terminate`, data).then(r => r.data);

// ── Monthly Paid Hours ────────────────────────────────────────────────────────
export const getMonthlyPaidHours = (id) =>
  http.get(`/providers/${id}/monthly-paid-hours`).then(r => r.data);

// ── SSN Verification (CMRS701E/CMRR701D) ─────────────────────────────────────
export const triggerSsnVerification = (id) =>
  http.put(`/providers/${id}/verify-ssn`).then(r => r.data);
export const updateSsnVerificationResult = (id, data) =>
  http.put(`/providers/${id}/ssn-verification-result`, data).then(r => r.data);
