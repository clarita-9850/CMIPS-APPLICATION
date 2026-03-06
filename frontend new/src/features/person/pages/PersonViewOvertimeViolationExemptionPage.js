import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'modify Overtime Violation Exemption', route: '/person/modify-overtime-violation-exemption' },
    { label: 'overtime Violation Exemption', route: '/person/overtime-violation-exemption' }
  ];

export function PersonViewOvertimeViolationExemptionPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewOvertimeViolationExemption"}
      title={"View Overtime Violation Exemption:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Exemption Type"} value={record && record['exemptionType']} />
          <UimField label={"Created By"} value={record && record['createdBy']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <UimSection title={"Recipients"}>
        <div className="uim-form-grid">
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Case Name"} value={record && record['caseName']} />
          <UimField label={"Program Type"} value={record && record['programType']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewOvertimeViolationExemptionPage;
