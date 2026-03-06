import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'create Message Center', route: '/case/create-message-center' },
    { label: 'view Message Center', route: '/case/view-message-center' },
    { label: 'user Home Popup', route: '/organization/user-home-popup' }
  ];

export function CaseListMessageCenterPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listMessageCenter"}
      title={"Message Center:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Entered By"} value={record && record['enteredBy']} />
          <UimField label={"Creation Date"} value={record && record['creationDate']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"High Priority"} value={record && record['highPriority']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Read Receipt"} value={record && record['readReceipt']} />
          <UimField label={"Entered By"} value={record && record['enteredBy']} />
          <UimField label={"Creation Date"} value={record && record['creationDate']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"High Priority"} value={record && record['highPriority']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Read Receipt"} value={record && record['readReceipt']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-message-center')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-message-center')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListMessageCenterPage;
