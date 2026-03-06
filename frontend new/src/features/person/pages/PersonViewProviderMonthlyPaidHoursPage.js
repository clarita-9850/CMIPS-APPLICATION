import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'resolve View Hours', route: '/misc/action-resolve-view-hours' },
    { label: 'provider Monthly Paid Hours', route: '/person/provider-monthly-paid-hours' }
  ];

export function PersonViewProviderMonthlyPaidHoursPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewProviderMonthlyPaidHours"}
      title={"View Provider Monthly Paid Hours:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient"} value={record && record['recipient']} />
          <UimField label={"Time Entry Type"} value={record && record['timeEntryType']} />
          <UimField label={"Transaction Number"} value={record && record['transactionNumber']} />
          <UimField label={"Program"} value={record && record['program']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Service Period Begin Date"} value={record && record['servicePeriodBeginDate']} />
          <UimField label={"Processed Date"} value={record && record['processedDate']} />
          <UimField label={"Claimed Hours"} value={record && record['claimedHours']} />
          <UimField label={"Paid Hours"} value={record && record['paidHours']} />
          <UimField label={"Ineligible Hours Cutback"} value={record && record['ineligibleHoursCutback']} />
          <UimField label={"Exemption Cutback"} value={record && record['exemptionCutback']} />
          <UimField label={"Overpayment Recovery"} value={record && record['overpaymentRecovery']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewProviderMonthlyPaidHoursPage;
