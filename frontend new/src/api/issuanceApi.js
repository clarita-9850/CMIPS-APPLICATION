import http from './httpClient';

const base = '/timesheet-issuances';

export const listByCase = (caseId) => http.get(`${base}/case/${caseId}`).then(r => r.data);
export const listByProvider = (providerId) => http.get(`${base}/provider/${providerId}`).then(r => r.data);
export const listPendingGeneration = () => http.get(`${base}/pending-generation`).then(r => r.data);
export const listPendingPrint = () => http.get(`${base}/pending-print`).then(r => r.data);
export const getByIssuanceNumber = (num) => http.get(`${base}/${num}`).then(r => r.data);
export const createIssuance = (data) => http.post(base, data).then(r => r.data);
export const generateTimesheet = (id) => http.put(`${base}/${id}/generate`).then(r => r.data);
export const markMailed = (id) => http.put(`${base}/${id}/mail`).then(r => r.data);
export const deliverElectronic = (id) => http.put(`${base}/${id}/deliver-electronic`).then(r => r.data);
export const cancelIssuance = (id, reason) => http.put(`${base}/${id}/cancel?reason=${encodeURIComponent(reason)}`).then(r => r.data);
export const reissue = (id, reason) => http.post(`${base}/${id}/reissue?reason=${encodeURIComponent(reason)}`).then(r => r.data);
export const batchGenerate = () => http.post(`${base}/batch-generate`).then(r => r.data);
