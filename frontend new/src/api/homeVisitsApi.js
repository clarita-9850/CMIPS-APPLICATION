import http from './httpClient';

export const getHomeVisits = (caseId) =>
  http.get(`/cases/${caseId}/home-visits`).then(r => r.data);

export const createHomeVisit = (caseId, data) =>
  http.post(`/cases/${caseId}/home-visits`, data).then(r => r.data);

export const getHomeVisit = (id) =>
  http.get(`/cases/home-visits/${id}`).then(r => r.data);
