import http from './httpClient';

const base = '/analytics';

export const getRealtimeMetrics = () => http.get(`${base}/realtime-metrics`).then(r => r.data);
export const getFilters = () => http.get(`${base}/filters`).then(r => r.data);
export const getAdhocFilters = () => http.get(`${base}/adhoc-filters`).then(r => r.data);
export const getAdhocStats = (params) => {
  const query = new URLSearchParams(params).toString();
  return http.get(`${base}/adhoc-stats?${query}`).then(r => r.data);
};
export const getAdhocData = (params) => {
  const query = new URLSearchParams(params).toString();
  return http.get(`${base}/adhoc-data?${query}`).then(r => r.data);
};
export const getGenderDemographics = () => http.get(`${base}/demographics/gender`).then(r => r.data);
export const getEthnicityDemographics = () => http.get(`${base}/demographics/ethnicity`).then(r => r.data);
export const getAgeDemographics = () => http.get(`${base}/demographics/age`).then(r => r.data);
export const getHealthStatus = () => http.get(`${base}/health`).then(r => r.data);
