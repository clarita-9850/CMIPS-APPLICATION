import React, { useState } from 'react';
import PropTypes from 'prop-types';
import './LoginPage.css';

/**
 * @summary CMIPS Portal Login Page
 * 
 * Login page for California's In-Home Supportive Services (IHSS) CMIPS Portal
 * 
 * @param {function} onLogin - Callback function when login is submitted
 * @param {boolean} isLoading - Loading state for login process
 * @param {string} errorMessage - Error message to display
 * 
 * @returns {React.ReactElement} Returns the Login Page component
 */

const LoginPage = ({ onLogin, isLoading = false, errorMessage = '' }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [formErrors, setFormErrors] = useState({});

  const validateForm = () => {
    const errors = {};
    
    if (!username.trim()) {
      errors.username = 'Username is required';
    }
    
    if (!password.trim()) {
      errors.password = 'Password is required';
    }
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (validateForm() && onLogin) {
      onLogin({ username, password });
    }
  };

  return (
    <div className="csp-login-page">
      <div className="csp-login-wrapper">
        {/* Unified Header - Matching Workspace */}
        <div className="unified-header">
          <div className="unified-header-content">
            <div className="header-left">
              <img 
                src="https://www.cdss.ca.gov/Portals/13/Images/cdss-logo-v3.png?ver=clYTY_iqlcDpaW8FClTMww%3d%3d" 
                alt="California Department of Social Services" 
                className="cdss-logo"
              />
              <div className="cdss-text">
                <span className="cdss-label">Welcome to CMIPS</span>
                <span className="cdss-subtitle">California Department of Social Services</span>
              </div>
            </div>
          </div>
        </div>

        {/* Main Content */}
        <div className="container">
          <div className="csp-content">
            <div className="row">
              {/* Login Form Section */}
              <div className="col-md-7">
                <div className="csp-login-box">
                  <h2 className="csp-heading">Login to Your Account</h2>
                  
                  {/* Error Alert */}
                  {errorMessage && (
                    <div className="alert alert-danger" role="alert">
                      <strong>Error:</strong> {errorMessage}
                    </div>
                  )}

                  {/* Login Form */}
                  <form onSubmit={handleSubmit} noValidate>
                    {/* Username Field */}
                    <div className="form-group">
                      <label htmlFor="username" className="csp-label">
                        Username <span className="text-danger">*</span>
                      </label>
                      <input
                        type="text"
                        id="username"
                        name="username"
                        className={`form-control csp-input ${formErrors.username ? 'is-invalid' : ''}`}
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        disabled={isLoading}
                        autoComplete="username"
                        aria-required="true"
                        placeholder="Enter your username"
                      />
                      {formErrors.username && (
                        <div className="invalid-feedback">
                          {formErrors.username}
                        </div>
                      )}
                    </div>

                    {/* Password Field */}
                    <div className="form-group">
                      <label htmlFor="password" className="csp-label">
                        Password <span className="text-danger">*</span>
                      </label>
                      <input
                        type="password"
                        id="password"
                        name="password"
                        className={`form-control csp-input ${formErrors.password ? 'is-invalid' : ''}`}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        disabled={isLoading}
                        autoComplete="current-password"
                        aria-required="true"
                        placeholder="Enter your password"
                      />
                      {formErrors.password && (
                        <div className="invalid-feedback">
                          {formErrors.password}
                        </div>
                      )}
                    </div>

                    {/* Submit Button */}
                    <div className="form-group">
                      <button
                        type="submit"
                        className="btn btn-primary csp-login-btn"
                        disabled={isLoading}
                      >
                        {isLoading ? 'Logging in...' : 'Login'}
                      </button>
                    </div>

                  </form>
                </div>
              </div>

              {/* Information Section */}
              <div className="col-md-5">
                <div className="csp-info-box">
                  <h3 className="csp-info-heading">Welcome to CMIPS</h3>
                  <p className="csp-info-text">
                    The CMIPS Portal provides secure access to Case Management Information and Payroll System 
                    for the In-Home Supportive Services (IHSS) program.
                  </p>
                  
                  <div className="csp-help">
                    <h4 className="csp-help-title">Need Help?</h4>
                    <p className="csp-help-text">
                      <strong>Technical Support:</strong><br />
                      Contact the CMIPS Help Desk<br />
                      Monday - Friday, 8:00 AM - 5:00 PM PST
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="csp-footer">
          <div className="container">
            <div className="csp-footer-content">
              <p className="csp-footer-text">
                © 2026 California Department of Social Services. All rights reserved.
              </p>
              <div className="csp-footer-links">
                <a href="https://www.cdss.ca.gov/Privacy-Policy" target="_blank" rel="noopener noreferrer">
                  Privacy Policy
                </a>
                <span>|</span>
                <a href="https://www.cdss.ca.gov/Accessibility" target="_blank" rel="noopener noreferrer">
                  Accessibility
                </a>
                <span>|</span>
                <a href="https://www.cdss.ca.gov/Contact" target="_blank" rel="noopener noreferrer">
                  Contact Us
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

LoginPage.propTypes = {
  onLogin: PropTypes.func,
  isLoading: PropTypes.bool,
  errorMessage: PropTypes.string
};

export default LoginPage;
