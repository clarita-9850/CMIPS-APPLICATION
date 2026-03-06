import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'create Case Investigation', route: '/case/create-case-investigation' },
    { label: 'edit Case Investigation', route: '/case/edit-case-investigation' }
  ];

export function CaseListCaseInvestigationPage() {
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listCaseInvestigation"}
      title={"Case Investigation Referral List:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Case Referred To"} value={record && record['caseReferredTo']} />
          <UimField label={"Date Case Referred"} value={record && record['dateCaseReferred']} />
          <UimField label={"Provider"} value={record && record['provider']} />
          <UimField label={"Case Investigation Outcome"} value={record && record['caseInvestigationOutcome']} />
          <UimField label={"Case Investigation Outcome Date"} value={record && record['caseInvestigationOutcomeDate']} />
        </div>
      </UimSection>
      <UimSection title={"Case Investigation Referrals"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListCaseInvestigationPage;
