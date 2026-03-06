import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Provider', route: '/case/list-provider' }
  ];

export function CaseModifyProviderPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_modifyProvider"}
      title={"Modify Case Provider:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Relationship to Recipient"} value={record && record['relationshipToRecipient']} />
          <UimField label={"Elective SDI"} value={record && record['electiveSDI']} />
          <UimField label={"SDI Begin Date"} value={record && record['sDIBeginDate']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Relationship Status Date"} value={record && record['relationshipStatusDate']} />
          <UimField label={"Timesheet Review"} value={record && record['timesheetReview']} />
          <UimField label={"SDI End Date"} value={record && record['sDIEndDate']} />
          <UimField label={"County Use Comments"} value={record && record['countyUseComments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseModifyProviderPage;
