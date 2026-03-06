import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'register To Referral', route: '/person/register-to-referral' },
    { label: 'confirmation', route: '/misc/reopen-referral-confirmation' },
    { label: 'register From Referral', route: '/person/register-from-referral' },
    { label: 'confirmation', route: '/misc/closed-referral-confirmation' },
    { label: 'register From Provider', route: '/person/register-from-provider' },
    { label: 'home', route: '/case/ihss-case-home' },
    { label: 'verification', route: '/misc/taxpayer-verification' },
    { label: 'verification', route: '/misc/ssn-verification' },
    { label: 'modify Person', route: '/person/modify-person' }
  ];

export function PersonHomePagePage() {
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_homePage"}
      title={"Person Home:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Title"} value={record && record['title']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Effective Date"} value={record && record['effectiveDate']} />
          <UimField label={"Middle Name"} value={record && record['middleName']} />
          <UimField label={"Suffix"} value={record && record['suffix']} />
          <UimField label={"Title"} value={record && record['title']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Effective Date"} value={record && record['effectiveDate']} />
          <UimField label={"Middle Name"} value={record && record['middleName']} />
          <UimField label={"Suffix"} value={record && record['suffix']} />
          <UimField label={"Title"} value={record && record['title']} />
        </div>
      </UimSection>
      <UimSection title={"Name"}>
        <div className="uim-form-grid">
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Effective Date"} value={record && record['effectiveDate']} />
          <UimField label={"Middle Name"} value={record && record['middleName']} />
          <UimField label={"Suffix"} value={record && record['suffix']} />
          <UimField label={"Residence Address"} value={record && record['residenceAddress']} />
          <UimField label={"IHSS Website User"} value={record && record['iHSSWebsiteUser']} />
          <UimField label={"Email Address"} value={record && record['emailAddress']} />
          <UimField label={"Primary Phone Number"} value={record && record['primaryPhoneNumber']} />
          <UimField label={"Primary Phone Number"} value={record && record['primaryPhoneNumber']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Taxpayer ID"} value={record && record['taxpayerID']} />
          <UimField label={"Person Type"} value={record && record['personType']} />
        </div>
      </UimSection>
      <UimSection title={"Name"}>
        <div className="uim-form-grid">
          <UimField label={"Meets Residency Requirements"} value={record && record['meetsResidencyRequirements']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Spoken Language"} value={record && record['spokenLanguage']} />
          <UimField label={"Other Spoken Language Detail"} value={record && record['otherSpokenLanguageDetail']} />
          <UimField label={"Reported Date of Death"} value={record && record['reportedDateOfDeath']} />
          <UimField label={"Death Notification Source"} value={record && record['deathNotificationSource']} />
          <UimField label={"Blank SSN Reason"} value={record && record['blankSSNReason']} />
          <UimField label={"SSN"} value={record && record['sSN']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Referral Source"} value={record && record['referralSource']} />
          <UimField label={"Medi-Cal Pseudo"} value={record && record['mediCalPseudo']} />
          <UimField label={"Ethnicity"} value={record && record['ethnicity']} />
        </div>
      </UimSection>
      <UimSection title={"Name"}>
        <div className="uim-form-grid">
          <UimField label={"Written Language"} value={record && record['writtenLanguage']} />
          <UimField label={"Other Written Language Detail"} value={record && record['otherWrittenLanguageDetail']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Date of Death"} value={record && record['dateOfDeath']} />
          <UimField label={"Death Outcome"} value={record && record['deathOutcome']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Taxpayer ID"} value={record && record['taxpayerID']} />
          <UimField label={"Person Type"} value={record && record['personType']} />
          <UimField label={"Meets Residency Requirements"} value={record && record['meetsResidencyRequirements']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Gender Identity"} value={record && record['genderIdentity']} />
          <UimField label={"Spoken Language"} value={record && record['spokenLanguage']} />
        </div>
      </UimSection>
      <UimSection title={"Contact Information"}>
        <div className="uim-form-grid">
          <UimField label={"Other Spoken Language Detail"} value={record && record['otherSpokenLanguageDetail']} />
          <UimField label={"Reported Date of Death"} value={record && record['reportedDateOfDeath']} />
          <UimField label={"Death Notification Source"} value={record && record['deathNotificationSource']} />
          <UimField label={"Blank SSN Reason"} value={record && record['blankSSNReason']} />
          <UimField label={"SSN"} value={record && record['sSN']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Referral Source"} value={record && record['referralSource']} />
          <UimField label={"Medi-Cal Pseudo"} value={record && record['mediCalPseudo']} />
          <UimField label={"Ethnicity"} value={record && record['ethnicity']} />
          <UimField label={"Sexual Orientation"} value={record && record['sexualOrientation']} />
          <UimField label={"Written Language"} value={record && record['writtenLanguage']} />
          <UimField label={"Other Written Language Detail"} value={record && record['otherWrittenLanguageDetail']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Date of Death"} value={record && record['dateOfDeath']} />
          <UimField label={"Death Outcome"} value={record && record['deathOutcome']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Person Type"} value={record && record['personType']} />
          <UimField label={"Meets Residency Requirements"} value={record && record['meetsResidencyRequirements']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Spoken Language"} value={record && record['spokenLanguage']} />
          <UimField label={"Other Spoken Language Detail"} value={record && record['otherSpokenLanguageDetail']} />
          <UimField label={"Reported Date of Death"} value={record && record['reportedDateOfDeath']} />
          <UimField label={"Death Notification Source"} value={record && record['deathNotificationSource']} />
          <UimField label={"Fingerprint Exemption"} value={record && record['fingerprintExemption']} />
          <UimField label={"Blank SSN Reason"} value={record && record['blankSSNReason']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Referral Source"} value={record && record['referralSource']} />
          <UimField label={"Medi-Cal Pseudo"} value={record && record['mediCalPseudo']} />
          <UimField label={"Ethnicity"} value={record && record['ethnicity']} />
          <UimField label={"Written Language"} value={record && record['writtenLanguage']} />
          <UimField label={"Other Written Language Detail"} value={record && record['otherWrittenLanguageDetail']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Date of Death"} value={record && record['dateOfDeath']} />
          <UimField label={"Death Outcome"} value={record && record['deathOutcome']} />
          <UimField label={"Date Fingerprinted"} value={record && record['dateFingerprinted']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Person Type"} value={record && record['personType']} />
          <UimField label={"Meets Residency Requirements"} value={record && record['meetsResidencyRequirements']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Gender Identity"} value={record && record['genderIdentity']} />
          <UimField label={"Spoken Language"} value={record && record['spokenLanguage']} />
          <UimField label={"Other Spoken Language Detail"} value={record && record['otherSpokenLanguageDetail']} />
          <UimField label={"Reported Date of Death"} value={record && record['reportedDateOfDeath']} />
          <UimField label={"Death Notification Source"} value={record && record['deathNotificationSource']} />
          <UimField label={"Fingerprint Exemption"} value={record && record['fingerprintExemption']} />
          <UimField label={"Blank SSN Reason"} value={record && record['blankSSNReason']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Referral Source"} value={record && record['referralSource']} />
          <UimField label={"Medi-Cal Pseudo"} value={record && record['mediCalPseudo']} />
          <UimField label={"Ethnicity"} value={record && record['ethnicity']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Sexual Orientation"} value={record && record['sexualOrientation']} />
          <UimField label={"Written Language"} value={record && record['writtenLanguage']} />
          <UimField label={"Other Written Language Detail"} value={record && record['otherWrittenLanguageDetail']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Date of Death"} value={record && record['dateOfDeath']} />
          <UimField label={"Death Outcome"} value={record && record['deathOutcome']} />
          <UimField label={"Date Fingerprinted"} value={record && record['dateFingerprinted']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Referral')}>Create Referral</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Re-Open Referral')}>Re-Open Referral</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Application')}>Create Application</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Close Referral')}>Close Referral</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Provider')}>Create Provider</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonHomePagePage;
