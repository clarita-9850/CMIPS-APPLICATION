import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'home', route: '/evidence/home' },
    { label: 'program Evidence Home', route: '/evidence/program-evidence-home' },
    { label: 'modify Functional Ranks', route: '/evidence/modify-functional-ranks' },
    { label: 'modify Assessment Narrative', route: '/evidence/modify-assessment-narrative' },
    { label: 'view Assessment Narrative', route: '/evidence/view-assessment-narrative' }
  ];

export function EvidenceServiceEvidenceHomePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_serviceEvidenceHome"}
      title={"Service Evidence:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Functional Ranks"}>
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
          <UimField label={"Functional Index"} value={record && record['functionalIndex']} />
          <UimField label={"Service Type"} value={record && record['serviceType']} />
          <UimField label={"W/M"} value={record && record['wM']} />
          <UimField label={"HTG"} value={record && record['hTG']} />
          <UimField label={"Total Assessed Need"} value={record && record['totalAssessedNeed']} />
          <UimField label={"Adj"} value={record && record['adj']} />
          <UimField label={"Ind Assessed Need"} value={record && record['indAssessedNeed']} />
          <UimField label={"Alt+Ref+Vol"} value={record && record['altRefVol']} />
          <UimField label={"Net Adj Need"} value={record && record['netAdjNeed']} />
        </div>
      </UimSection>
      <UimSection title={"Service Type Details"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/home')}>Evidence Home</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next')}>Next</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Functional Ranks')}>Edit Functional Ranks</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Assessment Narrative')}>Edit Assessment Narrative</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/view-assessment-narrative')}>View Assessment Narrative</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/home')}>Evidence Home</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next')}>Next</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceServiceEvidenceHomePage;
