import http from './httpClient';

const base = '/timesheets';

export const getTimesheets = (params = {}) => {
  const query = new URLSearchParams();
  if (params.status) query.append('status', params.status);
  if (params.userId) query.append('userId', params.userId);
  if (params.department) query.append('department', params.department);
  if (params.page !== undefined) query.append('page', params.page);
  if (params.size) query.append('size', params.size);
  const qs = query.toString();
  return http.get(`${base}${qs ? '?' + qs : ''}`).then(r => r.data);
};

export const getTimesheetById = (id) => http.get(`${base}/${id}`).then(r => r.data);

export const createTimesheet = (data) => http.post(base, data).then(r => r.data);

export const updateTimesheet = (id, data) => http.put(`${base}/${id}`, data).then(r => r.data);

export const deleteTimesheet = (id) => http.delete(`${base}/${id}`).then(r => r.data);

export const submitTimesheet = (id) => http.post(`${base}/${id}/submit`).then(r => r.data);

export const approveTimesheet = (id) => http.post(`${base}/${id}/approve`).then(r => r.data);

export const rejectTimesheet = (id, comments) => http.post(`${base}/${id}/reject`, null, { params: { comments } }).then(r => r.data);

export const getTimesheetActions = () => http.get(`${base}/actions`).then(r => r.data);
