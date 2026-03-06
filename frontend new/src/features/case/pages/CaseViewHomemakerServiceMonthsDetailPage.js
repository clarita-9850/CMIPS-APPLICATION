import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [];

export function CaseViewHomemakerServiceMonthsDetailPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewHomemakerServiceMonthsDetail"}
      title={"View Homemaker/PA Contract Service Month Detail:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Homemaker/PA Contract"} value={record && record['homemakerPAContract']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"Amount"} value={record && record['amount']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewHomemakerServiceMonthsDetailPage;
