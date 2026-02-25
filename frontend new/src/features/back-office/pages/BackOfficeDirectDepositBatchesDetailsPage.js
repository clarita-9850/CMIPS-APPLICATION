import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function BackOfficeDirectDepositBatchesDetailsPage() {
  const navigate = useNavigate();
  const backOfficeApi = getDomainApi('back-office');
  const { data, loading, error } = useDomainData('back-office', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"BackOffice_DirectDepositBatchesDetails"}
      title={"Batch Name:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Employee ID"} value={record && record['employeeID']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Routing Number"} value={record && record['routingNumber']} />
          <UimField label={"Account Number"} value={record && record['accountNumber']} />
          <UimField label={"Account Type"} value={record && record['accountType']} />
          <UimField label={"Bank Name"} value={record && record['bankName']} />
          <UimField label={"Rejected"} value={record && record['rejected']} />
          <UimField label={"Processed"} value={record && record['processed']} />
          <UimField label={"Processed Date"} value={record && record['processedDate']} />
          <UimField label={"OperatorID"} value={record && record['operatorID']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { backOfficeApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
      </div>
    </UimPageLayout>
  );
}

export default BackOfficeDirectDepositBatchesDetailsPage;
