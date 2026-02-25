import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function CaseCreateUnannouncedHomeVisitPopUpPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  return (
    <UimPageLayout
      pageId={"Case_createUnannouncedHomeVisitPopUp"}
      title={"Create Home Visit Entry:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Unannounced Home Visit"}>
        <div className="uim-form-grid">
          <UimField label={"Home Visit Date Time"} />
          <UimField label={"Outcome"} />
        </div>
      </UimSection>
      <UimSection title={"Home Visit Details"}>
        <div className="uim-form-grid">
          <UimField label={"Comments"} />
          <UimField label={"Home Visit Date Time"} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Outcome"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateUnannouncedHomeVisitPopUpPage;
