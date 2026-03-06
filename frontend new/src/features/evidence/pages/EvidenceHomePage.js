import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'create Assessment Type', route: '/evidence/create-assessment-type' },
    { label: 'check Eligibility', route: '/evidence/check-eligibility' },
    { label: 'submit', route: '/evidence/submit' },
    { label: 'cancel Submission', route: '/evidence/cancel-submission' },
    { label: 'approve', route: '/evidence/approve' },
    { label: 'reject Approval Requests', route: '/evidence/reject-approval-requests' },
    { label: 'delete Pending Evidence', route: '/evidence/delete-pending-evidence' }
  ];

export function EvidenceHomePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const eligibilityApi = getDomainApi('evidence');
  const [activeTab, setActiveTab] = React.useState(0);
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_home"}
      title={"Evidence Workspace:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <div className="uim-tab-nav">
        <button className={activeTab===0 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(0)}>Manage</button>
      </div>
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Evidence Type"} value={record && record['evidenceType']} />
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Action"} value={record && record['action']} />
          <UimField label={"Evidence Type"} value={record && record['evidenceType']} />
          <UimField label={"Assessment Type"} value={record && record['assessmentType']} />
          <UimField label={"Auth Start Date"} value={record && record['authStartDate']} />
          <UimField label={"Auth End Date"} value={record && record['authEndDate']} />
          <UimField label={"Last Update Date"} value={record && record['lastUpdateDate']} />
          <UimField label={"Action"} value={record && record['action']} />
          <UimField label={"Evidence Type"} value={record && record['evidenceType']} />
          <UimField label={"Assessment Type"} value={record && record['assessmentType']} />
          <UimField label={"Auth Start Date"} value={record && record['authStartDate']} />
          <UimField label={"Auth End Date"} value={record && record['authEndDate']} />
          <UimField label={"Last Update Date"} value={record && record['lastUpdateDate']} />
          <UimField label={"Action"} value={record && record['action']} />
          <UimField label={"Evidence Type"} value={record && record['evidenceType']} />
          <UimField label={"Assessment Type"} value={record && record['assessmentType']} />
          <UimField label={"Auth Start Date"} value={record && record['authStartDate']} />
          <UimField label={"Auth End Date"} value={record && record['authEndDate']} />
          <UimField label={"Last Update Date"} value={record && record['lastUpdateDate']} />
          <UimField label={"Action"} value={record && record['action']} />
          <UimField label={"Evidence Type"} value={record && record['evidenceType']} />
          <UimField label={"Assessment Type"} value={record && record['assessmentType']} />
          <UimField label={"Auth Start Date"} value={record && record['authStartDate']} />
          <UimField label={"Auth End Date"} value={record && record['authEndDate']} />
          <UimField label={"Last Update Date"} value={record && record['lastUpdateDate']} />
        </div>
      </UimSection>
      <UimSection title={"Pending Evidence Details"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Active Evidence Details"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Pending Evidence Details"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Active Evidence Details"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Add New Evidence')}>Add New Evidence</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/check-eligibility')}>Check Eligibility</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/submit')}>Submit for Approval</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/cancel-submission')}>Cancel Submission</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { eligibilityApi.update(id, { status: 'approved' }).then(() => { alert('Approved successfully'); navigate(-1); }).catch(err => alert('Approve failed: ' + err.message)); }}>Approve</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { eligibilityApi.update(id, { status: 'denied' }).then(() => { alert('Denied successfully'); navigate(-1); }).catch(err => alert('Action failed: ' + err.message)); }}>Reject</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { eligibilityApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/delete-pending-evidence')}>Delete Pending Evidence</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/delete-pending-evidence')}>Delete Pending Evidence</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceHomePage;
