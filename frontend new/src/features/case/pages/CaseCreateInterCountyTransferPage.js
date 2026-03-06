import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function CaseCreateInterCountyTransferPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  const [activeTab, setActiveTab] = React.useState(0);
  return (
    <UimPageLayout
      pageId={"Case_createInterCountyTransfer"}
      title={"Create Inter-County Transfer:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <div className="uim-tab-nav">
        <button className={activeTab===0 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(0)}>Residence Address</button>
      </div>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Sending County"} />
          <UimField label={"Receiving County"} />
          <UimField label={"Sending County Notified"} />
          <UimField label={"Date Of Move"} />
        </div>
      </UimSection>
      <UimSection title={"Residence Address"}>
        <div className="uim-form-grid">
          <UimField label={"Extension"} />
          <UimField label={"New Address"} />
          <UimField label={"New Address"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateInterCountyTransferPage;
