import http from './httpClient';

const base = '/service-eligibility';

export const getAssessments = (caseId) => http.get(`${base}/case/${caseId}/assessments`).then(r => r.data);
export const getAssessmentById = (id) => http.get(`${base}/assessments/${id}`).then(r => r.data);
export const createAssessment = (caseId, data) => http.post(`${base}/case/${caseId}/assessments`, data).then(r => r.data);
export const scheduleReassessment = (caseId, data) => http.post(`${base}/case/${caseId}/schedule-reassessment`, data).then(r => r.data);
export const getHomeVisits = (caseId) => http.get(`${base}/case/${caseId}/home-visits`).then(r => r.data);
export const createHomeVisit = (caseId, data) => http.post(`${base}/case/${caseId}/home-visits`, data).then(r => r.data);
export const getServicePlans = (caseId) => http.get(`${base}/case/${caseId}/service-plans`).then(r => r.data);
export const createServicePlan = (caseId, data) => http.post(`${base}/case/${caseId}/service-plans`, data).then(r => r.data);
export const getHealthCareCerts = (caseId) => http.get(`${base}/case/${caseId}/health-care-certs`).then(r => r.data);
export const createHealthCareCert = (caseId, data) => http.post(`${base}/case/${caseId}/health-care-certs`, data).then(r => r.data);
