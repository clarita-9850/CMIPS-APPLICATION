import React from 'react';
import { useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'resolve Timesheet Preferences', route: '/case/resolve-timesheet-preferences' }
  ];

export function CaseVerifyPhoneNumberPreferencePage() {
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_verifyPhoneNumberPreference"}
      title={"Verify Cell Phone Number:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Enter Verification Code"}>
        <div className="uim-form-grid">
          <UimField label={"Verification Code"} value={record && record['verificationCode']} />
        </div>
      </UimSection>
      <UimSection title={"Cell Phone Number Verified"}>
        <div className="uim-form-grid">
          <UimField label={"Cell Phone Number"} value={record && record['cellPhoneNumber']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Resend Code')}>Resend Code</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, { action: 'validate' }).then(() => alert('Validated successfully')).catch(err => alert('Validation failed: ' + err.message)); }}>Verify</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Done')}>Done</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseVerifyPhoneNumberPreferencePage;
