import http from './httpClient';

const base = '/bi';

export const generateReport = (data) => http.post(`${base}/reports/generate`, data).then(r => r.data);
export const getJobStatus = (jobId) => http.get(`${base}/jobs/${jobId}/status`).then(r => r.data);
export const getJobResult = (jobId) => http.get(`${base}/jobs/${jobId}/result`).then(r => r.data);
export const downloadJob = (jobId) => http.get(`${base}/jobs/${jobId}/download`, { responseType: 'blob' }).then(r => r.data);
export const cancelJob = (jobId) => http.post(`${base}/jobs/${jobId}/cancel`).then(r => r.data);
export const getAllJobs = () => http.get(`${base}/jobs/status/ALL`).then(r => r.data);
export const getJobsByStatus = (status) => http.get(`${base}/jobs/status/${status}`).then(r => r.data);
