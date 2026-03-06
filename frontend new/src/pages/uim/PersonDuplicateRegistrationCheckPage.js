/**
 * PersonDuplicateRegistrationCheckPage
 * Route: /person/duplicate-registration-check
 * "New Application" shortcut from My Shortcuts.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './UimPage.css';
import { PersonDuplicateSearchForm } from './PersonDuplicateSearchForm';
import http from '../../api/httpClient';

const emptyForm = {
  lastName: '', soundsLike: false, ssn: '', allSSN: false,
  personType: '', dateOfBirth: '', county: '', firstName: '', cin: '',
  providerID: '', gender: '', districtOffice: '',
  streetNumber: '', unitType: '', city: '', streetName: '', unitNumber: '',
  emailAddress: '', phoneAreaCode: '', phoneNumber: '',
};

export const PersonDuplicateRegistrationCheckPage = () => {
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

    const params = {};
    if (form.lastName) params.lastName = form.lastName;
    if (form.firstName) params.firstName = form.firstName;
    if (form.ssn) params.ssn = form.ssn;
    if (form.county) params.countyCode = form.county;
    if (form.providerID) params.providerNumber = form.providerID;

    const qs = new URLSearchParams(params).toString();
    const url = qs ? `/providers/search?${qs}` : '/providers';

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

  return (
    <div className="uim-page">
      <div className="container">
        <h1 className="page-title">New Application – Duplicate Check</h1>
        <PersonDuplicateSearchForm
          form={form}
          onChange={handleChange}
          onSearch={handleSearch}
          onReset={handleReset}
          continueLabel="Continue Registration"
          onContinue={() => navigate('/person/register')}
          searched={searched}
          results={results}
          loading={loading}
        />
      </div>
    </div>
  );
};
