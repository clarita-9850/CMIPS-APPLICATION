import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'resolve View Hours', route: '/misc/action-resolve-view-hours' },
    { label: 'search Weekly Provider Paid Hours', route: '/person/search-weekly-provider-paid-hours' }
  ];

export function PersonViewWeeklyHoursDetailsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewWeeklyHoursDetails"}
      title={"View Hours Details:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Time Entry Type"} value={record && record['timeEntryType']} />
          <UimField label={"Transaction Number"} value={record && record['transactionNumber']} />
          <UimField label={"Program"} value={record && record['program']} />
          <UimField label={"Warrant Status"} value={record && record['warrantStatus']} />
          <UimField label={"Travel"} value={record && record['travel']} />
          <UimField label={"Total Hours"} value={record && record['totalHours']} />
          <UimField label={"OT Hours"} value={record && record['oTHours']} />
        </div>
      </UimSection>
      <UimSection title={"Transaction & Time Entry [HH:MM] Details"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewWeeklyHoursDetailsPage;
