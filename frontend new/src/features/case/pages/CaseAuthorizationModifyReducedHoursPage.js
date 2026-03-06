import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function CaseAuthorizationModifyReducedHoursPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"CaseAuthorization_modifyReducedHours"}
      title={"Modify Adjusted Hours"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Total Auth to Purchase After LMA (HH:MM)"} value={record && record['totalAuthToPurchaseAfterLMAHHMM']} />
          <UimField label={"Unmet Need After LMA (HH:MM)"} value={record && record['unmetNeedAfterLMAHHMM']} />
          <UimField label={"Adjusted Hours (HH:MM)"} value={record && record['adjustedHoursHHMM']} />
          <UimField label={"Reinstated Hours (HH:MM)"} value={record && record['reinstatedHoursHHMM']} />
          <UimField label={"Unmet Need After Adjusted Hours (HH:MM)"} value={record && record['unmetNeedAfterAdjustedHoursHHMM']} />
          <UimField label={"Total Auth to Purchase After Adjusted Hours (HH:MM)"} value={record && record['totalAuthToPurchaseAfterAdjustedHoursHHMM']} />
          <UimField label={"Social Worker Certification"} value={record && record['socialWorkerCertification']} />
          <UimField label={"Verified by Case Owner or Supervisor"} value={record && record['verifiedByCaseOwnerOrSupervisor']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseAuthorizationModifyReducedHoursPage;
