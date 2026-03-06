import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as applicationsApi from '../api/applicationsApi';
import './WorkQueues.css';

export const ApplicationDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();
  const [app, setApp] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionError, setActionError] = useState('');

  useEffect(() => {
    setBreadcrumbs([{ label: 'Applications', path: '/cases' }, { label: `Application #${id}` }]);
    return () => setBreadcrumbs([]);
  }, [id, setBreadcrumbs]);

  useEffect(() => {
    applicationsApi.getApplicationById(id)
      .then(data => setApp(data))
      .catch(() => setApp(null))
      .finally(() => setLoading(false));
  }, [id]);

  const handleAction = async (action) => {
    setActionError('');
    try {
      if (action === 'approve') await applicationsApi.approveApplication(id, {});
      else if (action === 'deny') await applicationsApi.denyApplication(id, {});
      else if (action === 'cin') await applicationsApi.cinClearance(id);
      else if (action === 'ssn') await applicationsApi.ssnValidation(id);
      // Reload
      const updated = await applicationsApi.getApplicationById(id);
      setApp(updated);
    } catch (err) {
      setActionError(err?.response?.data?.message || err.message || 'Action failed');
    }
  };

  if (loading) return <div className="wq-page"><p>Loading application...</p></div>;
  if (!app) return <div className="wq-page"><p>Application not found.</p></div>;

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Application #{id}</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/cases')}>Back</button>
      </div>

      {/* Action Bar */}
      <div className="wq-manage-bar">
        <button className="wq-btn wq-btn-primary" onClick={() => handleAction('cin')}>CIN Clearance</button>
        <button className="wq-btn wq-btn-primary" onClick={() => handleAction('ssn')}>SSN Validation</button>
        <button className="wq-btn wq-btn-primary" onClick={() => handleAction('approve')}>Approve</button>
        <button className="wq-btn" style={{ background: '#c53030', color: 'white' }} onClick={() => handleAction('deny')}>Deny</button>
      </div>

      {actionError && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {actionError}
        </div>
      )}

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Application Details</h4></div>
        <div className="wq-panel-body">
          <div className="wq-detail-grid">
            <div className="wq-detail-row"><span className="wq-detail-label">Status</span><span className="wq-detail-value"><span className={`wq-badge wq-badge-${(app.status || '').toLowerCase()}`}>{app.status}</span></span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">Applicant</span><span className="wq-detail-value">{app.firstName} {app.lastName}</span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">DOB</span><span className="wq-detail-value">{app.dateOfBirth || 'N/A'}</span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">County</span><span className="wq-detail-value">{app.countyCode || app.county}</span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">Program</span><span className="wq-detail-value">{app.programType || 'N/A'}</span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">Created By</span><span className="wq-detail-value">{app.createdBy}</span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">Created</span><span className="wq-detail-value">{app.createdAt ? new Date(app.createdAt).toLocaleString() : 'N/A'}</span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">CIN Clearance</span><span className="wq-detail-value">{app.cinCleared ? 'Cleared' : 'Pending'}</span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">SSN Validated</span><span className="wq-detail-value">{app.ssnValidated ? 'Validated' : 'Pending'}</span></div>
          </div>
        </div>
      </div>
    </div>
  );
};
