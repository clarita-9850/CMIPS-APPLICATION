import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function CaseListCountyPayLevelsPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listCountyPayLevels"}
      title={"List County Pay Levels"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"Grade"} value={record && record['grade']} />
          <UimField label={"Step"} value={record && record['step']} />
          <UimField label={"Amount"} value={record && record['amount']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
        </div>
      </UimSection>
      <UimSection title={"Pay Rate"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Select')}>Select</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListCountyPayLevelsPage;
