import http from './httpClient';

const base = '/career-pathway';

export const getByProvider = (providerId) =>
  http.get(`${base}/claims?providerId=${providerId}`).then(r => r.data);

export const create = (data) =>
  http.post(`${base}/claims`, data).then(r => r.data);

export const getById = (id) =>
  http.get(`${base}/claims/${id}`).then(r => r.data);

export const submitForApproval = (id, updates = {}) =>
  http.put(`${base}/claims/${id}/submit-for-approval`, updates).then(r => r.data);

export const approve = (id) =>
  http.put(`${base}/claims/${id}/approve`).then(r => r.data);

export const reject = (id, rejectionReason, notes = '') =>
  http.put(`${base}/claims/${id}/reject`, { rejectionReason, notes }).then(r => r.data);

export const reissue = (id) =>
  http.post(`${base}/claims/${id}/reissue`).then(r => r.data);

export const getPendingReview = () =>
  http.get(`${base}/claims/pending-review`).then(r => r.data);

export const getPendingApproval = () =>
  http.get(`${base}/claims/pending-approval`).then(r => r.data);

export const getCumulativeHours = (providerId) =>
  http.get(`${base}/cumulative-hours/${providerId}`).then(r => r.data);
