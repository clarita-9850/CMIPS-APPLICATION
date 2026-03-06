import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useBreadcrumbs } from '../../../lib/BreadcrumbContext';
import * as casesApi from '../../../api/casesApi';
import '../../../pages/WorkQueues.css';

/**
 * FPO Eligibility — DSD Section 23 CI-67555
 *
 * FPO (Fiscal Personal Option) is a case-level designation indicating the
 * recipient qualifies for the Public Authority (PA)-managed provider program.
 * County staff can set or update the FPO eligibility status and date range.
 */
export function CaseViewFPOEligibilityPage() {
  const { caseId } = useParams();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [record, setRecord] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showHistory, setShowHistory] = useState(false);

  const [editModal, setEditModal] = useState(false);
  const [form, setForm] = useState({
    fpoEligible: true,
    beginDate: '',
    endDate: '',
    notes: ''
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const crumbs = [{ label: 'My Workspace', path: '/workspace' }];
    if (caseId) {
      crumbs.push({ label: `Case ${caseId}`, path: `/cases/${caseId}` });
      crumbs.push({ label: 'FPO Eligibility' });
    }
    setBreadcrumbs(crumbs);
    return () => setBreadcrumbs([]);
  }, [caseId, setBreadcrumbs]);

  useEffect(() => { if (caseId) load(); }, [caseId]);

  const load = async () => {
    setLoading(true); setError('');
    try {
      const [current, hist] = await Promise.all([
        casesApi.getFpoEligibility(caseId).catch(() => null),
        casesApi.getFpoEligibilityHistory(caseId).catch(() => [])
      ]);
      setRecord(current);
      setHistory(Array.isArray(hist) ? hist : []);
    } catch {
      setError('Failed to load FPO eligibility.');
    } finally {
      setLoading(false);
    }
  };

  const openEdit = () => {
    setForm({
      fpoEligible: record?.fpoEligible ?? true,
      beginDate: record?.beginDate || '',
      endDate: record?.endDate || '',
      notes: record?.notes || ''
    });
    setEditModal(true);
  };

  const handleSave = async () => {
    setSaving(true); setError('');
    try {
      await casesApi.setFpoEligibility(caseId, {
        fpoEligible: form.fpoEligible,
        beginDate: form.beginDate || null,
        endDate: form.endDate || null,
        notes: form.notes || null
      });
      setSuccess('FPO eligibility updated.');
      setEditModal(false);
      load();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to update FPO eligibility.');
    } finally {
      setSaving(false);
    }
  };

  const fmt = (val) => val || '—';
  const fmtDate = (d) => d ? new Date(d + 'T00:00:00').toLocaleDateString() : '—';
  const fmtBool = (b) => b === true ? 'Yes' : b === false ? 'No' : '—';

  const statusBadge = (eligible) => {
    if (eligible === true) return { bg: '#c6f6d5', color: '#276749', label: 'Eligible' };
    if (eligible === false) return { bg: '#fed7d7', color: '#9b2335', label: 'Not Eligible' };
    return { bg: '#e2e8f0', color: '#4a5568', label: 'Unknown' };
  };

  const badge = statusBadge(record?.fpoEligible);

  return (
    <div className="wq-page">
      <div className="wq-header">
        <div>
          <h1 className="wq-title">FPO Eligibility</h1>
          <p className="wq-subtitle">Fiscal Personal Option — DSD Section 23 CI-67555</p>
        </div>
        {caseId && (
          <button className="wq-btn wq-btn-primary" onClick={openEdit}>
            {record ? 'Update FPO Eligibility' : 'Set FPO Eligibility'}
          </button>
        )}
      </div>

      {error && <div className="wq-error-msg">{error}</div>}
      {success && <div className="wq-success-msg">{success}</div>}

      {loading ? (
        <div className="wq-loading">Loading…</div>
      ) : (
        <>
          {/* Current Status Card */}
          <div className="wq-card" style={{ marginBottom: 16 }}>
            <div style={{ padding: '16px 20px', borderBottom: '1px solid #e2e8f0', fontWeight: 700, fontSize: 15, color: '#2d3748' }}>
              Current FPO Eligibility Status
            </div>
            <div style={{ padding: '20px' }}>
              {!record ? (
                <div className="wq-empty">No FPO eligibility record found for this case.</div>
              ) : (
                <div className="wq-form-grid">
                  <div>
                    <span className="wq-label">FPO Eligible</span>
                    <div style={{ marginTop: 4 }}>
                      <span style={{
                        background: badge.bg, color: badge.color,
                        padding: '3px 10px', borderRadius: 12, fontSize: 13, fontWeight: 700
                      }}>{badge.label}</span>
                    </div>
                  </div>
                  <div>
                    <span className="wq-label">Begin Date</span>
                    <div>{fmtDate(record.beginDate)}</div>
                  </div>
                  <div>
                    <span className="wq-label">End Date</span>
                    <div>{fmtDate(record.endDate)}</div>
                  </div>
                  <div>
                    <span className="wq-label">Status</span>
                    <div>{fmt(record.status)}</div>
                  </div>
                  <div>
                    <span className="wq-label">Notes</span>
                    <div>{fmt(record.notes)}</div>
                  </div>
                  <div>
                    <span className="wq-label">Set By</span>
                    <div>{fmt(record.createdBy)}</div>
                  </div>
                  <div>
                    <span className="wq-label">Set On</span>
                    <div>{record.createdAt ? new Date(record.createdAt).toLocaleString() : '—'}</div>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* History */}
          <div className="wq-card">
            <div
              style={{ padding: '12px 20px', borderBottom: '1px solid #e2e8f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
            >
              <span style={{ fontWeight: 700, fontSize: 14, color: '#2d3748' }}>Eligibility History</span>
              <button className="wq-btn wq-btn-secondary wq-btn-sm" onClick={() => setShowHistory(h => !h)}>
                {showHistory ? 'Hide History' : 'Show History'}
              </button>
            </div>
            {showHistory && (
              history.length === 0 ? (
                <div className="wq-empty">No history records found.</div>
              ) : (
                <table className="wq-table">
                  <thead>
                    <tr>
                      <th>FPO Eligible</th>
                      <th>Begin Date</th>
                      <th>End Date</th>
                      <th>Status</th>
                      <th>Notes</th>
                      <th>Set By</th>
                      <th>Set On</th>
                    </tr>
                  </thead>
                  <tbody>
                    {history.map(h => {
                      const b = statusBadge(h.fpoEligible);
                      return (
                        <tr key={h.id}>
                          <td>
                            <span style={{ background: b.bg, color: b.color, padding: '2px 8px', borderRadius: 12, fontSize: 12, fontWeight: 600 }}>
                              {b.label}
                            </span>
                          </td>
                          <td>{fmtDate(h.beginDate)}</td>
                          <td>{fmtDate(h.endDate)}</td>
                          <td>{fmt(h.status)}</td>
                          <td>{fmt(h.notes)}</td>
                          <td>{fmt(h.createdBy)}</td>
                          <td>{h.createdAt ? new Date(h.createdAt).toLocaleString() : '—'}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )
            )}
          </div>
        </>
      )}

      {/* Edit Modal */}
      {editModal && (
        <div className="wq-modal-overlay">
          <div className="wq-modal">
            <div className="wq-modal-header">
              <h3>{record ? 'Update FPO Eligibility' : 'Set FPO Eligibility'}</h3>
              <button className="wq-modal-close" onClick={() => setEditModal(false)}>✕</button>
            </div>
            <div className="wq-modal-body">
              <div style={{ marginBottom: 12, padding: '8px 12px', background: '#ebf8ff', border: '1px solid #90cdf4', borderRadius: 6, fontSize: 13, color: '#2a4365' }}>
                FPO (Fiscal Personal Option) eligibility indicates whether the recipient qualifies for the Public Authority-managed provider program.
              </div>
              <div className="wq-form-grid">
                <div className="wq-form-group">
                  <label className="wq-label">FPO Eligible *</label>
                  <select className="wq-input" value={String(form.fpoEligible)}
                    onChange={e => setForm(f => ({ ...f, fpoEligible: e.target.value === 'true' }))}>
                    <option value="true">Yes — Eligible</option>
                    <option value="false">No — Not Eligible</option>
                  </select>
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Begin Date</label>
                  <input type="date" className="wq-input" value={form.beginDate}
                    onChange={e => setForm(f => ({ ...f, beginDate: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">End Date</label>
                  <input type="date" className="wq-input" value={form.endDate}
                    onChange={e => setForm(f => ({ ...f, endDate: e.target.value }))} />
                </div>
              </div>
              <div className="wq-form-group" style={{ marginTop: 8 }}>
                <label className="wq-label">Notes</label>
                <textarea className="wq-input" rows={3} value={form.notes}
                  onChange={e => setForm(f => ({ ...f, notes: e.target.value }))}
                  placeholder="Optional notes on eligibility determination" />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => setEditModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleSave} disabled={saving}>
                {saving ? 'Saving…' : 'Save'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default CaseViewFPOEligibilityPage;
