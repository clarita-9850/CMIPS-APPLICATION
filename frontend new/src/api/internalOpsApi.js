import httpClient from './httpClient';

// ==================== Garnishments (DSD Section 32) ====================

export const getProviderGarnishments = (providerId) => httpClient.get(`/internal-ops/garnishments/providers/${providerId}`);
export const getActiveGarnishments = (providerId) => httpClient.get(`/internal-ops/garnishments/providers/${providerId}/active`);
export const getGarnishment = (id) => httpClient.get(`/internal-ops/garnishments/${id}`);
export const createGarnishment = (providerId, data) => httpClient.post(`/internal-ops/garnishments/providers/${providerId}`, data);
export const updateGarnishment = (id, data) => httpClient.put(`/internal-ops/garnishments/${id}`, data);
export const suspendGarnishment = (id, data) => httpClient.put(`/internal-ops/garnishments/${id}/suspend`, data);
export const satisfyGarnishment = (id) => httpClient.put(`/internal-ops/garnishments/${id}/satisfy`);
export const terminateGarnishment = (id) => httpClient.put(`/internal-ops/garnishments/${id}/terminate`);

// ==================== Direct Deposit (DSD Section 32) ====================

export const getProviderDirectDeposits = (providerId) => httpClient.get(`/internal-ops/direct-deposit/providers/${providerId}`);
export const getActiveDirectDeposit = (providerId) => httpClient.get(`/internal-ops/direct-deposit/providers/${providerId}/active`);
export const getDirectDeposit = (id) => httpClient.get(`/internal-ops/direct-deposit/${id}`);
export const createDirectDeposit = (providerId, data) => httpClient.post(`/internal-ops/direct-deposit/providers/${providerId}`, data);
export const verifyPrenote = (id) => httpClient.put(`/internal-ops/direct-deposit/${id}/verify-prenote`);
export const inactivateDirectDeposit = (id, data) => httpClient.put(`/internal-ops/direct-deposit/${id}/inactivate`, data);
export const getPendingPrenotes = () => httpClient.get('/internal-ops/direct-deposit/pending-prenote');
