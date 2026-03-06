import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Special Transaction Travel', route: '/case/view-special-transaction-travel' },
    { label: 'list Special Transactions', route: '/case/list-special-transactions' }
  ];

export function CaseCreateSpecialTransactionTravelPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  return (
    <UimPageLayout
      pageId={"Case_createSpecialTransactionTravel"}
      title={"Create Special Transaction - Additional Options:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} />
          <UimField label={"To Date"} />
          <UimField label={"Payee Name"} />
          <UimField label={"Type"} />
        </div>
      </UimSection>
      <UimSection title={"Cluster1.Title.Service.Period"}>
        <div className="uim-form-grid">
          <UimField label={"Hours (HH:MM)"} />
          <UimField label={"Program"} />
          <UimField label={"Amount"} />
          <UimField label={"Rate Override"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateSpecialTransactionTravelPage;
