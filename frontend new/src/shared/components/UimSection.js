import React from 'react';

/**
 * UimSection - renders a single CLUSTER panel.
 * Props:
 *   title    - cluster title string
 *   children - fields / tables / action bars inside the cluster
 */
export function UimSection({ title, children }) {
  return (
    <div className="uim-cluster">
      {title && <h3 className="uim-cluster-title">{title}</h3>}
      <div className="uim-cluster-body">{children}</div>
    </div>
  );
}

export default UimSection;
