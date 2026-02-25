import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'modify Unannounced Home Visit All Fields', route: '/case/modify-unannounced-home-visit-all-fields' },
    { label: 'modify Unannounced Home Visit', route: '/case/modify-unannounced-home-visit' },
    { label: 'create U H V Letter Followup', route: '/case/create-uhv-letter-followup' },
    { label: 'modify U H V Letter Followup', route: '/case/modify-uhv-letter-followup' },
    { label: 'inactivate U H V Followup', route: '/case/inactivate-uhv-followup' },
    { label: 'create U H V Phone Followup', route: '/case/create-uhv-phone-followup' },
    { label: 'modify U H V Phone Followup', route: '/case/modify-uhv-phone-followup' },
    { label: 'create Unannounced Home Visit Pop Up', route: '/case/create-unannounced-home-visit-pop-up' },
    { label: 'modify Unannounced Home Visit Pop Up', route: '/case/modify-unannounced-home-visit-pop-up' },
    { label: 'list Unannounced Home Visit', route: '/case/list-unannounced-home-visit' }
  ];

export function CaseViewUnsuccessfulUHVPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewUnsuccessfulUHV"}
      title={"View Unannounced Home Visit Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Unannounced Home Visit Details"}>
        <div className="uim-form-grid">
          <UimField label={"UHV Status"} value={record && record['uHVStatus']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
        </div>
      </UimSection>
      <UimSection title={"Status"}>
        <div className="uim-form-grid">
          <UimField label={"Home Visit Date Time"} value={record && record['homeVisitDateTime']} />
          <UimField label={"Reason"} value={record && record['reason']} />
        </div>
      </UimSection>
      <UimSection title={"Initial UHV Attempt"}>
        <div className="uim-form-grid">
          <UimField label={"Visit Focus"} value={record && record['visitFocus']} />
          <UimField label={"Outcome"} value={record && record['outcome']} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Provider"} value={record && record['provider']} />
          <UimField label={"Date Created"} value={record && record['dateCreated']} />
        </div>
      </UimSection>
      <UimSection title={"Follow-up UHV Attempt"}>
        <div className="uim-form-grid">
          <UimField label={"Letter Mailed Date"} value={record && record['letterMailedDate']} />
          <UimField label={"Date Created"} value={record && record['dateCreated']} />
        </div>
      </UimSection>
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Comments"} value={record && record['comments']} />
          <UimField label={"Last Updated"} value={record && record['lastUpdated']} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Phone Call Date Time"} value={record && record['phoneCallDateTime']} />
          <UimField label={"Date Created"} value={record && record['dateCreated']} />
        </div>
      </UimSection>
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Last Updated"} value={record && record['lastUpdated']} />
          <UimField label={"Home Visit Date Time"} value={record && record['homeVisitDateTime']} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Outcome"} value={record && record['outcome']} />
          <UimField label={"Date Created"} value={record && record['dateCreated']} />
        </div>
      </UimSection>
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Last Updated"} value={record && record['lastUpdated']} />
          <UimField label={"Phone Call Date Time"} value={record && record['phoneCallDateTime']} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Date Created"} value={record && record['dateCreated']} />
          <UimField label={"Last Updated"} value={record && record['lastUpdated']} />
        </div>
      </UimSection>
      <UimSection title={"Final UHV Attempt"}>
        <div className="uim-form-grid">
          <UimField label={"Home Visit Date Time"} value={record && record['homeVisitDateTime']} />
          <UimField label={"Outcome"} value={record && record['outcome']} />
        </div>
      </UimSection>
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Date Created"} value={record && record['dateCreated']} />
          <UimField label={"Last Updated"} value={record && record['lastUpdated']} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
      </UimSection>
      <UimSection title={"Manage"}>
      </UimSection>
      <UimSection title={"Comments"}>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Comments')}>Edit Comments</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Follow-up Letter')}>Create Follow-up Letter</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Modify Follow-up Letter')}>Modify Follow-up Letter</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { casesApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Follow-up Phone Call')}>Create Follow-up Phone Call</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Modify Follow-up Phone Call')}>Modify Follow-up Phone Call</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { casesApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Follow-up Visit')}>Create Follow-up Visit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Modify Follow-up Visit')}>Modify Follow-up Visit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Final Phone Call')}>Create Final Phone Call</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Modify Final Phone Call')}>Modify Final Phone Call</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { casesApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Final Visit')}>Create Final Visit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Modify Final Visit')}>Modify Final Visit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewUnsuccessfulUHVPage;
