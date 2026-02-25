import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'modify Disaster Preparedness', route: '/case/modify-disaster-preparedness' },
    { label: 'home', route: '/evidence/home' },
    { label: 'service Evidence Home', route: '/evidence/service-evidence-home' }
  ];

export function CaseViewDisasterPreparednessPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewDisasterPreparedness"}
      title={"Disaster Preparedness:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Contact and Information"}>
        <div className="uim-form-grid">
          <UimField label={"Degree of Contact"} value={record && record['degreeOfContact']} />
          <UimField label={"Comments"} value={record && record['comments']} />
          <UimField label={"No Contact Required"} value={record && record['noContactRequired']} />
          <UimField label={"Power Outage"} value={record && record['powerOutage']} />
          <UimField label={"Extreme Cold"} value={record && record['extremeCold']} />
          <UimField label={"Disaster"} value={record && record['disaster']} />
        </div>
      </UimSection>
      <UimSection title={"Events"}>
        <div className="uim-form-grid">
          <UimField label={"Extreme Heat"} value={record && record['extremeHeat']} />
          <UimField label={"Electricity Dependent?"} value={record && record['electricityDependent']} />
          <UimField label={"No Supplies Needed"} value={record && record['noSuppliesNeeded']} />
          <UimField label={"Life Support Medications"} value={record && record['lifeSupportMedications']} />
          <UimField label={"Insulin"} value={record && record['insulin']} />
          <UimField label={"Oxygen"} value={record && record['oxygen']} />
        </div>
      </UimSection>
      <UimSection title={"Electricity and Life Support Supply Needed"}>
        <div className="uim-form-grid">
          <UimField label={"Dialysis"} value={record && record['dialysis']} />
          <UimField label={"Ventilator"} value={record && record['ventilator']} />
          <UimField label={"No Special Impairments"} value={record && record['noSpecialImpairments']} />
          <UimField label={"Blind"} value={record && record['blind']} />
          <UimField label={"Mental/Cognitive Disability - Requires Assistance"} value={record && record['mentalCognitiveDisabilityRequiresAssistance']} />
          <UimField label={"Bed-bound"} value={record && record['bedBound']} />
        </div>
      </UimSection>
      <UimSection title={"Special Impairments"}>
        <div className="uim-form-grid">
          <UimField label={"Deaf"} value={record && record['deaf']} />
          <UimField label={"Use of Mobility Equipment"} value={record && record['useOfMobilityEquipment']} />
          <UimField label={"Heavy Medication"} value={record && record['heavyMedication']} />
          <UimField label={"Non-ambulatory/Transfer Dependent"} value={record && record['nonAmbulatoryTransferDependent']} />
          <UimField label={"None"} value={record && record['none']} />
          <UimField label={"Home Difficult to Access"} value={record && record['homeDifficultToAccess']} />
        </div>
      </UimSection>
      <UimSection title={"Other Emergency Services Considerations"}>
        <div className="uim-form-grid">
          <UimField label={"Lacks Transportation"} value={record && record['lacksTransportation']} />
          <UimField label={"Lives in Isolated Area"} value={record && record['livesInIsolatedArea']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Disaster Preparedness Information')}>Edit Disaster Preparedness Information</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/home')}>Evidence Home</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next')}>Next</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewDisasterPreparednessPage;
