import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { formatDate } from '../../../lib/providerConstants';

/**
 * CaseListContractorServiceMonthsPage
 * Lists contractor service months for a case.
 * Fixed: removed duplicated fields from original stub.
 */
export function CaseListContractorServiceMonthsPage() {
  const navigate = useNavigate();
  const [serviceMonths, setServiceMonths] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(1);
  const pageSize = 50;

  useEffect(() => {
    // Load service months data — placeholder until case context is wired
    setLoading(true);
    // Simulated data load; in production, would use case context ID
    setTimeout(() => {
      setServiceMonths([]);
      setLoading(false);
    }, 500);
  }, []);

  const columns = [
    { key: 'serviceMonth', label: 'Service Month' },
    { key: 'contractorName', label: 'Contractor' },
    { key: 'hours', label: 'Hours' },
    { key: 'amount', label: 'Amount' },
  ];

  const totalPages = Math.max(1, Math.ceil(serviceMonths.length / pageSize));
  const start = (page - 1) * pageSize;
  const pageData = serviceMonths.slice(start, start + pageSize).map(sm => ({
    ...sm,
    serviceMonth: formatDate(sm.serviceMonth),
    amount: sm.amount != null ? `$${Number(sm.amount).toFixed(2)}` : '\u2014',
  }));

  return (
    <UimPageLayout
      pageId="Case_listContractorServiceMonths"
      title="County Contractor Service Month:"
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>{error}</div>}

      <UimSection title="Service Months">
        <UimTable
          columns={columns}
          data={pageData}
          pageSize={pageSize}
          actions={[
            { label: 'View', onClick: (row) => navigate('/case/view-contractor-service-months', { state: { record: row } }) },
          ]}
          emptyMessage="No service month records found"
        />
      </UimSection>

      {serviceMonths.length > pageSize && (
        <div className="uim-action-bar">
          <button
            className="uim-btn uim-btn-secondary"
            onClick={() => setPage(p => Math.max(1, p - 1))}
            disabled={page === 1}
          >
            &laquo; Previous
          </button>
          <span style={{ padding: '0.4rem 0.75rem', fontSize: '0.875rem' }}>
            Page {page} of {totalPages} ({serviceMonths.length} records)
          </span>
          <button
            className="uim-btn uim-btn-secondary"
            onClick={() => setPage(p => Math.min(totalPages, p + 1))}
            disabled={page === totalPages}
          >
            Next &raquo;
          </button>
        </div>
      )}
    </UimPageLayout>
  );
}

export default CaseListContractorServiceMonthsPage;
