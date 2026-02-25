import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as timesheetApi from '../api/timesheetApi';
import './WorkQueues.css';

export const TimesheetCreatePage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [form, setForm] = useState({
    employeeId: '',
    employeeName: '',
    department: '',
    location: '',
    payPeriodStart: '',
    payPeriodEnd: '',
    regularHours: '',
    overtimeHours: '',
    holidayHours: '',
    sickHours: '',
    vacationHours: '',
    comments: ''
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    setBreadcrumbs([
      { label: 'Payments', path: '/payments/timesheets' },
      { label: 'Timesheets', path: '/payments/timesheets' },
      { label: 'New Timesheet' }
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const handleChange = (field, value) => setForm(prev => ({ ...prev, [field]: value }));

  const handleSubmit = async (andSubmit = false) => {
    if (!form.employeeId || !form.employeeName || !form.department || !form.location || !form.payPeriodStart || !form.payPeriodEnd) {
      setError('Please fill in all required fields.');
      return;
    }
    setError('');
    setSubmitting(true);
    try {
      const payload = {
        employeeId: form.employeeId,
        employeeName: form.employeeName,
        department: form.department,
        location: form.location,
        payPeriodStart: form.payPeriodStart,
        payPeriodEnd: form.payPeriodEnd,
        regularHours: parseFloat(form.regularHours) || 0,
        overtimeHours: parseFloat(form.overtimeHours) || 0,
        holidayHours: parseFloat(form.holidayHours) || 0,
        sickHours: parseFloat(form.sickHours) || 0,
        vacationHours: parseFloat(form.vacationHours) || 0,
        comments: form.comments
      };
      const created = await timesheetApi.createTimesheet(payload);
      const tsId = created?.id || created?.timesheetId;
      if (andSubmit && tsId) {
        await timesheetApi.submitTimesheet(tsId);
      }
      navigate(tsId ? `/payments/timesheets/${tsId}` : '/payments/timesheets');
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Failed to create timesheet');
    } finally {
      setSubmitting(false);
    }
  };

  const totalHours = (parseFloat(form.regularHours) || 0) + (parseFloat(form.overtimeHours) || 0)
    + (parseFloat(form.holidayHours) || 0) + (parseFloat(form.sickHours) || 0) + (parseFloat(form.vacationHours) || 0);

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>New Timesheet</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Cancel</button>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Employee Information</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Employee ID *</label>
              <input type="text" value={form.employeeId} onChange={e => handleChange('employeeId', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Employee Name *</label>
              <input type="text" value={form.employeeName} onChange={e => handleChange('employeeName', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Department *</label>
              <input type="text" value={form.department} onChange={e => handleChange('department', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Location *</label>
              <input type="text" value={form.location} onChange={e => handleChange('location', e.target.value)} />
            </div>
          </div>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Pay Period & Hours</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Pay Period Start *</label>
              <input type="date" value={form.payPeriodStart} onChange={e => handleChange('payPeriodStart', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Pay Period End *</label>
              <input type="date" value={form.payPeriodEnd} onChange={e => handleChange('payPeriodEnd', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Regular Hours</label>
              <input type="number" step="0.5" min="0" value={form.regularHours} onChange={e => handleChange('regularHours', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Overtime Hours</label>
              <input type="number" step="0.5" min="0" value={form.overtimeHours} onChange={e => handleChange('overtimeHours', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Holiday Hours</label>
              <input type="number" step="0.5" min="0" value={form.holidayHours} onChange={e => handleChange('holidayHours', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Sick Hours</label>
              <input type="number" step="0.5" min="0" value={form.sickHours} onChange={e => handleChange('sickHours', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Vacation Hours</label>
              <input type="number" step="0.5" min="0" value={form.vacationHours} onChange={e => handleChange('vacationHours', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Total Hours (calculated)</label>
              <input type="text" value={totalHours.toFixed(1)} readOnly style={{ background: '#f7fafc' }} />
            </div>
          </div>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Comments</h4></div>
        <div className="wq-panel-body">
          <div className="wq-form-field" style={{ maxWidth: '100%' }}>
            <textarea rows={3} value={form.comments} onChange={e => handleChange('comments', e.target.value)}
              placeholder="Optional comments..." style={{ width: '100%', padding: '0.5rem', border: '1px solid #cbd5e0', borderRadius: '4px' }} />
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
        <button className="wq-btn wq-btn-outline" onClick={() => handleSubmit(false)} disabled={submitting}>
          {submitting ? 'Saving...' : 'Save as Draft'}
        </button>
        <button className="wq-btn wq-btn-primary" onClick={() => handleSubmit(true)} disabled={submitting}>
          {submitting ? 'Saving...' : 'Save & Submit'}
        </button>
      </div>
    </div>
  );
};
