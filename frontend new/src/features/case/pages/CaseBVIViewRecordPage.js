import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'B V I Modify Record', route: '/case/bvi-modify-record' },
    { label: 'B V I Inactivate Record', route: '/case/bvi-inactivate-record' },
    { label: 'blind Visually Impaired', route: '/case/blind-visually-impaired' },
    { label: 'B V I History', route: '/case/bvi-history' }
  ];

export function CaseBVIViewRecordPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_BVIViewRecord"}
      title={"View Blind or Visually Impaired:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Created By"} value={record && record['createdBy']} />
          <UimField label={"From"} value={record && record['from']} />
          <UimField label={"To"} value={record && record['to']} />
          <UimField label={"Blind or Visually Impaired"} value={record && record['blindOrVisuallyImpaired']} />
        </div>
      </UimSection>
      <UimSection title={"View Blind or Visually Impaired"}>
        <div className="uim-form-grid">
          <UimField label={"Notice of Action Option"} value={record && record['noticeOfActionOption']} />
          <UimField label={"Notice of Action Option Language"} value={record && record['noticeOfActionOptionLanguage']} />
          <UimField label={"IHSS Required Forms Option"} value={record && record['iHSSRequiredFormsOption']} />
          <UimField label={"IHSS Required Forms Option Language"} value={record && record['iHSSRequiredFormsOptionLanguage']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { casesApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseBVIViewRecordPage;
