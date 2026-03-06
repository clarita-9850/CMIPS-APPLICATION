import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Telehealth Questionnaire Resolver', route: '/case/view-telehealth-questionnaire-resolver' },
    { label: 'view Screening History', route: '/case/view-screening-history' }
  ];

export function CaseViewTelehealthQuestionnaireDetailsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewTelehealthQuestionnaireDetails"}
      title={"Telehealth Screening History Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Potentially Stable Indicator Results"}>
        <div className="uim-form-grid">
          <UimField label={"Assessment Type"} value={record && record['assessmentType']} />
          <UimField label={"Recipient Age"} value={record && record['recipientAge']} />
          <UimField label={"Residence Address"} value={record && record['residenceAddress']} />
          <UimField label={"Provider Gap"} value={record && record['providerGap']} />
        </div>
      </UimSection>
      <UimSection title={"Telehealth Eligibility Criteria Evaluated by Potentially Stable Indicator"}>
        <div className="uim-form-grid">
          <UimField label={"MOJ & Living Arrangement"} value={record && record['mOJLivingArrangement']} />
          <UimField label={"Protective Supervision"} value={record && record['protectiveSupervision']} />
          <UimField label={"Recipient Response Due Date"} value={record && record['recipientResponseDueDate']} />
          <UimField label={"County Response Due Date"} value={record && record['countyResponseDueDate']} />
        </div>
      </UimSection>
      <UimSection title={"Telehealth Questionnaire Details"}>
        <div className="uim-form-grid">
          <UimField label={"Telehealth Questionnaire Results"} value={record && record['telehealthQuestionnaireResults']} />
          <UimField label={"Response Submitted By"} value={record && record['responseSubmittedBy']} />
          <UimField label={"Stable Care Exception"} value={record && record['stableCareException']} />
          <UimField label={"Telehealth Status"} value={record && record['telehealthStatus']} />
        </div>
      </UimSection>
      <UimSection title={"Telehealth Questionnaire Overview"}>
        <div className="uim-form-grid">
          <UimField label={"Telehealth Status Last Update Date"} value={record && record['telehealthStatusLastUpdateDate']} />
          <UimField label={"Telehealth Status Updated By"} value={record && record['telehealthStatusUpdatedBy']} />
          <UimField label={"Stable Care Exception"} value={record && record['stableCareException']} />
          <UimField label={"Telehealth Status"} value={record && record['telehealthStatus']} />
        </div>
      </UimSection>
      <UimSection title={"Telehealth Questionnaire Responses"}>
        <div className="uim-form-grid">
          <UimField label={"Telehealth Status Last Update Date"} value={record && record['telehealthStatusLastUpdateDate']} />
          <UimField label={"Telehealth Status Updated By"} value={record && record['telehealthStatusUpdatedBy']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <UimSection title={"Telehealth Screening Outcome"}>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewTelehealthQuestionnaireDetailsPage;
