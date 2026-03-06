import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'reset Registration Code', route: '/person/reset-registration-code' },
    { label: 'register For T T S', route: '/person/register-for-tts' },
    { label: 'reset Pass Code', route: '/person/reset-pass-code' }
  ];

export function PersonTimesheetPreferencesPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_timesheetPreferences"}
      title={"View Provider Timesheet Preferences:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"IHSS Electronic Services Portal"}>
        <div className="uim-form-grid">
          <UimField label={"Registration Date"} value={record && record['registrationDate']} />
          <UimField label={"Website User Name"} value={record && record['websiteUserName']} />
          <UimField label={"TTS Registration Code"} value={record && record['tTSRegistrationCode']} />
        </div>
      </UimSection>
      <UimSection title={"Telephone Timesheet System"}>
        <div className="uim-form-grid">
          <UimField label={"TTS Registration Code"} value={record && record['tTSRegistrationCode']} />
          <UimField label={"TTS Registration Code"} value={record && record['tTSRegistrationCode']} />
          <UimField label={"Registration Date"} value={record && record['registrationDate']} />
        </div>
      </UimSection>
      <UimSection title={"Telephone Timesheet System"}>
        <div className="uim-form-grid">
          <UimField label={"Passcode Locked"} value={record && record['passcodeLocked']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/reset-registration-code')}>Reset Registration Code</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Register Provider for TTS')}>Register Provider for TTS</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Reset Passcode')}>Reset Passcode</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonTimesheetPreferencesPage;
