import React, { createContext, useContext, useState, useCallback } from 'react';

const BreadcrumbContext = createContext({ breadcrumbs: [], setBreadcrumbs: () => {} });

export const BreadcrumbProvider = ({ children }) => {
  const [breadcrumbs, setBreadcrumbsState] = useState([]);

  const setBreadcrumbs = useCallback((crumbs) => {
    setBreadcrumbsState(crumbs);
  }, []);

  return (
    <BreadcrumbContext.Provider value={{ breadcrumbs, setBreadcrumbs }}>
      {children}
    </BreadcrumbContext.Provider>
  );
};

/**
 * Hook to read and set breadcrumbs from any page.
 * Usage in a page component:
 *   const { setBreadcrumbs } = useBreadcrumbs();
 *   useEffect(() => {
 *     setBreadcrumbs([{ label: 'Cases', path: '/cases' }, { label: 'Case #123' }]);
 *     return () => setBreadcrumbs([]);
 *   }, [setBreadcrumbs]);
 */
export const useBreadcrumbs = () => useContext(BreadcrumbContext);
