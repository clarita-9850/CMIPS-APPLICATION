import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'create Recipient Waiver', route: '/case/create-recipient-waiver' },
    { label: 'view Recipient Waiver', route: '/case/view-recipient-waiver' },
    { label: 'edit Recipient Waiver', route: '/case/edit-recipient-waiver' },
    { label: 'home Page', route: '/person/home-page' }
  ];

export function CaseListRecipientWaiverPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listRecipientWaiver"}
      title={"Recipient Waiver:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"CORI Date"} value={record && record['cORIDate']} />
          <UimField label={"Recipient Waiver Begin Date"} value={record && record['recipientWaiverBeginDate']} />
          <UimField label={"Recipient Waiver End Date"} value={record && record['recipientWaiverEndDate']} />
          <UimField label={"Termination Reason"} value={record && record['terminationReason']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-recipient-waiver')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListRecipientWaiverPage;
