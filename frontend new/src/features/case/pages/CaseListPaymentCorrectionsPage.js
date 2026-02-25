import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'enter Payment Correction Timesheet Number', route: '/case/enter-payment-correction-timesheet-number' },
    { label: 'view Payment Correction', route: '/case/view-payment-correction' },
    { label: 'edit Payment Correction', route: '/case/edit-payment-correction' }
  ];

export function CaseListPaymentCorrectionsPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listPaymentCorrections"}
      title={"Payment Corrections:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Cluster1.Title"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Timesheet Type"} value={record && record['timesheetType']} />
          <UimField label={"Correction Type"} value={record && record['correctionType']} />
          <UimField label={"Total Hours"} value={record && record['totalHours']} />
        </div>
      </UimSection>
      <UimSection title={"Payment Corrections"}>
        <div className="uim-form-grid">
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Timesheet Type"} value={record && record['timesheetType']} />
        </div>
      </UimSection>
      <UimSection title={"Payment Corrections"}>
        <div className="uim-form-grid">
          <UimField label={"Correction Type"} value={record && record['correctionType']} />
          <UimField label={"Total Hours"} value={record && record['totalHours']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-payment-correction')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-payment-correction')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListPaymentCorrectionsPage;
