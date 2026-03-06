import http from './httpClient';

export const listAttachments = (caseId) =>
  http.get(`/cases/${caseId}/attachments`).then(r => r.data);

export const uploadAttachment = (caseId, formData) =>
  http.post(`/cases/${caseId}/attachments`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(r => r.data);

export const archiveAttachment = (id) =>
  http.put(`/cases/attachments/${id}/archive`).then(r => r.data);

export const restoreAttachment = (id) =>
  http.put(`/cases/attachments/${id}/restore`).then(r => r.data);

export const downloadAttachment = (id) =>
  http.get(`/cases/attachments/${id}/download`).then(r => r.data);
