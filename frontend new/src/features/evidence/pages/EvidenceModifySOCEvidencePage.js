import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'soc Evidence Home', route: '/evidence/soc-evidence-home' }
  ];

export function EvidenceModifySOCEvidencePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const eligibilityApi = getDomainApi('evidence');
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_modifySOCEvidence"}
      title={"Modify Share of Cost Evidence"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Share of Cost Evidence"}>
        <div className="uim-form-grid">
          <UimField label={"Share of Cost Linkage"} value={record && record['shareOfCostLinkage']} />
          <UimField label={"Dependents"} value={record && record['dependents']} />
          <UimField label={"Benefit Level Code"} value={record && record['benefitLevelCode']} />
        </div>
      </UimSection>
      <UimSection title={"Share of Cost Calculation"}>
        <div className="uim-form-grid">
          <UimField label={"Countable Income"} value={record && record['countableIncome']} />
          <UimField label={"IHSS Share of Cost"} value={record && record['iHSSShareOfCost']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { eligibilityApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceModifySOCEvidencePage;
