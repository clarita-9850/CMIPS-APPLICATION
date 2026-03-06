import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';

const NAV_LINKS = [
    { label: 'display Message', route: '/person/display-message' },
    { label: 'list Message Center', route: '/person/list-message-center' }
  ];

export function PersonCreateMessageCenterPage() {
  const navigate = useNavigate();
  return (
    <UimPageLayout
      pageId={"Person_createMessageCenter"}
      title={"Create Message:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Subject"} />
          <UimField label={"High Priority"} />
          <UimField label={"Form Name"} />
        </div>
      </UimSection>
      <UimSection title={"Message Preview"}>
        <div className="uim-form-grid">
          <UimField label={"Action Date"} />
          <UimField label={"Subject"} />
          <UimField label={"Body"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Preview')}>Preview</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonCreateMessageCenterPage;
