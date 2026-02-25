import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'select Exemption Type', route: '/person/select-exemption-type' },
    { label: 'view Overtime Violation Exemption Extraordinary Circumstances', route: '/person/view-overtime-violation-exemption-extraordinary-circumstances' },
    { label: 'view Overtime Violation Exemption', route: '/person/view-overtime-violation-exemption' },
    { label: 'modify Overtime Violation Exemption E C', route: '/person/modify-overtime-violation-exemption-ec' },
    { label: 'modify Overtime Violation Exemption', route: '/person/modify-overtime-violation-exemption' },
    { label: 'overtime Violation Exemption History E C', route: '/person/overtime-violation-exemption-history-ec' },
    { label: 'overtime Violation Exemption History', route: '/person/overtime-violation-exemption-history' },
    { label: 'inactivate Overtime Violation Exemption', route: '/person/inactivate-overtime-violation-exemption' }
  ];

export function PersonOvertimeViolationExemptionPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_overtimeViolationExemption"}
      title={"Overtime Violation Exemption:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Exemption Type"} value={record && record['exemptionType']} />
          <UimField label={"Outcome"} value={record && record['outcome']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"Last Updated"} value={record && record['lastUpdated']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-overtime-violation-exemption-extraordinary-circumstances')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/overtime-violation-exemption-history-ec')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { personsApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonOvertimeViolationExemptionPage;
