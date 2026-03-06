import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Timesheet', route: '/case/view-timesheet' }
  ];

export function CaseModifyTimesheetPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_modifyTimesheet"}
      title={"Modify Timesheet Manual Entry:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Type"} value={record && record['type']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries [HH:MM]"}>
        <div className="uim-form-grid">
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries [HH:MM]"}>
        <div className="uim-form-grid">
          <UimField label={"Timesheet Number"} value={record && record['timesheetNumber']} />
          <UimField label={"Total (HH:MM)"} value={record && record['totalHHMM']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries [HH:MM]"}>
        <div className="uim-form-grid">
          <UimField label={"Total (HH:MM)"} value={record && record['totalHHMM']} />
          <UimField label={"Total (HH:MM)"} value={record && record['totalHHMM']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries [HH:MM]"}>
        <div className="uim-form-grid">
          <UimField label={"Total (HH:MM)"} value={record && record['totalHHMM']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseModifyTimesheetPage;
