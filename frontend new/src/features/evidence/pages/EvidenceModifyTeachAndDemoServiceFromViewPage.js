import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Teach And Demo Service', route: '/evidence/view-teach-and-demo-service' },
    { label: 'create Single Task With Proration For Svc Type From View', route: '/evidence/create-single-task-with-proration-for-svc-type-from-view' },
    { label: 'create Freeform Task With Proration For Svc Type From View', route: '/evidence/create-freeform-task-with-proration-for-svc-type-from-view' },
    { label: 'create List Task With Proration For Svc Type From View', route: '/evidence/create-list-task-with-proration-for-svc-type-from-view' },
    { label: 'modify Task With Proration For Svc Type From View', route: '/evidence/modify-task-with-proration-for-svc-type-from-view' },
    { label: 'delete Task With Proration For Svc Type From View', route: '/evidence/delete-task-with-proration-for-svc-type-from-view' }
  ];

export function EvidenceModifyTeachAndDemoServiceFromViewPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const eligibilityApi = getDomainApi('evidence');
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_modifyTeachAndDemoServiceFromView"}
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
          <UimField label={"Proration"} value={record && record['proration']} />
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
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Add Tasks')}>Add Tasks</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { eligibilityApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceModifyTeachAndDemoServiceFromViewPage;
