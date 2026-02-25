import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [];

export function MediCalSawsEligibilityInformationPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"MediCal_sawsEligibilityInformation"}
      title={"SAWS Eligibility Information:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"SAWS Eligibility Information"}>
        <div className="uim-form-grid">
          <UimField label={"Last Updated"} value={record && record['lastUpdated']} />
          <UimField label={"Medi-Cal Status"} value={record && record['mediCalStatus']} />
          <UimField label={"Medi-Cal Status Effective Date"} value={record && record['mediCalStatusEffectiveDate']} />
          <UimField label={"Medi-Cal Status Reason"} value={record && record['mediCalStatusReason']} />
          <UimField label={"SAWS Serial"} value={record && record['sAWSSerial']} />
          <UimField label={"Update Type"} value={record && record['updateType']} />
          <UimField label={"EW First Name"} value={record && record['eWFirstName']} />
          <UimField label={"EW Last Name"} value={record && record['eWLastName']} />
          <UimField label={"Eligibility Worker Phone"} value={record && record['eligibilityWorkerPhone']} />
          <UimField label={"Eligibility Worker Email"} value={record && record['eligibilityWorkerEmail']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default MediCalSawsEligibilityInformationPage;
