import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function CaseModifyProviderWPCSHoursPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_modifyProviderWPCSHours"}
      title={"Modify WPCS Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"WPCS Rate"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Type"} value={record && record['providerType']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"Pay Rate"} value={record && record['payRate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Differential Pay Rate"} value={record && record['differentialPayRate']} />
          <UimField label={"Max Differential Pay Rate Hours (HH:MM)"} value={record && record['maxDifferentialPayRateHoursHHMM']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseModifyProviderWPCSHoursPage;
