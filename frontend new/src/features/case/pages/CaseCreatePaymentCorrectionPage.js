import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Payment Correction', route: '/case/view-payment-correction' },
    { label: 'enter Payment Correction Timesheet Number', route: '/case/enter-payment-correction-timesheet-number' }
  ];

export function CaseCreatePaymentCorrectionPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  return (
    <UimPageLayout
      pageId={"Case_createPaymentCorrection"}
      title={"Create Payment Correction:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} />
          <UimField label={"Service Period From"} />
          <UimField label={"Timesheet Type"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Name"} />
          <UimField label={"Correction Type"} />
          <UimField label={"Timesheet Number"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Total Correction (HH:MM)"} />
          <UimField label={"Total Correction (HH:MM)"} />
          <UimField label={"Total Correction (HH:MM)"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Total Correction (HH:MM)"} />
          <UimField label={"Comments"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Save &amp; New')}>Save &amp; New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreatePaymentCorrectionPage;
