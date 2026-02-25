import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Timesheet', route: '/person/view-timesheet' },
    { label: 'view Payment Correction Pop Up', route: '/person/view-payment-correction-pop-up' }
  ];

export function PersonViewPaymentCorrectionListPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewPaymentCorrectionList"}
      title={"View Payment Corrections List:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"TimeAdjustment Number"} value={record && record['timeAdjustmentNumber']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Timesheet Number"} value={record && record['timesheetNumber']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Timesheet Type"} value={record && record['timesheetType']} />
          <UimField label={"Correction Type"} value={record && record['correctionType']} />
          <UimField label={"Total Hours"} value={record && record['totalHours']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewPaymentCorrectionListPage;
