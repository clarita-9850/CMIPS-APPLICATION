/**
 * PlaceholderPage - Generic page for routes that don't have full screens yet
 */

import React from 'react';
import './PlaceholderPage.css';

export const PlaceholderPage = ({ title, children }) => (
  <div className="placeholder-page">
    <h2>{title}</h2>
    {children || <p>Content will load from backend.</p>}
  </div>
);
