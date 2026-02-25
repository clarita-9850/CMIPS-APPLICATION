import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'ihss Notification To S A W S', route: '/evidence/ihss-notification-to-saws' },
    { label: 'resolve S O C Calculation', route: '/evidence/resolve-soc-calculation' },
    { label: 'modify S O C Evidence', route: '/evidence/modify-soc-evidence' },
    { label: 'delete S O C Evidence', route: '/evidence/delete-soc-evidence' },
    { label: 'create S O C Evidence', route: '/evidence/create-soc-evidence' },
    { label: 'create Income Evidence', route: '/evidence/create-income-evidence' },
    { label: 'modify Income Evidence', route: '/evidence/modify-income-evidence' },
    { label: 'delete Income Evidence', route: '/evidence/delete-income-evidence' },
    { label: 'home', route: '/evidence/home' }
  ];

export function EvidenceSocEvidenceHomePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const eligibilityApi = getDomainApi('evidence');
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_socEvidenceHome"}
      title={"Share of Cost Evidence:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Share of Cost Linkage"} value={record && record['shareOfCostLinkage']} />
          <UimField label={"Dependents"} value={record && record['dependents']} />
        </div>
      </UimSection>
      <UimSection title={"Share of Cost Evidence"}>
        <div className="uim-form-grid">
          <UimField label={"Benefit Level Code"} value={record && record['benefitLevelCode']} />
          <UimField label={"Source"} value={record && record['source']} />
        </div>
      </UimSection>
      <UimSection title={"Income Evidence"}>
        <div className="uim-form-grid">
          <UimField label={"Monthly Income Amount"} value={record && record['monthlyIncomeAmount']} />
          <UimField label={"Deduction"} value={record && record['deduction']} />
        </div>
      </UimSection>
      <UimSection title={"Share of Cost Calculation"}>
        <div className="uim-form-grid">
          <UimField label={"Countable Income"} value={record && record['countableIncome']} />
          <UimField label={"IHSS Share Of Cost"} value={record && record['iHSSShareOfCost']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: IHSS Notification to SAWS')}>IHSS Notification to SAWS</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Calculate IHSS Share of Cost')}>Calculate IHSS Share of Cost</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { eligibilityApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Delete</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { eligibilityApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Add</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { eligibilityApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Delete</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/home')}>Evidence Home</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceSocEvidenceHomePage;
