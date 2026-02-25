import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'home', route: '/case/ihss-case-home' }
  ];

export function CaseRescindCaseWithCINPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_rescindCaseWithCIN"}
      title={"Rescind Case:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Rescission Details"}>
        <div className="uim-form-grid">
          <UimField label={"Last Medi-Cal Eligibility Month"} value={record && record['lastMediCalEligibilityMonth']} />
          <UimField label={"Rescind Date"} value={record && record['rescindDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Reason"} value={record && record['reason']} />
          <UimField label={"CLIENT INDEX NUMBER"} value={record && record['cLIENTINDEXNUMBER']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseRescindCaseWithCINPage;
