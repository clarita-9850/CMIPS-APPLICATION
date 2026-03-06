import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'edit Inter County Transfer', route: '/case/edit-inter-county-transfer' },
    { label: 'cancel Inter County Transfer', route: '/case/cancel-inter-county-transfer' },
    { label: 'list Inter County Transfer', route: '/case/list-inter-county-transfer' }
  ];

export function CaseViewInterCountyTransferPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewInterCountyTransfer"}
      title={"View Inter-County Transfer:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Sending County"} value={record && record['sendingCounty']} />
          <UimField label={"Receiving County"} value={record && record['receivingCounty']} />
          <UimField label={"New Phone Number"} value={record && record['newPhoneNumber']} />
          <UimField label={"Date Of Move"} value={record && record['dateOfMove']} />
          <UimField label={"Assigned Worker"} value={record && record['assignedWorker']} />
          <UimField label={"Transfer Date"} value={record && record['transferDate']} />
          <UimField label={"Sending County Notified"} value={record && record['sendingCountyNotified']} />
          <UimField label={"Receiving County Notified"} value={record && record['receivingCountyNotified']} />
          <UimField label={"Extension"} value={record && record['extension']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Authorization Start Date"} value={record && record['authorizationStartDate']} />
          <UimField label={"Cancel Date"} value={record && record['cancelDate']} />
          <UimField label={"New Address"} value={record && record['newAddress']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Cancel Transfer Request')}>Cancel Transfer Request</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewInterCountyTransferPage;
