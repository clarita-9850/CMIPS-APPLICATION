import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function CaseListSpecialTransactionsPopupPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listSpecialTransactionsPopup"}
      title={"Special Transaction Numbers:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Special Transaction Number"} value={record && record['specialTransactionNumber']} />
          <UimField label={"Payee Name"} value={record && record['payeeName']} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Amount"} value={record && record['amount']} />
          <UimField label={"Hours"} value={record && record['hours']} />
        </div>
      </UimSection>
      <UimSection title={"Special Transactions"}>
        <div className="uim-form-grid">
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Special Transaction Number"} value={record && record['specialTransactionNumber']} />
          <UimField label={"Payee Name"} value={record && record['payeeName']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
        </div>
      </UimSection>
      <UimSection title={"Special Transactions"}>
        <div className="uim-form-grid">
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Amount"} value={record && record['amount']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Select')}>Select</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListSpecialTransactionsPopupPage;
