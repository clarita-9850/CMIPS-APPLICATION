import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Overtime Violations', route: '/person/view-overtime-violations' }
  ];

export function PersonModifyDisputeSupervisorPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_modifyDisputeSupervisor"}
      title={"Modify Dispute Supervisor:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"County Dispute"}>
        <div className="uim-form-grid">
          <UimField label={"County Dispute Filed Date"} value={record && record['countyDisputeFiledDate']} />
          <UimField label={"Unanticipated Need"} value={record && record['unanticipatedNeed']} />
          <UimField label={"Immediate Need"} value={record && record['immediateNeed']} />
          <UimField label={"Dispute Entered Date"} value={record && record['disputeEnteredDate']} />
          <UimField label={"Health and Safety Issue"} value={record && record['healthAndSafetyIssue']} />
          <UimField label={"Other"} value={record && record['other']} />
          <UimField label={"County Dispute Outcome"} value={record && record['countyDisputeOutcome']} />
        </div>
      </UimSection>
      <UimSection title={"Dispute Outcome"}>
        <div className="uim-form-grid">
          <UimField label={"Override Reason"} value={record && record['overrideReason']} />
          <UimField label={"Upheld - Need not unanticipated"} value={record && record['upheldNeedNotUnanticipated']} />
          <UimField label={"County Dispute User Name"} value={record && record['countyDisputeUserName']} />
          <UimField label={"County Dispute Outcome Date"} value={record && record['countyDisputeOutcomeDate']} />
          <UimField label={"Upheld - Need not immediate"} value={record && record['upheldNeedNotImmediate']} />
          <UimField label={"Upheld - No Health or Safety Issue"} value={record && record['upheldNoHealthOrSafetyIssue']} />
          <UimField label={"County Dispute Outcome Entered Date"} value={record && record['countyDisputeOutcomeEnteredDate']} />
        </div>
      </UimSection>
      <UimSection title={"Dispute Supervisor"}>
        <div className="uim-form-grid">
          <UimField label={"Supervisor Dispute Outcome"} value={record && record['supervisorDisputeOutcome']} />
          <UimField label={"Dispute Supervisor Name"} value={record && record['disputeSupervisorName']} />
          <UimField label={"Supervisor Outcome Date"} value={record && record['supervisorOutcomeDate']} />
          <UimField label={"Supervisor Outcome Entered Date"} value={record && record['supervisorOutcomeEnteredDate']} />
          <UimField label={"County Dispute Letter Date"} value={record && record['countyDisputeLetterDate']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonModifyDisputeSupervisorPage;
