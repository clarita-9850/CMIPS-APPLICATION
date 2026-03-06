import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Provider', route: '/misc/pa-case-view-provider' }
  ];

export function PACaseListProviderPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"PACase_listProvider"}
      title={"Provider Management - Case Providers:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"IHSS/WPCS"} value={record && record['iHSSWPCS']} />
          <UimField label={"Provider Status"} value={record && record['providerStatus']} />
          <UimField label={"Assigned Hours"} value={record && record['assignedHours']} />
          <UimField label={"Relationship to Recipient"} value={record && record['relationshipToRecipient']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/misc/pa-case-view-provider')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default PACaseListProviderPage;
