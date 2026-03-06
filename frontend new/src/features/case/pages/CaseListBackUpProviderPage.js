import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'edit Back Up Provider', route: '/case/edit-back-up-provider' }
  ];

export function CaseListBackUpProviderPage() {
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listBackUpProvider"}
      title={"Back-up Provider Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Fiscal Year"} value={record && record['fiscalYear']} />
          <UimField label={"BUP Hours Exception"} value={record && record['bUPHoursException']} />
          <UimField label={"Back-up Hours Paid"} value={record && record['backUpHoursPaid']} />
          <UimField label={"Remaining Hours"} value={record && record['remainingHours']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListBackUpProviderPage;
