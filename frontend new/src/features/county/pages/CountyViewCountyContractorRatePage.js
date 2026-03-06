import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getRateById } from '../../../api/countyContractorApi';
import { formatDate } from '../../../lib/providerConstants';

export function CountyViewCountyContractorRatePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [rate, setRate] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    getRateById(id)
      .then(data => setRate(data))
      .catch(err => setError(err.message || 'Failed to load rate'))
      .finally(() => setLoading(false));
  }, [id]);

  return (
    <UimPageLayout
      pageId="County_viewCountyContractorRate"
      title="View County Contractor Rate:"
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>{error}</div>}

      {rate && (
        <UimSection title="Details">
          <div className="uim-form-grid">
            <UimField label="Contractor Name" value={rate.contractorName} />
            <UimField label="Effective Date" value={formatDate(rate.fromDate)} />
            <UimField label="End Date" value={formatDate(rate.toDate)} />
            <UimField label="Rate" value={rate.rateAmt != null ? `$${Number(rate.rateAmt).toFixed(2)}` : null} />
            <UimField label="Wage" value={rate.wageAmt != null ? `$${Number(rate.wageAmt).toFixed(2)}` : null} />
            <UimField label="MACR" value={rate.macrAmt != null ? `$${Number(rate.macrAmt).toFixed(2)}` : null} />
          </div>
        </UimSection>
      )}

      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(`/county/edit-county-contractor-rate/${id}`)}>
          Edit
        </button>
        <button className="uim-btn uim-btn-secondary" onClick={() => navigate(-1)}>
          Close
        </button>
      </div>
    </UimPageLayout>
  );
}

export default CountyViewCountyContractorRatePage;
