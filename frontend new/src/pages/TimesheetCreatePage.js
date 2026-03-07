import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as tsApi from '../api/timesheetApi';
import './WorkQueues.css';

const TIMESHEET_TYPES = ['STANDARD', 'LARGE_FONT', 'EVV_EXCEPTION', 'ADVANCE_PAY', 'SUPPLEMENTAL', 'NEXT_ARREARS', 'LIVE_IN'];
const PROGRAM_TYPES = ['IHSS', 'WPCS'];

const buildDailyGrid = (start, end) => {
  if (!start || !end) return [];
  const s = new Date(start + 'T00:00:00');
  const e = new Date(end + 'T00:00:00');
  const days = [];
  for (let d = new Date(s); d <= e; d.setDate(d.getDate() + 1)) {
    const iso = d.toISOString().split('T')[0];
    const dow = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'][d.getDay()];
    days.push({ date: iso, dow, hours: '', minutes: '' });
  }
  return days;
};

export const TimesheetCreatePage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [form, setForm] = useState({
    caseId: '', recipientId: '', providerId: '', programType: 'IHSS', timesheetType: 'STANDARD',
    payPeriodStart: '', payPeriodEnd: '', authorizedHoursMonthly: '', assignedHours: '',
    remainingRecipientHours: '', remainingProviderHours: '',
    providerSignaturePresent: true, recipientSignaturePresent: true,
    recipientIsBvi: false, isSupplemental: false, flaggedForReview: false,
    countyCode: '', notes: '', createdBy: ''
  });
  const [dailyEntries, setDailyEntries] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    setBreadcrumbs([
      { label: 'Payments', path: '/payments/timesheets' },
      { label: 'IHSS Timesheets', path: '/payments/timesheets' },
      { label: 'Manual Entry' }
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    setDailyEntries(buildDailyGrid(form.payPeriodStart, form.payPeriodEnd));
  }, [form.payPeriodStart, form.payPeriodEnd]);

  const setF = (k, v) => setForm(prev => ({ ...prev, [k]: v }));
  const updateDay = (idx, field, val) => {
    setDailyEntries(prev => prev.map((d, i) => i === idx ? { ...d, [field]: val } : d));
  };

  const totalClaimed = dailyEntries.reduce((s, d) => s + (parseFloat(d.hours) || 0), 0);

  const handleSubmit = async (andValidate = false) => {
    if (!form.caseId || !form.recipientId || !form.providerId || !form.payPeriodStart || !form.payPeriodEnd) {
      setError('Case ID, Recipient ID, Provider ID, and Pay Period are required.');
      return;
    }
    setError(''); setSuccess(''); setSubmitting(true);
    try {
      const payload = {
        ...form,
        caseId: parseInt(form.caseId),
        recipientId: parseInt(form.recipientId),
        providerId: parseInt(form.providerId),
        authorizedHoursMonthly: form.authorizedHoursMonthly ? parseFloat(form.authorizedHoursMonthly) : null,
        assignedHours: form.assignedHours ? parseFloat(form.assignedHours) : null,
        remainingRecipientHours: form.remainingRecipientHours ? parseFloat(form.remainingRecipientHours) : null,
        remainingProviderHours: form.remainingProviderHours ? parseFloat(form.remainingProviderHours) : null,
        dailyEntries: dailyEntries.filter(d => d.hours).map(d => ({
          date: d.date,
          hours: parseFloat(d.hours),
          minutes: d.minutes ? parseInt(d.minutes) : null
        }))
      };
      const ts = await tsApi.createManualTimesheet(payload);
      if (andValidate && ts?.id) {
        await tsApi.validateTimesheet(ts.id);
        setSuccess(`Timesheet ${ts.timesheetNumber || ts.id} created and validated.`);
        setTimeout(() => navigate(`/payments/timesheets/${ts.id}`), 1500);
      } else {
        setSuccess(`Timesheet ${ts.timesheetNumber || ts.id} created successfully.`);
        setTimeout(() => navigate(`/payments/timesheets/${ts.id}`), 1500);
      }
    } catch (err) {
      setError('Failed: ' + (err?.response?.data?.error || err?.message || 'Unknown error'));
    } finally { setSubmitting(false); }
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Manual Timesheet Entry</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Back to List</button>
      </div>

      {error && <div style={{ padding: '0.75rem', background: '#fef2f2', border: '1px solid #fca5a5', borderRadius: '6px', color: '#dc2626', marginBottom: '1rem' }}>{error}</div>}
      {success && <div style={{ padding: '0.75rem', background: '#f0fdf4', border: '1px solid #86efac', borderRadius: '6px', color: '#16a34a', marginBottom: '1rem' }}>{success}</div>}

      {/* Case & Provider Info */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Case & Provider Information</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Case ID *</label>
              <input type="number" value={form.caseId} onChange={e => setF('caseId', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Recipient ID *</label>
              <input type="number" value={form.recipientId} onChange={e => setF('recipientId', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Provider ID *</label>
              <input type="number" value={form.providerId} onChange={e => setF('providerId', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Program Type</label>
              <select value={form.programType} onChange={e => setF('programType', e.target.value)}>
                {PROGRAM_TYPES.map(p => <option key={p} value={p}>{p}</option>)}
              </select>
            </div>
            <div className="wq-form-field">
              <label>Timesheet Type</label>
              <select value={form.timesheetType} onChange={e => setF('timesheetType', e.target.value)}>
                {TIMESHEET_TYPES.map(t => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
              </select>
            </div>
            <div className="wq-form-field">
              <label>County Code</label>
              <input type="text" value={form.countyCode} onChange={e => setF('countyCode', e.target.value)} placeholder="e.g. 19" />
            </div>
          </div>
        </div>
      </div>

      {/* Pay Period & Hours */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Pay Period & Authorization</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Pay Period Start *</label>
              <input type="date" value={form.payPeriodStart} onChange={e => setF('payPeriodStart', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Pay Period End *</label>
              <input type="date" value={form.payPeriodEnd} onChange={e => setF('payPeriodEnd', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Authorized Hours/Mo</label>
              <input type="number" step="0.1" value={form.authorizedHoursMonthly} onChange={e => setF('authorizedHoursMonthly', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Assigned Hours</label>
              <input type="number" step="0.1" value={form.assignedHours} onChange={e => setF('assignedHours', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Remaining Recipient Hrs</label>
              <input type="number" step="0.1" value={form.remainingRecipientHours} onChange={e => setF('remainingRecipientHours', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Remaining Provider Hrs</label>
              <input type="number" step="0.1" value={form.remainingProviderHours} onChange={e => setF('remainingProviderHours', e.target.value)} />
            </div>
          </div>
        </div>
      </div>

      {/* Signatures & Flags */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Signatures & Flags</h4></div>
        <div className="wq-panel-body">
          <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <input type="checkbox" checked={form.providerSignaturePresent} onChange={e => setF('providerSignaturePresent', e.target.checked)} />
              Provider Signature Present
            </label>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <input type="checkbox" checked={form.recipientSignaturePresent} onChange={e => setF('recipientSignaturePresent', e.target.checked)} />
              Recipient Signature Present
            </label>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <input type="checkbox" checked={form.recipientIsBvi} onChange={e => setF('recipientIsBvi', e.target.checked)} />
              Recipient is BVI
            </label>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <input type="checkbox" checked={form.isSupplemental} onChange={e => setF('isSupplemental', e.target.checked)} />
              Supplemental Timesheet
            </label>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <input type="checkbox" checked={form.flaggedForReview} onChange={e => setF('flaggedForReview', e.target.checked)} />
              Flag for Review
            </label>
          </div>
        </div>
      </div>

      {/* Daily Hours Grid */}
      {dailyEntries.length > 0 && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Daily Hours ({dailyEntries.length} days)</h4>
            <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Total: {totalClaimed.toFixed(1)} hrs</span>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            <table className="wq-table" style={{ fontSize: '0.8rem' }}>
              <thead>
                <tr>
                  <th>Day</th>
                  <th>Date</th>
                  <th>DOW</th>
                  <th>Hours</th>
                  <th>Minutes</th>
                </tr>
              </thead>
              <tbody>
                {dailyEntries.map((d, i) => (
                  <tr key={i} style={{ background: d.dow === 'Sat' || d.dow === 'Sun' ? '#f8fafc' : 'transparent' }}>
                    <td>{i + 1}</td>
                    <td>{d.date}</td>
                    <td style={{ fontWeight: (d.dow === 'Sat' || d.dow === 'Sun') ? 600 : 400 }}>{d.dow}</td>
                    <td>
                      <input type="number" min="0" max="24" step="0.5" value={d.hours}
                        onChange={e => updateDay(i, 'hours', e.target.value)}
                        style={{ width: '70px', padding: '0.25rem 0.5rem', border: '1px solid #d1d5db', borderRadius: '4px' }} />
                    </td>
                    <td>
                      <input type="number" min="0" max="59" value={d.minutes}
                        onChange={e => updateDay(i, 'minutes', e.target.value)}
                        style={{ width: '60px', padding: '0.25rem 0.5rem', border: '1px solid #d1d5db', borderRadius: '4px' }} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Notes */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Notes</h4></div>
        <div className="wq-panel-body">
          <textarea rows={3} value={form.notes} onChange={e => setF('notes', e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '0.85rem' }}
            placeholder="Optional notes..." />
        </div>
      </div>

      {/* Submit */}
      <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
        <button className="wq-btn wq-btn-primary" onClick={() => handleSubmit(false)} disabled={submitting}>
          {submitting ? 'Saving...' : 'Save Timesheet'}
        </button>
        <button className="wq-btn wq-btn-primary" style={{ background: '#16a34a' }} onClick={() => handleSubmit(true)} disabled={submitting}>
          Save & Validate
        </button>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')} disabled={submitting}>Cancel</button>
      </div>
    </div>
  );
};
