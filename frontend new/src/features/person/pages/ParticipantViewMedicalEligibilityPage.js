import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'search S C I', route: '/person/search-sci' },
    { label: 'create Alternate I D', route: '/person/create-alternate-id' }
  ];

export function ParticipantViewMedicalEligibilityPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Participant_viewMedicalEligibility"}
      title={"Medi-Cal Eligibility Information - Home:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Medi-Cal Eligibility Information"}>
        <div className="uim-form-grid">
          <UimField label={"Eligibility Month"} value={record && record['eligibilityMonth']} />
          <UimField label={"Medi-Cal Eligibility Status"} value={record && record['mediCalEligibilityStatus']} />
          <UimField label={"Medi-Cal Aid Code"} value={record && record['mediCalAidCode']} />
          <UimField label={"Medi-Cal Share Of Cost"} value={record && record['mediCalShareOfCost']} />
          <UimField label={"Medi-Cal County ID"} value={record && record['mediCalCountyID']} />
          <UimField label={"County FBU"} value={record && record['countyFBU']} />
          <UimField label={"MEDS ID"} value={record && record['mEDSID']} />
          <UimField label={"SSN Verification Code"} value={record && record['sSNVerificationCode']} />
          <UimField label={"SSI Living Arrangement"} value={record && record['sSILivingArrangement']} />
          <UimField label={"Optional Living Arrangement"} value={record && record['optionalLivingArrangement']} />
          <UimField label={"Date of Death"} value={record && record['dateOfDeath']} />
          <UimField label={"Death Source"} value={record && record['deathSource']} />
          <UimField label={"CIN"} value={record && record['cIN']} />
          <UimField label={"Disability Onset Date"} value={record && record['disabilityOnsetDate']} />
          <UimField label={"Application Date"} value={record && record['applicationDate']} />
          <UimField label={"Last Updated"} value={record && record['lastUpdated']} />
          <UimField label={"Record Type"} value={record && record['recordType']} />
          <UimField label={"FFP"} value={record && record['fFP']} />
          <UimField label={"BIC Issue Date"} value={record && record['bICIssueDate']} />
          <UimField label={"Medi-Cal County Serial"} value={record && record['mediCalCountySerial']} />
          <UimField label={"County Person Number"} value={record && record['countyPersonNumber']} />
          <UimField label={"Medi-Cal Date of Birth"} value={record && record['mediCalDateOfBirth']} />
          <UimField label={"Medi-Cal Denial Reason"} value={record && record['mediCalDenialReason']} />
          <UimField label={"Medi-Cal Denial Date"} value={record && record['mediCalDenialDate']} />
          <UimField label={"Refugee Alien Status"} value={record && record['refugeeAlienStatus']} />
          <UimField label={"INS Date Of Entry"} value={record && record['iNSDateOfEntry']} />
          <UimField label={"Identity Document Type"} value={record && record['identityDocumentType']} />
          <UimField label={"Citizenship Document Type"} value={record && record['citizenshipDocumentType']} />
          <UimField label={"RV Due Month"} value={record && record['rVDueMonth']} />
          <UimField label={"Last RV Completed Date"} value={record && record['lastRVCompletedDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Return To CIN Select')}>Return To CIN Select</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Select')}>Select</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Spacer')}>Spacer</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Select')}>Select</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Spacer')}>Spacer</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default ParticipantViewMedicalEligibilityPage;
