import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'edit Special Transaction', route: '/case/edit-special-transaction' },
    { label: 'list Special Transactions', route: '/case/list-special-transactions' }
  ];

export function CaseViewSpecialTransactionPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewSpecialTransaction"}
      title={"View Special Transaction:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Payee Name"} value={record && record['payeeName']} />
          <UimField label={"Type"} value={record && record['type']} />
        </div>
      </UimSection>
      <UimSection title={"Cluster1.Title.Service.Period"}>
        <div className="uim-form-grid">
          <UimField label={"Hours (HH:MM)"} value={record && record['hoursHHMM']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Program"} value={record && record['program']} />
          <UimField label={"Amount"} value={record && record['amount']} />
        </div>
      </UimSection>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Rate Override"} value={record && record['rateOverride']} />
          <UimField label={"Special Transaction Number"} value={record && record['specialTransactionNumber']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Hours Paid at Overtime Rate"} value={record && record['hoursPaidAtOvertimeRate']} />
        </div>
      </UimSection>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Bypass Hours"} value={record && record['bypassHours']} />
          <UimField label={"Taxation"} value={record && record['taxation']} />
          <UimField label={"Bypass Hours"} value={record && record['bypassHours']} />
          <UimField label={"Taxation"} value={record && record['taxation']} />
        </div>
      </UimSection>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Bypass Hours"} value={record && record['bypassHours']} />
          <UimField label={"Refund Hours (HH:MM)"} value={record && record['refundHoursHHMM']} />
          <UimField label={"Submitted By"} value={record && record['submittedBy']} />
          <UimField label={"Approved By/Rejected By"} value={record && record['approvedByRejectedBy']} />
        </div>
      </UimSection>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Comments"} value={record && record['comments']} />
          <UimField label={"Code"} value={record && record['code']} />
          <UimField label={"Description"} value={record && record['description']} />
        </div>
      </UimSection>
      <UimSection title={"Exceptions"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewSpecialTransactionPage;
