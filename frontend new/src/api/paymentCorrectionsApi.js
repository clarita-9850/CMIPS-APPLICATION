import http from './httpClient';

const caseBase = (caseId) => `/cases/${caseId}/payment-corrections`;
const corrBase = (id) => `/payment-corrections/${id}`;

export const getByCase = (caseId) =>
  http.get(caseBase(caseId)).then(r => r.data);

export const create = (caseId, data) =>
  http.post(caseBase(caseId), data).then(r => r.data);

export const getById = (id) =>
  http.get(corrBase(id)).then(r => r.data);

export const update = (id, data) =>
  http.put(corrBase(id), data).then(r => r.data);

export const submit = (id) =>
  http.post(`${corrBase(id)}/submit`).then(r => r.data);

export const approve = (id) =>
  http.put(`${corrBase(id)}/approve`).then(r => r.data);

export const reject = (id, reason) =>
  http.put(`${corrBase(id)}/reject`, { reason }).then(r => r.data);

export const cancel = (id) =>
  http.put(`${corrBase(id)}/cancel`).then(r => r.data);

export const getPendingApproval = () =>
  http.get('/payment-corrections/pending-approval').then(r => r.data);
