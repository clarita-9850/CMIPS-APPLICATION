import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Weekly Hours Details', route: '/person/view-weekly-hours-details' }
  ];

export function PersonSearchWeeklyProviderPaidHoursPage() {
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_searchWeeklyProviderPaidHours"}
      title={"Weekly Provider Paid Hours:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Week Starting Date"} value={record && record['weekStartingDate']} />
          <UimField label={"Week Ending Date"} value={record && record['weekEndingDate']} />
          <UimField label={"IHSS Paid Hours"} value={record && record['iHSSPaidHours']} />
          <UimField label={"IHSS Unpaid OT Hours"} value={record && record['iHSSUnpaidOTHours']} />
          <UimField label={"WPCS Paid Hours"} value={record && record['wPCSPaidHours']} />
          <UimField label={"WPCS Unpaid OT Hours"} value={record && record['wPCSUnpaidOTHours']} />
          <UimField label={"Travel Paid Hours"} value={record && record['travelPaidHours']} />
          <UimField label={"Training Paid Hours"} value={record && record['trainingPaidHours']} />
          <UimField label={"Total Paid Hours"} value={record && record['totalPaidHours']} />
          <UimField label={"OT Paid Hours"} value={record && record['oTPaidHours']} />
          <UimField label={"Overpaid OT Hours"} value={record && record['overpaidOTHours']} />
        </div>
      </UimSection>
      <UimSection title={"Search Results"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Details')}>View Details</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonSearchWeeklyProviderPaidHoursPage;
