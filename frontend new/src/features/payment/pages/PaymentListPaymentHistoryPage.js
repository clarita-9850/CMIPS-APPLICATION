import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Payment Details', route: '/payment/view-payment-details' },
    { label: 'list Void And Reissues', route: '/payment/list-void-and-reissues' },
    { label: 'list Cashed Warrant Copy Requests', route: '/payment/list-cashed-warrant-copy-requests' },
    { label: 'list Forged Endorsement Affidavits', route: '/payment/list-forged-endorsement-affidavits' }
  ];

export function PaymentListPaymentHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('payment', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Payment_listPaymentHistory"}
      title={"Payment History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Warrant Number"} value={record && record['warrantNumber']} />
          <UimField label={"Issue Date"} value={record && record['issueDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Void Type"} value={record && record['voidType']} />
          <UimField label={"Void Reason"} value={record && record['voidReason']} />
          <UimField label={"Replacement Date"} value={record && record['replacementDate']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
        </div>
      </UimSection>
      <UimSection title={"History"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-payment-details')}>Payment Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: History')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Void/Reissue/Replacement Activity')}>Void/Reissue/Replacement Activity</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Cashed Warrant Copies')}>Cashed Warrant Copies</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/list-forged-endorsement-affidavits')}>Forged Endorsement Affidavits</button>
      </div>
    </UimPageLayout>
  );
}

export default PaymentListPaymentHistoryPage;
