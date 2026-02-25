import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Overpayment Recovery', route: '/person/payment-view-overpayment-recovery' }
  ];

export function PersonListOverpaymentRecoveriesPage() {
  const navigate = useNavigate();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listOverpaymentRecoveries"}
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
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Overpayment Number"} value={record && record['overpaymentNumber']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Service To"} value={record && record['serviceTo']} />
          <UimField label={"Total Overpaid Hours"} value={record && record['totalOverpaidHours']} />
          <UimField label={"Recovery Amount"} value={record && record['recoveryAmount']} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"Balance"} value={record && record['balance']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Overpayment Number"} value={record && record['overpaymentNumber']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Service To"} value={record && record['serviceTo']} />
          <UimField label={"Total Overpaid Hours"} value={record && record['totalOverpaidHours']} />
          <UimField label={"Recovery Amount"} value={record && record['recoveryAmount']} />
          <UimField label={"Balance"} value={record && record['balance']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Overpayment Recoveries"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Overpayment Recoveries"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/payment-view-overpayment-recovery')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonListOverpaymentRecoveriesPage;
