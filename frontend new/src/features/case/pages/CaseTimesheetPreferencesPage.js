import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'reactivate I H S S E S P Status', route: '/case/reactivate-ihssesp-status' },
    { label: 'inactivate I H S S E S P Status', route: '/case/inactivate-ihssesp-status' },
    { label: 'list I H S S E S P History', route: '/case/list-ihssesp-history' },
    { label: 'reset Registration Code', route: '/case/reset-registration-code' },
    { label: 'register For T T S', route: '/case/register-for-tts' },
    { label: 'reset T T S Passcode', route: '/case/reset-tts-passcode' },
    { label: 'edit T S Accommodations', route: '/case/edit-ts-accommodations' }
  ];

export function CaseTimesheetPreferencesPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_timesheetPreferences"}
      title={"View Recipient Timesheet Preferences:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Electronic Timesheet Review Method"}>
        <div className="uim-form-grid">
          <UimField label={"Registration Date:"} value={record && record['registrationDate']} />
          <UimField label={"Status:"} value={record && record['status']} />
          <UimField label={"Updated By:"} value={record && record['updatedBy']} />
        </div>
      </UimSection>
      <UimSection title={"IHSS Electronic Services Portal"}>
        <div className="uim-form-grid">
          <UimField label={"ESP User Name:"} value={record && record['eSPUserName']} />
          <UimField label={"Status Date:"} value={record && record['statusDate']} />
          <UimField label={"Comments:"} value={record && record['comments']} />
        </div>
      </UimSection>
      <UimSection title={"Telephone Timesheet System"}>
        <div className="uim-form-grid">
          <UimField label={"TTS Registration Code:"} value={record && record['tTSRegistrationCode']} />
          <UimField label={"TTS Registration Code:"} value={record && record['tTSRegistrationCode']} />
          <UimField label={"TTS Registration Code:"} value={record && record['tTSRegistrationCode']} />
        </div>
      </UimSection>
      <UimSection title={"Telephone Timesheet System"}>
        <div className="uim-form-grid">
          <UimField label={"Registration Date:"} value={record && record['registrationDate']} />
          <UimField label={"Passcode Locked:"} value={record && record['passcodeLocked']} />
          <UimField label={"Timesheet Accommodation:"} value={record && record['timesheetAccommodation']} />
        </div>
      </UimSection>
      <UimSection title={"Timesheet Accommodations"}>
        <div className="uim-form-grid">
          <UimField label={"Timesheet Accommodation:"} value={record && record['timesheetAccommodation']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Reactivate IHSS ESP Registration')}>Reactivate IHSS ESP Registration</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Inactivate IHSS ESP Registration')}>Inactivate IHSS ESP Registration</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/list-ihssesp-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/reset-registration-code')}>Reset Registration Code</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Register Recipient for TTS')}>Register Recipient for TTS</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Reset Passcode')}>Reset Passcode</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Timesheet Accommodation')}>Edit Timesheet Accommodation</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseTimesheetPreferencesPage;
