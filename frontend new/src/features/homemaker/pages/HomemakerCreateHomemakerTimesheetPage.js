import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Homemaker Timesheet', route: '/homemaker/view-homemaker-timesheet' }
  ];

export function HomemakerCreateHomemakerTimesheetPage() {
  const navigate = useNavigate();
  const homemakerApi = getDomainApi('homemaker');
  return (
    <UimPageLayout
      pageId={"Homemaker_createHomemakerTimesheet"}
      title={"Create Homemaker/PA Contract Timesheet"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Select Month"} />
          <UimField label={"Select Year"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { homemakerApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default HomemakerCreateHomemakerTimesheetPage;
