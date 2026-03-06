import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';

const NAV_LINKS = [
    { label: 'view Travel Claim', route: '/case/view-travel-claim' },
    { label: 'enter Travel Claim Number', route: '/case/enter-travel-claim-number' }
  ];

export function CaseCreateConfirmTravelClaimPage() {
  const navigate = useNavigate();
  return (
    <UimPageLayout
      pageId={"Case_createConfirmTravelClaim"}
      title={"TravelClaim Manual Entry - Time Entries:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} />
          <UimField label={"Type"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Service Period From"} />
          <UimField label={"Recipient Name"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"TravelClaim Number"} />
          <UimField label={"Total (HH:MM)"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Total (HH:MM)"} />
          <UimField label={"Total (HH:MM)"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Navigating to next step')}>Continue</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateConfirmTravelClaimPage;
