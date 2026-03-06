import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function OrganizationAddPositionForOrgUnitPage() {
  const navigate = useNavigate();
  const organizationApi = getDomainApi('organization');
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_addPositionForOrgUnit"}
      title={"Create Position"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"Job"} value={record && record['job']} />
        </div>
      </UimSection>
      <UimSection title={"Reporting"}>
        <div className="uim-form-grid">
          <UimField label={"Lead Position"} value={record && record['leadPosition']} />
          <UimField label={"Lead Positions"} value={record && record['leadPositions']} />
          <UimField label={"Organization Unit Positions"} value={record && record['organizationUnitPositions']} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Parent Organization Unit Positions"} value={record && record['parentOrganizationUnitPositions']} />
          <UimField label={"Position Search"} value={record && record['positionSearch']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Save &amp; New')}>Save &amp; New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationAddPositionForOrgUnitPage;
