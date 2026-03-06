import React from 'react';

/**
 * UimField - renders a labeled form/display field.
 * Props:
 *   label       - field label string
 *   value       - display value or controlled input value
 *   readOnly    - if true shows as static text (legacy, auto-detected if no onChange)
 *   type        - 'text' | 'date' | 'number' | 'email' | 'select' | 'checkbox' | 'textarea'
 *   name        - input name attribute (for controlled forms)
 *   onChange     - change handler; if provided, renders controlled input
 *   required    - adds required indicator to label
 *   options     - array of {value, label} for type="select"
 *   checked     - boolean for type="checkbox"
 *   placeholder - input placeholder
 *   disabled    - disables the input
 *   maxLength   - max input length
 *   style       - optional style override for the wrapper div
 *   className   - additional CSS class for wrapper
 *   rows        - rows for textarea
 */
export function UimField({
  label,
  value,
  readOnly,
  type = 'text',
  name,
  onChange,
  required = false,
  options = [],
  checked,
  placeholder,
  disabled = false,
  maxLength,
  style,
  className,
  rows = 3,
}) {
  // Determine if this is an editable field
  const isEditable = typeof onChange === 'function';
  const isReadOnly = readOnly !== undefined ? readOnly : !isEditable;

  // Checkbox rendering
  if (type === 'checkbox') {
    if (isEditable) {
      return (
        <div className={`uim-field uim-field-checkbox ${className || ''}`} style={style}>
          <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: disabled ? 'default' : 'pointer' }}>
            <input
              type="checkbox"
              name={name}
              checked={!!checked}
              onChange={onChange}
              disabled={disabled}
            />
            <span>{label}{required && <span style={{ color: '#e53e3e' }}> *</span>}</span>
          </label>
        </div>
      );
    }
    // Read-only checkbox display
    return (
      <div className={`uim-field ${className || ''}`} style={style}>
        <label>{label}</label>
        <span className="uim-field-value">{checked ? 'Yes' : 'No'}</span>
      </div>
    );
  }

  // Read-only mode
  if (isReadOnly) {
    return (
      <div className={`uim-field ${className || ''}`} style={style}>
        <label>{label}{required && <span style={{ color: '#e53e3e' }}> *</span>}</label>
        <span className="uim-field-value">{value ?? '\u2014'}</span>
      </div>
    );
  }

  // Select dropdown
  if (type === 'select') {
    return (
      <div className={`uim-field ${className || ''}`} style={style}>
        <label>{label}{required && <span style={{ color: '#e53e3e' }}> *</span>}</label>
        <select
          name={name}
          value={value ?? ''}
          onChange={onChange}
          required={required}
          disabled={disabled}
        >
          {options.map(opt => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      </div>
    );
  }

  // Textarea
  if (type === 'textarea') {
    return (
      <div className={`uim-field ${className || ''}`} style={style}>
        <label>{label}{required && <span style={{ color: '#e53e3e' }}> *</span>}</label>
        <textarea
          name={name}
          value={value ?? ''}
          onChange={onChange}
          required={required}
          disabled={disabled}
          placeholder={placeholder}
          maxLength={maxLength}
          rows={rows}
          style={{
            padding: '0.4rem 0.6rem',
            border: '1px solid var(--border-color-dark)',
            borderRadius: '3px',
            fontSize: '0.9rem',
            fontFamily: 'inherit',
            resize: 'vertical',
          }}
        />
      </div>
    );
  }

  // Standard input (text, date, number, email)
  return (
    <div className={`uim-field ${className || ''}`} style={style}>
      <label>{label}{required && <span style={{ color: '#e53e3e' }}> *</span>}</label>
      <input
        type={type}
        name={name}
        value={value ?? ''}
        onChange={onChange}
        required={required}
        disabled={disabled}
        placeholder={placeholder}
        maxLength={maxLength}
      />
    </div>
  );
}

export default UimField;
