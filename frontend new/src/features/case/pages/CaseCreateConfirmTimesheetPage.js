import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';

const NAV_LINKS = [
    { label: 'view Timesheet', route: '/case/view-timesheet' },
    { label: 'enter Timesheet Number', route: '/case/enter-timesheet-number' }
  ];

export function CaseCreateConfirmTimesheetPage() {
  const navigate = useNavigate();
  return (
    <UimPageLayout
      pageId={"Case_createConfirmTimesheet"}
      title={"Timesheet Manual Entry - Time Entries:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} />
          <UimField label={"Type"} />
          <UimField label={"Service Period From"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Name"} />
          <UimField label={"Remaining Hrs (HH:MM)"} />
          <UimField label={"Timesheet Number"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Total (HH:MM)"} />
          <UimField label={"Total (HH:MM)"} />
          <UimField label={"Total (HH:MM)"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Navigating to next step')}>Continue</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateConfirmTimesheetPage;
