import http from './httpClient';

const base = '/waivers';

export const createWaiver = (data) => http.post(base, data).then(r => r.data);
export const getWaiver = (id) => http.get(`${base}/${id}`).then(r => r.data);
export const searchWaivers = (params) => {
  const query = new URLSearchParams(params).toString();
  return http.get(`${base}/search?${query}`).then(r => r.data);
};
export const getActiveWaivers = () => http.get(`${base}/active`).then(r => r.data);
export const getPendingWaivers = () => http.get(`${base}/pending`).then(r => r.data);
export const getPendingCountyReview = () => http.get(`${base}/pending-county-review`).then(r => r.data);
export const getPendingSupervisorReview = () => http.get(`${base}/pending-supervisor-review`).then(r => r.data);
export const getExpiringWaivers = () => http.get(`${base}/expiring`).then(r => r.data);
export const getByRecipient = (recipientId) => http.get(`${base}/recipient/${recipientId}`).then(r => r.data);
export const getByProvider = (providerId) => http.get(`${base}/provider/${providerId}`).then(r => r.data);
export const hasActiveWaiver = (providerId) => http.get(`${base}/provider/${providerId}/has-active`).then(r => r.data);
export const getStats = (countyCode) => http.get(`${base}/stats/${countyCode}`).then(r => r.data);

// Workflow actions
export const disclose = (id, data) => http.post(`${base}/${id}/disclose`, data).then(r => r.data);
export const recipientDecision = (id, data) => http.post(`${base}/${id}/recipient-decision`, data).then(r => r.data);
export const signSoc2298 = (id, data) => http.post(`${base}/${id}/soc-2298/sign`, data).then(r => r.data);
export const submitForReview = (id) => http.post(`${base}/${id}/submit-for-review`).then(r => r.data);
export const submitCountyReview = (id) => http.post(`${base}/${id}/submit-county-review`).then(r => r.data);
export const assignReviewer = (id, data) => http.post(`${base}/${id}/assign-reviewer`, data).then(r => r.data);
export const countyDecision = (id, data) => http.post(`${base}/${id}/county-decision`, data).then(r => r.data);
export const assignSupervisor = (id, data) => http.post(`${base}/${id}/assign-supervisor`, data).then(r => r.data);
export const supervisorDecision = (id, data) => http.post(`${base}/${id}/supervisor-decision`, data).then(r => r.data);
export const revokeWaiver = (id, data) => http.post(`${base}/${id}/revoke`, data).then(r => r.data);
