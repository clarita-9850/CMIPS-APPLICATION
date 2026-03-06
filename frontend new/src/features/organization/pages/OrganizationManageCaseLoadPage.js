import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Supervisor', route: '/organization/list-supervisor' },
    { label: 'reassign Case Load', route: '/organization/reassign-case-load' },
    { label: 'ihss Case Home', route: '/supervisor/ihss-case-home' }
  ];

export function OrganizationManageCaseLoadPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const organizationApi = getDomainApi('organization');
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_manageCaseLoad"}
      title={"Manage User Caseload"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Select a User"}>
        <div className="uim-form-grid">
          <UimField label={"My Users"} value={record && record['myUsers']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
          <UimField label={"Overdue Assessments"} value={record && record['overdueAssessments']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Recipient"} value={record && record['recipient']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"City"} value={record && record['city']} />
        </div>
      </UimSection>
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"Zip"} value={record && record['zip']} />
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Date Of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Companion Case"} value={record && record['companionCase']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
          <UimField label={"Reassessment Due Date"} value={record && record['reassessmentDueDate']} />
        </div>
      </UimSection>
      <UimSection title={"Caseload"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Get Caseload')}>Get Caseload</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.update(id, { action: 'assign' }).then(() => { alert('Assigned successfully'); navigate(-1); }).catch(err => alert('Assign failed: ' + err.message)); }}>Reassign</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: SelectAll')}>SelectAll</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Get Caseload')}>Get Caseload</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.update(id, { action: 'assign' }).then(() => { alert('Assigned successfully'); navigate(-1); }).catch(err => alert('Assign failed: ' + err.message)); }}>Reassign</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationManageCaseLoadPage;
