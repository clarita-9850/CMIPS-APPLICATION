import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'send Telehealth Questionnaire', route: '/case/send-telehealth-questionnaire' },
    { label: 'home', route: '/case/ihss-case-home' },
    { label: 'view Telehealth Psi Indicator Results', route: '/case/view-telehealth-psi-indicator-results' },
    { label: 'view Telehealth Questionnaire Resolver', route: '/case/view-telehealth-questionnaire-resolver' }
  ];

export function OrganizationReassessmentDashboardPage() {
  const organizationApi = getDomainApi('organization');
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_reassessmentDashboard"}
      title={"Reassessment Workspace"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Recipient First Name"} value={record && record['recipientFirstName']} />
          <UimField label={"Recipient Last Name"} value={record && record['recipientLastName']} />
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Overdue Assessments"} value={record && record['overdueAssessments']} />
          <UimField label={"Telehealth Questionnaire Results"} value={record && record['telehealthQuestionnaireResults']} />
          <UimField label={"Telehealth Status"} value={record && record['telehealthStatus']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
          <UimField label={"Potentially Stable"} value={record && record['potentiallyStable']} />
          <UimField label={"Companion Case"} value={record && record['companionCase']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Recipient"} value={record && record['recipient']} />
        </div>
      </UimSection>
      <UimSection title={"Case Details"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Address"} value={record && record['recipientAddress']} />
          <UimField label={"Phone"} value={record && record['phone']} />
          <UimField label={"Spoken Language"} value={record && record['spokenLanguage']} />
          <UimField label={"Comp Case"} value={record && record['compCase']} />
          <UimField label={"Case Status"} value={record && record['caseStatus']} />
          <UimField label={"Fund Source"} value={record && record['fundSource']} />
          <UimField label={"PS"} value={record && record['pS']} />
          <UimField label={"PM"} value={record && record['pM']} />
          <UimField label={"Reassessment Due Date"} value={record && record['reassessmentDueDate']} />
          <UimField label={"Potentially Stable"} value={record && record['potentiallyStable']} />
          <UimField label={"Telehealth Questionnaire Results"} value={record && record['telehealthQuestionnaireResults']} />
          <UimField label={"Telehealth Status"} value={record && record['telehealthStatus']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
        </div>
      </UimSection>
      <UimSection title={"Reassessment Due Date"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient"} value={record && record['recipient']} />
          <UimField label={"Recipient Address"} value={record && record['recipientAddress']} />
          <UimField label={"Phone"} value={record && record['phone']} />
          <UimField label={"Spoken Language"} value={record && record['spokenLanguage']} />
          <UimField label={"Comp Case"} value={record && record['compCase']} />
          <UimField label={"Case Status"} value={record && record['caseStatus']} />
          <UimField label={"Fund Source"} value={record && record['fundSource']} />
          <UimField label={"PS"} value={record && record['pS']} />
          <UimField label={"PM"} value={record && record['pM']} />
          <UimField label={"Reassessment Due Date"} value={record && record['reassessmentDueDate']} />
          <UimField label={"Potentially Stable"} value={record && record['potentiallyStable']} />
          <UimField label={"Telehealth Questionnaire Results"} value={record && record['telehealthQuestionnaireResults']} />
          <UimField label={"Telehealth Status"} value={record && record['telehealthStatus']} />
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
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Send Questionnaire')}>Send Questionnaire</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationReassessmentDashboardPage;
