import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Overpayment Recovery', route: '/person/payment-view-overpayment-recovery' },
    { label: 'list Overpayment Recovery History', route: '/person/list-overpayment-recovery-history' },
    { label: 'create Overpayment Collection', route: '/case/create-overpayment-collection' },
    { label: 'view Overpayment Collection', route: '/payment/view-overpayment-collection' },
    { label: 'edit Overpayment Collection', route: '/case/edit-overpayment-collection' },
    { label: 'list Overpayment Recoveries', route: '/case/list-overpayment-recoveries' },
    { label: 'list Overpayment Recoveries', route: '/person/list-overpayment-recoveries' }
  ];

export function PersonPaymentListOverpaymentRecoveryCollectionsPage() {
  const navigate = useNavigate();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"PersonPayment_listOverpaymentRecoveryCollections"}
      title={"Overpayment Recovery Collections:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Date Collected"} value={record && record['dateCollected']} />
        </div>
      </UimSection>
      <UimSection title={"Cluster1.Title.DateCollected"}>
        <div className="uim-form-grid">
          <UimField label={"Amount"} value={record && record['amount']} />
          <UimField label={"Warrant Number / Receipt Number"} value={record && record['warrantNumberReceiptNumber']} />
          <UimField label={"Mode Of Payment"} value={record && record['modeOfPayment']} />
        </div>
      </UimSection>
      <UimSection title={"Collections"}>
        <div className="uim-form-grid">
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date Collected"} value={record && record['dateCollected']} />
          <UimField label={"Amount"} value={record && record['amount']} />
        </div>
      </UimSection>
      <UimSection title={"Collections"}>
        <div className="uim-form-grid">
          <UimField label={"Warrant Number / Receipt Number"} value={record && record['warrantNumberReceiptNumber']} />
          <UimField label={"Mode Of Payment"} value={record && record['modeOfPayment']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/payment-view-overpayment-recovery')}>View Overpayment Recovery</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/list-overpayment-recovery-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Collections')}>Collections</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/payment-view-overpayment-recovery')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/payment-view-overpayment-recovery')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonPaymentListOverpaymentRecoveryCollectionsPage;
