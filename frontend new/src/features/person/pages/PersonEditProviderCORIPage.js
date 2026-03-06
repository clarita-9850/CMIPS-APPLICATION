import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function PersonEditProviderCORIPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_editProviderCORI"}
      title={"Modify Provider CORI"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Provider CORI Details"}>
        <div className="uim-form-grid">
          <UimField label={"CORI Date"} value={record && record['cORIDate']} />
          <UimField label={"Conviction or Release Date"} value={record && record['convictionOrReleaseDate']} />
          <UimField label={"General Exception Begin Date"} value={record && record['generalExceptionBeginDate']} />
          <UimField label={"CORI End Date"} value={record && record['cORIEndDate']} />
          <UimField label={"Tier"} value={record && record['tier']} />
          <UimField label={"General Exception End Date"} value={record && record['generalExceptionEndDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonEditProviderCORIPage;
