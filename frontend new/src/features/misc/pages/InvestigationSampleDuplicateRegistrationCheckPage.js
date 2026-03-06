import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [];

export function InvestigationSampleDuplicateRegistrationCheckPage() {
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"InvestigationSample_duplicateRegistrationCheck"}
      title={"Duplicate Registration Check"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"ID"} value={record && record['iD']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date"} value={record && record['date']} />
        </div>
      </UimSection>
    </UimPageLayout>
  );
}

export default InvestigationSampleDuplicateRegistrationCheckPage;
