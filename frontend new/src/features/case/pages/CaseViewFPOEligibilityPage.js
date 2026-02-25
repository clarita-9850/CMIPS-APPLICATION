import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Provider', route: '/case/view-provider' },
    { label: 'view Provider W P C S Hours', route: '/case/view-provider-wpcs-hours' },
    { label: 'search I H S S Provider Hours History', route: '/case/search-ihss-provider-hours-history' },
    { label: 'search W P C S Provider Hours History', route: '/case/search-wpcs-provider-hours-history' }
  ];

export function CaseViewFPOEligibilityPage() {
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewFPOEligibility"}
      title={"FPO Eligible:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"FPO Eligible"} value={record && record['fPOEligible']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Created By"} value={record && record['createdBy']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"FPO Eligible"} value={record && record['fPOEligible']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Created By"} value={record && record['createdBy']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
        </div>
      </UimSection>
      <UimSection title={"FPO Eligible"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"FPO Eligible"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Case Provider')}>View Case Provider</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View WPCS Details')}>View WPCS Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View IHSS Provider Hours History')}>View IHSS Provider Hours History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View WPCS Provider Hours History')}>View WPCS Provider Hours History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: FPO Eligible')}>FPO Eligible</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewFPOEligibilityPage;
