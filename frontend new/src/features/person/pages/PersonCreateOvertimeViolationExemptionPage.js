import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'overtime Violation Exemption', route: '/person/overtime-violation-exemption' },
    { label: 'cancel Popup Create O T V E', route: '/person/cancel-popup-create-otve' }
  ];

export function PersonCreateOvertimeViolationExemptionPage() {
  const navigate = useNavigate();
  const personsApi = getDomainApi('person');
  return (
    <UimPageLayout
      pageId={"Person_createOvertimeViolationExemption"}
      title={"Create Overtime Violation Exemption:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Begin Date"} />
          <UimField label={"End Date"} />
          <UimField label={"Exemption Type"} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Comments"} />
          <UimField label={"Case Number"} />
          <UimField label={"Case Name"} />
        </div>
      </UimSection>
      <UimSection title={"Select Recipients"}>
        <div className="uim-form-grid">
          <UimField label={"Case Status"} />
          <UimField label={"Program Type"} />
          <UimField label={"Provider Status"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonCreateOvertimeViolationExemptionPage;
