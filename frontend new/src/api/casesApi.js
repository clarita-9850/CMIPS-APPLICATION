import http from './httpClient';

export const getCases = (filters = {}) => {
  const qs = new URLSearchParams(filters).toString();
  return http.get(`/cases${qs ? '?' + qs : ''}`).then(r => r.data);
};

export const getCaseById = (id) =>
  http.get(`/cases/${id}`).then(r => r.data);

export const searchCases = (params) => {
  const qs = new URLSearchParams(params).toString();
  return http.get(`/cases/search?${qs}`).then(r => r.data);
};

export const createCase = (data) =>
  http.post('/cases', data).then(r => r.data);

export const approveCase = (id) =>
  http.put(`/cases/${id}/approve`).then(r => r.data);

export const denyCase = (id, data) =>
  http.put(`/cases/${id}/deny`, data).then(r => r.data);

export const terminateCase = (id, data) =>
  http.put(`/cases/${id}/terminate`, data).then(r => r.data);

export const assignCase = (id, data) =>
  http.put(`/cases/${id}/assign`, data).then(r => r.data);

export const withdrawCase = (id, data) =>
  http.put(`/cases/${id}/withdraw`, data).then(r => r.data);

export const placeOnLeave = (id, data) =>
  http.put(`/cases/${id}/leave`, data).then(r => r.data);

export const getCaseNotes = (id) =>
  http.get(`/cases/${id}/notes`).then(r => r.data);

export const addCaseNote = (id, data) =>
  http.post(`/cases/${id}/notes`, data).then(r => r.data);

export const appendCaseNote = (noteId, data) =>
  http.put(`/cases/notes/${noteId}/append`, data).then(r => r.data);

export const cancelCaseNote = (noteId, data) =>
  http.put(`/cases/notes/${noteId}/cancel`, data).then(r => r.data);

export const getCaseContacts = (id) =>
  http.get(`/cases/${id}/contacts`).then(r => r.data);

export const addCaseContact = (id, data) =>
  http.post(`/cases/${id}/contacts`, data).then(r => r.data);

export const inactivateContact = (contactId) =>
  http.put(`/cases/contacts/${contactId}/inactivate`).then(r => r.data);

export const initiateTransfer = (id, data) =>
  http.post(`/cases/${id}/transfer/initiate`, data).then(r => r.data);

export const completeTransfer = (id, data) =>
  http.post(`/cases/${id}/transfer/complete`, data).then(r => r.data);

export const getCaseStatistics = (countyCode) =>
  http.get(`/cases/statistics/${countyCode}`).then(r => r.data);

export const getDueForReassessment = (date) =>
  http.get(`/cases/due-for-reassessment${date ? '?date=' + date : ''}`).then(r => r.data);

export const getCaseStatusHistory = (id) =>
  http.get(`/cases/${id}/status-history`).then(r => r.data);

export const getCaseCodeTables = () =>
  http.get('/cases/code-tables').then(r => r.data);
