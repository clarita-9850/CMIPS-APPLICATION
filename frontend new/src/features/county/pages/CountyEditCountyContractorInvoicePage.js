import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { getInvoiceById, getInvoiceDetails, modifyInvoice } from '../../../api/countyContractorApi';
import { formatDate } from '../../../lib/providerConstants';

/**
 * Validate invoice edit form per DSD error messages.
 */
function validateForm(warrantNumber, paidDate, processedDate) {
  const errors = [];
  const hasWarrant = warrantNumber && warrantNumber.trim().length > 0;
  const hasPaidDate = !!paidDate;

  // EM 12: Paid Date is required when a warrant number is entered
  if (hasWarrant && !hasPaidDate) {
    errors.push('Paid Date is required when a warrant number is entered');
  }

  // EM 13: Warrant number is required when the Paid Date is entered
  if (hasPaidDate && !hasWarrant) {
    errors.push('Warrant number is required when the Paid Date is entered');
  }

  // EM 14: Paid Date must be later than Processed Date
  if (hasPaidDate && processedDate && paidDate < processedDate) {
    errors.push('Paid Date must be later than Processed Date');
  }

  return errors;
}

export function CountyEditCountyContractorInvoicePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [invoice, setInvoice] = useState(null);
  const [details, setDetails] = useState([]);
  const [warrantNumber, setWarrantNumber] = useState('');
  const [paidDate, setPaidDate] = useState('');
  const [loading, setLoading] = useState(true);
  const [errors, setErrors] = useState([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    Promise.all([
      getInvoiceById(id),
      getInvoiceDetails(id),
    ])
      .then(([invData, detData]) => {
        const inv = invData.invoice || invData;
        setInvoice(inv);
        setWarrantNumber(inv.warrantNumber || '');
        setPaidDate(inv.paidDate || '');
        setDetails(Array.isArray(detData) ? detData : (invData.details || []));
      })
      .catch(err => setErrors([err.message || 'Failed to load invoice']))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSave = async () => {
    const validationErrors = validateForm(warrantNumber, paidDate, invoice?.processedDate);
    if (validationErrors.length > 0) {
      setErrors(validationErrors);
      return;
    }
    setErrors([]);
    setSaving(true);
    try {
      await modifyInvoice(id, {
        warrantNumber: warrantNumber || null,
        paidDate: paidDate || null,
      });
      navigate(-1);
    } catch (err) {
      const msg = err?.data?.error || err.message || 'Save failed';
      setErrors([msg]);
    } finally {
      setSaving(false);
    }
  };

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
      pageId="County_editCountyContractorInvoice"
      title="Modify County Contractor:"
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {errors.length > 0 && (
        <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>
          {errors.map((e, i) => <div key={i}>{e}</div>)}
        </div>
      )}

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
              <UimField
                label="Warrant Number"
                name="warrantNumber"
                value={warrantNumber}
                onChange={(e) => setWarrantNumber(e.target.value)}
                maxLength={30}
              />
              <UimField
                label="Paid Date"
                type="date"
                name="paidDate"
                value={paidDate}
                onChange={(e) => setPaidDate(e.target.value)}
              />
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
        <button className="uim-btn uim-btn-primary" onClick={handleSave} disabled={saving || loading}>
          {saving ? 'Saving...' : 'Save'}
        </button>
        <button className="uim-btn uim-btn-secondary" onClick={() => navigate(-1)}>
          Cancel
        </button>
      </div>
    </UimPageLayout>
  );
}

export default CountyEditCountyContractorInvoicePage;
