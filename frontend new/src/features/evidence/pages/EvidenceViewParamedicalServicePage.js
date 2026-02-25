import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'modify Paramedical Service From View', route: '/evidence/modify-paramedical-service-from-view' },
    { label: 'delete Paramedical Service Hours', route: '/evidence/delete-paramedical-service-hours' },
    { label: 'reload Service Evidence Home', route: '/evidence/reload-service-evidence-home' }
  ];

export function EvidenceViewParamedicalServicePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const eligibilityApi = getDomainApi('evidence');
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_viewParamedicalService"}
      title={"View Service Type Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Expiration Dates"}>
        <div className="uim-form-grid">
          <UimField label={"Service Type"} value={record && record['serviceType']} />
          <UimField label={"Paramedical Services Form Sent Date"} value={record && record['paramedicalServicesFormSentDate']} />
          <UimField label={"Total Assessed Need (HH:MM)"} value={record && record['totalAssessedNeedHHMM']} />
          <UimField label={"Alternative Resources (HH:MM)"} value={record && record['alternativeResourcesHHMM']} />
          <UimField label={"Pending Receipt of Additional Information"} value={record && record['pendingReceiptOfAdditionalInformation']} />
          <UimField label={"Paramedical Services Form Received Date"} value={record && record['paramedicalServicesFormReceivedDate']} />
          <UimField label={"Refused Services (HH:MM)"} value={record && record['refusedServicesHHMM']} />
          <UimField label={"Voluntary Services (HH:MM)"} value={record && record['voluntaryServicesHHMM']} />
          <UimField label={"Expiration Date 1"} value={record && record['expirationDate1']} />
          <UimField label={"Expiration Date 3"} value={record && record['expirationDate3']} />
          <UimField label={"Expiration Date 2"} value={record && record['expirationDate2']} />
          <UimField label={"Expiration Date 4"} value={record && record['expirationDate4']} />
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

export default EvidenceViewParamedicalServicePage;
