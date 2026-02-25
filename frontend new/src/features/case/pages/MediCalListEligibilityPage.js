import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'saws Eligibility Information', route: '/case/saws-eligibility-information' },
    { label: 'view Eligibility', route: '/case/view-eligibility' }
  ];

export function MediCalListEligibilityPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"MediCal_listEligibility"}
      title={"Medi-Cal Eligibility:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Eligibility Month"} value={record && record['eligibilityMonth']} />
          <UimField label={"MEDS ID"} value={record && record['mEDSID']} />
          <UimField label={"Medi-Cal SOC"} value={record && record['mediCalSOC']} />
          <UimField label={"Medi-Cal Case Number"} value={record && record['mediCalCaseNumber']} />
          <UimField label={"FFP"} value={record && record['fFP']} />
          <UimField label={"Medi-Cal Eligibility Status"} value={record && record['mediCalEligibilityStatus']} />
          <UimField label={"AID Code"} value={record && record['aIDCode']} />
          <UimField label={"Last Updated Date"} value={record && record['lastUpdatedDate']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Eligibility Month"} value={record && record['eligibilityMonth']} />
          <UimField label={"MEDS ID"} value={record && record['mEDSID']} />
          <UimField label={"Medi-Cal SOC"} value={record && record['mediCalSOC']} />
          <UimField label={"Medi-Cal Case Number"} value={record && record['mediCalCaseNumber']} />
          <UimField label={"FFP"} value={record && record['fFP']} />
          <UimField label={"Medi-Cal Eligibility Status"} value={record && record['mediCalEligibilityStatus']} />
          <UimField label={"AID Code"} value={record && record['aIDCode']} />
          <UimField label={"Last Updated Date"} value={record && record['lastUpdatedDate']} />
          <UimField label={"Type"} value={record && record['type']} />
        </div>
      </UimSection>
      <UimSection title={"Medi-Cal Eligibility List"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Medi-Cal Eligibility List"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Add Eligibility Record')}>Add Eligibility Record</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Add Ineligibility Record')}>Add Ineligibility Record</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Copy Eligibility Record')}>Copy Eligibility Record</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/saws-eligibility-information')}>SAWS Eligibility Information</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-eligibility')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-eligibility')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default MediCalListEligibilityPage;
