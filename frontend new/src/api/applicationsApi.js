import http from './httpClient';

const base = '/applications';

export const searchApplications = (params) => http.get(base, { params }).then(r => {
  const d = r.data;
  return Array.isArray(d) ? d : (d?.content || d?.items || []);
});
export const getApplicationById = (id) => http.get(`${base}/${id}`).then(r => r.data);
export const createApplication = (data) => http.post(base, data).then(r => r.data);
export const updateApplication = (id, data) => http.put(`${base}/${id}`, data).then(r => r.data);
export const checkDuplicate = (data) => http.post(`${base}/duplicate-check`, data).then(r => r.data);
export const cinClearance = (id) => http.post(`${base}/${id}/cin-clearance`).then(r => r.data);
export const ssnValidation = (id) => http.post(`${base}/${id}/ssn-validation`).then(r => r.data);
export const approveApplication = (id, data) => http.post(`${base}/${id}/approve`, data).then(r => r.data);
export const denyApplication = (id, data) => http.post(`${base}/${id}/deny`, data).then(r => r.data);
export const assignProgram = (id, data) => http.post(`${base}/${id}/assign-program`, data).then(r => r.data);
