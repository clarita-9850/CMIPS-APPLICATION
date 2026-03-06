import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Payment Details', route: '/payment/view-payment-details' },
    { label: 'list Payment History', route: '/payment/list-payment-history' },
    { label: 'list Void And Reissues', route: '/payment/list-void-and-reissues' },
    { label: 'list Forged Endorsement Affidavits', route: '/payment/list-forged-endorsement-affidavits' },
    { label: 'request Cashed Warrant Copy', route: '/case/request-cashed-warrant-copy' },
    { label: 'edit Cashed Warrant Copy Request', route: '/case/edit-cashed-warrant-copy-request' },
    { label: 'cancel Cashed Warrant Copy Request', route: '/case/cancel-cashed-warrant-copy-request' }
  ];

export function PaymentListCashedWarrantCopyRequestsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('payment', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Payment_listCashedWarrantCopyRequests"}
      title={"Cashed Warrant Copy Requests:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Cashed Warrant Copies"}>
        <div className="uim-form-grid">
          <UimField label={"Request Date"} value={record && record['requestDate']} />
          <UimField label={"Requested By"} value={record && record['requestedBy']} />
          <UimField label={"Received Date"} value={record && record['receivedDate']} />
          <UimField label={"Cancelled"} value={record && record['cancelled']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-payment-details')}>Payment Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/list-payment-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Void/Reissue/Replacement Activity')}>Void/Reissue/Replacement Activity</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Cashed Warrant Copies')}>Cashed Warrant Copies</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/list-forged-endorsement-affidavits')}>Forged Endorsement Affidavits</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Cancel Request')}>Cancel Request</button>
      </div>
    </UimPageLayout>
  );
}

export default PaymentListCashedWarrantCopyRequestsPage;
