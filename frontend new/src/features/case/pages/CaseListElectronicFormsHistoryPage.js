import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [];

export function CaseListElectronicFormsHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listElectronicFormsHistory"}
      title={"Electronic Forms History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Form Name"} value={record && record['formName']} />
          <UimField label={"Sent to"} value={record && record['sentTo']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Due Date"} value={record && record['dueDate']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"Last Updated"} value={record && record['lastUpdated']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListElectronicFormsHistoryPage;
