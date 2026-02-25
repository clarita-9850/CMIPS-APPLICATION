import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'list Payment History', route: '/payment/list-payment-history' },
    { label: 'list Void And Reissues', route: '/payment/list-void-and-reissues' },
    { label: 'list Cashed Warrant Copy Requests', route: '/payment/list-cashed-warrant-copy-requests' },
    { label: 'list Forged Endorsement Affidavits', route: '/payment/list-forged-endorsement-affidavits' },
    { label: 'view Advance Pay Recon', route: '/payment/view-advance-pay-recon' },
    { label: 'view Employer Paid Taxes', route: '/payment/view-employer-paid-taxes' },
    { label: 'view Travel Claim', route: '/payment/view-travel-claim' },
    { label: 'view C P Claim', route: '/payment/view-cp-claim' },
    { label: 'view Timesheets', route: '/payment/view-timesheets' },
    { label: 'view Warrant Source Information', route: '/payment/view-warrant-source-information' }
  ];

export function PaymentViewPaymentDetailsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('payment', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Payment_viewPaymentDetails"}
      title={"View Payment Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Payee"}>
        <div className="uim-form-grid">
          <UimField label={"Payee Number"} value={record && record['payeeNumber']} />
          <UimField label={"Tax Relationship"} value={record && record['taxRelationship']} />
          <UimField label={"W-4 Allowances"} value={record && record['w4Allowances']} />
          <UimField label={"W-4 Status"} value={record && record['w4Status']} />
          <UimField label={"EIC Status"} value={record && record['eICStatus']} />
        </div>
      </UimSection>
      <UimSection title={"Address"}>
        <div className="uim-form-grid">
          <UimField label={"DE-4 Allowances"} value={record && record['dE4Allowances']} />
          <UimField label={"DE-4 Status"} value={record && record['dE4Status']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"District Office"} value={record && record['districtOffice']} />
        </div>
      </UimSection>
      <UimSection title={"Case"}>
        <div className="uim-form-grid">
          <UimField label={"Warrant Number"} value={record && record['warrantNumber']} />
          <UimField label={"Issue Date"} value={record && record['issueDate']} />
          <UimField label={"Pay Status"} value={record && record['payStatus']} />
          <UimField label={"EFT"} value={record && record['eFT']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
        </div>
      </UimSection>
      <UimSection title={"Payment"}>
        <div className="uim-form-grid">
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Reason"} value={record && record['reason']} />
          <UimField label={"Replacement Date"} value={record && record['replacementDate']} />
          <UimField label={"Pay Type"} value={record && record['payType']} />
        </div>
      </UimSection>
      <UimSection title={"Warrant Information"}>
        <div className="uim-form-grid">
          <UimField label={"Case Hours Paid (HH:MM)"} value={record && record['caseHoursPaidHHMM']} />
          <UimField label={"Travel Hours Paid (HH:MM)"} value={record && record['travelHoursPaidHHMM']} />
          <UimField label={"Sick Leave Hours Paid (HH:MM)"} value={record && record['sickLeaveHoursPaidHHMM']} />
          <UimField label={"Training Hours Paid (HH:MM)"} value={record && record['trainingHoursPaidHHMM']} />
          <UimField label={"Total Hours Paid(HH:MM)"} value={record && record['totalHoursPaidHHMM']} />
        </div>
      </UimSection>
      <UimSection title={"Void/Reissue/Replacement Action"}>
        <div className="uim-form-grid">
          <UimField label={"Pay Rate"} value={record && record['payRate']} />
          <UimField label={"From"} value={record && record['from']} />
          <UimField label={"To"} value={record && record['to']} />
          <UimField label={"Case Hours Not Paid (HH:MM)"} value={record && record['caseHoursNotPaidHHMM']} />
          <UimField label={"Travel Hours Not Paid(HH:MM)"} value={record && record['travelHoursNotPaidHHMM']} />
        </div>
      </UimSection>
      <UimSection title={"Pay Event"}>
        <div className="uim-form-grid">
          <UimField label={"Sick Leave Hours Not Paid (HH:MM)"} value={record && record['sickLeaveHoursNotPaidHHMM']} />
          <UimField label={"Training Hours Not Paid (HH:MM)"} value={record && record['trainingHoursNotPaidHHMM']} />
          <UimField label={"Hours Paid as Overtime(HH:MM)"} value={record && record['hoursPaidAsOvertimeHHMM']} />
          <UimField label={"Overtime Pay Rate"} value={record && record['overtimePayRate']} />
          <UimField label={"Current"} value={record && record['current']} />
        </div>
      </UimSection>
      <UimSection title={"Earnings Statement"}>
        <div className="uim-form-grid">
          <UimField label={"Year-to-Date"} value={record && record['yearToDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Payment Details')}>Payment Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/list-payment-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Void/Reissue/Replacement Activity')}>Void/Reissue/Replacement Activity</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Cashed Warrant Copies')}>Cashed Warrant Copies</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/list-forged-endorsement-affidavits')}>Forged Endorsement Affidavits</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Advance Pay Reconciliation')}>Advance Pay Reconciliation</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-employer-paid-taxes')}>Employer Paid Taxes</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-travel-claim')}>View Travel Claim Information</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Training Time Claim Information')}>View Training Time Claim Information</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Timesheet Information')}>View Timesheet Information</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-warrant-source-information')}>Warrant Source Information</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PaymentViewPaymentDetailsPage;
