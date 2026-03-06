import http from './httpClient';

const base = '/payments';

// ─── Payment Search ────────────────────────────────────────────────────────

export const searchByPerson = (params = {}) => {
  const q = new URLSearchParams();
  if (params.payeeId) q.append('payeeId', params.payeeId);
  if (params.servicePeriodFrom) q.append('servicePeriodFrom', params.servicePeriodFrom);
  if (params.servicePeriodTo) q.append('servicePeriodTo', params.servicePeriodTo);
  if (params.issueFrom) q.append('issueFrom', params.issueFrom);
  if (params.issueTo) q.append('issueTo', params.issueTo);
  if (params.warrantNumber) q.append('warrantNumber', params.warrantNumber);
  return http.get(`${base}/search/by-person?${q.toString()}`).then(r => r.data);
};

export const searchByCase = (params = {}) => {
  const q = new URLSearchParams();
  if (params.caseNumber) q.append('caseNumber', params.caseNumber);
  if (params.servicePeriodFrom) q.append('servicePeriodFrom', params.servicePeriodFrom);
  if (params.servicePeriodTo) q.append('servicePeriodTo', params.servicePeriodTo);
  if (params.issueFrom) q.append('issueFrom', params.issueFrom);
  if (params.issueTo) q.append('issueTo', params.issueTo);
  if (params.payeeName) q.append('payeeName', params.payeeName);
  if (params.warrantNumber) q.append('warrantNumber', params.warrantNumber);
  return http.get(`${base}/search/by-case?${q.toString()}`).then(r => r.data);
};

export const getPaymentDetails = (warrantId) =>
  http.get(`${base}/${warrantId}`).then(r => r.data);

// ─── Void / Stop / Reissue ─────────────────────────────────────────────────

export const requestVoidOrReissue = (warrantId, data) =>
  http.post(`${base}/${warrantId}/void-reissue`, data).then(r => r.data);

// ─── Cashed Warrant Copy ───────────────────────────────────────────────────

export const requestCashedCopy = (warrantId, data) =>
  http.post(`${base}/${warrantId}/cashed-copies`, data).then(r => r.data);

export const cancelCashedCopy = (id, data) =>
  http.put(`${base}/cashed-copies/${id}/cancel`, data).then(r => r.data);

// ─── Forged Endorsement Affidavit ─────────────────────────────────────────

export const createAffidavit = (warrantId, data) =>
  http.post(`${base}/${warrantId}/forged-affidavits`, data).then(r => r.data);

export const updateAffidavit = (id, data) =>
  http.put(`${base}/forged-affidavits/${id}`, data).then(r => r.data);

export const cancelAffidavit = (id) =>
  http.put(`${base}/forged-affidavits/${id}/cancel`).then(r => r.data);
