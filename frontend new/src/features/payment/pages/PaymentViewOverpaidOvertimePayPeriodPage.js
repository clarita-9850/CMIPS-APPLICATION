import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [];

export function PaymentViewOverpaidOvertimePayPeriodPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('payment', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Payment_viewOverpaidOvertimePayPeriod"}
      title={"View Overpayment Pay Period :"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Period"} value={record && record['servicePeriod']} />
          <UimField label={"Recovered Hours (HH:MM)"} value={record && record['recoveredHoursHHMM']} />
          <UimField label={"Recovered Differential Hours (HH:MM)"} value={record && record['recoveredDifferentialHoursHHMM']} />
          <UimField label={"Program"} value={record && record['program']} />
          <UimField label={"Recovered Overtime Hours (HH:MM)"} value={record && record['recoveredOvertimeHoursHHMM']} />
          <UimField label={"Recovered Differential Overtime Hours (HH:MM)"} value={record && record['recoveredDifferentialOvertimeHoursHHMM']} />
          <UimField label={"No Overpaid Hours for this Pay Period"} value={record && record['noOverpaidHoursForThisPayPeriod']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PaymentViewOverpaidOvertimePayPeriodPage;
