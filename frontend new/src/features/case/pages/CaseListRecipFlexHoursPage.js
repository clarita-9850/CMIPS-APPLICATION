import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'create Recip Flex Hours', route: '/case/create-recip-flex-hours' },
    { label: 'view Recip Flex Hours', route: '/case/view-recip-flex-hours' },
    { label: 'modify Recip Flex Hours', route: '/case/modify-recip-flex-hours' },
    { label: 'modify Recip Flex Hours After Pending', route: '/case/modify-recip-flex-hours-after-pending' },
    { label: 'list Recip Flex Hours History', route: '/case/list-recip-flex-hours-history' },
    { label: 'inactivate Recip Flex Hours', route: '/case/inactivate-recip-flex-hours' }
  ];

export function CaseListRecipFlexHoursPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listRecipFlexHours"}
      title={"Recipient Flexible Hours:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Month"} value={record && record['month']} />
          <UimField label={"Year"} value={record && record['year']} />
          <UimField label={"Frequency"} value={record && record['frequency']} />
          <UimField label={"Flexible Hours End Date"} value={record && record['flexibleHoursEndDate']} />
          <UimField label={"Program Type"} value={record && record['programType']} />
          <UimField label={"Recipient Requested Hours"} value={record && record['recipientRequestedHours']} />
          <UimField label={"County Approved Hours"} value={record && record['countyApprovedHours']} />
          <UimField label={"Appvd"} value={record && record['appvd']} />
          <UimField label={"Need Not Unanticipated"} value={record && record['needNotUnanticipated']} />
          <UimField label={"Need Not Immediate"} value={record && record['needNotImmediate']} />
          <UimField label={"No Health or Safety Issue"} value={record && record['noHealthOrSafetyIssue']} />
          <UimField label={"Request Outcome Date"} value={record && record['requestOutcomeDate']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-recip-flex-hours')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/list-recip-flex-hours-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { casesApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListRecipFlexHoursPage;
