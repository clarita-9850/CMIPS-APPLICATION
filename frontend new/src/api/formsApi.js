import http from './httpClient';

// Electronic forms
export const getForms = (caseId) =>
  http.get(`/cases/${caseId}/forms`).then(r => r.data);

export const requestForm = (caseId, data) =>
  http.post(`/cases/${caseId}/forms`, data).then(r => r.data);

export const downloadForm = (id) =>
  http.get(`/cases/forms/${id}/download`).then(r => r.data);

export const inactivateForm = (id) =>
  http.put(`/cases/forms/${id}/inactivate`).then(r => r.data);

export const suppressForm = (id) =>
  http.put(`/cases/forms/${id}/suppress`).then(r => r.data);

export const markFormPrinted = (id) =>
  http.put(`/cases/forms/${id}/mark-printed`).then(r => r.data);

// ESP (e-timesheet) enrollment
export const getEspRegistrations = (caseId) =>
  http.get(`/cases/${caseId}/esp-registrations`).then(r => r.data);

export const inactivateEsp = (espId, data) =>
  http.put(`/cases/esp-registrations/${espId}/inactivate`, data || {}).then(r => r.data);

export const reactivateEsp = (espId, data) =>
  http.put(`/cases/esp-registrations/${espId}/reactivate`, data || {}).then(r => r.data);

export const downloadSoc2321 = (espId) =>
  http.get(`/cases/esp-registrations/${espId}/soc2321`).then(r => r.data);

// Contractor invoices
export const getContractorInvoices = (caseId) =>
  http.get(`/cases/${caseId}/contractor-invoices`).then(r => r.data);

export const createContractorInvoice = (caseId, data) =>
  http.post(`/cases/${caseId}/contractor-invoices`, data).then(r => r.data);

export const authorizeInvoice = (id, data) =>
  http.put(`/cases/contractor-invoices/${id}/authorize`, data || {}).then(r => r.data);

export const rejectInvoice = (id, data) =>
  http.put(`/cases/contractor-invoices/${id}/reject`, data).then(r => r.data);
