import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'modify Service Type With Proration From View', route: '/evidence/modify-service-type-with-proration-from-view' },
    { label: 'reload Service Evidence Home', route: '/evidence/reload-service-evidence-home' }
  ];

export function EvidenceViewServiceTypeWithProrationPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_viewServiceTypeWithProration"}
      title={"View Service Type Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Type"} value={record && record['serviceType']} />
          <UimField label={"Service Task"} value={record && record['serviceTask']} />
          <UimField label={"Frequency"} value={record && record['frequency']} />
          <UimField label={"Quantity"} value={record && record['quantity']} />
          <UimField label={"Duration"} value={record && record['duration']} />
          <UimField label={"Proration"} value={record && record['proration']} />
          <UimField label={"Total Assessed Need (HH:MM)"} value={record && record['totalAssessedNeedHHMM']} />
          <UimField label={"Adjustments (HH:MM)"} value={record && record['adjustmentsHHMM']} />
          <UimField label={"Alternative Resources (HH:MM)"} value={record && record['alternativeResourcesHHMM']} />
          <UimField label={"Refused Services (HH:MM)"} value={record && record['refusedServicesHHMM']} />
          <UimField label={"Voluntary Services (HH:MM)"} value={record && record['voluntaryServicesHHMM']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <UimSection title={"Task Details:"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceViewServiceTypeWithProrationPage;
