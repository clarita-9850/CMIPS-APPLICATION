import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'create Overpayment Occurence', route: '/case/create-overpayment-occurence' },
    { label: 'view Overpayment Recovery', route: '/payment/view-overpayment-recovery' }
  ];

export function CaseListOverpaymentRecoveriesPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listOverpaymentRecoveries"}
      title={"Overpayment Recoveries:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Overpayment Number"} value={record && record['overpaymentNumber']} />
          <UimField label={"Payee Name"} value={record && record['payeeName']} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"Overpayment Number"} value={record && record['overpaymentNumber']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Service To"} value={record && record['serviceTo']} />
          <UimField label={"Total Overpaid Hours"} value={record && record['totalOverpaidHours']} />
          <UimField label={"Recovery Amount"} value={record && record['recoveryAmount']} />
        </div>
      </UimSection>
      <UimSection title={"Overpayment Recoveries"}>
        <div className="uim-form-grid">
          <UimField label={"Balance"} value={record && record['balance']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Payee Name"} value={record && record['payeeName']} />
          <UimField label={"Overpayment Number"} value={record && record['overpaymentNumber']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
        </div>
      </UimSection>
      <UimSection title={"Overpayment Recoveries"}>
        <div className="uim-form-grid">
          <UimField label={"Service To"} value={record && record['serviceTo']} />
          <UimField label={"Total Overpaid Hours"} value={record && record['totalOverpaidHours']} />
          <UimField label={"Recovery Amount"} value={record && record['recoveryAmount']} />
          <UimField label={"Balance"} value={record && record['balance']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-overpayment-recovery')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-overpayment-recovery')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListOverpaymentRecoveriesPage;
