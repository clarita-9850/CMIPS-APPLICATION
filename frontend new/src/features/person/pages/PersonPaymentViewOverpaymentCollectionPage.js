import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'list Overpayment Recovery Collections', route: '/payment/list-overpayment-recovery-collections' },
    { label: 'cancel Overpayment Collection', route: '/case/cancel-overpayment-collection' }
  ];

export function PersonPaymentViewOverpaymentCollectionPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"PersonPayment_viewOverpaymentCollection"}
      title={"View Overpayment Collection:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Date Collected"} value={record && record['dateCollected']} />
          <UimField label={"Mode Of Payment"} value={record && record['modeOfPayment']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Recorded By"} value={record && record['recordedBy']} />
        </div>
      </UimSection>
      <UimSection title={"Cluster.title.Details"}>
        <div className="uim-form-grid">
          <UimField label={"Amount"} value={record && record['amount']} />
          <UimField label={"Warrant Number / Receipt Number"} value={record && record['warrantNumberReceiptNumber']} />
          <UimField label={"Returned Check"} value={record && record['returnedCheck']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/cancel-overpayment-collection')}>Cancel Overpayment Collection</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonPaymentViewOverpaymentCollectionPage;
