import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'edit State Buy Out Payment', route: '/case/edit-state-buy-out-payment' }
  ];

export function MediCalListStateBuyOutPaymentPage() {
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"MediCal_listStateBuyOutPayment"}
      title={"State Buy-Out Payment Management:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Conversion Date"} value={record && record['conversionDate']} />
          <UimField label={"Termination Date"} value={record && record['terminationDate']} />
          <UimField label={"Termination Reason"} value={record && record['terminationReason']} />
          <UimField label={"Rescission Date"} value={record && record['rescissionDate']} />
          <UimField label={"Rescission Reason"} value={record && record['rescissionReason']} />
          <UimField label={"User"} value={record && record['user']} />
        </div>
      </UimSection>
      <UimSection title={"State Buy-Out Payment Management List"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default MediCalListStateBuyOutPaymentPage;
