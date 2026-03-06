import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'list Provider', route: '/misc/pa-case-list-provider' }
  ];

export function PACaseViewProviderPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"PACase_viewProvider"}
      title={"Provider Management - View Case Provider:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Provider Status"} value={record && record['providerStatus']} />
          <UimField label={"Relationship to Recipient"} value={record && record['relationshipToRecipient']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"Termination Reason"} value={record && record['terminationReason']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Provider Assigned Hours Form"} value={record && record['providerAssignedHoursForm']} />
          <UimField label={"Relationship Status Date"} value={record && record['relationshipStatusDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Termination Comment"} value={record && record['terminationComment']} />
          <UimField label={"W-4 Status"} value={record && record['w4Status']} />
          <UimField label={"W-4 Allowance"} value={record && record['w4Allowance']} />
          <UimField label={"W-4 Amount"} value={record && record['w4Amount']} />
          <UimField label={"W-4 Last Updated"} value={record && record['w4LastUpdated']} />
          <UimField label={"EIC"} value={record && record['eIC']} />
          <UimField label={"EIC Begin Year"} value={record && record['eICBeginYear']} />
          <UimField label={"EIC Expiration Date"} value={record && record['eICExpirationDate']} />
        </div>
      </UimSection>
      <UimSection title={"Financial"}>
        <div className="uim-form-grid">
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
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Assigned Hours"} value={record && record['assignedHours']} />
          <UimField label={"Pay Rate"} value={record && record['payRate']} />
          <UimField label={"Status"} value={record && record['status']} />
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
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PACaseViewProviderPage;
