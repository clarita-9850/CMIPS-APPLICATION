import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Travel Time History', route: '/person/view-travel-time-history' },
    { label: 'leave Terminate Provider', route: '/person/leave-terminate-provider' },
    { label: 'modify Travel Time', route: '/person/modify-travel-time' },
    { label: 'inactivate Travel Time', route: '/person/inactivate-travel-time' },
    { label: 'display Travel Time', route: '/person/display-travel-time' }
  ];

export function PersonViewTravelTimePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewTravelTime"}
      title={"View Travel Time:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"Weekly Travel Time Hours"} value={record && record['weeklyTravelTimeHours']} />
          <UimField label={"Program Type"} value={record && record['programType']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Traveling From"} value={record && record['travelingFrom']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-travel-time-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Leave/Terminate')}>Leave/Terminate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { personsApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewTravelTimePage;
