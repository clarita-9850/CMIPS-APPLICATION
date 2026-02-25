import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Provider', route: '/case/list-provider' }
  ];

export function CaseAssignProviderPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_assignProvider"}
      title={"Assign Case Provider:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Relationship to Recipient"} value={record && record['relationshipToRecipient']} />
          <UimField label={"Timesheet Review"} value={record && record['timesheetReview']} />
          <UimField label={"Provider Status"} value={record && record['providerStatus']} />
          <UimField label={"Print Initial Timesheet"} value={record && record['printInitialTimesheet']} />
          <UimField label={"Print Initial Timesheet"} value={record && record['printInitialTimesheet']} />
          <UimField label={"Provider has Workweek Agreement with Travel"} value={record && record['providerHasWorkweekAgreementWithTravel']} />
          <UimField label={"Prov Recip Resid Togeather"} value={record && record['provRecipResidTogeather']} />
        </div>
      </UimSection>
      <UimSection title={"IHSS Hours"}>
        <div className="uim-form-grid">
          <UimField label={"County Use Comments"} value={record && record['countyUseComments']} />
          <UimField label={"Provider Type"} value={record && record['providerType']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"Pay Rate"} value={record && record['payRate']} />
          <UimField label={"Assigned Hours (HH:MM)"} value={record && record['assignedHoursHHMM']} />
          <UimField label={"Provider Assigned Hours Form"} value={record && record['providerAssignedHoursForm']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Differential Pay Rate"} value={record && record['differentialPayRate']} />
        </div>
      </UimSection>
      <UimSection title={"WPCS Hours"}>
        <div className="uim-form-grid">
          <UimField label={"Max Differential Pay Rate Hours (HH:MM)"} value={record && record['maxDifferentialPayRateHoursHHMM']} />
          <UimField label={"WPCS Provider Type"} value={record && record['wPCSProviderType']} />
          <UimField label={"WPCS Begin Date"} value={record && record['wPCSBeginDate']} />
          <UimField label={"WPCS Pay Rate"} value={record && record['wPCSPayRate']} />
          <UimField label={"WPCS End Date"} value={record && record['wPCSEndDate']} />
          <UimField label={"WPCS Differential Pay Rate"} value={record && record['wPCSDifferentialPayRate']} />
          <UimField label={"WPCS Max Differential Pay Rate Hours (HH:MM)"} value={record && record['wPCSMaxDifferentialPayRateHoursHHMM']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseAssignProviderPage;
