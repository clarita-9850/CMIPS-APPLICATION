import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'health Care Cert History View', route: '/case/health-care-cert-history-view' },
    { label: 'health Care Cert Home', route: '/case/health-care-cert-home' }
  ];

export function CaseHealthCareCertHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_healthCareCertHistory"}
      title={"Health Care Certification History :"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Due Date"} value={record && record['dueDate']} />
          <UimField label={"Exception Granted Date"} value={record && record['exceptionGrantedDate']} />
          <UimField label={"Good Cause Extension Date"} value={record && record['goodCauseExtensionDate']} />
          <UimField label={"Health Care Certification Type"} value={record && record['healthCareCertificationType']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"Last Update By"} value={record && record['lastUpdateBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/health-care-cert-history-view')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseHealthCareCertHistoryPage;
