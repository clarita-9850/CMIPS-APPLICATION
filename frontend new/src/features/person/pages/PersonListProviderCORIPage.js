import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'create Provider C O R I', route: '/person/create-provider-cori' },
    { label: 'view Provider C O R I', route: '/person/view-provider-cori' },
    { label: 'edit Provider C O R I', route: '/person/edit-provider-cori' }
  ];

export function PersonListProviderCORIPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listProviderCORI"}
      title={"Provider CORI Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"CORI Date"} value={record && record['cORIDate']} />
          <UimField label={"Conviction or Release Date"} value={record && record['convictionOrReleaseDate']} />
          <UimField label={"Tier"} value={record && record['tier']} />
          <UimField label={"General Exception Begin Date"} value={record && record['generalExceptionBeginDate']} />
          <UimField label={"General Exception End Date"} value={record && record['generalExceptionEndDate']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Waiver Case"} value={record && record['waiverCase']} />
          <UimField label={"History Created Date"} value={record && record['historyCreatedDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-provider-cori')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonListProviderCORIPage;
