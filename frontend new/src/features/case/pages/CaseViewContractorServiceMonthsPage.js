import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { formatDate } from '../../../lib/providerConstants';

/**
 * CaseViewContractorServiceMonthsPage
 * Displays detail for a single contractor service month record.
 */
export function CaseViewContractorServiceMonthsPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const record = location.state?.record || {};

  const fmtAmt = (val) => val != null ? `$${Number(val).toFixed(2)}` : null;

  return (
    <UimPageLayout
      pageId="Case_viewContractorServiceMonths"
      title="View County Contractor Service Month Detail:"
      hidePlaceholderBanner={true}
    >
      <UimSection title="Details">
        <div className="uim-form-grid">
          <UimField label="Service Month" value={formatDate(record.serviceMonth)} />
          <UimField label="Billing Month" value={formatDate(record.billingMonth)} />
          <UimField label="Contractor" value={record.contractorName} />
          <UimField label="Hours" value={record.hours} />
          <UimField label="Amount" value={fmtAmt(record.amount)} />
        </div>
      </UimSection>

      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-secondary" onClick={() => navigate(-1)}>
          Close
        </button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewContractorServiceMonthsPage;
