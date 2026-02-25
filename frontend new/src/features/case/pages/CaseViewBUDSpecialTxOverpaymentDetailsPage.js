import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [];

export function CaseViewBUDSpecialTxOverpaymentDetailsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewBUDSpecialTxOverpaymentDetails"}
      title={"View Special Transaction Overpayment Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Special Transaction Overpayment Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Period"} value={record && record['servicePeriod']} />
          <UimField label={"Week 1"} value={record && record['week1']} />
          <UimField label={"Week 2"} value={record && record['week2']} />
          <UimField label={"Week 3"} value={record && record['week3']} />
          <UimField label={"Week 4"} value={record && record['week4']} />
          <UimField label={"Total Hours"} value={record && record['totalHours']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewBUDSpecialTxOverpaymentDetailsPage;
