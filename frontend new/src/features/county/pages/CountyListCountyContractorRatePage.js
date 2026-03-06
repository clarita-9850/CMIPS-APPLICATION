import React, { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { getRatesByCounty } from '../../../api/countyContractorApi';
import { COUNTY_OPTIONS } from '../../../lib/providerConstants';

export function CountyListCountyContractorRatePage() {
  const navigate = useNavigate();
  const [selectedCounty, setSelectedCounty] = useState('');
  const [rates, setRates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleFetchRates = useCallback(async () => {
    if (!selectedCounty) return;
    setLoading(true);
    setError(null);
    try {
      const data = await getRatesByCounty(selectedCounty);
      setRates(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Failed to load rates');
    } finally {
      setLoading(false);
    }
  }, [selectedCounty]);

  const columns = [
    { key: 'contractorName', label: 'Contractor Name' },
    { key: 'fromDate', label: 'Effective Date' },
    { key: 'toDate', label: 'End Date' },
    { key: 'rateAmt', label: 'Rate' },
    { key: 'wageAmt', label: 'Wage' },
    { key: 'macrAmt', label: 'MACR' },
  ];

  return (
    <UimPageLayout
      pageId="County_listCountyContractorRate"
      title="County Contractor Rate:"
      hidePlaceholderBanner={true}
    >
      <UimSection title="Select County">
        <div className="uim-form-grid">
          <UimField
            label="County"
            type="select"
            name="countyCode"
            value={selectedCounty}
            onChange={(e) => setSelectedCounty(e.target.value)}
            options={COUNTY_OPTIONS}
          />
        </div>
      </UimSection>

      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={handleFetchRates} disabled={!selectedCounty}>
          Get County Contractor Rates
        </button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/county/create-county-contractor-rate')}>
          New
        </button>
      </div>

      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>{error}</div>}

      {rates.length > 0 && (
        <UimSection title="Rates">
          <UimTable
            columns={columns}
            data={rates}
            actions={[
              { label: 'View', onClick: (row) => navigate(`/county/view-county-contractor-rate/${row.id}`) },
              { label: 'Edit', onClick: (row) => navigate(`/county/edit-county-contractor-rate/${row.id}`) },
            ]}
            emptyMessage="No rates found for selected county"
          />
        </UimSection>
      )}
    </UimPageLayout>
  );
}

export default CountyListCountyContractorRatePage;
