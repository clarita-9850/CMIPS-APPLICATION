import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'print Contract Expenditure Report', route: '/county/print-contract-expenditure-report' },
    { label: 'edit County Contractor Invoice', route: '/county/edit-county-contractor-invoice' },
    { label: 'list County Contractor Invoice', route: '/county/list-county-contractor-invoice' }
  ];

export function CountyViewCountyContractorInvoicePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('county', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"County_viewCountyContractorInvoice"}
      title={"View County Contractor:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Invoice Details"}>
        <div className="uim-form-grid">
          <UimField label={"Invoice Number"} value={record && record['invoiceNumber']} />
          <UimField label={"Original Amount"} value={record && record['originalAmount']} />
          <UimField label={"Authorized Amount"} value={record && record['authorizedAmount']} />
          <UimField label={"Invoice Date"} value={record && record['invoiceDate']} />
          <UimField label={"Rejected Amount"} value={record && record['rejectedAmount']} />
          <UimField label={"Warrant Number"} value={record && record['warrantNumber']} />
          <UimField label={"Processed Date"} value={record && record['processedDate']} />
          <UimField label={"Cut Back Amount"} value={record && record['cutBackAmount']} />
          <UimField label={"Paid Date"} value={record && record['paidDate']} />
          <UimField label={"Billing Month"} value={record && record['billingMonth']} />
          <UimField label={"SOC Collected Amount"} value={record && record['sOCCollectedAmount']} />
          <UimField label={"Case Count"} value={record && record['caseCount']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Amount"} value={record && record['amount']} />
        </div>
      </UimSection>
      <UimSection title={"Payment Details"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Contract Expenditures Form')}>Contract Expenditures Form</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CountyViewCountyContractorInvoicePage;
