/**
 * PersonDuplicateProviderCheckPage
 * Route: /person/duplicate-provider-check
 * "Register a Provider" shortcut from My Shortcuts.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './UimPage.css';
import { PersonDuplicateSearchForm } from './PersonDuplicateSearchForm';

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

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm(f => ({ ...f, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setSearched(true);
  };

  const handleReset = () => {
    setForm({ ...emptyForm });
    setSearched(false);
  };

  return (
    <div className="uim-page">
      <div className="container">
        <h1 className="page-title">Register a Provider – Duplicate Check</h1>
        <PersonDuplicateSearchForm
          form={form}
          onChange={handleChange}
          onSearch={handleSearch}
          onReset={handleReset}
          continueLabel="Continue Registration"
          onContinue={() => navigate('/person/register-provider')}
          searched={searched}
        />
      </div>
    </div>
  );
};
