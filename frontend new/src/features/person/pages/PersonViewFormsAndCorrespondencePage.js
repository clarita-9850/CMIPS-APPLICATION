import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'modify Forms And Correspondence', route: '/person/modify-forms-and-correspondence' },
    { label: 'list Forms Correspondence', route: '/person/list-forms-correspondence' }
  ];

export function PersonViewFormsAndCorrespondencePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewFormsAndCorrespondence"}
      title={"View Form:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Correspondent Details"}>
        <div className="uim-form-grid">
          <UimField label={"Correspondent Name"} value={record && record['correspondentName']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Mark As Inactivate/Not Mailed"} value={record && record['markAsInactivateNotMailed']} />
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Print in Nightly Batch')}>Print in Nightly Batch</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Print Now on CMIPS II Printer')}>Print Now on CMIPS II Printer</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Generate Now for Local Print')}>Generate Now for Local Print</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewFormsAndCorrespondencePage;
