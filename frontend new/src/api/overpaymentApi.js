import http from './httpClient';

const caseBase = (caseId) => `/cases/${caseId}/overpayments`;
const opBase = (id) => `/overpayments/${id}`;

export const getActive = (caseId) =>
  http.get(caseBase(caseId)).then(r => r.data);

export const getAll = (caseId) =>
  http.get(`${caseBase(caseId)}/all`).then(r => r.data);

export const create = (caseId, data) =>
  http.post(caseBase(caseId), data).then(r => r.data);

export const getById = (id) =>
  http.get(opBase(id)).then(r => r.data);

export const update = (id, data) =>
  http.put(opBase(id), data).then(r => r.data);

export const submit = (id) =>
  http.post(`${opBase(id)}/submit`).then(r => r.data);

export const cancel = (id) =>
  http.put(`${opBase(id)}/cancel`).then(r => r.data);

export const stop = (id) =>
  http.put(`${opBase(id)}/stop`).then(r => r.data);

export const getCollections = (id) =>
  http.get(`${opBase(id)}/collections`).then(r => r.data);

export const addCollection = (id, data) =>
  http.post(`${opBase(id)}/collections`, data).then(r => r.data);
