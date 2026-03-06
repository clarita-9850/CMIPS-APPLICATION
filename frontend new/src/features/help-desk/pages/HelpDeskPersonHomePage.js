import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'case create Forms Correspondence', route: '/help-desk/case-create-forms-correspondence' },
    { label: 'view Payment Details', route: '/payment/view-payment-details' },
    { label: 'case view Forms And Correspondence', route: '/help-desk/case-view-forms-and-correspondence' },
    { label: 'case modify Forms And Correspondence', route: '/help-desk/case-modify-forms-and-correspondence' }
  ];

export function HelpDeskPersonHomePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('help-desk', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"HelpDesk_personHome"}
      title={"HelpDesk Person Home:\\\\"}
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
          <UimField label={"Effective Date"} value={record && record['effectiveDate']} />
          <UimField label={"Middle Name"} value={record && record['middleName']} />
          <UimField label={"Suffix"} value={record && record['suffix']} />
          <UimField label={"Title"} value={record && record['title']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Effective Date"} value={record && record['effectiveDate']} />
          <UimField label={"Middle Name"} value={record && record['middleName']} />
          <UimField label={"Suffix"} value={record && record['suffix']} />
          <UimField label={"Mailing Address"} value={record && record['mailingAddress']} />
        </div>
      </UimSection>
      <UimSection title={"Name"}>
        <div className="uim-form-grid">
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Person Type"} value={record && record['personType']} />
          <UimField label={"Meets Residency Requirements"} value={record && record['meetsResidencyRequirements']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Spoken Language"} value={record && record['spokenLanguage']} />
          <UimField label={"Reported Date of Death"} value={record && record['reportedDateOfDeath']} />
          <UimField label={"Date of Death Confirmed"} value={record && record['dateOfDeathConfirmed']} />
          <UimField label={"Death Notification Source"} value={record && record['deathNotificationSource']} />
          <UimField label={"Blank SSN Reason"} value={record && record['blankSSNReason']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Referral Source"} value={record && record['referralSource']} />
          <UimField label={"Fingerprint Exemption"} value={record && record['fingerprintExemption']} />
        </div>
      </UimSection>
      <UimSection title={"Contact Information"}>
        <div className="uim-form-grid">
          <UimField label={"Ethnicity"} value={record && record['ethnicity']} />
          <UimField label={"Written Language"} value={record && record['writtenLanguage']} />
          <UimField label={"Date of Death"} value={record && record['dateOfDeath']} />
          <UimField label={"Death Outcome"} value={record && record['deathOutcome']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Person Type"} value={record && record['personType']} />
          <UimField label={"Meets Residency Requirements"} value={record && record['meetsResidencyRequirements']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Gender Identity"} value={record && record['genderIdentity']} />
          <UimField label={"Spoken Language"} value={record && record['spokenLanguage']} />
          <UimField label={"Ethnicity"} value={record && record['ethnicity']} />
          <UimField label={"Reported Date of Death"} value={record && record['reportedDateOfDeath']} />
          <UimField label={"Date of Death Confirmed"} value={record && record['dateOfDeathConfirmed']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Death Notification Source"} value={record && record['deathNotificationSource']} />
          <UimField label={"Blank SSN Reason"} value={record && record['blankSSNReason']} />
          <UimField label={"Referral Source"} value={record && record['referralSource']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Sexual Orientation"} value={record && record['sexualOrientation']} />
          <UimField label={"Written Language"} value={record && record['writtenLanguage']} />
          <UimField label={"Fingerprint Exemption"} value={record && record['fingerprintExemption']} />
          <UimField label={"Date of Death"} value={record && record['dateOfDeath']} />
          <UimField label={"Death Outcome"} value={record && record['deathOutcome']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Issued"} value={record && record['issued']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Warrant Number"} value={record && record['warrantNumber']} />
          <UimField label={"Gross"} value={record && record['gross']} />
          <UimField label={"Net"} value={record && record['net']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"SOC"} value={record && record['sOC']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date"} value={record && record['date']} />
        </div>
      </UimSection>
      <UimSection title={"Payment History for the recent three months"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Correspondence History for the recent three months"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New Correspondence')}>New Correspondence</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-payment-details')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default HelpDeskPersonHomePage;
