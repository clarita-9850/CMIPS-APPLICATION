import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Sick Leave Hours Details', route: '/person/view-sick-leave-hours-details' }
  ];

export function PersonListSickLeaveHoursPage() {
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listSickLeaveHours"}
      title={"Sick Leave Hours:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Action"} value={record && record['action']} />
          <UimField label={"Fiscal Year"} value={record && record['fiscalYear']} />
          <UimField label={"Accrued Date"} value={record && record['accruedDate']} />
          <UimField label={"Eligible Date"} value={record && record['eligibleDate']} />
          <UimField label={"Accrued Hours"} value={record && record['accruedHours']} />
          <UimField label={"Paid Hours"} value={record && record['paidHours']} />
          <UimField label={"Remaining Hours"} value={record && record['remainingHours']} />
        </div>
      </UimSection>
    </UimPageLayout>
  );
}

export default PersonListSickLeaveHoursPage;
