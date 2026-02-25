import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Comment Forms Histroy', route: '/case/view-comment-forms-histroy' },
    { label: 'list Forms Correspondence', route: '/case/list-forms-correspondence' }
  ];

export function CaseViewFormsAndCorrespondenceHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewFormsAndCorrespondenceHistory"}
      title={"Forms History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Last Update & Time"} value={record && record['lastUpdateTime']} />
          <UimField label={"NOA Status"} value={record && record['nOAStatus']} />
          <UimField label={"Inactivate/\\\\Not Mailed"} value={record && record['inactivateNotMailed']} />
          <UimField label={"BVI Status"} value={record && record['bVIStatus']} />
          <UimField label={"BVI Format"} value={record && record['bVIFormat']} />
          <UimField label={"Due Date"} value={record && record['dueDate']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Last Update & Time"} value={record && record['lastUpdateTime']} />
          <UimField label={"NOA Status"} value={record && record['nOAStatus']} />
          <UimField label={"Inactivate/\\\\Not Mailed"} value={record && record['inactivateNotMailed']} />
          <UimField label={"BVI Status"} value={record && record['bVIStatus']} />
          <UimField label={"BVI Format"} value={record && record['bVIFormat']} />
          <UimField label={"Due Date"} value={record && record['dueDate']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-comment-forms-histroy')}>View Comment</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.print()}>Print</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-comment-forms-histroy')}>View Comment</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.print()}>Print</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewFormsAndCorrespondenceHistoryPage;
