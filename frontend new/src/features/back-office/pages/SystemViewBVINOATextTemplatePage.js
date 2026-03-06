import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'modify B V I N O A Text Template', route: '/back-office/modify-bvinoa-text-template' },
    { label: 'list B V I N O A Text Template', route: '/back-office/list-bvinoa-text-template' },
    { label: 'select B V I Text Template Version', route: '/back-office/select-bvi-text-template-version' }
  ];

export function SystemViewBVINOATextTemplatePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('back-office', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"System_viewBVINOATextTemplate"}
      title={"View BVI NOA Text Template:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Description"} value={record && record['description']} />
          <UimField label={"Relates To"} value={record && record['relatesTo']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Template ID"} value={record && record['templateID']} />
        </div>
      </UimSection>
      <UimSection title={"Latest Version Details"}>
        <div className="uim-form-grid">
          <UimField label={"Created By"} value={record && record['createdBy']} />
          <UimField label={"Latest Version"} value={record && record['latestVersion']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Check Out Template')}>Check Out Template</button>
      </div>
    </UimPageLayout>
  );
}

export default SystemViewBVINOATextTemplatePage;
