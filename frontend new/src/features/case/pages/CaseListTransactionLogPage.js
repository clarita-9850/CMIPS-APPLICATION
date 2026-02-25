import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'resolve View Transaction Log Record', route: '/case/resolve-view-transaction-log-record' },
    { label: 'user Home', route: '/organization/user-home' }
  ];

export function CaseListTransactionLogPage() {
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listTransactionLog"}
      title={"View All Changes"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Displays the event type of the case"} value={record && record['displaysTheEventTypeOfTheCase']} />
          <UimField label={"Description"} value={record && record['description']} />
          <UimField label={"Date Time"} value={record && record['dateTime']} />
          <UimField label={"Created By"} value={record && record['createdBy']} />
        </div>
      </UimSection>
      <UimSection title={"All Changes"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
    </UimPageLayout>
  );
}

export default CaseListTransactionLogPage;
