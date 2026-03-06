import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'resolve Overpaid O T Daily Entries', route: '/case/resolve-overpaid-ot-daily-entries' },
    { label: 'view Overpayment Recovery', route: '/payment/view-overpayment-recovery' }
  ];

export function CaseCreateOverpaidOTOverpaymentPayPeriodPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  return (
    <UimPageLayout
      pageId={"Case_createOverpaidOTOverpaymentPayPeriod"}
      title={"Select Warrant:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Select Warrant - Service Period"} />
          <UimField label={"No Overpaid Hours for this Pay Period"} />
          <UimField label={"Warrant Number"} />
          <UimField label={"Service From"} />
          <UimField label={"Hours"} />
          <UimField label={"Issued"} />
          <UimField label={"Status"} />
          <UimField label={"Pay Type"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateOverpaidOTOverpaymentPayPeriodPage;
