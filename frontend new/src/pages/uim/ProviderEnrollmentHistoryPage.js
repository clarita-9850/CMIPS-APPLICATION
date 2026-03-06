import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as providersApi from '../../api/providersApi';
import { UimPageLayout } from '../../shared/components/UimPageLayout';
import { UimSection } from '../../shared/components/UimSection';
import { UimTable } from '../../shared/components/UimTable';
import { formatDate, getIneligibleReasonLabel } from '../../lib/providerConstants';
import '../../shared/components/UimPage.css';

export const ProviderEnrollmentHistoryPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) { setLoading(false); return; }
    providersApi.getEnrollmentHistory(id)
      .then(data => {
        const items = Array.isArray(data) ? data : (data?.content || data?.items || []);
        // Sort by historyCreated descending
        items.sort((a, b) => {
          const da = new Date(a.historyCreated || a.createdAt || 0);
          const db = new Date(b.historyCreated || b.createdAt || 0);
          return db - da;
        });
        setHistory(items);
      })
      .catch(err => {
        setError(err?.response?.data?.message || err.message || 'Failed to load enrollment history');
        setHistory([]);
      })
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="uim-page"><div className="container"><p>Loading enrollment history...</p></div></div>;

  return (
    <UimPageLayout title="Provider Enrollment History" hidePlaceholderBanner={true}>
      {error && <div className="uim-error-banner">{error}</div>}

      <UimSection title="Enrollment History">
        <UimTable
          columns={[
            { key: 'effectiveDate', label: 'Effective Date' },
            { key: 'endDate', label: 'End Date' },
            { key: 'eligible', label: 'Eligible' },
            { key: 'ineligibleReasonLabel', label: 'Ineligible Reason' },
            { key: 'dojCountyCode', label: 'DOJ County' },
            { key: 'appealStatus', label: 'Appeal Status' },
            { key: 'appealStatusDate', label: 'Appeal Status Date' },
            { key: 'adminHearingDate', label: 'Admin Hearing Date' },
            { key: 'createdBy', label: 'Created By' },
            { key: 'createdOn', label: 'Created On' },
            { key: 'updatedBy', label: 'Updated By' },
            { key: 'historyCreated', label: 'History Created' },
          ]}
          data={history.map(h => ({
            ...h,
            effectiveDate: formatDate(h.effectiveDate),
            endDate: formatDate(h.endDate),
            ineligibleReasonLabel: getIneligibleReasonLabel(h.ineligibleReason),
            appealStatusDate: formatDate(h.appealStatusDate || h.appealDate),
            adminHearingDate: formatDate(h.adminHearingDate),
            createdOn: formatDate(h.createdAt || h.createdOn),
            historyCreated: formatDate(h.historyCreated || h.createdAt),
            createdBy: h.createdBy || '\u2014',
            updatedBy: h.updatedBy || '\u2014',
            eligible: h.eligible || '\u2014',
            appealStatus: h.appealStatus || '\u2014',
            dojCountyCode: h.dojCountyCode || '\u2014',
          }))}
          pageSize={50}
          emptyMessage="No enrollment history records found"
        />
      </UimSection>

      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-secondary" onClick={() => navigate(`/providers/${id}`)}>
          &larr; Back to Provider
        </button>
      </div>
    </UimPageLayout>
  );
};
