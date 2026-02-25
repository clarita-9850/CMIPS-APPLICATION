import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'submit For Approval Homemaker Timesheet', route: '/homemaker/submit-for-approval-homemaker-timesheet' },
    { label: 'list Homemaker Timesheet', route: '/homemaker/list-homemaker-timesheet' },
    { label: 'inactivate Homemaker Timesheet', route: '/homemaker/inactivate-homemaker-timesheet' },
    { label: 'edit Homemaker Timesheet', route: '/homemaker/edit-homemaker-timesheet' },
    { label: 'view Homemaker Timesheet Reject Comment', route: '/homemaker/view-homemaker-timesheet-reject-comment' }
  ];

export function HomemakerViewHomemakerTimesheetPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const homemakerApi = getDomainApi('homemaker');
  const { data, loading, error } = useDomainData('homemaker', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Homemaker_viewHomemakerTimesheet"}
      title={"View Homemaker/PA Contract Timesheets:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Hours(HH:MM)"} value={record && record['hoursHHMM']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
        </div>
      </UimSection>
      <UimSection title={"Homemaker/PA Contract Timesheet Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/homemaker/submit-for-approval-homemaker-timesheet')}>Submit for Approval</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { homemakerApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { homemakerApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/homemaker/view-homemaker-timesheet-reject-comment')}>Comment</button>
      </div>
    </UimPageLayout>
  );
}

export default HomemakerViewHomemakerTimesheetPage;
