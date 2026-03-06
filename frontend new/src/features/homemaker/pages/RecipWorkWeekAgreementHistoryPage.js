import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Record', route: '/homemaker/view-record' }
  ];

export function RecipWorkWeekAgreementHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('homemaker', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"RecipWorkWeekAgreement_History"}
      title={"Recipient Workweek Agreement History:"}
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
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Weekly"} value={record && record['weekly']} />
          <UimField label={"Sun"} value={record && record['sun']} />
          <UimField label={"Mon"} value={record && record['mon']} />
          <UimField label={"Tue"} value={record && record['tue']} />
          <UimField label={"Wed"} value={record && record['wed']} />
          <UimField label={"Thu"} value={record && record['thu']} />
          <UimField label={"Fri"} value={record && record['fri']} />
          <UimField label={"Sat"} value={record && record['sat']} />
          <UimField label={"Total Hours"} value={record && record['totalHours']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"Updated Date"} value={record && record['updatedDate']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Provider Type"} value={record && record['providerType']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Weekly"} value={record && record['weekly']} />
          <UimField label={"Sun"} value={record && record['sun']} />
          <UimField label={"Mon"} value={record && record['mon']} />
          <UimField label={"Tue"} value={record && record['tue']} />
          <UimField label={"Wed"} value={record && record['wed']} />
          <UimField label={"Thu"} value={record && record['thu']} />
          <UimField label={"Fri"} value={record && record['fri']} />
          <UimField label={"Sat"} value={record && record['sat']} />
          <UimField label={"Total Hours"} value={record && record['totalHours']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"Updated Date"} value={record && record['updatedDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default RecipWorkWeekAgreementHistoryPage;
