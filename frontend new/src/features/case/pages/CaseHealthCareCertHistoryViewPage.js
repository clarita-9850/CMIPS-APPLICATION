import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'health Care Cert History', route: '/case/health-care-cert-history' }
  ];

export function CaseHealthCareCertHistoryViewPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_healthCareCertHistoryView"}
      title={"Health Care Certification History Details :"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"History Details"}>
        <div className="uim-form-grid">
          <UimField label={"SOC 873 & 874 Print Date"} value={record && record['sOC873874PrintDate']} />
          <UimField label={"Due Date"} value={record && record['dueDate']} />
          <UimField label={"SOC 873 & 874 Mailed/Given To Recipient"} value={record && record['sOC873874MailedGivenToRecipient']} />
          <UimField label={"SOC 873 & 874 Given To Recipient Entered Date"} value={record && record['sOC873874GivenToRecipientEnteredDate']} />
          <UimField label={"Exception Granted Date"} value={record && record['exceptionGrantedDate']} />
          <UimField label={"Exception Date Entered Date"} value={record && record['exceptionDateEnteredDate']} />
          <UimField label={"Good Cause Extension Date"} value={record && record['goodCauseExtensionDate']} />
          <UimField label={"Good Cause Extension Date Entered Date"} value={record && record['goodCauseExtensionDateEnteredDate']} />
          <UimField label={"Health Care Certification Type"} value={record && record['healthCareCertificationType']} />
          <UimField label={"Good Cause Extension Due Date"} value={record && record['goodCauseExtensionDueDate']} />
          <UimField label={"Documentation Received Date"} value={record && record['documentationReceivedDate']} />
          <UimField label={"Documentation Received Entered Date"} value={record && record['documentationReceivedEnteredDate']} />
          <UimField label={"No Form Printed"} value={record && record['noFormPrinted']} />
          <UimField label={"Created By"} value={record && record['createdBy']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"Last Update Date"} value={record && record['lastUpdateDate']} />
          <UimField label={"Last Update By"} value={record && record['lastUpdateBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseHealthCareCertHistoryViewPage;
