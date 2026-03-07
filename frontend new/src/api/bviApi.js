import http from './httpClient';

const base = '/bvi-reviews';

export const listPending = (countyCode) =>
  http.get(`${base}/pending${countyCode ? '?countyCode=' + countyCode : ''}`).then(r => r.data);

export const listByRecipient = (recipientId) =>
  http.get(`${base}/recipient/${recipientId}`).then(r => r.data);

export const getByReviewNumber = (reviewNumber) =>
  http.get(`${base}/${reviewNumber}`).then(r => r.data);

export const approveBviReview = (id, confirmationCode = 'TTS-AUTO') =>
  http.put(`${base}/${id}/approve?confirmationCode=${confirmationCode}`).then(r => r.data);

export const rejectBviReview = (id, rejectionReason) =>
  http.put(`${base}/${id}/reject?rejectionReason=${encodeURIComponent(rejectionReason)}`).then(r => r.data);

export const expireOverdue = () =>
  http.post(`${base}/expire-overdue`).then(r => r.data);
