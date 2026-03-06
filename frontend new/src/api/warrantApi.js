import http from './httpClient';

const base = '/warrants';

export const searchWarrants = (params = {}) => {
  const query = new URLSearchParams();
  if (params.warrantNumber) query.append('warrantNumber', params.warrantNumber);
  if (params.providerId) query.append('providerId', params.providerId);
  if (params.caseNumber) query.append('caseNumber', params.caseNumber);
  if (params.countyCode) query.append('countyCode', params.countyCode);
  if (params.status) query.append('status', params.status);
  if (params.startDate) query.append('startDate', params.startDate);
  if (params.endDate) query.append('endDate', params.endDate);
  const qs = query.toString();
  return http.get(`${base}${qs ? '?' + qs : ''}`).then(r => r.data);
};

export const getWarrantByNumber = (warrantNumber) => http.get(`${base}/${warrantNumber}`).then(r => r.data);

export const requestReplacement = (warrantNumber, data) => http.post(`${base}/${warrantNumber}/replace`, data).then(r => r.data);

export const getWarrantHistory = (warrantNumber) => http.get(`${base}/${warrantNumber}/history`).then(r => r.data);

export const getWarrantStats = () => http.get(`${base}/stats`).then(r => r.data);
