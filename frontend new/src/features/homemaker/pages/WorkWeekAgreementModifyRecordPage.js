import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Records', route: '/homemaker/list-records' }
  ];

export function WorkWeekAgreementModifyRecordPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const homemakerApi = getDomainApi('homemaker');
  const { data, loading, error } = useDomainData('homemaker', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"WorkWeekAgreement_modifyRecord"}
      title={"Modify Provider Workweek Agreement:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
        </div>
      </UimSection>
      <UimSection title={"Provider Workweek Agreement"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Weekly Authorized Hours"} value={record && record['recipientWeeklyAuthorizedHours']} />
          <UimField label={"Program Type"} value={record && record['programType']} />
          <UimField label={"Total Hours (HH:MM)"} value={record && record['totalHoursHHMM']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { homemakerApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default WorkWeekAgreementModifyRecordPage;
