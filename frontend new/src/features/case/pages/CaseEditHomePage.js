import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function CaseEditHomePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_editHome"}
      title={"Modify Case:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"IHSS Referral Date"} value={record && record['iHSSReferralDate']} />
          <UimField label={"IHSS Application Date"} value={record && record['iHSSApplicationDate']} />
          <UimField label={"Medi-Cal Eligibility Referral Date"} value={record && record['mediCalEligibilityReferralDate']} />
          <UimField label={"Medi-Cal Initial Eligibility Notification Date"} value={record && record['mediCalInitialEligibilityNotificationDate']} />
          <UimField label={"Assessment Date"} value={record && record['assessmentDate']} />
          <UimField label={"Interpreter Available"} value={record && record['interpreterAvailable']} />
        </div>
      </UimSection>
      <UimSection title={"County Use - Special Project Coding"}>
        <div className="uim-form-grid">
          <UimField label={"Number Of Household Members"} value={record && record['numberOfHouseholdMembers']} />
          <UimField label={"Active Providers"} value={record && record['activeProviders']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Resource Suspension End Date"} value={record && record['resourceSuspensionEndDate']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"District Office"} value={record && record['districtOffice']} />
          <UimField label={"Case Owner"} value={record && record['caseOwner']} />
        </div>
      </UimSection>
      <UimSection title={"County Use Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Companion Case"} value={record && record['companionCase']} />
          <UimField label={"State Hearing"} value={record && record['stateHearing']} />
          <UimField label={"Mail Designee"} value={record && record['mailDesignee']} />
          <UimField label={"County Use 1"} value={record && record['countyUse1']} />
          <UimField label={"County Use 2"} value={record && record['countyUse2']} />
          <UimField label={"County Use 3"} value={record && record['countyUse3']} />
          <UimField label={"County Use 4"} value={record && record['countyUse4']} />
          <UimField label={"County Use Comments"} value={record && record['countyUseComments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseEditHomePage;
