import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function CaseModifyHealthCareCertFormGenPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_modifyHealthCareCertFormGen"}
      title={"Modify Health Care Certification:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Modify Health Care Certification-Form Printed"}>
        <div className="uim-form-grid">
          <UimField label={"SOC 873 & 874 Print Date"} value={record && record['sOC873874PrintDate']} />
          <UimField label={"SOC 873 & 874 Mailed/Given To Recipient"} value={record && record['sOC873874MailedGivenToRecipient']} />
          <UimField label={"Exception Granted Date"} value={record && record['exceptionGrantedDate']} />
          <UimField label={"Good Cause Extension Date"} value={record && record['goodCauseExtensionDate']} />
          <UimField label={"Health Care Certification Type"} value={record && record['healthCareCertificationType']} />
          <UimField label={"Documentation Received Date"} value={record && record['documentationReceivedDate']} />
          <UimField label={"Due Date"} value={record && record['dueDate']} />
          <UimField label={"SOC 873 & 874 Given To Recipient Entered Date"} value={record && record['sOC873874GivenToRecipientEnteredDate']} />
          <UimField label={"Exception Date Entered Date"} value={record && record['exceptionDateEnteredDate']} />
          <UimField label={"Good Cause Extension Date Entered Date"} value={record && record['goodCauseExtensionDateEnteredDate']} />
          <UimField label={"Good Cause Extension Due Date"} value={record && record['goodCauseExtensionDueDate']} />
          <UimField label={"Documentation Received Entered Date"} value={record && record['documentationReceivedEnteredDate']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseModifyHealthCareCertFormGenPage;
