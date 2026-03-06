import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'search Person', route: '/help-desk/search-person' },
    { label: 'Direct Deposit Batches', route: '/back-office/direct-deposit-batches' }
  ];

export function HelpDeskWorkspacePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('help-desk', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"HelpDesk_workspace"}
      title={"Help Desk Workspace:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"My Shortcuts"}>
        <div className="uim-form-grid">
          <UimField label={"ID"} value={record && record['iD']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date"} value={record && record['date']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Find a Person')}>Find a Person</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/back-office/direct-deposit-batches')}>Direct Deposit Batches</button>
      </div>
    </UimPageLayout>
  );
}

export default HelpDeskWorkspacePage;
