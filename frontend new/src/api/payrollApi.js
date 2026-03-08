import httpClient from './httpClient';

// ==================== Advance Pay (DSD Section 14) ====================

export const getAdvancePayByCaseId = (caseId) => httpClient.get(`/advance-pay/cases/${caseId}`);
export const getAdvancePayByProvider = (providerId) => httpClient.get(`/advance-pay/providers/${providerId}`);
export const getAdvancePay = (id) => httpClient.get(`/advance-pay/${id}`);
export const createAdvancePay = (caseId, data) => httpClient.post(`/advance-pay/cases/${caseId}`, data);
export const issueAdvancePay = (id) => httpClient.put(`/advance-pay/${id}/issue`);
export const cancelAdvancePay = (id) => httpClient.put(`/advance-pay/${id}/cancel`);
export const recoverAdvancePay = (id) => httpClient.put(`/advance-pay/${id}/recover`);
export const getPendingAdvancePay = () => httpClient.get('/advance-pay/pending');

// ==================== Deductions (DSD Section 15) ====================

export const getDeductions = (providerId) => httpClient.get(`/deductions/providers/${providerId}`);
export const getActiveDeductions = (providerId) => httpClient.get(`/deductions/providers/${providerId}/active`);
export const getDeduction = (id) => httpClient.get(`/deductions/${id}`);
export const createDeduction = (providerId, data) => httpClient.post(`/deductions/providers/${providerId}`, data);
export const updateDeduction = (id, data) => httpClient.put(`/deductions/${id}`, data);
export const suspendDeduction = (id) => httpClient.put(`/deductions/${id}/suspend`);
export const reactivateDeduction = (id) => httpClient.put(`/deductions/${id}/reactivate`);
export const deleteDeduction = (id) => httpClient.delete(`/deductions/${id}`);

// ==================== Pay Rates (DSD Section 16) ====================

export const getPayRates = (countyCode) => httpClient.get(`/pay-rates/${countyCode}`);
export const getPayRatesByType = (countyCode, rateType) => httpClient.get(`/pay-rates/${countyCode}/${rateType}`);
export const getCurrentPayRate = (countyCode, rateType) => httpClient.get(`/pay-rates/${countyCode}/${rateType}/current`);
export const createPayRate = (data) => httpClient.post('/pay-rates', data);
export const updatePayRate = (id, data) => httpClient.put(`/pay-rates/${id}`, data);
export const inactivatePayRate = (id) => httpClient.put(`/pay-rates/${id}/inactivate`);

// ==================== Tax & Contribution (DSD Section 18) ====================

export const getTaxRecords = (providerId) => httpClient.get(`/tax/providers/${providerId}`);
export const getTaxByYear = (providerId, year) => httpClient.get(`/tax/providers/${providerId}/${year}`);
export const getTaxByQuarter = (providerId, year, quarter) => httpClient.get(`/tax/providers/${providerId}/${year}/${quarter}`);
export const createTaxRecord = (providerId, data) => httpClient.post(`/tax/providers/${providerId}`, data);
export const updateTaxRecord = (id, data) => httpClient.put(`/tax/${id}`, data);
export const generateW2 = (id) => httpClient.put(`/tax/${id}/generate-w2`);
export const getPendingW2 = (year) => httpClient.get(`/tax/w2-pending/${year}`);

// ==================== Payroll Batch (DSD Section 17) ====================

export const getRecentBatches = () => httpClient.get('/payroll-batch/recent');
export const getPayrollBatch = (id) => httpClient.get(`/payroll-batch/${id}`);
export const getPayrollBatchByNumber = (batchNumber) => httpClient.get(`/payroll-batch/batch-number/${batchNumber}`);
export const getPayrollBatchesByStatus = (status) => httpClient.get(`/payroll-batch/status/${status}`);
export const createPayrollBatch = (data) => httpClient.post('/payroll-batch', data);
export const processPayrollBatch = (id) => httpClient.put(`/payroll-batch/${id}/process`);
export const completePayrollBatch = (id) => httpClient.put(`/payroll-batch/${id}/complete`);
export const failPayrollBatch = (id, data) => httpClient.put(`/payroll-batch/${id}/fail`, data);
export const cancelPayrollBatch = (id) => httpClient.put(`/payroll-batch/${id}/cancel`);

// ==================== Earnings Statements (DSD Section 17) ====================

export const getEarningsStatements = (providerId) => httpClient.get(`/earnings-statements/providers/${providerId}`);
export const getEarningsStatement = (id) => httpClient.get(`/earnings-statements/${id}`);
export const getEarningsStatementByWarrant = (warrantId) => httpClient.get(`/earnings-statements/warrant/${warrantId}`);
export const createEarningsStatement = (data) => httpClient.post('/earnings-statements', data);
export const markStatementMailed = (id) => httpClient.put(`/earnings-statements/${id}/mark-mailed`);
