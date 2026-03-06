import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [];

export function PersonViewSpecialTransactionPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewSpecialTransaction"}
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
          <UimField label={"Bypass Hours"} value={record && record['bypassHours']} />
        </div>
      </UimSection>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Taxation"} value={record && record['taxation']} />
          <UimField label={"Bypass Hours"} value={record && record['bypassHours']} />
          <UimField label={"Taxation"} value={record && record['taxation']} />
          <UimField label={"Bypass Hours"} value={record && record['bypassHours']} />
        </div>
      </UimSection>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Refund Hours (HH:MM)"} value={record && record['refundHoursHHMM']} />
          <UimField label={"Submitted By"} value={record && record['submittedBy']} />
          <UimField label={"Approved By/Rejected By"} value={record && record['approvedByRejectedBy']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <UimSection title={"Options"}>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewSpecialTransactionPage;
