import http from './httpClient';

export const getFlexibleHours = (caseId) =>
  http.get(`/cases/${caseId}/flexible-hours`).then(r => r.data);

export const createFlexibleHours = (caseId, data) =>
  http.post(`/cases/${caseId}/flexible-hours`, data).then(r => r.data);

export const approveFlexibleHours = (id, data) =>
  http.put(`/cases/flexible-hours/${id}/approve`, data || {}).then(r => r.data);

export const denyFlexibleHours = (id) =>
  http.put(`/cases/flexible-hours/${id}/deny`).then(r => r.data);

export const cancelFlexibleHours = (id) =>
  http.put(`/cases/flexible-hours/${id}/cancel`).then(r => r.data);
