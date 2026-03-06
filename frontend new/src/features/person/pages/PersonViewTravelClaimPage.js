import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'travel Claim History', route: '/person/travel-claim-history' },
    { label: 'search Travel Claim', route: '/person/search-travel-claim' }
  ];

export function PersonViewTravelClaimPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewTravelClaim"}
      title={"View Travel Claim:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Recipient Number"} value={record && record['recipientNumber']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Print Method"} value={record && record['printMethod']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Travel Claim Number"} value={record && record['travelClaimNumber']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Mode of Entry"} value={record && record['modeOfEntry']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Print Date"} value={record && record['printDate']} />
          <UimField label={"Service Period To"} value={record && record['servicePeriodTo']} />
          <UimField label={"Received Date"} value={record && record['receivedDate']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries [HH:MM]"}>
        <div className="uim-form-grid">
          <UimField label={"Hours Claimed (HH:MM)"} value={record && record['hoursClaimedHHMM']} />
          <UimField label={"Travel Hours Paid (HH:MM)"} value={record && record['travelHoursPaidHHMM']} />
          <UimField label={"Hours Paid at Overtime Rate(HH:MM)"} value={record && record['hoursPaidAtOvertimeRateHHMM']} />
          <UimField label={"Travel Hours Not Paid (HH:MM)"} value={record && record['travelHoursNotPaidHHMM']} />
          <UimField label={"Code"} value={record && record['code']} />
          <UimField label={"Description"} value={record && record['description']} />
        </div>
      </UimSection>
      <UimSection title={"Exceptions"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Travel Claim Details')}>View Travel Claim Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/travel-claim-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Print E-Travel Claim')}>Print E-Travel Claim</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Image')}>View Image</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewTravelClaimPage;
