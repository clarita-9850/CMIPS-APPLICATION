import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'live In Prov Self Certification Search', route: '/person/live-in-prov-self-certification-search' }
  ];

export function PersonLiveInProvSelfCertificationEntryPage() {
  const navigate = useNavigate();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_liveInProvSelfCertificationEntry"}
      title={"IRS Live-In Provider Self-Certification Entry"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider County"} value={record && record['providerCounty']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Self-Certification Status"} value={record && record['selfCertificationStatus']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: ActionControl.label.Cancel')}>ActionControl.label.Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonLiveInProvSelfCertificationEntryPage;
