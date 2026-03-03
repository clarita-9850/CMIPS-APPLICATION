import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import { verifyMergeSsn, saveMergeSsn } from '../api/recipientsApi';
import './WorkQueues.css';

const MAX_DUPS_INITIAL = 5;
const MAX_DUPS_TOTAL = 15;

/**
 * Merge Duplicate SSN — DSD CI-446456
 *
 * Two-step Verify → Save workflow:
 *  Step 1: Enter SSN, Master CIN, Duplicate CINs (up to 15), optional Make Master
 *  Step 2: Review verified records, then Save to execute merge
 *
 * Implements EM-192 through EM-204 and BR-34 through BR-41.
 */
export const PersonMergePage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  // Form state
  const [ssn, setSsn] = useState('');
  const [masterCin, setMasterCin] = useState('');
  const [makeMaster, setMakeMaster] = useState(false);
  const [duplicateCins, setDuplicateCins] = useState(Array(MAX_DUPS_INITIAL).fill(''));
  const [showAllDups, setShowAllDups] = useState(false);

  // Verify/Save state
  const [step, setStep] = useState(1); // 1 = input, 2 = confirm
  const [verifyResult, setVerifyResult] = useState(null);
  const [errors, setErrors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(null);

  useEffect(() => {
    setBreadcrumbs([
      { label: 'Recipients', path: '/recipients' },
      { label: 'Merge Duplicate SSN' },
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  // SSN formatting: display as ###-##-####
  const formatSsnDisplay = (raw) => {
    const digits = raw.replace(/\D/g, '').slice(0, 9);
    if (digits.length <= 3) return digits;
    if (digits.length <= 5) return digits.slice(0, 3) + '-' + digits.slice(3);
    return digits.slice(0, 3) + '-' + digits.slice(3, 5) + '-' + digits.slice(5);
  };

  const handleSsnChange = (e) => {
    const digits = e.target.value.replace(/\D/g, '').slice(0, 9);
    setSsn(digits);
  };

  const handleDupChange = (idx, value) => {
    setDuplicateCins(prev => {
      const updated = [...prev];
      updated[idx] = value;
      return updated;
    });
  };

  const visibleDupCount = showAllDups ? MAX_DUPS_TOTAL : MAX_DUPS_INITIAL;

  const handleShowMore = () => {
    setShowAllDups(true);
    setDuplicateCins(prev => {
      if (prev.length < MAX_DUPS_TOTAL) {
        return [...prev, ...Array(MAX_DUPS_TOTAL - prev.length).fill('')];
      }
      return prev;
    });
  };

  // ── Step 1: Verify ──
  const handleVerify = async (e) => {
    e.preventDefault();
    setErrors([]);
    setVerifyResult(null);
    setSaveSuccess(null);

    // Client-side pre-validation
    if (!ssn || ssn.length !== 9) {
      setErrors(['EM-196: Invalid SSN format. SSN must be 9 numeric digits.']);
      return;
    }
    if (!masterCin.trim()) {
      setErrors(['EM-197: Master Record is required. Please enter a valid CIN or Provider Number.']);
      return;
    }
    const filledDups = duplicateCins.filter(c => c.trim() !== '');
    if (filledDups.length === 0) {
      setErrors(['EM-204: At least one Duplicate Record is required.']);
      return;
    }

    setLoading(true);
    try {
      const data = await verifyMergeSsn({
        ssn,
        masterCin: masterCin.trim(),
        duplicateCins: filledDups,
        makeMaster,
      });

      if (data.success) {
        setVerifyResult(data);
        setStep(2);
      } else {
        setErrors(data.errors || ['Verification failed.']);
      }
    } catch (err) {
      const errData = err?.data;
      if (errData?.errors) {
        setErrors(errData.errors);
      } else {
        setErrors([err?.message || 'Verification request failed.']);
      }
    } finally {
      setLoading(false);
    }
  };

  // ── Step 2: Save ──
  const handleSave = async () => {
    setErrors([]);
    setLoading(true);
    try {
      const filledDups = duplicateCins.filter(c => c.trim() !== '');
      const data = await saveMergeSsn({
        ssn,
        masterCin: masterCin.trim(),
        duplicateCins: filledDups,
        makeMaster,
      });

      if (data.success) {
        setSaveSuccess(data.message);
        setStep(1);
        setVerifyResult(null);
      } else {
        setErrors(data.errors || ['Save failed.']);
      }
    } catch (err) {
      const errData = err?.data;
      if (errData?.errors) {
        setErrors(errData.errors);
      } else {
        setErrors([err?.message || 'Save request failed.']);
      }
    } finally {
      setLoading(false);
    }
  };

  // ── Reset form ──
  const handleReset = () => {
    setSsn('');
    setMasterCin('');
    setMakeMaster(false);
    setDuplicateCins(Array(MAX_DUPS_INITIAL).fill(''));
    setShowAllDups(false);
    setStep(1);
    setVerifyResult(null);
    setErrors([]);
    setSaveSuccess(null);
  };

  // ── Back from confirm to input ──
  const handleBack = () => {
    setStep(1);
    setVerifyResult(null);
    setErrors([]);
  };

  return (
    <div className="wq-page">
      <h1 className="wq-page-title">Merge Duplicate SSN</h1>

      {/* Success banner */}
      {saveSuccess && (
        <div style={{
          background: '#f0fff4', border: '1px solid #68d391', padding: '0.75rem 1rem',
          borderRadius: '6px', marginBottom: '1rem', color: '#276749', fontSize: '0.9rem'
        }}>
          {saveSuccess}
          <button
            onClick={handleReset}
            style={{
              marginLeft: '1rem', background: '#276749', color: '#fff', border: 'none',
              borderRadius: '4px', padding: '0.3rem 0.75rem', cursor: 'pointer', fontSize: '0.8rem'
            }}
          >
            Merge Another
          </button>
        </div>
      )}

      {/* Error banner */}
      {errors.length > 0 && (
        <div style={{
          background: '#fff5f5', border: '1px solid #fc8181', padding: '0.75rem 1rem',
          borderRadius: '6px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem'
        }}>
          {errors.map((err, i) => (
            <div key={i} style={{ marginBottom: i < errors.length - 1 ? '0.4rem' : 0 }}>{err}</div>
          ))}
        </div>
      )}

      {/* Step 2: Confirmation View */}
      {step === 2 && verifyResult && (
        <>
          <div style={{
            background: '#ebf8ff', border: '1px solid #90cdf4', padding: '0.75rem 1rem',
            borderRadius: '6px', marginBottom: '1rem', color: '#2b6cb0', fontSize: '0.9rem'
          }}>
            Please review the records below and click <strong>Save</strong> to confirm the merge.
            {makeMaster && (
              <span style={{ marginLeft: '0.5rem', fontWeight: 600 }}>
                (Make Master: {verifyResult.effectiveMasterCin} will become the new Master)
              </span>
            )}
          </div>

          {/* Master Record */}
          <section className="wq-cluster" style={{ marginBottom: '1rem' }}>
            <h2 className="wq-cluster-title">Master Record</h2>
            <div className="wq-cluster-body" style={{ padding: 0 }}>
              <table className="wq-table-uim">
                <thead>
                  <tr>
                    <th>Role</th>
                    <th>CIN / Provider#</th>
                    <th>Name</th>
                    <th>DOB</th>
                    <th>Gender</th>
                    <th>Type</th>
                    <th>SSN</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {verifyResult.masterRecord && (
                    <tr>
                      <td><span style={{
                        background: '#c6f6d5', color: '#276749', padding: '0.15rem 0.5rem',
                        borderRadius: '4px', fontWeight: 600, fontSize: '0.75rem'
                      }}>MASTER</span></td>
                      <td style={{ fontWeight: 600 }}>{verifyResult.masterRecord.cin}</td>
                      <td>{[verifyResult.masterRecord.lastName, verifyResult.masterRecord.firstName, verifyResult.masterRecord.middleName].filter(Boolean).join(', ')}</td>
                      <td>{verifyResult.masterRecord.dateOfBirth || '\u2014'}</td>
                      <td>{verifyResult.masterRecord.gender || '\u2014'}</td>
                      <td>{verifyResult.masterRecord.type}</td>
                      <td>{verifyResult.masterRecord.ssn}</td>
                      <td>{verifyResult.masterRecord.status || '\u2014'}</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </section>

          {/* Duplicate Records */}
          <section className="wq-cluster" style={{ marginBottom: '1.5rem' }}>
            <h2 className="wq-cluster-title">Duplicate Records ({verifyResult.duplicateRecords?.length || 0})</h2>
            <div className="wq-cluster-body" style={{ padding: 0 }}>
              <table className="wq-table-uim">
                <thead>
                  <tr>
                    <th>Role</th>
                    <th>CIN / Provider#</th>
                    <th>Name</th>
                    <th>DOB</th>
                    <th>Gender</th>
                    <th>Type</th>
                    <th>SSN</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {(verifyResult.duplicateRecords || []).map((rec, i) => (
                    <tr key={i}>
                      <td><span style={{
                        background: '#fed7d7', color: '#c53030', padding: '0.15rem 0.5rem',
                        borderRadius: '4px', fontWeight: 600, fontSize: '0.75rem'
                      }}>DUPLICATE</span></td>
                      <td style={{ fontWeight: 600 }}>{rec.cin}</td>
                      <td>{[rec.lastName, rec.firstName, rec.middleName].filter(Boolean).join(', ')}</td>
                      <td>{rec.dateOfBirth || '\u2014'}</td>
                      <td>{rec.gender || '\u2014'}</td>
                      <td>{rec.type}</td>
                      <td>{rec.ssn}</td>
                      <td>{rec.status || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>

          {/* Action buttons */}
          <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
            <button
              className="wq-btn wq-btn-primary"
              onClick={handleSave}
              disabled={loading}
              style={{ minWidth: '120px' }}
            >
              {loading ? 'Saving...' : 'Save'}
            </button>
            <button
              className="wq-btn wq-btn-outline"
              onClick={handleBack}
              disabled={loading}
            >
              Back
            </button>
            <button
              className="wq-btn wq-btn-outline"
              onClick={() => navigate('/recipients')}
              disabled={loading}
            >
              Cancel
            </button>
          </div>
        </>
      )}

      {/* Step 1: Input Form */}
      {step === 1 && !saveSuccess && (
        <form onSubmit={handleVerify}>

          {/* Cluster 1: SSN / Master Record */}
          <section className="wq-cluster" style={{ marginBottom: '1rem' }}>
            <h2 className="wq-cluster-title">SSN / Master Record</h2>
            <div className="wq-cluster-body">
              <div style={{
                display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                gap: '1rem', padding: '0.5rem 0'
              }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#444', marginBottom: '0.3rem' }}>
                    SSN <span style={{ color: '#c53030' }}>*</span>
                  </label>
                  <input
                    type="text"
                    value={formatSsnDisplay(ssn)}
                    onChange={handleSsnChange}
                    placeholder="###-##-####"
                    maxLength={11}
                    style={{
                      width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e0',
                      borderRadius: '4px', fontSize: '0.9rem'
                    }}
                  />
                  <span style={{ fontSize: '0.7rem', color: '#888' }}>9 numeric digits</span>
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#444', marginBottom: '0.3rem' }}>
                    Master Record (CIN or Provider#) <span style={{ color: '#c53030' }}>*</span>
                  </label>
                  <input
                    type="text"
                    value={masterCin}
                    onChange={e => setMasterCin(e.target.value.toUpperCase())}
                    placeholder="e.g. A1234567"
                    maxLength={20}
                    style={{
                      width: '100%', padding: '0.5rem 0.75rem', border: '1px solid #cbd5e0',
                      borderRadius: '4px', fontSize: '0.9rem'
                    }}
                  />
                </div>
                <div style={{ display: 'flex', alignItems: 'center', paddingTop: '1.5rem' }}>
                  <input
                    type="checkbox"
                    id="makeMaster"
                    checked={makeMaster}
                    onChange={e => setMakeMaster(e.target.checked)}
                    style={{ marginRight: '0.5rem', width: '16px', height: '16px' }}
                  />
                  <label htmlFor="makeMaster" style={{ fontSize: '0.85rem', fontWeight: 500, color: '#333', cursor: 'pointer' }}>
                    Make Master
                  </label>
                  <span style={{ fontSize: '0.7rem', color: '#888', marginLeft: '0.5rem' }}>
                    (Promotes first duplicate to Master)
                  </span>
                </div>
              </div>
            </div>
          </section>

          {/* Cluster 2: Duplicate Records */}
          <section className="wq-cluster" style={{ marginBottom: '1.5rem' }}>
            <h2 className="wq-cluster-title">Duplicate Records</h2>
            <div className="wq-cluster-body">
              <div style={{
                display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                gap: '0.75rem', padding: '0.5rem 0'
              }}>
                {Array.from({ length: visibleDupCount }).map((_, i) => (
                  <div key={i}>
                    <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 500, color: '#555', marginBottom: '0.25rem' }}>
                      Duplicate {i + 1} {i === 0 && <span style={{ color: '#c53030' }}>*</span>}
                    </label>
                    <input
                      type="text"
                      value={duplicateCins[i] || ''}
                      onChange={e => handleDupChange(i, e.target.value.toUpperCase())}
                      placeholder="CIN or Provider#"
                      maxLength={20}
                      style={{
                        width: '100%', padding: '0.45rem 0.75rem', border: '1px solid #cbd5e0',
                        borderRadius: '4px', fontSize: '0.85rem'
                      }}
                    />
                  </div>
                ))}
              </div>
              {!showAllDups && (
                <button
                  type="button"
                  onClick={handleShowMore}
                  style={{
                    marginTop: '0.5rem', background: 'none', border: 'none', color: '#2b6cb0',
                    cursor: 'pointer', fontSize: '0.85rem', fontWeight: 500, textDecoration: 'underline'
                  }}
                >
                  Show More (up to 15 duplicates)
                </button>
              )}
            </div>
          </section>

          {/* Action buttons */}
          <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
            <button
              type="submit"
              className="wq-btn wq-btn-primary"
              disabled={loading}
              style={{ minWidth: '120px' }}
            >
              {loading ? 'Verifying...' : 'Verify'}
            </button>
            <button
              type="button"
              className="wq-btn wq-btn-outline"
              onClick={handleReset}
              disabled={loading}
            >
              Clear
            </button>
            <button
              type="button"
              className="wq-btn wq-btn-outline"
              onClick={() => navigate(-1)}
              disabled={loading}
            >
              Cancel
            </button>
          </div>
        </form>
      )}
    </div>
  );
};
