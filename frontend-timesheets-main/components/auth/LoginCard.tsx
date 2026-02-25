'use client';

import React, { useState, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/contexts/AuthContext';
import Alert from '@/components/Alert';

interface LoginCardProps {
  redirectPath?: string;
  title?: string;
  subtitle?: string;
}

export default function LoginCard({
  redirectPath = '/',
  title = 'Login',
  subtitle = 'Access the Timesheet Reporting System',
}: LoginCardProps) {
  const router = useRouter();
  const { login } = useAuth();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await login(username, password);
      // Redirect based on user role
      // Case workers go to My Workspace, supervisors and others go to dashboard/home
      const userRole = response?.user?.role?.toUpperCase();
      if (userRole === 'CASE_WORKER') {
        router.push('/my-workspace');
      } else {
        router.push(redirectPath);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <div className="card-body" style={{ padding: '2rem' }}>
        <div className="text-center mb-4">
          <span className="badge bg-primary mb-2" style={{ textTransform: 'uppercase' }}>
            <span className="ca-gov-icon-lock-fill" aria-hidden="true"></span>
            {' '}Secure Access
          </span>
          <h2 className="card-title mb-2">{title}</h2>
          <p style={{ color: 'var(--gray-600, #72717c)' }}>{subtitle}</p>
        </div>
        {error && (
          <Alert
            type="danger"
            message={error}
            onClose={() => setError('')}
          />
        )}
        <form onSubmit={handleSubmit}>
          <div className="form-group mb-3">
            <label htmlFor="username" className="form-label" style={{ fontWeight: 600 }}>
              <span className="ca-gov-icon-user" aria-hidden="true"></span>
              {' '}Username
            </label>
            <input
              type="text"
              className="form-control"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoComplete="username"
              placeholder="Enter your username"
              aria-describedby="username-help"
            />
          </div>
          <div className="form-group mb-3">
            <label htmlFor="password" className="form-label" style={{ fontWeight: 600 }}>
              <span className="ca-gov-icon-lock-fill" aria-hidden="true"></span>
              {' '}Password
            </label>
            <input
              type="password"
              className="form-control"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete="current-password"
              placeholder="Enter your password"
              aria-describedby="password-help"
            />
          </div>
          <button 
            type="submit" 
            className="btn btn-primary w-100" 
            disabled={loading}
            style={{ width: '100%' }}
          >
            <span className="ca-gov-icon-arrow-right" aria-hidden="true"></span>
            {' '}{loading ? 'Logging in...' : 'Sign In'}
          </button>
        </form>
      </div>
    </div>
  );
}

