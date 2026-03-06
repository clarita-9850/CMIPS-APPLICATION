import React from 'react';
import { useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'create Email Address', route: '/person/create-email-address' },
    { label: 'list Email Address', route: '/person/list-email-address' }
  ];

export function ParticipantEmailAddressSecurityVerificationResendCodePage() {
  const { id } = useParams();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Participant_emailAddressSecurityVerificationResendCode"}
      title={"Enter Verification Code:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Verification Code"} value={record && record['verificationCode']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.update(id, { action: 'validate' }).then(() => alert('Validated successfully')).catch(err => alert('Validation failed: ' + err.message)); }}>Verify</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Resend Code')}>Resend Code</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Could Not Verify')}>Could Not Verify</button>
      </div>
    </UimPageLayout>
  );
}

export default ParticipantEmailAddressSecurityVerificationResendCodePage;
