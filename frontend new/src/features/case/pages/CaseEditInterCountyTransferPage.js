import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function CaseEditInterCountyTransferPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const [activeTab, setActiveTab] = React.useState(0);
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_editInterCountyTransfer"}
      title={"Modify Inter-County Transfer:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <div className="uim-tab-nav">
        <button className={activeTab===0 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(0)}>Residence Address</button>
      </div>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Sending County"} value={record && record['sendingCounty']} />
          <UimField label={"Receiving County"} value={record && record['receivingCounty']} />
          <UimField label={"Assigned Worker"} value={record && record['assignedWorker']} />
          <UimField label={"Sending County Notified"} value={record && record['sendingCountyNotified']} />
        </div>
      </UimSection>
      <UimSection title={"Residence Address"}>
        <div className="uim-form-grid">
          <UimField label={"Date Of Move"} value={record && record['dateOfMove']} />
          <UimField label={"Extension"} value={record && record['extension']} />
          <UimField label={"New Address"} value={record && record['newAddress']} />
          <UimField label={"New Address"} value={record && record['newAddress']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseEditInterCountyTransferPage;
