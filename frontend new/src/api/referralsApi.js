import http from './httpClient';

export const createReferral = (data) =>
  http.post('/referrals', data).then(r => r.data);

export const getReferralById = (id) =>
  http.get(`/referrals/${id}`).then(r => r.data);

export const getReferrals = (filters = {}) => {
  const qs = new URLSearchParams(filters).toString();
  return http.get(`/referrals${qs ? '?' + qs : ''}`).then(r => r.data);
};

export const updateReferralStatus = (id, status) =>
  http.patch(`/referrals/${id}/status`, { status }).then(r => r.data);

export const assignReferral = (id, data) =>
  http.patch(`/referrals/${id}/assign`, data).then(r => r.data);

export const updateReferralPriority = (id, priority) =>
  http.patch(`/referrals/${id}/priority`, { priority }).then(r => r.data);

export const closeReferral = (id, data) =>
  http.post(`/referrals/${id}/close`, data).then(r => r.data);

export const reopenReferral = (id, data) =>
  http.post(`/referrals/${id}/reopen`, data).then(r => r.data);

export const convertToApplication = (id, data) =>
  http.post(`/referrals/${id}/convert`, data).then(r => r.data);

export const searchReferrals = (params) => {
  const qs = new URLSearchParams(params).toString();
  return http.get(`/referrals/search?${qs}`).then(r => r.data);
};

export const getOpenReferrals = (countyCode) =>
  http.get(`/referrals/open${countyCode ? '?countyCode=' + countyCode : ''}`).then(r => r.data);

export const getUrgentReferrals = (countyCode) =>
  http.get(`/referrals/urgent${countyCode ? '?countyCode=' + countyCode : ''}`).then(r => r.data);
