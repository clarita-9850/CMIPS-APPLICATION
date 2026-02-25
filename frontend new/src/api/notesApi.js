import http from './httpClient';

export const getPersonNotes = (personId, activeOnly = true) =>
  http.get(`/notes/person/${personId}?activeOnly=${activeOnly}`).then(r => r.data);

export const getCaseNotes = (caseId) =>
  http.get(`/notes/case/${caseId}`).then(r => r.data);

export const getNoteById = (noteId) =>
  http.get(`/notes/${noteId}`).then(r => r.data);

export const createRecipientNote = (recipientId, data) =>
  http.post(`/notes/recipient/${recipientId}`, data).then(r => r.data);

export const createProviderNote = (providerId, data) =>
  http.post(`/notes/provider/${providerId}`, data).then(r => r.data);

export const createCaseNote = (caseId, data) =>
  http.post(`/notes/case/${caseId}`, data).then(r => r.data);

export const updateNote = (noteId, data) =>
  http.put(`/notes/${noteId}`, data).then(r => r.data);

export const inactivateNote = (noteId, data) =>
  http.post(`/notes/${noteId}/inactivate`, data).then(r => r.data);

export const searchNotes = (params) => {
  const qs = new URLSearchParams(params).toString();
  return http.get(`/notes/search?${qs}`).then(r => r.data);
};

export const getRecentNotes = (personId, days = 30) =>
  http.get(`/notes/person/${personId}/recent?days=${days}`).then(r => r.data);

export const getNoteCount = (personId) =>
  http.get(`/notes/person/${personId}/count`).then(r => r.data);
