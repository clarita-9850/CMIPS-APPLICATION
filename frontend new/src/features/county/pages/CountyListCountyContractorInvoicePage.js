import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { getRatesByCounty } from '../../../api/countyContractorApi';
import { getInvoicesByContractor } from '../../../api/countyContractorApi';
import { COUNTY_OPTIONS } from '../../../lib/providerConstants';
import { formatDate } from '../../../lib/providerConstants';

export function CountyListCountyContractorInvoicePage() {
  const navigate = useNavigate();
  const [selectedCounty, setSelectedCounty] = useState('');
  const [contractors, setContractors] = useState([]);
  const [selectedContractorId, setSelectedContractorId] = useState('');
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Load contractors when county changes
  const handleFetchContractors = useCallback(async () => {
    if (!selectedCounty) return;
    setLoading(true);
    setError(null);
    setContractors([]);
    setSelectedContractorId('');
    setInvoices([]);
    try {
      const data = await getRatesByCounty(selectedCounty);
      // Deduplicate by contractor name, keep latest
      const seen = new Map();
      (Array.isArray(data) ? data : []).forEach(r => {
        if (!seen.has(r.contractorName)) seen.set(r.contractorName, r);
      });
      setContractors(Array.from(seen.values()));
    } catch (err) {
      setError(err.message || 'Failed to load contractors');
    } finally {
      setLoading(false);
    }
  }, [selectedCounty]);

  // Load invoices when contractor changes
  useEffect(() => {
    if (!selectedContractorId) {
      setInvoices([]);
      return;
    }
    setLoading(true);
    setError(null);
    getInvoicesByContractor(selectedContractorId)
      .then(data => setInvoices(Array.isArray(data) ? data : []))
      .catch(err => setError(err.message || 'Failed to load invoices'))
      .finally(() => setLoading(false));
  }, [selectedContractorId]);

  const contractorOptions = [
    { value: '', label: 'Select Contractor...' },
    ...contractors.map(c => ({ value: String(c.id), label: c.contractorName })),
  ];

  const columns = [
    { key: 'billingMonth', label: 'Billing Month' },
    { key: 'invoiceNumber', label: 'Invoice Number' },
    { key: 'processedDate', label: 'Processed Date' },
    { key: 'authorizedAmt', label: 'Authorized Amount' },
    { key: 'status', label: 'Status' },
  ];

  // Format invoice data for display
  const displayData = invoices.map(inv => ({
    ...inv,
    billingMonth: formatDate(inv.billingMonth),
    processedDate: formatDate(inv.processedDate),
    authorizedAmt: inv.authorizedAmt != null ? `$${Number(inv.authorizedAmt).toFixed(2)}` : '\u2014',
  }));

  return (
    <UimPageLayout
      pageId="County_listCountyContractorInvoice"
      title="County Contractor:"
      hidePlaceholderBanner={true}
    >
      <UimSection title="Select Contractor">
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
        <div className="uim-action-bar" style={{ marginTop: '0.5rem' }}>
          <button className="uim-btn uim-btn-primary" onClick={handleFetchContractors} disabled={!selectedCounty}>
            Get Contractors
          </button>
        </div>
        {contractors.length > 0 && (
          <div className="uim-form-grid" style={{ marginTop: '0.75rem' }}>
            <UimField
              label="Contractor"
              type="select"
              name="contractorId"
              value={selectedContractorId}
              onChange={(e) => setSelectedContractorId(e.target.value)}
              options={contractorOptions}
            />
          </div>
        )}
      </UimSection>

      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>{error}</div>}

      {displayData.length > 0 && (
        <UimSection title="Invoices">
          <UimTable
            columns={columns}
            data={displayData}
            actions={[
              { label: 'View', onClick: (row) => navigate(`/county/view-county-contractor-invoice/${row.id}`) },
              {
                label: 'Edit',
                onClick: (row) => navigate(`/county/edit-county-contractor-invoice/${row.id}`),
                visible: (row) => row.status === 'Pending',
              },
            ]}
            emptyMessage="No invoices found"
          />
        </UimSection>
      )}
    </UimPageLayout>
  );
}

export default CountyListCountyContractorInvoicePage;
