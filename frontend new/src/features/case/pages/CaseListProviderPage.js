import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'assign Provider', route: '/case/assign-provider' },
    { label: 'view Provider', route: '/case/view-provider' },
    { label: 'modify Provider For List', route: '/case/modify-provider-for-list' },
    { label: 'inactivate Provider', route: '/case/inactivate-provider' },
    { label: 'home Page', route: '/person/home-page' },
    { label: 'list Records', route: '/homemaker/list-records' }
  ];

export function CaseListProviderPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listProvider"}
      title={"Case Providers:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"IHSS/WPCS"} value={record && record['iHSSWPCS']} />
          <UimField label={"Provider Status"} value={record && record['providerStatus']} />
          <UimField label={"Assigned Hours"} value={record && record['assignedHours']} />
          <UimField label={"Relationship to Recipient"} value={record && record['relationshipToRecipient']} />
          <UimField label={"Timesheet Review"} value={record && record['timesheetReview']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"Provider Workweek Agreement"} value={record && record['providerWorkweekAgreement']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Assign a Provider')}>Assign a Provider</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-provider')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { casesApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListProviderPage;
