'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import dynamic from 'next/dynamic';
import NotificationCenter from '@/components/NotificationCenter';
import apiClient from '@/lib/api';

type Timesheet = {
  id: number;
  employeeName: string;
  payPeriodStart: string;
  payPeriodEnd: string;
  totalHours: number;
  createdAt: string;
  status: string;
};

type Provider = {
  id: number;
  name: string;
  status: string;
  role: string;
};

function RecipientDashboardComponent() {
  const { user, logout, loading: authLoading } = useAuth();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [pendingTimesheets, setPendingTimesheets] = useState<Timesheet[]>([]);
  const [providers, setProviders] = useState<Provider[]>([]);

  useEffect(() => {
    if (authLoading) return;
    if (!user || (user.role !== 'RECIPIENT' && !user.roles?.includes('RECIPIENT'))) {
      window.location.href = '/login';
      return;
    }
    fetchDashboardData();
  }, [user, authLoading]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      // Fetch pending timesheets
      try {
        const timesheetsResponse = await apiClient.get('/timesheets');
        const timesheets = timesheetsResponse.data.content || timesheetsResponse.data || [];
        setPendingTimesheets(timesheets.filter((ts: Timesheet) => ts.status === 'SUBMITTED'));
      } catch (err) {
        console.error('Error fetching timesheets:', err);
        setPendingTimesheets([]);
      }
      
      // Fetch providers
      try {
        const providersResponse = await apiClient.get('/provider-recipient/my-providers');
        if (providersResponse.data && providersResponse.data.length > 0) {
          const mappedProviders = providersResponse.data.map((rel: any) => ({
            id: rel.id || 1,
            name: rel.providerName || 'Provider',
            status: rel.status || 'Active',
            role: rel.relationship || 'Primary Caregiver',
          }));
          setProviders(mappedProviders);
        } else {
          setProviders([
            { id: 1, name: 'John Doe', status: 'Active', role: 'Primary Caregiver' },
            { id: 2, name: 'Mary Johnson', status: 'Active', role: 'Backup' }
          ]);
        }
      } catch (err) {
        console.error('Error fetching providers:', err);
        setProviders([
          { id: 1, name: 'John Doe', status: 'Active', role: 'Primary Caregiver' },
          { id: 2, name: 'Mary Johnson', status: 'Active', role: 'Backup' }
        ]);
      }
    } catch (err) {
      console.error('Error fetching dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading || authLoading || !user) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading Recipient Dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
      {/* Header */}
      <header role="banner" style={{ backgroundColor: 'white', borderBottom: '1px solid #e5e7eb', position: 'sticky', top: 0, zIndex: 100 }}>
        <div style={{ padding: '1rem 0', borderBottom: '1px solid #e5e7eb' }}>
          <div className="container">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-p2, #046b99)', margin: 0 }}>
                  CMIPSII Case Management Information Payroll System II
                </h1>
                <p className="text-muted mb-0" style={{ fontSize: '0.875rem', margin: '0.25rem 0 0 0' }}>
                  Recipient Dashboard
                </p>
              </div>
              <div className="d-flex align-items-center gap-3">
                <NotificationCenter userId={user?.username || ''} />
                <span className="text-muted">
                  Welcome, <strong>{user?.username || 'User'}</strong>
                </span>
                <button 
                  type="button" 
                  onClick={logout}
                  className="btn btn-danger"
                >
                  <span className="ca-gov-icon-logout" aria-hidden="true"></span>
                  Logout
                </button>
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container" style={{ padding: '1.5rem 0' }}>
        {/* Quick Actions */}
        <div className="row mb-4">
          <div className="col-lg-3 col-md-6 mb-3">
            <div
              className="card h-100"
              style={{ cursor: 'pointer' }}
              onClick={() => router.push('/recipient/timesheets')}
            >
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>📋</div>
                <h3 className="card-title">
                  TIMESHEETS TO REVIEW
                  {pendingTimesheets.length > 0 && (
                    <span className="badge bg-danger ms-2">{pendingTimesheets.length} Pending</span>
                  )}
                </h3>
                <p className="text-muted small mb-0">Review & approve timesheets</p>
              </div>
            </div>
          </div>

          <div className="col-lg-3 col-md-6 mb-3">
            <div
              className="card h-100"
              style={{ cursor: 'pointer' }}
              onClick={() => router.push('/recipient/providers')}
            >
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>👥</div>
                <h3 className="card-title">MY PROVIDERS</h3>
                <p className="text-muted small mb-0">Manage your caregivers</p>
              </div>
            </div>
          </div>

          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card h-100" style={{ cursor: 'pointer' }}>
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>📅</div>
                <h3 className="card-title">SERVICE SCHEDULE</h3>
                <p className="text-muted small mb-0">View upcoming services</p>
              </div>
            </div>
          </div>

          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card h-100" style={{ cursor: 'pointer' }}>
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>❓</div>
                <h3 className="card-title">HELP & SUPPORT</h3>
                <p className="text-muted small mb-0">Get assistance</p>
              </div>
            </div>
          </div>
        </div>

        {/* Timesheets Awaiting Approval */}
        <div className="card mb-4">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>🔔 TIMESHEETS AWAITING YOUR APPROVAL</h2>
          </div>
          <div className="card-body">
            {pendingTimesheets.length === 0 ? (
              <p className="text-center text-muted py-4">No timesheets pending review</p>
            ) : (
              <div>
                {pendingTimesheets.map((timesheet) => (
                  <div key={timesheet.id} className="alert alert-warning mb-3" style={{ borderLeft: '4px solid #ffc107' }}>
                    <div className="d-flex justify-content-between align-items-center">
                      <div>
                        <h5 className="fw-semibold mb-1">
                          {timesheet.employeeName} - {timesheet.payPeriodStart} to {timesheet.payPeriodEnd}
                        </h5>
                        <p className="text-muted small mb-0">
                          Total Hours: {timesheet.totalHours} • Submitted: {new Date(timesheet.createdAt).toLocaleDateString()}
                        </p>
                      </div>
                      <button
                        onClick={() => router.push(`/recipient/timesheet/${timesheet.id}`)}
                        className="btn btn-primary"
                      >
                        Review & Approve
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* My Providers */}
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>👥 MY PROVIDERS</h2>
          </div>
          <div className="card-body">
            {providers.length === 0 ? (
              <p className="text-center text-muted py-4">No providers assigned</p>
            ) : (
              <div>
                {providers.map((provider) => (
                  <div key={provider.id} className="card mb-3">
                    <div className="card-body">
                      <div className="d-flex justify-content-between align-items-center">
                        <div>
                          <h5 className="fw-semibold mb-1">{provider.name}</h5>
                          <p className="text-muted small mb-0">
                            <span className="badge bg-success">{provider.status}</span>
                            {' • '} {provider.role}
                          </p>
                        </div>
                        <button className="btn btn-secondary">
                          View Details
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

export default dynamic(() => Promise.resolve(RecipientDashboardComponent), { ssr: false });
