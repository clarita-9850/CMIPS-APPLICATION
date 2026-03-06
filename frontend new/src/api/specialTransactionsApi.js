import http from './httpClient';

const caseBase = (caseId) => `/cases/${caseId}/special-transactions`;
const txnBase = (id) => `/special-transactions/${id}`;

export const getByCase = (caseId) =>
  http.get(caseBase(caseId)).then(r => r.data);

export const create = (caseId, data) =>
  http.post(caseBase(caseId), data).then(r => r.data);

export const getById = (id) =>
  http.get(txnBase(id)).then(r => r.data);

export const update = (id, data) =>
  http.put(txnBase(id), data).then(r => r.data);

export const submit = (id) =>
  http.post(`${txnBase(id)}/submit`).then(r => r.data);

export const approve = (id) =>
  http.put(`${txnBase(id)}/approve`).then(r => r.data);

export const reject = (id, reason) =>
  http.put(`${txnBase(id)}/reject`, { reason }).then(r => r.data);

export const cancel = (id) =>
  http.put(`${txnBase(id)}/cancel`).then(r => r.data);

export const getPendingApproval = () =>
  http.get('/special-transactions/pending-approval').then(r => r.data);
