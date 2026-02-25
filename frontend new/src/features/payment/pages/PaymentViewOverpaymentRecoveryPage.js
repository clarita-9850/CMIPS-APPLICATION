import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'list Overpayment Recoveries', route: '/case/list-overpayment-recoveries' },
    { label: 'list Overpayment Recoveries', route: '/person/list-overpayment-recoveries' },
    { label: 'list Overpayment Recovery History', route: '/case/list-overpayment-recovery-history' },
    { label: 'list Overpayment Recovery Collections', route: '/payment/list-overpayment-recovery-collections' },
    { label: 'submit Overpayment Recovery', route: '/case/submit-overpayment-recovery' },
    { label: 'cancel Overpayment Recovery', route: '/case/cancel-overpayment-recovery' },
    { label: 'stop Overpayment Collection', route: '/case/stop-overpayment-collection' },
    { label: 'edit Overpayment Occurence', route: '/case/edit-overpayment-occurence' },
    { label: 'modify Overpayment Occurence', route: '/case/modify-overpayment-occurence' },
    { label: 'edit Excess Compensation Rate Details', route: '/case/edit-excess-compensation-rate-details' },
    { label: 'edit Advance Pay Overpayment Details', route: '/case/edit-advance-pay-overpayment-details' },
    { label: 'edit Special Tx Overpayment Details', route: '/case/edit-special-tx-overpayment-details' },
    { label: 'edit Overpayment Recovery Setup', route: '/case/edit-overpayment-recovery-setup' }
  ];

export function PaymentViewOverpaymentRecoveryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('payment', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Payment_viewOverpaymentRecovery"}
      title={"View Overpayment Recovery:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Payee Name"} value={record && record['payeeName']} />
          <UimField label={"Overpayment Type"} value={record && record['overpaymentType']} />
          <UimField label={"Special Transaction Number"} value={record && record['specialTransactionNumber']} />
        </div>
      </UimSection>
      <UimSection title={"Occurrence"}>
        <div className="uim-form-grid">
          <UimField label={"Program"} value={record && record['program']} />
          <UimField label={"Reason"} value={record && record['reason']} />
          <UimField label={"Overpayment Number"} value={record && record['overpaymentNumber']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"County"} value={record && record['county']} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"District Office"} value={record && record['districtOffice']} />
          <UimField label={"Overpaid Hours (HH:MM)"} value={record && record['overpaidHoursHHMM']} />
          <UimField label={"Total Net Overpayment"} value={record && record['totalNetOverpayment']} />
        </div>
      </UimSection>
      <UimSection title={"Case"}>
        <div className="uim-form-grid">
          <UimField label={"Paid Rate"} value={record && record['paidRate']} />
          <UimField label={"Correct Rate"} value={record && record['correctRate']} />
          <UimField label={"Overpaid Hours (HH:MM)"} value={record && record['overpaidHoursHHMM']} />
          <UimField label={"Total Gross Overpayment"} value={record && record['totalGrossOverpayment']} />
          <UimField label={"Rate"} value={record && record['rate']} />
        </div>
      </UimSection>
      <UimSection title={"Excess Compensation Rate Details"}>
        <div className="uim-form-grid">
          <UimField label={"Total Recovered Hours (HH:MM)"} value={record && record['totalRecoveredHoursHHMM']} />
          <UimField label={"Overpaid Hours (HH:MM)"} value={record && record['overpaidHoursHHMM']} />
          <UimField label={"Total Overpaid Overtime Hours (HH:MM)"} value={record && record['totalOverpaidOvertimeHoursHHMM']} />
          <UimField label={"Total Net Overpayment"} value={record && record['totalNetOverpayment']} />
          <UimField label={"Total Recovered Hours (HH:MM)"} value={record && record['totalRecoveredHoursHHMM']} />
        </div>
      </UimSection>
      <UimSection title={"Advance Payment Overpayment Details"}>
        <div className="uim-form-grid">
          <UimField label={"Total Recovered Overtime Hours (HH:MM)"} value={record && record['totalRecoveredOvertimeHoursHHMM']} />
          <UimField label={"Recovery Amount"} value={record && record['recoveryAmount']} />
          <UimField label={"Collected to Date"} value={record && record['collectedToDate']} />
          <UimField label={"Balance"} value={record && record['balance']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Special Transaction Overpayment Details"}>
        <div className="uim-form-grid">
          <UimField label={"Stop Date"} value={record && record['stopDate']} />
          <UimField label={"Recovery Case"} value={record && record['recoveryCase']} />
          <UimField label={"Recovery Method"} value={record && record['recoveryMethod']} />
          <UimField label={"Installment Type"} value={record && record['installmentType']} />
          <UimField label={"Recovery Payee"} value={record && record['recoveryPayee']} />
        </div>
      </UimSection>
      <UimSection title={"Recovery Status"}>
        <div className="uim-form-grid">
          <UimField label={"Amount Per Payment"} value={record && record['amountPerPayment']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <UimSection title={"Recovery Setup"}>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Overpayment Recovery')}>View Overpayment Recovery</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/list-overpayment-recovery-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/list-overpayment-recovery-collections')}>Collections</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Submit Recovery')}>Submit Recovery</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Cancel Recovery')}>Cancel Recovery</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Stop Collection')}>Stop Collection</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Occurrence')}>Edit Occurrence</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/edit-excess-compensation-rate-details')}>Edit Excess Compensation Rate Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Advance Payment Overpayment Details')}>Edit Advance Payment Overpayment Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Special Transaction Overpayment Details')}>Edit Special Transaction Overpayment Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Recovery Setup')}>Edit Recovery Setup</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PaymentViewOverpaymentRecoveryPage;
