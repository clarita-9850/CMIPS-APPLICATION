import http from './httpClient';

export const lookupForEntry = (data) =>
  http.post('/sick-leave-claims/lookup', data).then(r => r.data);

export const saveClaim = (data) =>
  http.post('/sick-leave-claims', data).then(r => r.data);

export const listClaimsByProvider = (providerId) =>
  http.get(`/sick-leave-claims/provider/${providerId}`).then(r => r.data);

export const updateClaim = (claimNumber, data) =>
  http.put(`/sick-leave-claims/${claimNumber}`, data).then(r => r.data);

export const cancelClaim = (claimNumber) =>
  http.delete(`/sick-leave-claims/${claimNumber}`).then(r => r.data);

export const getClaimByNumber = (claimNumber) =>
  http.get(`/sick-leave-claims/${claimNumber}`).then(r => r.data);
