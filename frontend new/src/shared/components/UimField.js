import React from 'react';

/**
 * UimField - renders a labeled form/display field.
 * Props:
 *   label    - field label string
 *   value    - placeholder value (default 'Sample value')
 *   readOnly - if true shows as static text, else renders input
 *   type     - input type (text, date, number) - only used when !readOnly
 */
export function UimField({ label, value = 'Sample value', readOnly = true, type = 'text' }) {
  return (
    <div className="uim-field">
      <label>{label}</label>
      {readOnly ? (
        <span className="uim-field-value">{value}</span>
      ) : (
        <input type={type} defaultValue={value} />
      )}
    </div>
  );
}

export default UimField;
