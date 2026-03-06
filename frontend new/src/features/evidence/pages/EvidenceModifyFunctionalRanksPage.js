import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'service Evidence Home', route: '/evidence/service-evidence-home' }
  ];

export function EvidenceModifyFunctionalRanksPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const eligibilityApi = getDomainApi('evidence');
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_modifyFunctionalRanks"}
      title={"Modify Functional Ranks:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Housework"} value={record && record['housework']} />
          <UimField label={"Shopping & Errands"} value={record && record['shoppingErrands']} />
          <UimField label={"Ambulation"} value={record && record['ambulation']} />
          <UimField label={"Dressing"} value={record && record['dressing']} />
          <UimField label={"Transfer"} value={record && record['transfer']} />
          <UimField label={"Respiration"} value={record && record['respiration']} />
          <UimField label={"Orientation"} value={record && record['orientation']} />
          <UimField label={"Laundry"} value={record && record['laundry']} />
          <UimField label={"Meal Prep & Clean-up"} value={record && record['mealPrepCleanUp']} />
          <UimField label={"Bathing & Grooming"} value={record && record['bathingGrooming']} />
          <UimField label={"Bowel, Bladder & Menstrual Care"} value={record && record['bowelBladderMenstrualCare']} />
          <UimField label={"Feeding"} value={record && record['feeding']} />
          <UimField label={"Memory"} value={record && record['memory']} />
          <UimField label={"Judgment"} value={record && record['judgment']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { eligibilityApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceModifyFunctionalRanksPage;
