import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'modify Protective Supervision From View', route: '/evidence/modify-protective-supervision-from-view' },
    { label: 'delete Protective Supervision Hours', route: '/evidence/delete-protective-supervision-hours' },
    { label: 'reload Service Evidence Home', route: '/evidence/reload-service-evidence-home' }
  ];

export function EvidenceViewProtectiveSupervisionPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const eligibilityApi = getDomainApi('evidence');
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_viewProtectiveSupervision"}
      title={"View Service Type Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Type"} value={record && record['serviceType']} />
          <UimField label={"Protective Supervision Form Sent Date"} value={record && record['protectiveSupervisionFormSentDate']} />
          <UimField label={"Total Assessed Need (HH:MM)"} value={record && record['totalAssessedNeedHHMM']} />
          <UimField label={"Adjustments (HH:MM)"} value={record && record['adjustmentsHHMM']} />
          <UimField label={"Companion Case Protective Supervision Adjustment (HH:MM)"} value={record && record['companionCaseProtectiveSupervisionAdjustmentHHMM']} />
          <UimField label={"Pending Receipt of Additional Information"} value={record && record['pendingReceiptOfAdditionalInformation']} />
          <UimField label={"Protective Supervision Form Received Date"} value={record && record['protectiveSupervisionFormReceivedDate']} />
          <UimField label={"Alternative Resources (HH:MM)"} value={record && record['alternativeResourcesHHMM']} />
          <UimField label={"Voluntary Services (HH:MM)"} value={record && record['voluntaryServicesHHMM']} />
          <UimField label={"24 Hour Care Plan Need (HH:MM)"} value={record && record['24HourCarePlanNeedHHMM']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { eligibilityApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Delete</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceViewProtectiveSupervisionPage;
