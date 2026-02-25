import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'task Home', route: '/task-management/task-home' },
    { label: 'duplicate Referral Check', route: '/person/duplicate-referral-check' },
    { label: 'duplicate Registration Check', route: '/person/duplicate-registration-check' },
    { label: 'search', route: '/person/search' },
    { label: 'search State Hearing', route: '/case/search-state-hearing' },
    { label: 'duplicate Provider Check', route: '/person/duplicate-provider-check' },
    { label: 'merge Duplicate S S N', route: '/person/merge-duplicate-ssn' },
    { label: 'list Warrant Replacements', route: '/payment/list-warrant-replacements' },
    { label: 'reissue B V I Timesheet', route: '/case/reissue-bvi-timesheet' },
    { label: 'enter Travel Claim Num', route: '/case/enter-travel-claim-num' },
    { label: 'live In Prov Self Certification Search', route: '/person/live-in-prov-self-certification-search' },
    { label: 'Direct Deposit Batches', route: '/back-office/direct-deposit-batches' },
    { label: 'enter Sick Leave Claim Manual Entry', route: '/person/enter-sick-leave-claim-manual-entry' },
    { label: 'home', route: '/case/ihss-case-home' }
  ];

export function HDBVINOATSCustomHomePage() {
  const navigate = useNavigate();
  const miscApi = getDomainApi('misc');
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"HDBVINOATSCustomHome"}
      title={"My Workspace:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"My Shortcuts"}>
        <div className="uim-form-grid">
          <UimField label={"Task"} value={record && record['task']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Due Date"} value={record && record['dueDate']} />
        </div>
      </UimSection>
      <UimSection title={"Case Search"}>
        <div className="uim-form-grid">
          <UimField label={"Priority"} value={record && record['priority']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
        </div>
      </UimSection>
      <UimSection title={"My Tasks"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New Referral')}>New Referral</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New Application')}>New Application</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Find a Person')}>Find a Person</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Find a State Hearing Case')}>Find a State Hearing Case</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Register a Provider')}>Register a Provider</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Merge Duplicate SSN')}>Merge Duplicate SSN</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Enter Warrant Replacements')}>Enter Warrant Replacements</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Reissue Large Font Timesheet')}>Reissue Large Font Timesheet</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Travel Claim Manual Entry')}>Travel Claim Manual Entry</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: IRS Live-In Provider Self-Certification')}>IRS Live-In Provider Self-Certification</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/back-office/direct-deposit-batches')}>Direct Deposit Batches</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/enter-sick-leave-claim-manual-entry')}>Sick Leave Claim Manual Entry</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { miscApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
      </div>
    </UimPageLayout>
  );
}

export default HDBVINOATSCustomHomePage;
