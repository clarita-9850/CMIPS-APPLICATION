import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [];

export function CaseViewTelehealthPsiIndicatorResultsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewTelehealthPsiIndicatorResults"}
      title={"Potentially Stable Indicator Results:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Telehealth Eligibility Criteria Evaluated by Potentially Stable Indicator:"}>
        <div className="uim-form-grid">
          <UimField label={"Assessment Type"} value={record && record['assessmentType']} />
          <UimField label={"Recipient Age"} value={record && record['recipientAge']} />
          <UimField label={"Residence Address"} value={record && record['residenceAddress']} />
          <UimField label={"Provider Gap"} value={record && record['providerGap']} />
          <UimField label={"MOJ & Living Arrangement"} value={record && record['mOJLivingArrangement']} />
          <UimField label={"Protective Supervision"} value={record && record['protectiveSupervision']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewTelehealthPsiIndicatorResultsPage;
