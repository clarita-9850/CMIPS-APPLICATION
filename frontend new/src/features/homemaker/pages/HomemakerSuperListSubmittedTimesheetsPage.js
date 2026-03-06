import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'edit Homemaker Timesheet', route: '/homemaker/edit-homemaker-timesheet' },
    { label: 'approve Homemaker Timesheet', route: '/homemaker/approve-homemaker-timesheet' },
    { label: 'reject Homemaker Timesheet', route: '/homemaker/reject-homemaker-timesheet' },
    { label: 'home', route: '/case/ihss-case-home' }
  ];

export function HomemakerSuperListSubmittedTimesheetsPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const homemakerApi = getDomainApi('homemaker');
  const { data, loading, error } = useDomainData('homemaker', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"HomemakerSuper_listSubmittedTimesheets"}
      title={"Homemaker/PA Contract Supervisor Ready For Review"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"Homemaker/PA Contract"} value={record && record['homemakerPAContract']} />
        </div>
      </UimSection>
      <UimSection title={"Homemaker/PA Contract Timesheet Details"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { homemakerApi.update(id, { status: 'approved' }).then(() => { alert('Approved successfully'); navigate(-1); }).catch(err => alert('Approve failed: ' + err.message)); }}>Approve</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { homemakerApi.update(id, { status: 'denied' }).then(() => { alert('Denied successfully'); navigate(-1); }).catch(err => alert('Action failed: ' + err.message)); }}>Reject</button>
      </div>
    </UimPageLayout>
  );
}

export default HomemakerSuperListSubmittedTimesheetsPage;
