import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Payment Details', route: '/payment/view-payment-details' },
    { label: 'list Payment History', route: '/payment/list-payment-history' },
    { label: 'list Cashed Warrant Copy Requests', route: '/payment/list-cashed-warrant-copy-requests' },
    { label: 'list Forged Endorsement Affidavits', route: '/payment/list-forged-endorsement-affidavits' },
    { label: 'request Void Or Reissue', route: '/case/request-void-or-reissue' },
    { label: 'cancel Void Or Reissue Request', route: '/case/cancel-void-or-reissue-request' }
  ];

export function PaymentListVoidAndReissuesPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('payment', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Payment_listVoidAndReissues"}
      title={"Payment Void/Reissue/Replacement Activity:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Voids/Reissues/Replacements"}>
        <div className="uim-form-grid">
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Reason"} value={record && record['reason']} />
          <UimField label={"Request Date"} value={record && record['requestDate']} />
          <UimField label={"Requested By"} value={record && record['requestedBy']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-payment-details')}>Payment Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/list-payment-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Void/Reissue/Replacement Activity')}>Void/Reissue/Replacement Activity</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Cashed Warrant Copies')}>Cashed Warrant Copies</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/list-forged-endorsement-affidavits')}>Forged Endorsement Affidavits</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Cancel Request')}>Cancel Request</button>
      </div>
    </UimPageLayout>
  );
}

export default PaymentListVoidAndReissuesPage;
