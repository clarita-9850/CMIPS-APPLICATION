/**
 * PersonDuplicateSearchForm – shared search form used by
 * New Referral, New Application, and Register a Provider duplicate checks.
 */

import { useNavigate } from 'react-router-dom';
import { mockPersonSearchResults, mockCountyOptions, mockDistrictOfficeOptions } from './mockData';

export const PersonDuplicateSearchForm = ({
  form, onChange, onSearch, onReset,
  continueLabel = 'Continue', onContinue, searched,
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
                    <label className="uim-checkbox-row" style={{ marginTop: 0 }}>
                      <input type="checkbox" name="soundsLike" checked={form.soundsLike} onChange={onChange} />
                      Sounds-like
                    </label>
                  </div>
                </div>
                <div className="uim-field">
                  <label>SSN</label>
                  <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                    <input name="ssn" value={form.ssn} onChange={onChange} placeholder="XXX-XX-XXXX" style={{ flex: 1 }} />
                    <label className="uim-checkbox-row" style={{ marginTop: 0 }}>
                      <input type="checkbox" name="allSSN" checked={form.allSSN} onChange={onChange} />
                      All SSN
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
                    {mockCountyOptions.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
                  </select>
                </div>
                <div className="uim-field"><label>First Name</label><input name="firstName" value={form.firstName} onChange={onChange} /></div>
                <div className="uim-field"><label>CIN</label><input name="cin" value={form.cin} onChange={onChange} /></div>
                <div className="uim-field"><label>Provider ID</label><input name="providerID" value={form.providerID} onChange={onChange} /></div>
                <div className="uim-field">
                  <label>Gender</label>
                  <select name="gender" value={form.gender} onChange={onChange}>
                    <option value="">-- Select --</option>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Other">Other</option>
                  </select>
                </div>
                <div className="uim-field">
                  <label>District Office</label>
                  <select name="districtOffice" value={form.districtOffice} onChange={onChange}>
                    {mockDistrictOfficeOptions.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
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
          <h2 className="uim-cluster-title">Search Results</h2>
          <div className="uim-cluster-body">
            <div className="uim-table-wrapper">
              <table className="uim-table">
                <thead>
                  <tr>
                    <th>Full Name</th><th>SSN</th><th>Type</th><th>CIN</th>
                    <th>Date of Birth</th><th>Gender</th><th>Person Type</th>
                    <th>Residence Address</th><th>City</th><th>County</th>
                  </tr>
                </thead>
                <tbody>
                  {mockPersonSearchResults.map(row => (
                    <tr key={row.id}>
                      <td className="link-cell">
                        <button type="button" onClick={() => navigate('/person/home-page', { state: { concernRoleID: row.concernRoleID } })}>
                          {row.personFullName}
                        </button>
                      </td>
                      <td>{row.socialSecurityNumber}</td>
                      <td>{row.ssnType}</td>
                      <td>{row.cin}</td>
                      <td>{row.dateOfBirth}</td>
                      <td>{row.sex}</td>
                      <td>{row.personType}</td>
                      <td>{row.residenceAddress}</td>
                      <td>{row.city}</td>
                      <td>{row.countyCode}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="uim-pagination">
              <button type="button" className="uim-btn uim-btn-secondary" disabled>← Previous</button>
              <button type="button" className="uim-btn uim-btn-secondary" disabled>Next →</button>
            </div>
          </div>
        </section>
      )}
    </form>
  );
};
