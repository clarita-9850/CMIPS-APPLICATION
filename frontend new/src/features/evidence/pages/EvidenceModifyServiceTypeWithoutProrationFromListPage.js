import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'service Evidence Home', route: '/evidence/service-evidence-home' },
    { label: 'resolve Next Service Type Evidence I D', route: '/evidence/resolve-next-service-type-evidence-id' },
    { label: 'reload Service Evidence Home', route: '/evidence/reload-service-evidence-home' },
    { label: 'create Single Task Without Proration For Svc Type From List', route: '/evidence/create-single-task-without-proration-for-svc-type-from-list' },
    { label: 'create Freeform Task Without Proration For Svc Type From List', route: '/evidence/create-freeform-task-without-proration-for-svc-type-from-list' },
    { label: 'create List Task Without Proration For Svc Type From List', route: '/evidence/create-list-task-without-proration-for-svc-type-from-list' },
    { label: 'modify Task Without Proration For Svc Type From List', route: '/evidence/modify-task-without-proration-for-svc-type-from-list' },
    { label: 'delete Task Without Proration For Svc Type From List', route: '/evidence/delete-task-without-proration-for-svc-type-from-list' }
  ];

export function EvidenceModifyServiceTypeWithoutProrationFromListPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const eligibilityApi = getDomainApi('evidence');
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_modifyServiceTypeWithoutProrationFromList"}
      title={"Un-reserve Task:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Type"} value={record && record['serviceType']} />
          <UimField label={"Service Task"} value={record && record['serviceTask']} />
          <UimField label={"Frequency"} value={record && record['frequency']} />
          <UimField label={"Quantity"} value={record && record['quantity']} />
          <UimField label={"Duration"} value={record && record['duration']} />
        </div>
      </UimSection>
      <UimSection title={"Task Details:"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { eligibilityApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Save &amp; Next')}>Save &amp; Next</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Add Tasks')}>Add Tasks</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { eligibilityApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceModifyServiceTypeWithoutProrationFromListPage;
