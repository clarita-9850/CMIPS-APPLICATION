import http from './httpClient';

const base = '/evv';

export const checkIn = (data) => http.post(`${base}/check-in`, data).then(r => r.data);
export const checkOut = (evvId, data) => http.post(`${base}/check-out/${evvId}`, data).then(r => r.data);
export const getMyRecords = () => http.get(`${base}/my-records`).then(r => r.data);
export const getActiveCheckin = () => http.get(`${base}/active-checkin`).then(r => r.data);
export const getTimesheetRecords = (timesheetId) => http.get(`${base}/timesheet/${timesheetId}`).then(r => r.data);
