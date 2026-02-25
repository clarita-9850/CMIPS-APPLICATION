import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Special Transaction', route: '/case/view-special-transaction' },
    { label: 'list Special Transactions', route: '/case/list-special-transactions' }
  ];

export function CaseCreateSpecialTransactionAdditionalOptionsPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  return (
    <UimPageLayout
      pageId={"Case_createSpecialTransactionAdditionalOptions"}
      title={"Create Special Transaction - Additional Options:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} />
          <UimField label={"To Date"} />
          <UimField label={"Payee Name"} />
        </div>
      </UimSection>
      <UimSection title={"Cluster1.Title.Service.Period"}>
        <div className="uim-form-grid">
          <UimField label={"Type"} />
          <UimField label={"Hours (HH:MM)"} />
          <UimField label={"Program"} />
        </div>
      </UimSection>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Amount"} />
          <UimField label={"Rate Override"} />
          <UimField label={"Bypass Hours"} />
        </div>
      </UimSection>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Taxation"} />
          <UimField label={"Bypass Hours"} />
          <UimField label={"Taxation"} />
        </div>
      </UimSection>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Bypass Hours"} />
          <UimField label={"Refund Hours (HH:MM)"} />
          <UimField label={"Comments"} />
        </div>
      </UimSection>
      <UimSection title={"Options"}>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateSpecialTransactionAdditionalOptionsPage;
