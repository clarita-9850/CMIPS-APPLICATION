import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Provider', route: '/case/view-provider' },
    { label: 'search I H S S Provider Hours History', route: '/case/search-ihss-provider-hours-history' },
    { label: 'search W P C S Provider Hours History', route: '/case/search-wpcs-provider-hours-history' },
    { label: 'view F P O Eligibility', route: '/case/view-fpo-eligibility' },
    { label: 'home Page', route: '/person/home-page' },
    { label: 'create Provider W P C S Hours', route: '/case/create-provider-wpcs-hours' },
    { label: 'leave Terminate Provider', route: '/case/leave-terminate-provider' },
    { label: 'view W P C S D P R Hours History', route: '/case/view-wpcsdpr-hours-history' },
    { label: 'modify Provider W P C S Hours', route: '/case/modify-provider-wpcs-hours' }
  ];

export function CaseViewProviderWPCSHoursPage() {
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewProviderWPCSHours"}
      title={"View WPCS Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Provider Status"} value={record && record['providerStatus']} />
          <UimField label={"Relationship to Recipient"} value={record && record['relationshipToRecipient']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"Termination Reason"} value={record && record['terminationReason']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Relationship Status Date"} value={record && record['relationshipStatusDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Termination Comment"} value={record && record['terminationComment']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Provider Type"} value={record && record['providerType']} />
          <UimField label={"WPCS Authorized Hours"} value={record && record['wPCSAuthorizedHours']} />
          <UimField label={"Pay Rate"} value={record && record['payRate']} />
          <UimField label={"Differential Pay Rate"} value={record && record['differentialPayRate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Case Provider')}>View Case Provider</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View WPCS Details')}>View WPCS Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View IHSS Provider Hours History')}>View IHSS Provider Hours History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View WPCS Provider Hours History')}>View WPCS Provider Hours History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: FPO Eligible')}>FPO Eligible</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Leave/Terminate')}>Leave/Terminate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Differential Pay Rate Hours History')}>Differential Pay Rate Hours History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewProviderWPCSHoursPage;
