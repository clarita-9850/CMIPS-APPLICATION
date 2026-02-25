import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'create Record', route: '/homemaker/create-record' },
    { label: 'view Inactive History', route: '/homemaker/view-inactive-history' },
    { label: 'view Record', route: '/homemaker/view-record' },
    { label: 'modify Record', route: '/homemaker/modify-record' },
    { label: 'list Records', route: '/homemaker/list-records' }
  ];

export function RecipListWorkWeekAgreementPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Recip_listWorkWeekAgreement"}
      title={"Recipient Workweek Agreement:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Provider Type"} value={record && record['providerType']} />
          <UimField label={"Total Hours"} value={record && record['totalHours']} />
          <UimField label={"Weekly"} value={record && record['weekly']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"Updated Date"} value={record && record['updatedDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Provider Work Week Agreement"} value={record && record['providerWorkWeekAgreement']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/homemaker/view-inactive-history')}>View Inactive History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/homemaker/view-inactive-history')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default RecipListWorkWeekAgreementPage;
