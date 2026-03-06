import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'home', route: '/case/ihss-case-home' },
    { label: 'home Page', route: '/person/home-page' }
  ];

export function PersonModifyPersonPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_modifyPerson"}
      title={"Modify Person:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Name"}>
        <div className="uim-form-grid">
          <UimField label={"Title"} value={record && record['title']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Name Change Effective Date"} value={record && record['nameChangeEffectiveDate']} />
          <UimField label={"Middle Name"} value={record && record['middleName']} />
          <UimField label={"Suffix"} value={record && record['suffix']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Taxpayer ID"} value={record && record['taxpayerID']} />
          <UimField label={"Person Type"} value={record && record['personType']} />
          <UimField label={"Meets Residency Requirements"} value={record && record['meetsResidencyRequirements']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Spoken Language"} value={record && record['spokenLanguage']} />
          <UimField label={"Other Spoken Language Detail"} value={record && record['otherSpokenLanguageDetail']} />
          <UimField label={"Ethnicity"} value={record && record['ethnicity']} />
          <UimField label={"Reported Date Of Death"} value={record && record['reportedDateOfDeath']} />
          <UimField label={"Death Notification Source"} value={record && record['deathNotificationSource']} />
          <UimField label={"Fingerprint Exemption"} value={record && record['fingerprintExemption']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Blank SSN Reason"} value={record && record['blankSSNReason']} />
          <UimField label={"Medi-Cal Pseudo"} value={record && record['mediCalPseudo']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Referral Source"} value={record && record['referralSource']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Written Language"} value={record && record['writtenLanguage']} />
          <UimField label={"Other Written Language Detail"} value={record && record['otherWrittenLanguageDetail']} />
          <UimField label={"Date of Death"} value={record && record['dateOfDeath']} />
          <UimField label={"Death Outcome"} value={record && record['deathOutcome']} />
          <UimField label={"Date Fingerprinted"} value={record && record['dateFingerprinted']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Taxpayer ID"} value={record && record['taxpayerID']} />
          <UimField label={"Person Type"} value={record && record['personType']} />
          <UimField label={"Meets Residency Requirements"} value={record && record['meetsResidencyRequirements']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Gender Identity"} value={record && record['genderIdentity']} />
          <UimField label={"Spoken Language"} value={record && record['spokenLanguage']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Other Spoken Language Detail"} value={record && record['otherSpokenLanguageDetail']} />
          <UimField label={"Ethnicity"} value={record && record['ethnicity']} />
          <UimField label={"Reported Date Of Death"} value={record && record['reportedDateOfDeath']} />
          <UimField label={"Death Notification Source"} value={record && record['deathNotificationSource']} />
          <UimField label={"Fingerprint Exemption"} value={record && record['fingerprintExemption']} />
          <UimField label={"Blank SSN Reason"} value={record && record['blankSSNReason']} />
          <UimField label={"Medi-Cal Pseudo"} value={record && record['mediCalPseudo']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Referral Source"} value={record && record['referralSource']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Sexual Orientation"} value={record && record['sexualOrientation']} />
          <UimField label={"Written Language"} value={record && record['writtenLanguage']} />
          <UimField label={"Other Written Language Detail"} value={record && record['otherWrittenLanguageDetail']} />
          <UimField label={"Date of Death"} value={record && record['dateOfDeath']} />
          <UimField label={"Death Outcome"} value={record && record['deathOutcome']} />
          <UimField label={"Date Fingerprinted"} value={record && record['dateFingerprinted']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonModifyPersonPage;
