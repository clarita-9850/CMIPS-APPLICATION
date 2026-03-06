import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'B V I Create Record', route: '/case/bvi-create-record' },
    { label: 'B V I History', route: '/case/bvi-history' },
    { label: 'B V I View Record', route: '/case/bvi-view-record' },
    { label: 'B V I Modify Record', route: '/case/bvi-modify-record' }
  ];

export function CaseBlindVisuallyImpairedPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_blindVisuallyImpaired"}
      title={"Blind or Visually Impaired:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Blind or Visually Impaired"} value={record && record['blindOrVisuallyImpaired']} />
          <UimField label={"Notice of Action Option"} value={record && record['noticeOfActionOption']} />
          <UimField label={"Notice of Action Option Language"} value={record && record['noticeOfActionOptionLanguage']} />
          <UimField label={"IHSS Required Forms Option"} value={record && record['iHSSRequiredFormsOption']} />
          <UimField label={"IHSS Required Forms Option Language"} value={record && record['iHSSRequiredFormsOptionLanguage']} />
          <UimField label={"Created By"} value={record && record['createdBy']} />
          <UimField label={"From"} value={record && record['from']} />
          <UimField label={"To"} value={record && record['to']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/bvi-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/bvi-view-record')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseBlindVisuallyImpairedPage;
