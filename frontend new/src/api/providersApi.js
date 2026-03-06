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

export const createProvider = (data) => {
  console.log('[providersApi] createProvider called with:', data);
  return http.post('/providers', data).then(r => {
    console.log('[providersApi] createProvider response:', r);
    return r.data;
  });
};

export const approveEnrollment = (id) =>
  http.put(`/providers/${id}/approve-enrollment`).then(r => r.data);

export const rejectEnrollment = (id, data) =>
  http.put(`/providers/${id}/reject-enrollment`, data).then(r => r.data);

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

export const modifyCori = (coriId, data) =>
  http.put(`/providers/cori/${coriId}/modify`, data).then(r => r.data);

export const inactivateCori = (coriId) =>
  http.put(`/providers/cori/${coriId}/modify`, { status: 'INACTIVE' }).then(r => r.data);

export const addGeneralException = (coriId, data) =>
  http.put(`/providers/cori/${coriId}/general-exception`, data).then(r => r.data);

export const getProviderViolations = (id) =>
  http.get(`/providers/${id}/violations`).then(r => r.data);

export const createViolation = (id, data) =>
  http.post(`/providers/${id}/violations`, data).then(r => r.data);

export const modifyEnrollment = (id, data) =>
  http.put(`/providers/${id}/modify-enrollment`, data).then(r => r.data);

export const getEligibleProviders = (countyCode) =>
  http.get(`/providers/eligible${countyCode ? '?countyCode=' + countyCode : ''}`).then(r => r.data);

export const getEnrollmentHistory = (id) =>
  http.get(`/providers/${id}/enrollment-history`).then(r => r.data);

export const getPreIneligibilityData = (id) =>
  http.get(`/providers/${id}/pre-ineligibility-data`).then(r => r.data);

export const triggerSsnVerification = (providerId) =>
  http.post('/integration/ssa/verify-ssn', { providerNumber: providerId }).then(r => r.data);

export const triggerBackgroundCheck = (providerId) =>
  http.post('/integration/doj/background-check', { providerNumber: providerId }).then(r => r.data);

export const checkMediCalStatus = (providerId) =>
  http.post('/integration/medi-cal/check-suspended', { providerNumber: providerId }).then(r => r.data);

/** Run all verifications (SSN, DOJ, Medi-Cal) and auto-update provider entity */
export const runAllVerifications = (providerId) =>
  http.post(`/providers/${providerId}/run-verifications`).then(r => r.data);

/** Update enrollment requirements and trigger auto-eligibility check */
export const updateEnrollmentRequirements = (providerId, data) =>
  http.put(`/providers/${providerId}/update-enrollment-requirements`, data).then(r => r.data);
