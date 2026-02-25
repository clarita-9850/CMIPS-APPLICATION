import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'resolve View Warrant Details', route: '/misc/action-resolve-view-warrant-details' }
  ];

export function PaymentViewWarrantSourceInformationPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('payment', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Payment_viewWarrantSourceInformation"}
      title={"Warrant Source Information:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Warrant Source Number"} value={record && record['warrantSourceNumber']} />
          <UimField label={"Warrant Source Type"} value={record && record['warrantSourceType']} />
          <UimField label={"Pay Type"} value={record && record['payType']} />
          <UimField label={"Pay Period From"} value={record && record['payPeriodFrom']} />
          <UimField label={"Hours Paid"} value={record && record['hoursPaid']} />
          <UimField label={"Pay Rate"} value={record && record['payRate']} />
          <UimField label={"Gross"} value={record && record['gross']} />
        </div>
      </UimSection>
      <UimSection title={"Warrant Source Information"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/misc/action-resolve-view-warrant-details')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default PaymentViewWarrantSourceInformationPage;
