import http from './httpClient';

export const searchRecipients = (params) => {
  const qs = new URLSearchParams(params).toString();
  return http.get(`/recipients/search?${qs}`).then(r => r.data);
};

export const getRecipientById = (id) =>
  http.get(`/recipients/${id}`).then(r => r.data);

export const getRecipients = (page = 0, size = 50) =>
  http.get(`/recipients?page=${page}&size=${size}`).then(r => r.data);

export const createRecipient = (data) =>
  http.post('/recipients', data).then(r => r.data);

export const updateRecipient = (id, data) =>
  http.put(`/recipients/${id}`, data).then(r => r.data);

export const updateAddress = (id, data) =>
  http.put(`/recipients/${id}/address`, data).then(r => r.data);

export const createReferral = (data) =>
  http.post('/recipients/referrals', data).then(r => r.data);

export const closeReferral = (id, data) =>
  http.put(`/recipients/referrals/${id}/close`, data).then(r => r.data);

export const reopenReferral = (id, data) =>
  http.put(`/recipients/referrals/${id}/reopen`, data).then(r => r.data);

export const getOpenReferrals = (countyCode) =>
  http.get(`/recipients/referrals/open${countyCode ? '?countyCode=' + countyCode : ''}`).then(r => r.data);

export const getClosedReferrals = (countyCode) =>
  http.get(`/recipients/referrals/closed${countyCode ? '?countyCode=' + countyCode : ''}`).then(r => r.data);

export const getCompanionCases = (id) =>
  http.get(`/recipients/${id}/companion-cases`).then(r => r.data);
