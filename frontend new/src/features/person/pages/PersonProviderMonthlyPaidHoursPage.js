import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Provider Monthly Paid Hours', route: '/person/view-provider-monthly-paid-hours' }
  ];

export function PersonProviderMonthlyPaidHoursPage() {
  const navigate = useNavigate();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_providerMonthlyPaidHours"}
      title={"Monthly Provider Paid Hours:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Overtime Exemption"} value={record && record['overtimeExemption']} />
          <UimField label={"Claimed Hours"} value={record && record['claimedHours']} />
          <UimField label={"Paid Hours"} value={record && record['paidHours']} />
          <UimField label={"Ineligible Hours Cutback"} value={record && record['ineligibleHoursCutback']} />
          <UimField label={"IHSS Exemption Cutback"} value={record && record['iHSSExemptionCutback']} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"WPCS Exemption Cutback"} value={record && record['wPCSExemptionCutback']} />
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Overtime Exemption"} value={record && record['overtimeExemption']} />
          <UimField label={"Claimed Hours"} value={record && record['claimedHours']} />
          <UimField label={"Paid Hours"} value={record && record['paidHours']} />
          <UimField label={"Ineligible Hours Cutback"} value={record && record['ineligibleHoursCutback']} />
          <UimField label={"IHSS Exemption Cutback"} value={record && record['iHSSExemptionCutback']} />
          <UimField label={"WPCS Exemption Cutback"} value={record && record['wPCSExemptionCutback']} />
        </div>
      </UimSection>
      <UimSection title={"Search Results"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
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
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-provider-monthly-paid-hours')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonProviderMonthlyPaidHoursPage;
