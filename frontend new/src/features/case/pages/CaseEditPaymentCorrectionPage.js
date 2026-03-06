import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Payment Correction', route: '/case/view-payment-correction' }
  ];

export function CaseEditPaymentCorrectionPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_editPaymentCorrection"}
      title={"Modify Payment Correction:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Timesheet Type"} value={record && record['timesheetType']} />
          <UimField label={"Submitted By"} value={record && record['submittedBy']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Correction Type"} value={record && record['correctionType']} />
          <UimField label={"Timesheet Number"} value={record && record['timesheetNumber']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Approved By/Rejected By"} value={record && record['approvedByRejectedBy']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Total Correction (HH:MM)"} value={record && record['totalCorrectionHHMM']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Total Correction (HH:MM)"} value={record && record['totalCorrectionHHMM']} />
          <UimField label={"Total Correction (HH:MM)"} value={record && record['totalCorrectionHHMM']} />
          <UimField label={"Total Correction (HH:MM)"} value={record && record['totalCorrectionHHMM']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseEditPaymentCorrectionPage;
