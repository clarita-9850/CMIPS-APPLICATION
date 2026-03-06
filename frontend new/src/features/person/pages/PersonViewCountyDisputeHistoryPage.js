import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view County Dispute History Details', route: '/person/view-county-dispute-history-details' },
    { label: 'view Overtime Violations', route: '/person/view-overtime-violations' }
  ];

export function PersonViewCountyDisputeHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewCountyDisputeHistory"}
      title={"County Dispute History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"County Dispute Filed Date"} value={record && record['countyDisputeFiledDate']} />
          <UimField label={"Dispute Entered Date"} value={record && record['disputeEnteredDate']} />
          <UimField label={"County Dispute Outcome"} value={record && record['countyDisputeOutcome']} />
          <UimField label={"County Dispute User Name"} value={record && record['countyDisputeUserName']} />
          <UimField label={"Supervisor Dispute Outcome"} value={record && record['supervisorDisputeOutcome']} />
          <UimField label={"Supervisor Outcome Date"} value={record && record['supervisorOutcomeDate']} />
          <UimField label={"Dispute Supervisor Name"} value={record && record['disputeSupervisorName']} />
          <UimField label={"Supervisor Outcome Entered Date"} value={record && record['supervisorOutcomeEnteredDate']} />
          <UimField label={"Last Updated Date"} value={record && record['lastUpdatedDate']} />
          <UimField label={"Last Updated By"} value={record && record['lastUpdatedBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-county-dispute-history-details')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewCountyDisputeHistoryPage;
