import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [];

export function PersonListEnrollmentHistoryPage() {
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listEnrollmentHistory"}
      title={"Enrollment History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Effective Date"} value={record && record['effectiveDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Eligible"} value={record && record['eligible']} />
          <UimField label={"Ineligible Reason"} value={record && record['ineligibleReason']} />
          <UimField label={"DOJ County"} value={record && record['dOJCounty']} />
          <UimField label={"Appeal Status"} value={record && record['appealStatus']} />
          <UimField label={"Appeal Status Date"} value={record && record['appealStatusDate']} />
          <UimField label={"Admin Hearing Date"} value={record && record['adminHearingDate']} />
          <UimField label={"Created By"} value={record && record['createdBy']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
          <UimField label={"Effective Date"} value={record && record['effectiveDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Eligible"} value={record && record['eligible']} />
          <UimField label={"Ineligible Reason"} value={record && record['ineligibleReason']} />
          <UimField label={"DOJ County"} value={record && record['dOJCounty']} />
          <UimField label={"Appeal Status"} value={record && record['appealStatus']} />
          <UimField label={"Appeal Status Date"} value={record && record['appealStatusDate']} />
          <UimField label={"Admin Hearing Date"} value={record && record['adminHearingDate']} />
          <UimField label={"Created By"} value={record && record['createdBy']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonListEnrollmentHistoryPage;
