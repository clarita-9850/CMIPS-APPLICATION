import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function EvidenceModifyProgramEvidencePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const eligibilityApi = getDomainApi('evidence');
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_modifyProgramEvidence"}
      title={"Modify Program Evidence:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Program Information"}>
        <div className="uim-form-grid">
          <UimField label={"Authorization Start Date"} value={record && record['authorizationStartDate']} />
          <UimField label={"Assessment Date"} value={record && record['assessmentDate']} />
          <UimField label={"Health Care Certification Date"} value={record && record['healthCareCertificationDate']} />
          <UimField label={"Presumptive Eligibility"} value={record && record['presumptiveEligibility']} />
          <UimField label={"Authorization End Date"} value={record && record['authorizationEndDate']} />
          <UimField label={"Re-Assessment Due Date"} value={record && record['reAssessmentDueDate']} />
        </div>
      </UimSection>
      <UimSection title={"IHSS Program"}>
        <div className="uim-form-grid">
          <UimField label={"Health Care Certification Reason"} value={record && record['healthCareCertificationReason']} />
          <UimField label={"Waiver Program"} value={record && record['waiverProgram']} />
          <UimField label={"IHSS Aid Code"} value={record && record['iHSSAidCode']} />
          <UimField label={"Restaurant Meals Allowance"} value={record && record['restaurantMealsAllowance']} />
          <UimField label={"Recipient Declines CFCO"} value={record && record['recipientDeclinesCFCO']} />
          <UimField label={"Advance Pay"} value={record && record['advancePay']} />
        </div>
      </UimSection>
      <UimSection title={"Modes Of Service"}>
        <div className="uim-form-grid">
          <UimField label={"Advance Pay Rate"} value={record && record['advancePayRate']} />
          <UimField label={"Individual Provider"} value={record && record['individualProvider']} />
          <UimField label={"Homemaker/PA Contract"} value={record && record['homemakerPAContract']} />
          <UimField label={"County Contractor"} value={record && record['countyContractor']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { eligibilityApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceModifyProgramEvidencePage;
