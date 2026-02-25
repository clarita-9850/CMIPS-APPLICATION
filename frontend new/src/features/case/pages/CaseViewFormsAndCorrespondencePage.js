import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'modify Forms And Correspondence', route: '/case/modify-forms-and-correspondence' },
    { label: 'Create B V I Assitance Line Call Record', route: '/case/create-bvi-assitance-line-call-record' },
    { label: 'bvi Assistance Line Call Record History', route: '/case/bvi-assistance-line-call-record-history' },
    { label: 'list Forms Correspondence', route: '/case/list-forms-correspondence' }
  ];

export function CaseViewFormsAndCorrespondencePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewFormsAndCorrespondence"}
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
          <UimField label={"Electronic Form Due Date"} value={record && record['electronicFormDueDate']} />
          <UimField label={"Language"} value={record && record['language']} />
        </div>
      </UimSection>
      <UimSection title={"BVI Format"}>
        <div className="uim-form-grid">
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Suppress NOA"} value={record && record['suppressNOA']} />
          <UimField label={"Form Name"} value={record && record['formName']} />
          <UimField label={"Option"} value={record && record['option']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Reprint in Another Language"}>
        <div className="uim-form-grid">
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <UimSection title={"BVI Assistance Line Call Record Details"}>
        <div className="uim-form-grid">
          <UimField label={"Paramedical Form Text"} value={record && record['paramedicalFormText']} />
          <UimField label={"Entered By"} value={record && record['enteredBy']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"BVI Comments"} value={record && record['bVIComments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Add BVI Assistance Line Call Record')}>Add BVI Assistance Line Call Record</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/bvi-assistance-line-call-record-history')}>BVI Assistance Line Call Record History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Print in Nightly Batch')}>Print in Nightly Batch</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Print Now on CMIPS II Printer')}>Print Now on CMIPS II Printer</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Generate Now for Local Print')}>Generate Now for Local Print</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewFormsAndCorrespondencePage;
