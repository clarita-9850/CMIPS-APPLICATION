import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function CaseEditOverpaymentCollectionPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_editOverpaymentCollection"}
      title={"Modify Overpayment Collection:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Date Collected"} value={record && record['dateCollected']} />
          <UimField label={"Mode Of Payment"} value={record && record['modeOfPayment']} />
          <UimField label={"Amount"} value={record && record['amount']} />
          <UimField label={"Receipt Number"} value={record && record['receiptNumber']} />
          <UimField label={"Date Collected"} value={record && record['dateCollected']} />
          <UimField label={"Mode Of Payment"} value={record && record['modeOfPayment']} />
          <UimField label={"Amount"} value={record && record['amount']} />
          <UimField label={"Receipt Number"} value={record && record['receiptNumber']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Recorded By"} value={record && record['recordedBy']} />
          <UimField label={"Returned Check"} value={record && record['returnedCheck']} />
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

export default CaseEditOverpaymentCollectionPage;
