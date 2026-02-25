import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'send Verfication Phone', route: '/organization/send-verfication-phone' },
    { label: 'send Verfication Email', route: '/organization/send-verfication-email' },
    { label: 'edit Auto Approve', route: '/organization/edit-auto-approve' }
  ];

export function OrganizationCspVerifyContactInfoPage() {
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_cspVerifyContactInfo"}
      title={"CMIPS Services Portal Access:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Cell Phone Number"}>
        <div className="uim-form-grid">
          <UimField label={"Cell Phone Number"} value={record && record['cellPhoneNumber']} />
          <UimField label={"Cell Phone Number"} value={record && record['cellPhoneNumber']} />
        </div>
      </UimSection>
      <UimSection title={"Email Address"}>
        <div className="uim-form-grid">
          <UimField label={"Email Address"} value={record && record['emailAddress']} />
          <UimField label={"Email Address"} value={record && record['emailAddress']} />
        </div>
      </UimSection>
      <UimSection title={"Default Auto-Acceptance Setting for Submitted Electronic Forms"}>
        <div className="uim-form-grid">
          <UimField label={"Automatically accept submitted electronic forms by default"} value={record && record['automaticallyAcceptSubmittedElectronicFormsByDefault']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationCspVerifyContactInfoPage;
