import httpClient from './httpClient';

// ==================== Report Definitions (DSD Section 28) ====================

export const getReportDefinitions = () => httpClient.get('/reports/definitions');
export const getReportsByCategory = (category) => httpClient.get(`/reports/definitions/category/${category}`);
export const getReportDefinition = (reportCode) => httpClient.get(`/reports/definitions/${reportCode}`);
export const createReportDefinition = (data) => httpClient.post('/reports/definitions', data);
export const updateReportDefinition = (id, data) => httpClient.put(`/reports/definitions/${id}`, data);
export const inactivateReportDefinition = (id) => httpClient.put(`/reports/definitions/${id}/inactivate`);

// ==================== Report Executions (DSD Section 28) ====================

export const getRecentExecutions = () => httpClient.get('/reports/executions/recent');
export const getExecutionsByReport = (reportDefinitionId) => httpClient.get(`/reports/executions/report/${reportDefinitionId}`);
export const getExecution = (id) => httpClient.get(`/reports/executions/${id}`);
export const executeReport = (reportCode, params) => httpClient.post(`/reports/execute/${reportCode}`, params || {});
export const getRunningExecutions = () => httpClient.get('/reports/executions/running');

// ==================== Fraud Cases (DSD Section 26) ====================

export const getFraudCasesByCaseId = (caseId) => httpClient.get(`/fraud/cases/${caseId}`);
export const getFraudCasesByProvider = (providerId) => httpClient.get(`/fraud/providers/${providerId}`);
export const getFraudCasesByStatus = (status) => httpClient.get(`/fraud/status/${status}`);
export const getFraudCase = (id) => httpClient.get(`/fraud/${id}`);
export const createFraudCase = (data) => httpClient.post('/fraud', data);
export const updateFraudCase = (id, data) => httpClient.put(`/fraud/${id}`, data);
export const substantiateFraudCase = (id, data) => httpClient.put(`/fraud/${id}/substantiate`, data);
export const referFraudToDA = (id, data) => httpClient.put(`/fraud/${id}/refer-to-da`, data);
export const closeFraudCase = (id) => httpClient.put(`/fraud/${id}/close`, {});
export const getFraudByInvestigator = (investigatorId) => httpClient.get(`/fraud/investigator/${investigatorId}`);

// ==================== Targeted Mailings (DSD Section 26) ====================

export const getTargetedMailings = (countyCode) => httpClient.get('/targeted-mailings', { params: { countyCode } });
export const getMailingsByStatus = (status) => httpClient.get(`/targeted-mailings/status/${status}`);
export const getTargetedMailing = (id) => httpClient.get(`/targeted-mailings/${id}`);
export const createTargetedMailing = (data) => httpClient.post('/targeted-mailings', data);
export const updateTargetedMailing = (id, data) => httpClient.put(`/targeted-mailings/${id}`, data);
export const scheduleMailing = (id, data) => httpClient.put(`/targeted-mailings/${id}/schedule`, data);
export const executeMailing = (id) => httpClient.put(`/targeted-mailings/${id}/execute`);
export const cancelMailing = (id) => httpClient.put(`/targeted-mailings/${id}/cancel`);

// ==================== Death Match (DSD Section 26) ====================

export const getPendingDeathMatches = () => httpClient.get('/death-match/pending');
export const getDeathMatchByPerson = (personId, personType) => httpClient.get(`/death-match/person/${personId}/${personType}`);
export const getDeathMatchBySource = (source, status) => httpClient.get(`/death-match/source/${source}`, { params: { status } });
export const getDeathMatch = (id) => httpClient.get(`/death-match/${id}`);
export const createDeathMatch = (data) => httpClient.post('/death-match', data);
export const verifyDeathMatch = (id, data) => httpClient.put(`/death-match/${id}/verify`, data);
export const markFalseMatch = (id, data) => httpClient.put(`/death-match/${id}/false-match`, data);
export const recordDeathMatchAction = (id, data) => httpClient.put(`/death-match/${id}/action`, data);
