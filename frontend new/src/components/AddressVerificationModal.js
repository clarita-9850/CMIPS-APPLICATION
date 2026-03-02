import React, { useState } from 'react';
import { verifyAddress } from '../api/addressApi';

/**
 * AddressVerificationModal — Mock CASS address verification per DSD CI-116197.
 *
 * Non-blocking: worker can proceed even if CASS fails or returns corrections.
 *
 * Props:
 *   isOpen       {boolean}  — controls visibility
 *   onClose      {function} — called when modal is dismissed without saving
 *   onConfirm    {function(addressObj)} — called when worker accepts an address
 *                addressObj: { streetNumber, streetName, unitType, unitNumber, city, state, zip,
 *                              cassMatch, cassUpdates, cassFailed }
 *   initialData  {object}   — pre-fill with existing address values
 *   title        {string}   — optional modal title override
 */
const AddressVerificationModal = ({ isOpen, onClose, onConfirm, initialData = {}, title = 'Address Verification' }) => {
  const [form, setForm] = useState({
    streetNumber: initialData.streetNumber || '',
    streetName:   initialData.streetName   || '',
    unitType:     initialData.unitType     || '',
    unitNumber:   initialData.unitNumber   || '',
    city:         initialData.city         || '',
    state:        initialData.state        || 'CA',
    zip:          initialData.zip          || '',
  });
  const [verifying, setVerifying] = useState(false);
  const [result, setResult] = useState(null); // null = not verified yet

  const handleChange = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }));
    setResult(null); // reset verification when fields change
  };

  const handleVerify = async () => {
    setVerifying(true);
    setResult(null);
    try {
      const resp = await verifyAddress(form);
      setResult(resp);
    } catch {
      // Network error — treat as unverifiable, non-blocking
      setResult({ cassMatch: false, cassFailed: true, cassUpdates: 'EM-177: Address could not be verified. You may still proceed.' });
    } finally {
      setVerifying(false);
    }
  };

  const handleAccept = (addressToUse) => {
    onConfirm({
      ...addressToUse,
      cassMatch:   result?.cassMatch   ?? false,
      cassUpdates: result?.cassUpdates ?? null,
      cassFailed:  result?.cassFailed  ?? false,
    });
  };

  const handleSkip = () => {
    // Non-blocking: worker can proceed without CASS verification
    onConfirm({ ...form, cassMatch: false, cassUpdates: null, cassFailed: true });
  };

  if (!isOpen) return null;

  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
      <div style={{ background: '#fff', borderRadius: '8px', width: '580px', maxWidth: '95vw', maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 20px 60px rgba(0,0,0,0.3)' }}>
        {/* Header */}
        <div style={{ background: '#153554', color: '#fff', padding: '1rem 1.5rem', borderRadius: '8px 8px 0 0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 style={{ margin: 0, fontSize: '1.1rem' }}>{title}</h3>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#fff', fontSize: '1.5rem', cursor: 'pointer', lineHeight: 1 }}>×</button>
        </div>

        {/* Body */}
        <div style={{ padding: '1.5rem' }}>
          {/* Address Fields */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '0.75rem', marginBottom: '0.75rem' }}>
            <div>
              <label style={labelStyle}>Street Number</label>
              <input style={inputStyle} value={form.streetNumber} onChange={e => handleChange('streetNumber', e.target.value)} placeholder="e.g. 1234" />
            </div>
            <div>
              <label style={labelStyle}>Street Name *</label>
              <input style={inputStyle} value={form.streetName} onChange={e => handleChange('streetName', e.target.value)} placeholder="e.g. Main St" />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '0.75rem' }}>
            <div>
              <label style={labelStyle}>Unit Type</label>
              <select style={inputStyle} value={form.unitType} onChange={e => handleChange('unitType', e.target.value)}>
                <option value="">None</option>
                <option value="APT">APT</option><option value="STE">STE</option>
                <option value="UNIT">UNIT</option><option value="RM">RM</option>
                <option value="FL">FL</option><option value="BLDG">BLDG</option>
                <option value="DEPT">DEPT</option><option value="LOT">LOT</option>
                <option value="PMB">PMB</option><option value="BOX">BOX</option>
              </select>
            </div>
            <div>
              <label style={labelStyle}>Unit Number</label>
              <input style={inputStyle} value={form.unitNumber} onChange={e => handleChange('unitNumber', e.target.value)} placeholder="e.g. 204" />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
            <div>
              <label style={labelStyle}>City *</label>
              <input style={inputStyle} value={form.city} onChange={e => handleChange('city', e.target.value)} placeholder="e.g. Sacramento" />
            </div>
            <div>
              <label style={labelStyle}>State</label>
              <input style={inputStyle} value={form.state} onChange={e => handleChange('state', e.target.value)} maxLength={2} />
            </div>
            <div>
              <label style={labelStyle}>ZIP *</label>
              <input style={inputStyle} value={form.zip} onChange={e => handleChange('zip', e.target.value)} placeholder="e.g. 95814" />
            </div>
          </div>

          {/* Verify Button */}
          <button
            onClick={handleVerify}
            disabled={verifying || !form.streetName.trim() || !form.zip.trim()}
            style={{ ...btnPrimary, marginBottom: '1rem', width: '100%' }}
          >
            {verifying ? 'Verifying...' : 'Verify Address with CASS'}
          </button>

          {/* CASS Result */}
          {result && (
            <div style={{ marginBottom: '1rem' }}>
              {result.cassMatch && (
                <div style={successBanner}>
                  <strong>Address Verified</strong> — CASS confirms this address.
                  <br />
                  <button onClick={() => handleAccept(form)} style={{ ...btnPrimary, marginTop: '0.5rem' }}>Use This Address</button>
                </div>
              )}
              {!result.cassMatch && result.cassUpdates && (
                <div style={warningBanner}>
                  <strong>EM-178:</strong> {result.cassUpdates}
                  <div style={{ marginTop: '0.75rem', display: 'flex', gap: '0.5rem' }}>
                    <button onClick={() => handleAccept(form)} style={btnPrimary}>Use Original Address</button>
                    <button onClick={() => handleAccept({ ...form, ...result })} style={btnSecondary}>Use Suggested Address</button>
                  </div>
                </div>
              )}
              {result.cassFailed && !result.cassUpdates && (
                <div style={errorBanner}>
                  <strong>EM-177:</strong> Address could not be verified with CASS. You may still proceed.
                  <br />
                  <button onClick={() => handleAccept(form)} style={{ ...btnPrimary, marginTop: '0.5rem' }}>Proceed with Unverified Address</button>
                </div>
              )}
            </div>
          )}

          {/* Skip link */}
          <div style={{ textAlign: 'center', marginTop: '0.5rem' }}>
            <button onClick={handleSkip} style={{ background: 'none', border: 'none', color: '#153554', textDecoration: 'underline', cursor: 'pointer', fontSize: '0.875rem' }}>
              Skip Verification (address will be saved unverified)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

const labelStyle = { display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#4a5568', marginBottom: '0.25rem' };
const inputStyle = { width: '100%', padding: '0.4rem 0.6rem', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '0.875rem', boxSizing: 'border-box' };
const btnPrimary = { background: '#153554', color: '#fff', border: 'none', padding: '0.4rem 1rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.875rem' };
const btnSecondary = { background: '#fff', color: '#153554', border: '1px solid #153554', padding: '0.4rem 1rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.875rem' };
const successBanner = { background: '#f0fff4', border: '1px solid #68d391', padding: '0.75rem', borderRadius: '4px', color: '#276749', fontSize: '0.875rem' };
const warningBanner = { background: '#fffbeb', border: '1px solid #f6ad55', padding: '0.75rem', borderRadius: '4px', color: '#c05621', fontSize: '0.875rem' };
const errorBanner  = { background: '#fff5f5', border: '1px solid #fc8181', padding: '0.75rem', borderRadius: '4px', color: '#c53030', fontSize: '0.875rem' };

export default AddressVerificationModal;
