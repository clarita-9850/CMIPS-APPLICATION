/**
 * County Contractor API
 * Dedicated API client for County Contractor Rate and Invoice endpoints.
 */

import http from './httpClient';

// ==================== Rate Endpoints ====================

export const getRatesByCounty = (countyCode) =>
  http.get(`/county-contractors/rates?countyCode=${countyCode}`).then(r => r.data);

export const getRateById = (id) =>
  http.get(`/county-contractors/rates/${id}`).then(r => r.data);

export const createRate = (data) =>
  http.post('/county-contractors/rates', data).then(r => r.data);

export const updateRate = (id, data) =>
  http.put(`/county-contractors/rates/${id}`, data).then(r => r.data);

// ==================== Invoice Endpoints ====================

export const getInvoicesByContractor = (countyContractorId) =>
  http.get(`/county-contractors/invoices?countyContractorId=${countyContractorId}`).then(r => r.data);

export const getInvoiceById = (id) =>
  http.get(`/county-contractors/invoices/${id}`).then(r => r.data);

export const modifyInvoice = (id, data) =>
  http.put(`/county-contractors/invoices/${id}`, data).then(r => r.data);

export const getInvoiceDetails = (id) =>
  http.get(`/county-contractors/invoices/${id}/details`).then(r => r.data);

export const getSoc432 = (id) =>
  http.get(`/county-contractors/invoices/${id}/soc432`).then(r => r.data);
