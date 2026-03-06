/**
 * PersonDuplicateSearchForm – shared search form used by
 * New Referral, New Application, and Register a Provider duplicate checks.
 */

import { useNavigate } from 'react-router-dom';
import { COUNTY_OPTIONS, DISTRICT_OFFICE_OPTIONS, GENDER_OPTIONS } from '../../lib/providerConstants';

export const PersonDuplicateSearchForm = ({
  form, onChange, onSearch, onReset,
  continueLabel = 'Continue', onContinue, searched,
  results = [], loading = false,
}) => {
  const navigate = useNavigate();

  return (
    <form onSubmit={onSearch}>
      <section className="uim-cluster">
        <h2 className="uim-cluster-title">Search Criteria</h2>
        <div className="uim-cluster-body">

          {/* General */}
          <section className="uim-cluster" style={{ border: 'none', marginBottom: 0 }}>
            <h3 className="uim-cluster-title" style={{ fontSize: '0.9rem', background: '#4a7ba7' }}>General</h3>
            <div className="uim-cluster-body">
              <div className="uim-form-grid">
                <div className="uim-field">
                  <label>Last Name</label>
                  <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                    <input name="lastName" value={form.lastName} onChange={onChange} style={{ flex: '0 0 55%' }} />
                    <label className="uim-checkbox-row" style={{ marginTop: 0, display: 'flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.85rem' }}>
                      <input type="checkbox" name="soundsLike" checked={form.soundsLike} onChange={onChange} />
                      SX
                    </label>
                  </div>
                </div>
                <div className="uim-field">
                  <label>SSN</label>
                  <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                    <input name="ssn" value={form.ssn} onChange={onChange} placeholder="XXX-XX-XXXX" style={{ flex: 1 }} />
                    <label className="uim-checkbox-row" style={{ marginTop: 0, display: 'flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.85rem' }}>
                      <input type="checkbox" name="allSSN" checked={form.allSSN} onChange={onChange} />
                      All SSNs
                    </label>
                  </div>
                </div>
                <div className="uim-field">
                  <label>Person Type</label>
                  <select name="personType" value={form.personType} onChange={onChange}>
                    <option value="">-- Select --</option>
                    <option value="Recipient">Recipient</option>
                    <option value="Provider">Provider</option>
                    <option value="Other">Other</option>
                  </select>
                </div>
                <div className="uim-field">
                  <label>Date of Birth</label>
                  <input type="date" name="dateOfBirth" value={form.dateOfBirth} onChange={onChange} />
                </div>
                <div className="uim-field">
                  <label>County</label>
                  <select name="county" value={form.county} onChange={onChange}>
                    {COUNTY_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
                  </select>
                </div>
                <div className="uim-field"><label>First Name</label><input name="firstName" value={form.firstName} onChange={onChange} /></div>
                <div className="uim-field"><label>CIN</label><input name="cin" value={form.cin} onChange={onChange} /></div>
                <div className="uim-field"><label>Provider ID</label><input name="providerID" value={form.providerID} onChange={onChange} /></div>
                <div className="uim-field">
                  <label>Gender</label>
                  <select name="gender" value={form.gender} onChange={onChange}>
                    {GENDER_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
                  </select>
                </div>
                <div className="uim-field">
                  <label>District Office</label>
                  <select name="districtOffice" value={form.districtOffice} onChange={onChange}>
                    {DISTRICT_OFFICE_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
                  </select>
                </div>
              </div>
            </div>
          </section>

          {/* Address */}
          <section className="uim-cluster" style={{ border: 'none', marginBottom: 0, marginTop: '0.5rem' }}>
            <h3 className="uim-cluster-title" style={{ fontSize: '0.9rem', background: '#4a7ba7' }}>Address</h3>
            <div className="uim-cluster-body">
              <div className="uim-form-grid">
                <div className="uim-field"><label>Street Number</label><input name="streetNumber" value={form.streetNumber} onChange={onChange} /></div>
                <div className="uim-field">
                  <label>Unit Type</label>
                  <select name="unitType" value={form.unitType} onChange={onChange}>
                    <option value="">-- Select --</option>
                    <option value="APT">APT</option>
                    <option value="STE">STE</option>
                    <option value="UNIT">UNIT</option>
                  </select>
                </div>
                <div className="uim-field"><label>City</label><input name="city" value={form.city} onChange={onChange} /></div>
                <div className="uim-field"><label>Street Name</label><input name="streetName" value={form.streetName} onChange={onChange} /></div>
                <div className="uim-field"><label>Unit Number</label><input name="unitNumber" value={form.unitNumber} onChange={onChange} /></div>
              </div>
            </div>
          </section>

          {/* Other Contact Info */}
          <section className="uim-cluster" style={{ border: 'none', marginBottom: 0, marginTop: '0.5rem' }}>
            <h3 className="uim-cluster-title" style={{ fontSize: '0.9rem', background: '#4a7ba7' }}>Other Contact Info</h3>
            <div className="uim-cluster-body">
              <div className="uim-form-grid">
                <div className="uim-field"><label>Email Address</label><input type="email" name="emailAddress" value={form.emailAddress} onChange={onChange} /></div>
                <div className="uim-field">
                  <label>Phone</label>
                  <div style={{ display: 'flex', gap: '0.4rem' }}>
                    <input name="phoneAreaCode" value={form.phoneAreaCode} onChange={onChange} placeholder="Area" style={{ width: '70px' }} maxLength={3} />
                    <input name="phoneNumber" value={form.phoneNumber} onChange={onChange} placeholder="Number" style={{ flex: 1 }} maxLength={7} />
                  </div>
                </div>
              </div>
              <div className="uim-action-bar center" style={{ marginTop: '1rem' }}>
                <button type="submit" className="uim-btn uim-btn-primary">Search</button>
                <button type="button" className="uim-btn uim-btn-secondary" onClick={onReset}>Reset</button>
                {onContinue && (
                  <button type="button" className="uim-btn uim-btn-secondary" onClick={onContinue}>
                    {continueLabel}
                  </button>
                )}
              </div>
            </div>
          </section>

        </div>
      </section>

      {/* Search Results */}
      {searched && (
        <section className="uim-cluster" style={{ marginTop: '1.5rem' }}>
          <h2 className="uim-cluster-title">Search Results ({results.length})</h2>
          <div className="uim-cluster-body">
            {loading ? (
              <p style={{ padding: '1rem', textAlign: 'center' }}>Searching...</p>
            ) : results.length === 0 ? (
              <p style={{ padding: '1rem', textAlign: 'center' }}>No matching records found. You may continue with registration.</p>
            ) : (
              <div className="uim-table-wrapper">
                <table className="uim-table">
                  <thead>
                    <tr>
                      <th>Full Name</th><th>SSN</th><th>Provider ID</th>
                      <th>Date of Birth</th><th>Gender</th><th>Type</th>
                      <th>Address</th><th>City</th><th>County</th>
                    </tr>
                  </thead>
                  <tbody>
                    {results.map(row => {
                      const ssn = row.ssn || '';
                      const cleanSsn = ssn.replace(/-/g, '');
                      return (
                        <tr key={row.id}>
                          <td className="link-cell">
                            <button type="button" onClick={() => navigate(`/providers/${row.id}`)}>
                              {`${row.lastName || ''}, ${row.firstName || ''}`.replace(/^, |, $/g, '') || '\u2014'}
                            </button>
                          </td>
                          <td>{cleanSsn.length >= 4 ? `***-**-${cleanSsn.slice(-4)}` : '\u2014'}</td>
                          <td>{row.providerNumber || '\u2014'}</td>
                          <td>{row.dateOfBirth || '\u2014'}</td>
                          <td>{row.gender || '\u2014'}</td>
                          <td>Provider</td>
                          <td>{row.streetAddress || row.address || '\u2014'}</td>
                          <td>{row.city || '\u2014'}</td>
                          <td>{row.countyCode || '\u2014'}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </section>
      )}
    </form>
  );
};
