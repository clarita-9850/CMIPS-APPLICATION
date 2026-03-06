import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';

const NAV_LINKS = [];

export function OrganizationCreateAndAssignUserForPositionPage() {
  return (
    <UimPageLayout
      pageId={"Organization_createAndAssignUserForPosition"}
      title={"Create And Assign User For Position"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"ID"} />
          <UimField label={"Status"} />
          <UimField label={"Date"} />
        </div>
      </UimSection>
    </UimPageLayout>
  );
}

export default OrganizationCreateAndAssignUserForPositionPage;
