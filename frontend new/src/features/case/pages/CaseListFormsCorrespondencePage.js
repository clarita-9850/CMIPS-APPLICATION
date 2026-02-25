import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Electronic Forms', route: '/case/list-electronic-forms' },
    { label: 'create Forms Correspondence', route: '/case/create-forms-correspondence' },
    { label: 'view Forms And Correspondence', route: '/case/view-forms-and-correspondence' },
    { label: 'modify Forms And Correspondence', route: '/case/modify-forms-and-correspondence' },
    { label: 'view Forms And Correspondence History', route: '/case/view-forms-and-correspondence-history' }
  ];

export function CaseListFormsCorrespondencePage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listFormsCorrespondence"}
      title={"Forms:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date Time"} value={record && record['dateTime']} />
          <UimField label={"BVI Format"} value={record && record['bVIFormat']} />
          <UimField label={"Due Date"} value={record && record['dueDate']} />
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date Time"} value={record && record['dateTime']} />
          <UimField label={"BVI Format"} value={record && record['bVIFormat']} />
          <UimField label={"Due Date"} value={record && record['dueDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Forms/Correspondence')}>Forms/Correspondence</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/list-electronic-forms')}>Electronic Forms</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-forms-and-correspondence')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-forms-and-correspondence-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.print()}>Print</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-forms-and-correspondence')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-forms-and-correspondence-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.print()}>Print</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListFormsCorrespondencePage;
