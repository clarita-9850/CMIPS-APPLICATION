import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Disaster Preparedness', route: '/case/view-disaster-preparedness' }
  ];

export function CaseModifyDisasterPreparednessPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_modifyDisasterPreparedness"}
      title={"Modify Disaster Preparedness:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Disaster Preparedness Information"}>
        <div className="uim-form-grid">
          <UimField label={"Degree of Contact"} value={record && record['degreeOfContact']} />
          <UimField label={"Comments"} value={record && record['comments']} />
          <UimField label={"No Contact Required"} value={record && record['noContactRequired']} />
          <UimField label={"Extreme Cold"} value={record && record['extremeCold']} />
          <UimField label={"Extreme Heat"} value={record && record['extremeHeat']} />
          <UimField label={"Power Outage"} value={record && record['powerOutage']} />
        </div>
      </UimSection>
      <UimSection title={"Events - At least one selection is required"}>
        <div className="uim-form-grid">
          <UimField label={"Disaster"} value={record && record['disaster']} />
          <UimField label={"Electricity Dependent?"} value={record && record['electricityDependent']} />
          <UimField label={"No Supplies Needed"} value={record && record['noSuppliesNeeded']} />
          <UimField label={"Oxygen"} value={record && record['oxygen']} />
          <UimField label={"Life Support Medications"} value={record && record['lifeSupportMedications']} />
          <UimField label={"Dialysis"} value={record && record['dialysis']} />
        </div>
      </UimSection>
      <UimSection title={"Electricity and Life Support Supply Needed - At least one Life Support Supply selection is required"}>
        <div className="uim-form-grid">
          <UimField label={"Insulin"} value={record && record['insulin']} />
          <UimField label={"Ventilator"} value={record && record['ventilator']} />
          <UimField label={"No Special Impairments"} value={record && record['noSpecialImpairments']} />
          <UimField label={"Bed-bound"} value={record && record['bedBound']} />
          <UimField label={"Heavy Medication"} value={record && record['heavyMedication']} />
          <UimField label={"Blind"} value={record && record['blind']} />
        </div>
      </UimSection>
      <UimSection title={"Special Impairments - At least one selection is required"}>
        <div className="uim-form-grid">
          <UimField label={"Deaf"} value={record && record['deaf']} />
          <UimField label={"Non-ambulatory/Transfer Dependent"} value={record && record['nonAmbulatoryTransferDependent']} />
          <UimField label={"Mental/Cognitive Disability - Requires Assistance"} value={record && record['mentalCognitiveDisabilityRequiresAssistance']} />
          <UimField label={"Use of Mobility Equipment"} value={record && record['useOfMobilityEquipment']} />
          <UimField label={"None"} value={record && record['none']} />
          <UimField label={"Lacks Transportation"} value={record && record['lacksTransportation']} />
        </div>
      </UimSection>
      <UimSection title={"Other Emergency Services Considerations - At least one selection is required"}>
        <div className="uim-form-grid">
          <UimField label={"Lives in Isolated Area"} value={record && record['livesInIsolatedArea']} />
          <UimField label={"Home Difficult to Access"} value={record && record['homeDifficultToAccess']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseModifyDisasterPreparednessPage;
