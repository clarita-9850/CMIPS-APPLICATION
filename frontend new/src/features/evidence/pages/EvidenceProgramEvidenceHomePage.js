import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'modify Program Evidence', route: '/evidence/modify-program-evidence' },
    { label: 'modify Manual N O As', route: '/evidence/modify-manual-no-as' },
    { label: 'home', route: '/evidence/home' },
    { label: 'soc Evidence Home', route: '/evidence/soc-evidence-home' }
  ];

export function EvidenceProgramEvidenceHomePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_programEvidenceHome"}
      title={"Program Evidence:"}
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
        </div>
      </UimSection>
      <UimSection title={"IHSS Program"}>
        <div className="uim-form-grid">
          <UimField label={"Re-Assessment Due Date"} value={record && record['reAssessmentDueDate']} />
          <UimField label={"Health Care Certification Reason"} value={record && record['healthCareCertificationReason']} />
          <UimField label={"Waiver Program"} value={record && record['waiverProgram']} />
          <UimField label={"IHSS AID Code"} value={record && record['iHSSAIDCode']} />
          <UimField label={"Restaurant Meals Allowance"} value={record && record['restaurantMealsAllowance']} />
        </div>
      </UimSection>
      <UimSection title={"Modes Of Service"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Declines CFCO"} value={record && record['recipientDeclinesCFCO']} />
          <UimField label={"Advance Pay"} value={record && record['advancePay']} />
          <UimField label={"Advance Pay Rate"} value={record && record['advancePayRate']} />
          <UimField label={"Individual Provider"} value={record && record['individualProvider']} />
          <UimField label={"Homemaker/PA Contract"} value={record && record['homemakerPAContract']} />
        </div>
      </UimSection>
      <UimSection title={"Manual NOAs"}>
        <div className="uim-form-grid">
          <UimField label={"County Contractor"} value={record && record['countyContractor']} />
          <UimField label={"NOA Code"} value={record && record['nOACode']} />
          <UimField label={"NOA Text"} value={record && record['nOAText']} />
          <UimField label={"Freeform Text"} value={record && record['freeformText']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Program Evidence')}>Edit Program Evidence</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Add\\/\\Edit NOAs')}>Add\/\Edit NOAs</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/home')}>Evidence Home</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next')}>Next</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceProgramEvidenceHomePage;
