import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'Test', route: '/misc/test' }
  ];

export function MediCalEditStateBuyOutPaymentPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"MediCal_editStateBuyOutPayment"}
      title={"Modify State Buy-Out Payment Management:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Conversion Date"} value={record && record['conversionDate']} />
          <UimField label={"Termination Date"} value={record && record['terminationDate']} />
          <UimField label={"Termination Reason"} value={record && record['terminationReason']} />
          <UimField label={"State Buy-Out Payment Status"} value={record && record['stateBuyOutPaymentStatus']} />
          <UimField label={"Rescission Date"} value={record && record['rescissionDate']} />
          <UimField label={"Rescission Reason"} value={record && record['rescissionReason']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default MediCalEditStateBuyOutPaymentPage;
