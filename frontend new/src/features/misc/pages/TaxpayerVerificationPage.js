import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'home Page', route: '/person/home-page' }
  ];

export function TaxpayerVerificationPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const miscApi = getDomainApi('misc');
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Taxpayer_verification"}
      title={"Enter Taxpayer ID:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Taxpayer ID Verification"}>
        <div className="uim-form-grid">
          <UimField label={"Taxpayer ID"} value={record && record['taxpayerID']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { miscApi.update(id, { action: 'validate' }).then(() => alert('Validated successfully')).catch(err => alert('Validation failed: ' + err.message)); }}>Verify</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default TaxpayerVerificationPage;
