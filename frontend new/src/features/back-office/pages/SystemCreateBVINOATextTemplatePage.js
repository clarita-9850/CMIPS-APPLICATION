import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function SystemCreateBVINOATextTemplatePage() {
  const navigate = useNavigate();
  const backOfficeApi = getDomainApi('back-office');
  return (
    <UimPageLayout
      pageId={"System_createBVINOATextTemplate"}
      title={"Create BVI NOA Text Template"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Template Details"}>
        <div className="uim-form-grid">
          <UimField label={"Description"} />
          <UimField label={"Relates To"} />
          <UimField label={"Type"} />
          <UimField label={"Template ID"} />
          <UimField label={"Locale"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { backOfficeApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Save &amp; New')}>Save &amp; New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default SystemCreateBVINOATextTemplatePage;
