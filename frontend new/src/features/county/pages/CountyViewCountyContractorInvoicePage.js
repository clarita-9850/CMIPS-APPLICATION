import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { getInvoiceById, getInvoiceDetails, getSoc432 } from '../../../api/countyContractorApi';
import { formatDate } from '../../../lib/providerConstants';

export function CountyViewCountyContractorInvoicePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [invoice, setInvoice] = useState(null);
  const [details, setDetails] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    Promise.all([
      getInvoiceById(id),
      getInvoiceDetails(id),
    ])
      .then(([invData, detData]) => {
        // getInvoiceById returns { invoice, details } or the invoice directly
        const inv = invData.invoice || invData;
        setInvoice(inv);
        setDetails(Array.isArray(detData) ? detData : (invData.details || []));
      })
      .catch(err => setError(err.message || 'Failed to load invoice'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSoc432 = async () => {
    try {
      await getSoc432(id);
      navigate('/county/print-contract-expenditure-report');
    } catch (err) {
      const msg = err?.data?.error || err.message || 'Failed to generate report';
      alert(msg);
    }
  };

  const isPaid = invoice && invoice.status === 'Paid';
  const hasWarrantAndPaidDate = invoice && invoice.warrantNumber && invoice.paidDate;

  const detailColumns = [
    { key: 'caseCount', label: 'Case Count' },
    { key: 'fundingAidCode', label: 'Funding Source' },
    { key: 'serviceMonth', label: 'Service Month' },
    { key: 'amount', label: 'Amount' },
  ];

  const displayDetails = details.map(d => ({
    ...d,
    serviceMonth: formatDate(d.serviceMonth),
    amount: d.amount != null ? `$${Number(d.amount).toFixed(2)}` : '\u2014',
  }));

  const fmtAmt = (val) => val != null ? `$${Number(val).toFixed(2)}` : null;

  return (
    <UimPageLayout
      pageId="County_viewCountyContractorInvoice"
      title="View County Contractor:"
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>{error}</div>}

      {invoice && (
        <>
          <UimSection title="Invoice Details">
            <div className="uim-form-grid">
              <UimField label="Invoice Number" value={invoice.invoiceNumber} />
              <UimField label="Invoice Date" value={formatDate(invoice.invoiceDate)} />
              <UimField label="Processed Date" value={formatDate(invoice.processedDate)} />
              <UimField label="Billing Month" value={formatDate(invoice.billingMonth)} />
              <UimField label="Original Amount" value={fmtAmt(invoice.originalAmt)} />
              <UimField label="Rejected Amount" value={fmtAmt(invoice.rejectedAmt)} />
              <UimField label="Cut Back Amount" value={fmtAmt(invoice.cutBackAmt)} />
              <UimField label="SOC Collected Amount" value={fmtAmt(invoice.socCollectedAmt)} />
              <UimField label="Authorized Amount" value={fmtAmt(invoice.authorizedAmt)} />
              <UimField label="Warrant Number" value={invoice.warrantNumber} />
              <UimField label="Paid Date" value={formatDate(invoice.paidDate)} />
              <UimField label="Status" value={invoice.status} />
            </div>
          </UimSection>

          <UimSection title="Payment Details">
            <UimTable
              columns={detailColumns}
              data={displayDetails}
              emptyMessage="No payment details found"
            />
          </UimSection>
        </>
      )}

      <div className="uim-action-bar">
        {hasWarrantAndPaidDate && (
          <button className="uim-btn uim-btn-primary" onClick={handleSoc432}>
            Contract Expenditures Form
          </button>
        )}
        {invoice && invoice.status === 'Pending' && (
          <button className="uim-btn uim-btn-primary" onClick={() => navigate(`/county/edit-county-contractor-invoice/${id}`)}>
            Edit
          </button>
        )}
        <button className="uim-btn uim-btn-secondary" onClick={() => navigate(-1)}>
          Close
        </button>
      </div>
    </UimPageLayout>
  );
}

export default CountyViewCountyContractorInvoicePage;
