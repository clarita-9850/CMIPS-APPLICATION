import http from './httpClient';

const base = '/pipeline';

export const extractEnhanced = (data) => http.post(`${base}/extract-enhanced`, data).then(r => r.data);
export const getMaskingRules = (userRole) => http.get(`${base}/masking-rules/${userRole}`).then(r => r.data);
export const updateMaskingRules = (userRole, data) => http.post(`${base}/masking-rules/${userRole}`, data).then(r => r.data);
export const getUserRoles = () => http.get(`${base}/user-roles`).then(r => r.data);
export const getCounties = () => http.get(`${base}/counties`).then(r => r.data);
export const getReportTypes = () => http.get(`${base}/report-types`).then(r => r.data);
export const generateReport = (data) => http.post(`${base}/generate-report`, data).then(r => r.data);
export const getStatus = () => http.get(`${base}/status`).then(r => r.data);
export const getFieldVisibility = (userRole) => http.get(`${base}/field-visibility/${userRole}`).then(r => r.data);
export const getAvailableFields = () => http.get(`${base}/available-fields`).then(r => r.data);
export const compareRoles = (data) => http.post(`${base}/compare-roles`, data).then(r => r.data);
