/**
 * useDomainData Hook
 *
 * React hook for generated pages to fetch data from their domain's API.
 * Handles loading, error, and empty states automatically.
 *
 * Usage:
 *   const { data, loading, error, refetch } = useDomainData('case', 'list');
 *   const { data, loading, error } = useDomainData('person', 'get', recipientId);
 *   const { data, loading, error } = useDomainData('case', 'search', { county: '19' });
 */

import { useState, useEffect, useCallback } from 'react';
import { getDomainApi } from '../../api/domainApi';

export function useDomainData(domain, operation = 'list', params = null) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const api = getDomainApi(domain);
      const fn = api[operation];
      if (!fn) {
        setData(null);
        setLoading(false);
        return;
      }
      const result = params !== null && params !== undefined
        ? await fn(params)
        : await fn();
      setData(result);
    } catch (err) {
      console.warn(`[useDomainData] ${domain}.${operation} failed:`, err.message);
      setError(err);
      setData(null);
    } finally {
      setLoading(false);
    }
  }, [domain, operation, params]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return { data, loading, error, refetch: fetchData };
}

export default useDomainData;
