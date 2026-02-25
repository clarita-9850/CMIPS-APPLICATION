import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [];

export function PaymentViewOverpaymentRecoveryFromHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('payment', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Payment_viewOverpaymentRecoveryFromHistory"}
      title={"View Overpayment Recovery History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Occurrence"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Payee Name"} value={record && record['payeeName']} />
          <UimField label={"Overpayment Type"} value={record && record['overpaymentType']} />
          <UimField label={"Special Transaction Number"} value={record && record['specialTransactionNumber']} />
          <UimField label={"Program"} value={record && record['program']} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"Reason"} value={record && record['reason']} />
          <UimField label={"Overpayment Number"} value={record && record['overpaymentNumber']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
        </div>
      </UimSection>
      <UimSection title={"Case"}>
        <div className="uim-form-grid">
          <UimField label={"District Office"} value={record && record['districtOffice']} />
          <UimField label={"Total Overpaid Hours (HH:MM)"} value={record && record['totalOverpaidHoursHHMM']} />
          <UimField label={"Total Overpaid Overtime Hours (HH:MM)"} value={record && record['totalOverpaidOvertimeHoursHHMM']} />
          <UimField label={"Total Net Overpayment"} value={record && record['totalNetOverpayment']} />
          <UimField label={"Total Recovered Hours (HH:MM)"} value={record && record['totalRecoveredHoursHHMM']} />
          <UimField label={"Total Recovered Overtime Hours (HH:MM)"} value={record && record['totalRecoveredOvertimeHoursHHMM']} />
        </div>
      </UimSection>
      <UimSection title={"Excess Compensation Hours Details"}>
        <div className="uim-form-grid">
          <UimField label={"Total Overpaid Differential Hours (HH:MM)"} value={record && record['totalOverpaidDifferentialHoursHHMM']} />
          <UimField label={"Total Overpaid Differential Overtime Hours (HH:MM)"} value={record && record['totalOverpaidDifferentialOvertimeHoursHHMM']} />
          <UimField label={"Total Recovered Differential Hours (HH:MM)"} value={record && record['totalRecoveredDifferentialHoursHHMM']} />
          <UimField label={"Total Recovered Differential Overtime Hours (HH:MM)"} value={record && record['totalRecoveredDifferentialOvertimeHoursHHMM']} />
          <UimField label={"Overpaid Hours (HH:MM)"} value={record && record['overpaidHoursHHMM']} />
          <UimField label={"Total Net Overpayment"} value={record && record['totalNetOverpayment']} />
        </div>
      </UimSection>
      <UimSection title={"Excess Compensation  Rate Details"}>
        <div className="uim-form-grid">
          <UimField label={"Paid Rate"} value={record && record['paidRate']} />
          <UimField label={"Correct Rate"} value={record && record['correctRate']} />
          <UimField label={"Overpaid Hours (HH:MM)"} value={record && record['overpaidHoursHHMM']} />
          <UimField label={"Total Gross Overpayment"} value={record && record['totalGrossOverpayment']} />
          <UimField label={"Rate"} value={record && record['rate']} />
          <UimField label={"Total Recovered Hours (HH:MM)"} value={record && record['totalRecoveredHoursHHMM']} />
        </div>
      </UimSection>
      <UimSection title={"Advance Payment Overpayment Details"}>
        <div className="uim-form-grid">
          <UimField label={"Overpaid Hours (HH:MM)"} value={record && record['overpaidHoursHHMM']} />
          <UimField label={"Total Overpaid Overtime Hours (HH:MM)"} value={record && record['totalOverpaidOvertimeHoursHHMM']} />
          <UimField label={"Total Net Overpayment"} value={record && record['totalNetOverpayment']} />
          <UimField label={"Total Recovered Hours (HH:MM)"} value={record && record['totalRecoveredHoursHHMM']} />
          <UimField label={"Total Recovered Overtime Hours (HH:MM)"} value={record && record['totalRecoveredOvertimeHoursHHMM']} />
          <UimField label={"Total Overpaid Differential Hours (HH:MM)"} value={record && record['totalOverpaidDifferentialHoursHHMM']} />
        </div>
      </UimSection>
      <UimSection title={"Special Transaction Overpayment Details"}>
        <div className="uim-form-grid">
          <UimField label={"Total Overpaid Differential Overtime Hours (HH:MM)"} value={record && record['totalOverpaidDifferentialOvertimeHoursHHMM']} />
          <UimField label={"Total Net Overpayment"} value={record && record['totalNetOverpayment']} />
          <UimField label={"Total Recovered Differential Hours (HH:MM)"} value={record && record['totalRecoveredDifferentialHoursHHMM']} />
          <UimField label={"Total Recovered Differential Overtime Hours (HH:MM)"} value={record && record['totalRecoveredDifferentialOvertimeHoursHHMM']} />
          <UimField label={"Recovery Amount"} value={record && record['recoveryAmount']} />
          <UimField label={"Collected to Date"} value={record && record['collectedToDate']} />
        </div>
      </UimSection>
      <UimSection title={"Special Transaction Overpayment Details"}>
        <div className="uim-form-grid">
          <UimField label={"Balance"} value={record && record['balance']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Stop Date"} value={record && record['stopDate']} />
          <UimField label={"Recovery Case"} value={record && record['recoveryCase']} />
          <UimField label={"Recovery Method"} value={record && record['recoveryMethod']} />
          <UimField label={"Installment Type"} value={record && record['installmentType']} />
        </div>
      </UimSection>
      <UimSection title={"Recovery Status"}>
        <div className="uim-form-grid">
          <UimField label={"Recovery Payee"} value={record && record['recoveryPayee']} />
          <UimField label={"Amount Per Payment"} value={record && record['amountPerPayment']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <UimSection title={"Recovery Setup"}>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PaymentViewOverpaymentRecoveryFromHistoryPage;
