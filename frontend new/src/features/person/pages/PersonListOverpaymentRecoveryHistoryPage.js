import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Overpayment Recovery', route: '/person/payment-view-overpayment-recovery' },
    { label: 'list Overpayment Recovery Collections', route: '/person/payment-list-overpayment-recovery-collections' },
    { label: 'view Overpayment Recovery From History', route: '/payment/view-overpayment-recovery-from-history' },
    { label: 'list Overpayment Recoveries', route: '/person/list-overpayment-recoveries' }
  ];

export function PersonListOverpaymentRecoveryHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listOverpaymentRecoveryHistory"}
      title={"Overpayment Recovery History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Service To"} value={record && record['serviceTo']} />
          <UimField label={"Total Overpaid Hours"} value={record && record['totalOverpaidHours']} />
          <UimField label={"Total Overpaid Overtime Hours"} value={record && record['totalOverpaidOvertimeHours']} />
          <UimField label={"Recovery Amount"} value={record && record['recoveryAmount']} />
          <UimField label={"Balance"} value={record && record['balance']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/payment-view-overpayment-recovery')}>View Overpayment Recovery</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-overpayment-recovery-from-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/payment-list-overpayment-recovery-collections')}>Collections</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/payment-view-overpayment-recovery')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonListOverpaymentRecoveryHistoryPage;
