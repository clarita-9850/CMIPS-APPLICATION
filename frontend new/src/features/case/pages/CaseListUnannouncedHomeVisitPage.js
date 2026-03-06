import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'create Unannounced Home Visit', route: '/case/create-unannounced-home-visit' },
    { label: 'in Activate Unannounced Home Visit Pop Up', route: '/case/in-activate-unannounced-home-visit-pop-up' },
    { label: 'view Unsuccessful U H V', route: '/case/view-unsuccessful-uhv' }
  ];

export function CaseListUnannouncedHomeVisitPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listUnannouncedHomeVisit"}
      title={"Unannounced Home Visit:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Home Visit Date Time"} value={record && record['homeVisitDateTime']} />
          <UimField label={"Reason"} value={record && record['reason']} />
          <UimField label={"Outcome"} value={record && record['outcome']} />
          <UimField label={"Visit Focus"} value={record && record['visitFocus']} />
          <UimField label={"Provider"} value={record && record['provider']} />
          <UimField label={"UHV Status"} value={record && record['uHVStatus']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Entered By"} value={record && record['enteredBy']} />
          <UimField label={"Last Updated"} value={record && record['lastUpdated']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { casesApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-unsuccessful-uhv')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListUnannouncedHomeVisitPage;
