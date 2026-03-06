import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'create B V I N O A Text Template', route: '/back-office/create-bvinoa-text-template' },
    { label: 'view B V I N O A Text Template', route: '/back-office/view-bvinoa-text-template' },
    { label: 'modify B V I N O A Text Template', route: '/back-office/modify-bvinoa-text-template' }
  ];

export function SystemListBVINOATextTemplatePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('back-office', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"System_listBVINOATextTemplate"}
      title={"BVI NOA Text Templates"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Template ID"} value={record && record['templateID']} />
          <UimField label={"Checked Out By"} value={record && record['checkedOutBy']} />
          <UimField label={"Relates To"} value={record && record['relatesTo']} />
          <UimField label={"Template Type"} value={record && record['templateType']} />
          <UimField label={"Locale"} value={record && record['locale']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/back-office/view-bvinoa-text-template')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default SystemListBVINOATextTemplatePage;
