import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Overpaid Overtime Pay Period', route: '/payment/view-overpaid-overtime-pay-period' },
    { label: 'create Overpaid O T Overpayment Pay Period', route: '/case/create-overpaid-ot-overpayment-pay-period' }
  ];

export function CaseCreateOverpaidOvertimePayPeriodTimeEntriesPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  return (
    <UimPageLayout
      pageId={"Case_createOverpaidOvertimePayPeriodTimeEntries"}
      title={"Create Overpayment Pay Period:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Period"} />
          <UimField label={"Program"} />
          <UimField label={"No Overpaid Hours for this Pay Period"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Save &amp; New')}>Save &amp; New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateOverpaidOvertimePayPeriodTimeEntriesPage;
