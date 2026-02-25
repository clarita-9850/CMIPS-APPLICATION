import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'inactivate Record', route: '/homemaker/inactivate-record' },
    { label: 'History', route: '/homemaker/history' },
    { label: 'modify Record', route: '/homemaker/modify-record' },
    { label: 'list Work Week Agreement', route: '/misc/recip-list-work-week-agreement' }
  ];

export function RecipWorkWeekAgreementViewRecordPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const homemakerApi = getDomainApi('homemaker');
  const { data, loading, error } = useDomainData('homemaker', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"RecipWorkWeekAgreement_viewRecord"}
      title={"View Recipient Workweek Agreement:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Back-Up Provider"} value={record && record['backUpProvider']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Type"} value={record && record['providerType']} />
          <UimField label={"Recipient Weekly Authorized Hours"} value={record && record['recipientWeeklyAuthorizedHours']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { homemakerApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/homemaker/history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default RecipWorkWeekAgreementViewRecordPage;
