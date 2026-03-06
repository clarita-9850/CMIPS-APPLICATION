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
    { label: 'list Cashed Warrant Copy Requests', route: '/payment/list-cashed-warrant-copy-requests' },
    { label: 'submit Forged Endorsement Affidavit', route: '/case/submit-forged-endorsement-affidavit' },
    { label: 'print Forged Endorsement Affidavit Form', route: '/case/print-forged-endorsement-affidavit-form' },
    { label: 'edit Forged Endorsement Affidavit', route: '/case/edit-forged-endorsement-affidavit' },
    { label: 'cancel Forged Endorsement Affidavit', route: '/case/cancel-forged-endorsement-affidavit' }
  ];

export function PaymentListForgedEndorsementAffidavitsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('payment', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Payment_listForgedEndorsementAffidavits"}
      title={"Forged Endorsement Affidavits:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Forged Endorsement Affidavits"}>
        <div className="uim-form-grid">
          <UimField label={"Signed Date"} value={record && record['signedDate']} />
          <UimField label={"Submitted Date"} value={record && record['submittedDate']} />
          <UimField label={"SCO Response"} value={record && record['sCOResponse']} />
          <UimField label={"SCO Response Date"} value={record && record['sCOResponseDate']} />
          <UimField label={"Cancelled"} value={record && record['cancelled']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-payment-details')}>Payment Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/list-payment-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Void/Reissue/Replacement Activity')}>Void/Reissue/Replacement Activity</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Cashed Warrant Copies')}>Cashed Warrant Copies</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Forged Endorsement Affidavits')}>Forged Endorsement Affidavits</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/print-forged-endorsement-affidavit-form')}>Print Forged Endorsement Affidavit Form</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Cancel Affidavit')}>Cancel Affidavit</button>
      </div>
    </UimPageLayout>
  );
}

export default PaymentListForgedEndorsementAffidavitsPage;
