import http from './httpClient';

const base = '/evv';

export const checkIn = (data) => http.post(`${base}/check-in`, data).then(r => r.data);
export const checkOut = (evvId, data) => http.post(`${base}/check-out/${evvId}`, data).then(r => r.data);
export const getMyRecords = () => http.get(`${base}/my-records`).then(r => r.data);
export const getActiveCheckin = () => http.get(`${base}/active-checkin`).then(r => r.data);
export const getTimesheetRecords = (timesheetId) => http.get(`${base}/timesheet/${timesheetId}`).then(r => r.data);

// ── EVV Exception Approval Workflow ──
const excBase = '/evv-exceptions';

export const listPendingExceptions = (countyCode) =>
  http.get(`${excBase}/pending${countyCode ? '?countyCode=' + countyCode : ''}`).then(r => r.data);

export const listExceptionsByTimesheet = (timesheetId) =>
  http.get(`${excBase}/timesheet/${timesheetId}`).then(r => r.data);

export const listExceptionsByProvider = (providerId) =>
  http.get(`${excBase}/provider/${providerId}`).then(r => r.data);

export const listExceptionsByCase = (caseId) =>
  http.get(`${excBase}/case/${caseId}`).then(r => r.data);

export const getExceptionByNumber = (exceptionNumber) =>
  http.get(`${excBase}/${exceptionNumber}`).then(r => r.data);

export const submitException = (data) =>
  http.post(excBase, data).then(r => r.data);

export const approveException = (id, reviewedBy, reviewNotes = '') =>
  http.put(`${excBase}/${id}/approve?reviewedBy=${encodeURIComponent(reviewedBy)}&reviewNotes=${encodeURIComponent(reviewNotes)}`).then(r => r.data);

export const denyException = (id, reviewedBy, denialReason) =>
  http.put(`${excBase}/${id}/deny?reviewedBy=${encodeURIComponent(reviewedBy)}&denialReason=${encodeURIComponent(denialReason)}`).then(r => r.data);
