import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Provider W P C S Hours', route: '/case/view-provider-wpcs-hours' },
    { label: 'search I H S S Provider Hours History', route: '/case/search-ihss-provider-hours-history' },
    { label: 'search W P C S Provider Hours History', route: '/case/search-wpcs-provider-hours-history' },
    { label: 'view F P O Eligibility', route: '/case/view-fpo-eligibility' },
    { label: 'home Page', route: '/person/home-page' },
    { label: 'view Recipient Waiver Popup', route: '/case/view-recipient-waiver-popup' },
    { label: 'modify Provider For View', route: '/case/modify-provider-for-view' },
    { label: 'list Provider', route: '/case/list-provider' },
    { label: 'create Provider Hours', route: '/case/create-provider-hours' },
    { label: 'leave Terminate Provider', route: '/case/leave-terminate-provider' },
    { label: 'view I H S S D P R Hours History', route: '/case/view-ihssdpr-hours-history' },
    { label: 'modify Provider Hours', route: '/case/modify-provider-hours' }
  ];

export function CaseViewProviderPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewProvider"}
      title={"View Case Provider:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Provider Status"} value={record && record['providerStatus']} />
          <UimField label={"Timesheet Review"} value={record && record['timesheetReview']} />
          <UimField label={"Relationship to Recipient"} value={record && record['relationshipToRecipient']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"Termination Reason"} value={record && record['terminationReason']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"IRS Live-In Self-Certification Status"} value={record && record['iRSLiveInSelfCertificationStatus']} />
          <UimField label={"EVV Effective Date"} value={record && record['eVVEffectiveDate']} />
          <UimField label={"Provider Assigned Hours Form"} value={record && record['providerAssignedHoursForm']} />
          <UimField label={"Relationship Status Date"} value={record && record['relationshipStatusDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Termination Comment"} value={record && record['terminationComment']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
          <UimField label={"Recipient Waiver Begin Date"} value={record && record['recipientWaiverBeginDate']} />
          <UimField label={"IRS Live-In Self-Certification Status Date"} value={record && record['iRSLiveInSelfCertificationStatusDate']} />
          <UimField label={"IRS Live-In Mode of Entry"} value={record && record['iRSLiveInModeOfEntry']} />
          <UimField label={"County Use Comments"} value={record && record['countyUseComments']} />
          <UimField label={"W-4 Status"} value={record && record['w4Status']} />
          <UimField label={"W-4 Allowance"} value={record && record['w4Allowance']} />
          <UimField label={"W-4 Amount"} value={record && record['w4Amount']} />
          <UimField label={"W-4 Last Updated"} value={record && record['w4LastUpdated']} />
          <UimField label={"EIC"} value={record && record['eIC']} />
          <UimField label={"EIC Begin Year"} value={record && record['eICBeginYear']} />
        </div>
      </UimSection>
      <UimSection title={"Financial"}>
        <div className="uim-form-grid">
          <UimField label={"EIC Expiration Date"} value={record && record['eICExpirationDate']} />
          <UimField label={"W-2 Issued"} value={record && record['w2Issued']} />
          <UimField label={"W-2 Reprinted"} value={record && record['w2Reprinted']} />
          <UimField label={"DE-4 Status"} value={record && record['dE4Status']} />
          <UimField label={"DE-4 Allowance"} value={record && record['dE4Allowance']} />
          <UimField label={"DE-4 Amount"} value={record && record['dE4Amount']} />
          <UimField label={"DE-4 Last Updated"} value={record && record['dE4LastUpdated']} />
          <UimField label={"W-2C Issued"} value={record && record['w2CIssued']} />
          <UimField label={"W-2C Reprinted"} value={record && record['w2CReprinted']} />
          <UimField label={"Elective SDI"} value={record && record['electiveSDI']} />
          <UimField label={"SDI Begin Date"} value={record && record['sDIBeginDate']} />
          <UimField label={"SDI End Date"} value={record && record['sDIEndDate']} />
          <UimField label={"W-4 Exemption Amount"} value={record && record['w4ExemptionAmount']} />
          <UimField label={"CalSavers Status"} value={record && record['calSaversStatus']} />
          <UimField label={"CalSavers Amount"} value={record && record['calSaversAmount']} />
          <UimField label={"CalSavers Last Updated"} value={record && record['calSaversLastUpdated']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Provider Type"} value={record && record['providerType']} />
          <UimField label={"Assigned Hours"} value={record && record['assignedHours']} />
          <UimField label={"Pay Rate"} value={record && record['payRate']} />
          <UimField label={"Differential Pay Rate"} value={record && record['differentialPayRate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
        </div>
      </UimSection>
      <UimSection title={"Provider Hours"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Case Provider')}>View Case Provider</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View WPCS Details')}>View WPCS Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View IHSS Provider Hours History')}>View IHSS Provider Hours History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View WPCS Provider Hours History')}>View WPCS Provider Hours History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: FPO Eligible')}>FPO Eligible</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Leave/Terminate')}>Leave/Terminate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Differential Pay Rate Hours History')}>Differential Pay Rate Hours History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewProviderPage;
