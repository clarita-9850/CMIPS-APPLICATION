import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'create Record', route: '/homemaker/create-record-2' },
    { label: 'inactivatelist Records', route: '/homemaker/inactivatelist-records' },
    { label: 'view Record', route: '/homemaker/view-record-2' },
    { label: 'modify Record', route: '/homemaker/modify-record-2' }
  ];

export function WorkWeekAgreementListRecordsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('homemaker', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"WorkWeekAgreement_listRecords"}
      title={"Provider Workweek Agreement:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Program Type"} value={record && record['programType']} />
          <UimField label={"Total Hours"} value={record && record['totalHours']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"Updated Date"} value={record && record['updatedDate']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Inactive History')}>View Inactive History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/homemaker/view-record-2')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default WorkWeekAgreementListRecordsPage;
