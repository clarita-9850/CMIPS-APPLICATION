import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'household Evidence Home', route: '/evidence/household-evidence-home' }
  ];

export function EvidenceModifyResidenceInformationPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const eligibilityApi = getDomainApi('evidence');
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_modifyResidenceInformation"}
      title={"Modify Residence Information:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Residence Information"}>
        <div className="uim-form-grid">
          <UimField label={"Stove"} value={record && record['stove']} />
          <UimField label={"Refrigerator"} value={record && record['refrigerator']} />
          <UimField label={"Washer"} value={record && record['washer']} />
          <UimField label={"Dryer"} value={record && record['dryer']} />
          <UimField label={"Yard"} value={record && record['yard']} />
          <UimField label={"Living Arrangement"} value={record && record['livingArrangement']} />
          <UimField label={"Residence Type"} value={record && record['residenceType']} />
          <UimField label={"Number of Recipient only Rooms"} value={record && record['numberOfRecipientOnlyRooms']} />
          <UimField label={"Number of Shared Rooms"} value={record && record['numberOfSharedRooms']} />
          <UimField label={"Number of Rooms not Used"} value={record && record['numberOfRoomsNotUsed']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { eligibilityApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceModifyResidenceInformationPage;
