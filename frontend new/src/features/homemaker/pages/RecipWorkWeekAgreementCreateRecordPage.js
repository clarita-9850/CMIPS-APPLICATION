import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Work Week Agreement', route: '/misc/recip-list-work-week-agreement' }
  ];

export function RecipWorkWeekAgreementCreateRecordPage() {
  const navigate = useNavigate();
  const homemakerApi = getDomainApi('homemaker');
  return (
    <UimPageLayout
      pageId={"RecipWorkWeekAgreement_createRecord"}
      title={"Create Recipient Workweek Agreement:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Begin Date"} />
          <UimField label={"Total Hours (HH:MM)"} />
          <UimField label={"Back-Up Provider"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} />
          <UimField label={"Provider Type"} />
          <UimField label={"Recipient Weekly Authorized Hours"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { homemakerApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default RecipWorkWeekAgreementCreateRecordPage;
