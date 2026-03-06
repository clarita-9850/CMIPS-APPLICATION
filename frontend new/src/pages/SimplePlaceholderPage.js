/**
 * SimplePlaceholderPage - Static placeholder for routes without backend screens
 */

import React from 'react';
import './PlaceholderPage.css';

export const SimplePlaceholderPage = ({ title, message }) => (
  <div className="placeholder-page">
    <h2>{title}</h2>
    <p>{message || 'Content will load from backend.'}</p>
  </div>
);
