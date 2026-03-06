import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Overpayment Recovery', route: '/payment/view-overpayment-recovery' }
  ];

export function CaseCreateOverpaymentOccurencePage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  return (
    <UimPageLayout
      pageId={"Case_createOverpaymentOccurence"}
      title={"Create Overpayment Occurrence:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Overpayment Occurrence"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} />
          <UimField label={"To Date"} />
          <UimField label={"Payee Name"} />
          <UimField label={"Overpayment Type"} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"Special Transaction Number"} />
          <UimField label={"Program"} />
          <UimField label={"Reason"} />
          <UimField label={"Comments"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateOverpaymentOccurencePage;
