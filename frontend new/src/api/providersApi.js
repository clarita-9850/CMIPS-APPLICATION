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
