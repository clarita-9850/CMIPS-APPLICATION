import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'contacts History', route: '/case/contacts-history' }
  ];

export function CaseViewContactHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewContactHistory"}
      title={"View Contact:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Mailing Address"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Spoken Language"} value={record && record['spokenLanguage']} />
          <UimField label={"Email Address"} value={record && record['emailAddress']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Relationship"} value={record && record['relationship']} />
          <UimField label={"Mailing Address"} value={record && record['mailingAddress']} />
          <UimField label={"Type"} value={record && record['type']} />
        </div>
      </UimSection>
      <UimSection title={"Phone Numbers"}>
        <div className="uim-form-grid">
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Phone # 1"} value={record && record['phone1']} />
          <UimField label={"Phone # 2"} value={record && record['phone2']} />
          <UimField label={"Extension"} value={record && record['extension']} />
          <UimField label={"Extension"} value={record && record['extension']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewContactHistoryPage;
