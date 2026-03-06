import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Uploaded Documents', route: '/person/list-uploaded-documents' },
    { label: 'print Approved Form', route: '/person/print-approved-form' },
    { label: 'list Electronic Forms History', route: '/person/list-electronic-forms-history' },
    { label: 'print History Approved Form', route: '/person/print-history-approved-form' }
  ];

export function PersonListApprovedElectronicFormsPage() {
  const navigate = useNavigate();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listApprovedElectronicForms"}
      title={"Completed Electronic Forms:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"Display Latest Form Only"} value={record && record['displayLatestFormOnly']} />
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Form Name"} value={record && record['formName']} />
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Sent to"} value={record && record['sentTo']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"Created By"} value={record && record['createdBy']} />
          <UimField label={"Form Name"} value={record && record['formName']} />
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Sent to"} value={record && record['sentTo']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"Created By"} value={record && record['createdBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Attachments')}>Attachments</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Completed Electronic Forms')}>Completed Electronic Forms</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt; Previous')}>&lt;&lt; Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next &gt;&gt;')}>Next &gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt; Search Previous 6 Months')}>&lt;&lt; Search Previous 6 Months</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Search Next 6 Months &gt;&gt;')}>Search Next 6 Months &gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.print()}>Print</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/list-electronic-forms-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/print-history-approved-form')}>Print History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.print()}>Print</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/list-electronic-forms-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/print-history-approved-form')}>Print History</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonListApprovedElectronicFormsPage;
