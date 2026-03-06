import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Telehealth Questionnaire Details', route: '/case/view-telehealth-questionnaire-details' },
    { label: 'view Telehealth Questionnaire Resolver', route: '/case/view-telehealth-questionnaire-resolver' },
    { label: 'send Telehealth Q From History', route: '/case/send-telehealth-q-from-history' }
  ];

export function CaseViewScreeningHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewScreeningHistory"}
      title={"Telehealth Screening History:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Assessment Date"} value={record && record['assessmentDate']} />
          <UimField label={"Assessment Type"} value={record && record['assessmentType']} />
          <UimField label={"Telehealth Questionnaire Results"} value={record && record['telehealthQuestionnaireResults']} />
          <UimField label={"Telehealth Status"} value={record && record['telehealthStatus']} />
          <UimField label={"Telehealth Status Updated By"} value={record && record['telehealthStatusUpdatedBy']} />
          <UimField label={"Telehealth Status Last Update Date"} value={record && record['telehealthStatusLastUpdateDate']} />
        </div>
      </UimSection>
      <UimSection title={"Telehealth Screening History"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-telehealth-questionnaire-details')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/send-telehealth-q-from-history')}>Send</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewScreeningHistoryPage;
