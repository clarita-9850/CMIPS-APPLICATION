import http from './httpClient';

const base = '/state-hearings';

// List all hearings for a case (by-case endpoint from StateHearingController)
export const getHearingsByCase = (caseId) =>
  http.get(`${base}/by-case/${caseId}`).then(r => r.data);

// Get single hearing
export const getHearing = (id) =>
  http.get(`${base}/${id}`).then(r => r.data);

// Create / schedule a new hearing for a case
export const createHearing = (data) =>
  http.post(`${base}`, data).then(r => r.data);

// Modify hearing (reschedule date, record outcome, compliance)
export const updateHearing = (id, data) =>
  http.put(`${base}/${id}`, data).then(r => r.data);

// Search (used by StateHearingSearchPage)
export const searchHearings = (params) =>
  http.get(`${base}/search`, { params }).then(r => r.data);

// Code tables
export const getStatusCodes = () =>
  http.get(`${base}/code-tables/status`).then(r => r.data);

export const getOutcomeCodes = () =>
  http.get(`${base}/code-tables/outcomes`).then(r => r.data);
