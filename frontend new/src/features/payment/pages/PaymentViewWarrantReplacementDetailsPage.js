import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function PaymentViewWarrantReplacementDetailsPage() {
  const navigate = useNavigate();
  const timesheetsApi = getDomainApi('payment');
  const { data, loading, error } = useDomainData('payment', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Payment_viewWarrantReplacementDetails"}
      title={"Enter Warrant Replacement - Details"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Payee"}>
        <div className="uim-form-grid">
          <UimField label={"Payee Number"} value={record && record['payeeNumber']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"County"} value={record && record['county']} />
        </div>
      </UimSection>
      <UimSection title={"Case"}>
        <div className="uim-form-grid">
          <UimField label={"Warrant Number"} value={record && record['warrantNumber']} />
          <UimField label={"Issue Date"} value={record && record['issueDate']} />
          <UimField label={"Replacement Date"} value={record && record['replacementDate']} />
        </div>
      </UimSection>
      <UimSection title={"Payment"}>
        <div className="uim-form-grid">
          <UimField label={"Net Amount"} value={record && record['netAmount']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
          <UimField label={"Pay Type"} value={record && record['payType']} />
        </div>
      </UimSection>
      <UimSection title={"Warrant Information"}>
        <div className="uim-form-grid">
          <UimField label={"From"} value={record && record['from']} />
          <UimField label={"To"} value={record && record['to']} />
        </div>
      </UimSection>
      <UimSection title={"Pay Event"}>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { timesheetsApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Save &amp; New')}>Save &amp; New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PaymentViewWarrantReplacementDetailsPage;
