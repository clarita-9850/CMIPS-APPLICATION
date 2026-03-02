import http from './httpClient';

/**
 * Search workers/users for case owner assignment.
 * DSD CI-67746 — User Search
 *
 * @param {Object} params — workerNumber, username, firstName, lastName,
 *                           districtOffice, unit, zipCode, position, language
 * @returns {Promise<Array>} list of worker records
 */
export const searchUsers = (params = {}) => {
  const qs = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v))
  ).toString();
  return http.get(`/users/search${qs ? '?' + qs : ''}`).then(r => r.data);
};
