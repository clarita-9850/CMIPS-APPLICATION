import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getRateById, updateRate } from '../../../api/countyContractorApi';

/**
 * Validate rate form per DSD error messages (same rules as create, minus county check).
 */
function validateForm(form) {
  const errors = [];

  // EM 14: Effective Date must be the first day of a month
  if (form.fromDate) {
    const d = new Date(form.fromDate);
    if (d.getUTCDate() !== 1) {
      errors.push('Effective Date must be the first day of a month');
    }
  }

  // EM 18: End Date must be the last day of a month
  if (form.toDate) {
    const d = new Date(form.toDate);
    const lastDay = new Date(d.getUTCFullYear(), d.getUTCMonth() + 1, 0).getUTCDate();
    if (d.getUTCDate() !== lastDay) {
      errors.push('End Date must be the last day of a month');
    }
  }

  // EM 17: Effective Date must be on or before End Date
  if (form.fromDate && form.toDate && form.fromDate > form.toDate) {
    errors.push('Effective Date must be on or before End Date');
  }

  // EM 15: Rate must be >= Wage
  const rate = parseFloat(form.rateAmt) || 0;
  const wage = parseFloat(form.wageAmt) || 0;
  if (form.rateAmt && form.wageAmt && rate < wage) {
    errors.push('Rate must be greater than or equal to Wage');
  }

  return errors;
}

export function CountyEditCountyContractorRatePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [form, setForm] = useState({
    contractorName: '',
    countyCode: '',
    fromDate: '',
    toDate: '',
    rateAmt: '',
    wageAmt: '',
    macrAmt: '',
  });
  const [loading, setLoading] = useState(true);
  const [errors, setErrors] = useState([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    getRateById(id)
      .then(data => {
        setForm({
          contractorName: data.contractorName || '',
          countyCode: data.countyCode || '',
          fromDate: data.fromDate || '',
          toDate: data.toDate || '',
          rateAmt: data.rateAmt != null ? String(data.rateAmt) : '',
          wageAmt: data.wageAmt != null ? String(data.wageAmt) : '',
          macrAmt: data.macrAmt != null ? String(data.macrAmt) : '',
        });
      })
      .catch(err => setErrors([err.message || 'Failed to load rate']))
      .finally(() => setLoading(false));
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    const validationErrors = validateForm(form);
    if (validationErrors.length > 0) {
      setErrors(validationErrors);
      return;
    }
    setErrors([]);
    setSaving(true);
    try {
      await updateRate(id, {
        countyCode: form.countyCode,
        contractorName: form.contractorName,
        fromDate: form.fromDate,
        toDate: form.toDate || null,
        rateAmt: parseFloat(form.rateAmt) || 0,
        wageAmt: parseFloat(form.wageAmt) || 0,
        macrAmt: parseFloat(form.macrAmt) || 0,
      });
      navigate('/county/list-county-contractor-rate');
    } catch (err) {
      const msg = err?.data?.error || err.message || 'Save failed';
      setErrors([msg]);
    } finally {
      setSaving(false);
    }
  };

  return (
    <UimPageLayout
      pageId="County_editCountyContractorRate"
      title="Modify County Contractor Rate:"
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {errors.length > 0 && (
        <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>
          {errors.map((e, i) => <div key={i}>{e}</div>)}
        </div>
      )}

      {!loading && (
        <UimSection title="Details">
          <div className="uim-form-grid">
            <UimField
              label="Contractor Name"
              value={form.contractorName}
              readOnly
            />
            <UimField
              label="Effective Date"
              type="date"
              name="fromDate"
              value={form.fromDate}
              onChange={handleChange}
              required
            />
            <UimField
              label="End Date"
              type="date"
              name="toDate"
              value={form.toDate}
              onChange={handleChange}
            />
            <UimField
              label="Rate"
              type="number"
              name="rateAmt"
              value={form.rateAmt}
              onChange={handleChange}
              placeholder="0.00"
              required
            />
            <UimField
              label="Wage"
              type="number"
              name="wageAmt"
              value={form.wageAmt}
              onChange={handleChange}
              placeholder="0.00"
              required
            />
            <UimField
              label="MACR"
              type="number"
              name="macrAmt"
              value={form.macrAmt}
              onChange={handleChange}
              placeholder="0.00"
            />
          </div>
        </UimSection>
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

export default CountyEditCountyContractorRatePage;
