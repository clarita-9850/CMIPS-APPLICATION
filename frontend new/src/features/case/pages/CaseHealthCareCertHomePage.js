import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'modify Health Care Cert', route: '/case/modify-health-care-cert' },
    { label: 'health Care Cert History', route: '/case/health-care-cert-history' },
    { label: 'inactivate Health Care Cert', route: '/case/inactivate-health-care-cert' }
  ];

export function CaseHealthCareCertHomePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_healthCareCertHome"}
      title={"Health Care Certification:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Health Care Certification"}>
        <div className="uim-form-grid">
          <UimField label={"ID"} value={record && record['iD']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Health Care Certification"}>
        <div className="uim-form-grid">
          <UimField label={"Date"} value={record && record['date']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Health Care Certification')}>Edit Health Care Certification</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/health-care-cert-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { casesApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseHealthCareCertHomePage;
