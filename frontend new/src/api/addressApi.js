import http from './httpClient';

/**
 * Verify an address against mock CASS (CI-116197).
 * Non-blocking — informational only per DSD spec.
 * @param {Object} address - { streetNumber, streetName, unitType, unitNumber, city, state, zip }
 */
export const verifyAddress = (address) =>
  http.post('/address/verify', address).then(r => r.data);
