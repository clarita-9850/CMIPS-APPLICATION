import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as timesheetApi from '../api/timesheetApi';
import './WorkQueues.css';

export const TimesheetDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { roles } = useAuth();
  const { setBreadcrumbs } = useBreadcrumbs();
  const isSupervisor = roles.some(r => r.includes('SUPERVISOR'));

  const [ts, setTs] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionError, setActionError] = useState('');

  const load = useCallback(async () => {
    try {
      const data = await timesheetApi.getTimesheetById(id);
      setTs(data);
    } catch (err) {
      console.warn('[TimesheetDetail] Error:', err?.message);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    setBreadcrumbs([
      { label: 'Payments', path: '/payments/timesheets' },
      { label: 'Timesheets', path: '/payments/timesheets' },
      { label: `Timesheet ${id}` }
    ]);
    return () => setBreadcrumbs([]);
  }, [id, setBreadcrumbs]);

  const doAction = async (fn, label) => {
    setActionError('');
    try {
      await fn();
      await load();
    } catch (err) {
      setActionError(`${label} failed: ${err?.response?.data?.message || err?.message || 'Unknown error'}`);
    }
  };

  const handleReject = async () => {
    const comments = prompt('Enter rejection comments:');
    if (comments) {
      await doAction(() => timesheetApi.rejectTimesheet(id, comments), 'Reject');
    }
  };

  const formatDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';
  const formatDateTime = (d) => d ? new Date(d).toLocaleString() : '\u2014';

  if (loading) return <div className="wq-page"><p>Loading timesheet...</p></div>;
  if (!ts) return <div className="wq-page"><p>Timesheet not found.</p><button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Back</button></div>;

  const canSubmit = ts.status === 'DRAFT';
  const canApprove = isSupervisor && (ts.status === 'SUBMITTED' || ts.status === 'DRAFT');
  const canReject = isSupervisor && ts.status === 'SUBMITTED';
  const canDelete = ts.status === 'DRAFT';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Timesheet #{ts.id}</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Back to Timesheets</button>
      </div>

      {actionError && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {actionError}
        </div>
      )}

      {/* Actions */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Actions</h4></div>
        <div className="wq-manage-bar">
          {canSubmit && (
            <button className="wq-manage-action" onClick={() => doAction(() => timesheetApi.submitTimesheet(id), 'Submit')}>
              <span className="action-icon">&#9654;</span> Submit
            </button>
          )}
          {canApprove && (
            <button className="wq-manage-action" onClick={() => doAction(() => timesheetApi.approveTimesheet(id), 'Approve')}>
              <span className="action-icon">&#10003;</span> Approve
            </button>
          )}
          {canReject && (
            <button className="wq-manage-action" onClick={handleReject}>
              <span className="action-icon">&#10005;</span> Reject
            </button>
          )}
          {canDelete && (
            <button className="wq-manage-action" onClick={() => {
              if (window.confirm('Delete this draft timesheet?')) {
                doAction(() => timesheetApi.deleteTimesheet(id), 'Delete').then(() => navigate('/payments/timesheets'));
              }
            }}>
              <span className="action-icon">&#128465;</span> Delete
            </button>
          )}
        </div>
      </div>

      <div className="wq-task-columns">
        {/* Employee Info */}
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Employee Information</h4></div>
          <div className="wq-panel-body">
            <div className="wq-detail-grid">
              <div className="wq-detail-row"><span className="wq-detail-label">Employee ID:</span><span className="wq-detail-value">{ts.employeeId || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Employee Name:</span><span className="wq-detail-value">{ts.employeeName || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Department:</span><span className="wq-detail-value">{ts.department || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Location:</span><span className="wq-detail-value">{ts.location || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Pay Period:</span><span className="wq-detail-value">{formatDate(ts.payPeriodStart)} - {formatDate(ts.payPeriodEnd)}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Status:</span><span className="wq-detail-value"><span className={`wq-badge wq-badge-${(ts.status || '').toLowerCase()}`}>{ts.status}</span></span></div>
            </div>
          </div>
        </div>

        {/* Hours Breakdown */}
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Hours Breakdown</h4></div>
          <div className="wq-panel-body">
            <div className="wq-detail-grid">
              <div className="wq-detail-row"><span className="wq-detail-label">Regular Hours:</span><span className="wq-detail-value">{ts.regularHours ?? 0}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Overtime Hours:</span><span className="wq-detail-value">{ts.overtimeHours ?? 0}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Holiday Hours:</span><span className="wq-detail-value">{ts.holidayHours ?? 0}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Sick Hours:</span><span className="wq-detail-value">{ts.sickHours ?? 0}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Vacation Hours:</span><span className="wq-detail-value">{ts.vacationHours ?? 0}</span></div>
              <div className="wq-detail-row" style={{ fontWeight: 600, borderTop: '1px solid #e2e8f0', paddingTop: '0.5rem' }}>
                <span className="wq-detail-label">Total Hours:</span><span className="wq-detail-value">{ts.totalHours ?? 0}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Comments & Audit */}
      <div className="wq-task-columns">
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Comments</h4></div>
          <div className="wq-panel-body">
            <div className="wq-detail-grid">
              <div className="wq-detail-row"><span className="wq-detail-label">Employee Comments:</span><span className="wq-detail-value" style={{ whiteSpace: 'pre-wrap' }}>{ts.comments || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Supervisor Comments:</span><span className="wq-detail-value" style={{ whiteSpace: 'pre-wrap' }}>{ts.supervisorComments || '\u2014'}</span></div>
            </div>
          </div>
        </div>

        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Audit Trail</h4></div>
          <div className="wq-panel-body">
            <div className="wq-detail-grid">
              <div className="wq-detail-row"><span className="wq-detail-label">Created:</span><span className="wq-detail-value">{formatDateTime(ts.createdAt)}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Last Updated:</span><span className="wq-detail-value">{formatDateTime(ts.updatedAt)}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Submitted By:</span><span className="wq-detail-value">{ts.submittedBy || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Submitted At:</span><span className="wq-detail-value">{formatDateTime(ts.submittedAt)}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Approved By:</span><span className="wq-detail-value">{ts.approvedBy || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Approved At:</span><span className="wq-detail-value">{formatDateTime(ts.approvedAt)}</span></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
