import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Recipient Monthly O T Max', route: '/case/view-recipient-monthly-ot-max' }
  ];

export function CaseMonthlyOTMaxPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_MonthlyOTMax"}
      title={"Monthly Overtime Maximum:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Monthly Overtime Maximum Search"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Month"} value={record && record['month']} />
          <UimField label={"Monthly Auth Hours"} value={record && record['monthlyAuthHours']} />
          <UimField label={"Weekly Auth Hours"} value={record && record['weeklyAuthHours']} />
          <UimField label={"Weekly Maximum"} value={record && record['weeklyMaximum']} />
          <UimField label={"Monthly Overtime Maximum"} value={record && record['monthlyOvertimeMaximum']} />
          <UimField label={"Month"} value={record && record['month']} />
          <UimField label={"Monthly Auth Hours"} value={record && record['monthlyAuthHours']} />
          <UimField label={"Weekly Auth Hours"} value={record && record['weeklyAuthHours']} />
          <UimField label={"Weekly Maximum"} value={record && record['weeklyMaximum']} />
          <UimField label={"Monthly Overtime Maximum"} value={record && record['monthlyOvertimeMaximum']} />
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
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-recipient-monthly-ot-max')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseMonthlyOTMaxPage;
