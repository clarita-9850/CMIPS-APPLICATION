/**
 * PersonDuplicateProviderCheckPage
 * Route: /person/duplicate-provider-check
 * "Register a Provider" shortcut from My Shortcuts.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../shared/components/UimPage.css';
import { PersonDuplicateSearchForm } from './PersonDuplicateSearchForm';
import http from '../../api/httpClient';

const emptyForm = {
  lastName: '', soundsLike: false, ssn: '', allSSN: false,
  personType: '', dateOfBirth: '', county: '', firstName: '', cin: '',
  providerID: '', gender: '', districtOffice: '',
  streetNumber: '', unitType: '', city: '', streetName: '', unitNumber: '',
  emailAddress: '', phoneAreaCode: '', phoneNumber: '',
};

export const PersonDuplicateProviderCheckPage = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ ...emptyForm });
  const [searched, setSearched] = useState(false);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm(f => ({ ...f, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setLoading(true);
    setSearched(true);

    // Build search params from form — map frontend field names to backend params
    const params = {};
    if (form.lastName) params.lastName = form.lastName;
    if (form.firstName) params.firstName = form.firstName;
    if (form.ssn) params.ssn = form.ssn.replace(/-/g, '');
    if (form.county) params.countyCode = form.county;
    if (form.providerID) params.providerNumber = form.providerID;
    if (form.dateOfBirth) params.dateOfBirth = form.dateOfBirth;
    if (form.gender) params.gender = form.gender;
    if (form.city) params.city = form.city;
    if (form.emailAddress) params.email = form.emailAddress;
    if (form.phoneAreaCode || form.phoneNumber) {
      params.phone = (form.phoneAreaCode || '') + (form.phoneNumber || '');
    }

    // Require at least one search field
    if (Object.keys(params).length === 0) {
      setResults([]);
      setLoading(false);
      return;
    }

    const qs = new URLSearchParams(params).toString();
    const url = `/providers/search?${qs}`;

    http.get(url)
      .then(res => {
        const d = res?.data;
        const resultArray = Array.isArray(d) ? d : (d?.content || d?.items || []);
        setResults(resultArray);
      })
      .catch(err => {
        console.error('Search error:', err);
        setResults([]);
      })
      .finally(() => setLoading(false));
  };

  const handleReset = () => {
    setForm({ ...emptyForm });
    setSearched(false);
    setResults([]);
  };

  const handleContinue = () => {
    // Pass pre-fill data to registration page via query params
    const prefill = new URLSearchParams();
    if (form.firstName) prefill.set('firstName', form.firstName);
    if (form.lastName) prefill.set('lastName', form.lastName);
    if (form.ssn) prefill.set('ssn', form.ssn);
    if (form.dateOfBirth) prefill.set('dob', form.dateOfBirth);
    if (form.gender) prefill.set('gender', form.gender);
    if (form.county) prefill.set('county', form.county);
    const qs = prefill.toString();
    navigate(`/providers/register${qs ? '?' + qs : ''}`);
  };

  return (
    <div className="uim-page">
      <div className="container">
        <h1 className="page-title">Register a Provider &ndash; Duplicate Check</h1>
        <PersonDuplicateSearchForm
          form={form}
          onChange={handleChange}
          onSearch={handleSearch}
          onReset={handleReset}
          continueLabel="Continue Registration"
          onContinue={handleContinue}
          searched={searched}
          results={results}
          loading={loading}
        />
      </div>
    </div>
  );
};
