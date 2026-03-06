import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'modify Teach And Demo Service From List', route: '/evidence/modify-teach-and-demo-service-from-list' },
    { label: 'modify Service Type With Proration From List', route: '/evidence/modify-service-type-with-proration-from-list' }
  ];

export function EvidenceDeleteTaskWithProrationForSvcTypeFromListPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_deleteTaskWithProrationForSvcTypeFromList"}
      title={"Delete Task With Proration For Svc Type From List"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"ID"} value={record && record['iD']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date"} value={record && record['date']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Yes</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: No')}>No</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceDeleteTaskWithProrationForSvcTypeFromListPage;
