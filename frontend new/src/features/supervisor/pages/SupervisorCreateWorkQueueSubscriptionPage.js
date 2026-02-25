import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';

const NAV_LINKS = [];

export function SupervisorCreateWorkQueueSubscriptionPage() {
  return (
    <UimPageLayout
      pageId={"Supervisor_createWorkQueueSubscription"}
      title={"Create Work Queue Subscription"}
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

export default SupervisorCreateWorkQueueSubscriptionPage;
