import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Provider Details', route: '/person/view-provider-details' }
  ];

export function PersonEditProviderEnrollmentPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_editProviderEnrollment"}
      title={"Modify Enrollment:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Enrollment"}>
        <div className="uim-form-grid">
          <UimField label={"Eligible"} value={record && record['eligible']} />
          <UimField label={"SOC 426 - Provider Enrollment"} value={record && record['sOC426ProviderEnrollment']} />
          <UimField label={"SOC 846 - Overtime Agreement"} value={record && record['sOC846OvertimeAgreement']} />
          <UimField label={"Provider Orientation"} value={record && record['providerOrientation']} />
          <UimField label={"Effective Date"} value={record && record['effectiveDate']} />
          <UimField label={"Provider Enrollment Begin Date"} value={record && record['providerEnrollmentBeginDate']} />
          <UimField label={"Ineligible Reason"} value={record && record['ineligibleReason']} />
          <UimField label={"DOJ Background Check"} value={record && record['dOJBackgroundCheck']} />
          <UimField label={"SOC 846 - Provider Agreement"} value={record && record['sOC846ProviderAgreement']} />
        </div>
      </UimSection>
      <UimSection title={"Enrollment"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Orientation Date"} value={record && record['providerOrientationDate']} />
          <UimField label={"Enrollment County"} value={record && record['enrollmentCounty']} />
          <UimField label={"Good Cause Extension"} value={record && record['goodCauseExtension']} />
          <UimField label={"Provider Enrollment Due Date"} value={record && record['providerEnrollmentDueDate']} />
          <UimField label={"Active Cases"} value={record && record['activeCases']} />
          <UimField label={"Eligible"} value={record && record['eligible']} />
          <UimField label={"SOC 426 - Provider Enrollment"} value={record && record['sOC426ProviderEnrollment']} />
          <UimField label={"SOC 846 - Overtime Agreement"} value={record && record['sOC846OvertimeAgreement']} />
          <UimField label={"Provider Orientation"} value={record && record['providerOrientation']} />
        </div>
      </UimSection>
      <UimSection title={"Appeals"}>
        <div className="uim-form-grid">
          <UimField label={"Effective Date"} value={record && record['effectiveDate']} />
          <UimField label={"Provider Enrollment Begin Date"} value={record && record['providerEnrollmentBeginDate']} />
          <UimField label={"Ineligible Reason"} value={record && record['ineligibleReason']} />
          <UimField label={"DOJ Background Check"} value={record && record['dOJBackgroundCheck']} />
          <UimField label={"SOC 846 - Provider Agreement"} value={record && record['sOC846ProviderAgreement']} />
          <UimField label={"Provider Orientation Date"} value={record && record['providerOrientationDate']} />
          <UimField label={"Enrollment County"} value={record && record['enrollmentCounty']} />
          <UimField label={"Good Cause Extension"} value={record && record['goodCauseExtension']} />
          <UimField label={"Provider Enrollment Due Date"} value={record && record['providerEnrollmentDueDate']} />
        </div>
      </UimSection>
      <UimSection title={"County Use"}>
        <div className="uim-form-grid">
          <UimField label={"Active Cases"} value={record && record['activeCases']} />
          <UimField label={"Appeal Status Date"} value={record && record['appealStatusDate']} />
          <UimField label={"Admin Hearing Date"} value={record && record['adminHearingDate']} />
          <UimField label={"Appeal Status"} value={record && record['appealStatus']} />
          <UimField label={"County Use 1"} value={record && record['countyUse1']} />
          <UimField label={"County Use 2"} value={record && record['countyUse2']} />
          <UimField label={"County Use 3"} value={record && record['countyUse3']} />
          <UimField label={"County Use 4"} value={record && record['countyUse4']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonEditProviderEnrollmentPage;
