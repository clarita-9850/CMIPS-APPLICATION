import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'resolve View Hours', route: '/misc/action-resolve-view-hours' }
  ];

export function PersonViewWeeklyHoursDetailsPopupPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewWeeklyHoursDetailsPopup"}
      title={"View Hours Details:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Weekly and Overtime Maximum"} value={record && record['weeklyAndOvertimeMaximum']} />
          <UimField label={"Weekly Maximum"} value={record && record['weeklyMaximum']} />
          <UimField label={"Monthly Overtime Maximum"} value={record && record['monthlyOvertimeMaximum']} />
          <UimField label={" "} value={record && record['']} />
          <UimField label={" "} value={record && record['']} />
          <UimField label={" "} value={record && record['']} />
          <UimField label={" "} value={record && record['']} />
          <UimField label={"Recipient"} value={record && record['recipient']} />
          <UimField label={"Time Entry"} value={record && record['timeEntry']} />
          <UimField label={"Transaction      "} value={record && record['transaction']} />
          <UimField label={"Program "} value={record && record['program']} />
          <UimField label={"   Warrant"} value={record && record['warrant']} />
          <UimField label={"Name    "} value={record && record['name']} />
          <UimField label={"Source Type"} value={record && record['sourceType']} />
          <UimField label={"Number        "} value={record && record['number']} />
          <UimField label={" "} value={record && record['']} />
          <UimField label={"Status       "} value={record && record['status']} />
          <UimField label={"Time Entries [HH:MM]"} value={record && record['timeEntriesHHMM']} />
          <UimField label={"SU"} value={record && record['sU']} />
          <UimField label={"MO"} value={record && record['mO']} />
          <UimField label={"TU"} value={record && record['tU']} />
          <UimField label={"WE"} value={record && record['wE']} />
          <UimField label={"TH"} value={record && record['tH']} />
          <UimField label={"FR"} value={record && record['fR']} />
          <UimField label={"SA"} value={record && record['sA']} />
          <UimField label={" "} value={record && record['']} />
          <UimField label={"Total"} value={record && record['total']} />
          <UimField label={"OT"} value={record && record['oT']} />
          <UimField label={"OT"} value={record && record['oT']} />
          <UimField label={"SU"} value={record && record['sU']} />
          <UimField label={"MO"} value={record && record['mO']} />
          <UimField label={"TU"} value={record && record['tU']} />
          <UimField label={"WE"} value={record && record['wE']} />
          <UimField label={"TH"} value={record && record['tH']} />
          <UimField label={"FR"} value={record && record['fR']} />
          <UimField label={"SA"} value={record && record['sA']} />
          <UimField label={"Travel"} value={record && record['travel']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"Hours"} value={record && record['hours']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewWeeklyHoursDetailsPopupPage;
