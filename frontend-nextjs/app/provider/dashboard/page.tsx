'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import dynamic from 'next/dynamic';
import NotificationCenter from '@/components/NotificationCenter';
import apiClient from '@/lib/api';

type Recipient = {
  id: number;
  name: string;
  status: string;
  authorizedHours: number;
  caseNumber: string;
};

function ProviderDashboardComponent() {
  const { user, logout, loading: authLoading } = useAuth();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [pendingActions, setPendingActions] = useState<Array<{ type: string; message: string; priority: string }>>([]);

  useEffect(() => {
    if (authLoading) return;
    if (!user || (user.role !== 'PROVIDER' && !user.roles?.includes('PROVIDER'))) {
      window.location.href = '/login';
      return;
    }
    fetchDashboardData();
  }, [user, authLoading]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      // Fetch assigned recipients from backend
      try {
        const recipientsData = await apiClient.get('/provider-recipient/my-recipients');
        if (recipientsData.data && recipientsData.data.length > 0) {
          const mappedRecipients = recipientsData.data.map((rel: any) => ({
            id: rel.id || 1,
            name: rel.recipientName || 'recipient1',
            status: rel.status || 'Active',
            authorizedHours: rel.authorizedHoursPerMonth || 40,
            caseNumber: rel.caseNumber || 'CASE-001',
          }));
          setRecipients(mappedRecipients);
        } else {
          setRecipients([{
            id: 1,
            name: 'recipient1',
            status: 'Active',
            authorizedHours: 40,
            caseNumber: 'CASE-001',
          }]);
        }
      } catch (apiError) {
        console.error('Error fetching recipients from API, using default:', apiError);
        setRecipients([{
          id: 1,
          name: 'recipient1',
          status: 'Active',
          authorizedHours: 40,
          caseNumber: 'CASE-001',
        }]);
      }
      
      setPendingActions([
        { type: 'timesheet', message: 'Submit timesheet for Sep 15-30 (Due: Oct 5)', priority: 'high' },
        { type: 'review', message: 'Review rejected timesheet for Aug 2025', priority: 'medium' }
      ]);
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
          <p className="text-muted mb-0">Loading Provider Dashboard...</p>
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
                  Provider Dashboard
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
              onClick={() => router.push('/provider/evv-checkin')}
            >
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>📍</div>
                <h3 className="card-title">EVV CHECK-IN</h3>
                <p className="text-muted small mb-0">Start your service visit</p>
              </div>
            </div>
          </div>

          <div className="col-lg-3 col-md-6 mb-3">
            <div
              className="card h-100"
              style={{ cursor: 'pointer' }}
              onClick={() => router.push('/provider/timesheets')}
            >
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>📋</div>
                <h3 className="card-title">TIMESHEETS</h3>
                <p className="text-muted small mb-0">Submit & view timesheets</p>
              </div>
            </div>
          </div>

          <div className="col-lg-3 col-md-6 mb-3">
            <div
              className="card h-100"
              style={{ cursor: 'pointer' }}
              onClick={() => router.push('/provider/payments')}
            >
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>💰</div>
                <h3 className="card-title">PAYMENT HISTORY</h3>
                <p className="text-muted small mb-0">View your payments</p>
              </div>
            </div>
          </div>

          <div className="col-lg-3 col-md-6 mb-3">
            <div
              className="card h-100"
              style={{ cursor: 'pointer' }}
              onClick={() => router.push('/provider/profile')}
            >
              <div className="card-body text-center">
                <div className="mb-3" style={{ fontSize: '3rem' }}>👤</div>
                <h3 className="card-title">MY PROFILE</h3>
                <p className="text-muted small mb-0">Update my address & details</p>
              </div>
            </div>
          </div>
        </div>

        {/* My Recipients */}
        <div className="card mb-4">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>📋 MY RECIPIENTS</h2>
          </div>
          <div className="card-body">
            {recipients.length === 0 ? (
              <p className="text-center text-muted py-4">No recipients assigned</p>
            ) : (
              <div>
                {recipients.map((recipient) => (
                  <div key={recipient.id} className="card mb-3">
                    <div className="card-body">
                      <div className="d-flex justify-content-between align-items-center">
                        <div>
                          <h5 className="fw-semibold mb-1">{recipient.name}</h5>
                          <p className="text-muted small mb-0">
                            Status: <span className="badge bg-success">{recipient.status}</span>
                            {' • '} Authorized: {recipient.authorizedHours} hours/month
                          </p>
                        </div>
                        <button
                          onClick={() => router.push(`/provider/timesheet/new/${recipient.id}`)}
                          className="btn btn-primary"
                        >
                          Submit Timesheet
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Pending Actions */}
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>⏰ PENDING ACTIONS</h2>
          </div>
          <div className="card-body">
            {pendingActions.length === 0 ? (
              <p className="text-center text-muted py-4">No pending actions</p>
            ) : (
              <div>
                {pendingActions.map((action, index) => (
                  <div
                    key={index}
                    className={`alert mb-3 ${action.priority === 'high' ? 'alert-danger' : 'alert-warning'}`}
                    style={{ borderLeft: '4px solid ' + (action.priority === 'high' ? '#dc3545' : '#ffc107') }}
                  >
                    <p className="mb-0">{action.message}</p>
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

export default dynamic(() => Promise.resolve(ProviderDashboardComponent), { ssr: false });

