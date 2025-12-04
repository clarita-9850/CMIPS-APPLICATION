'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import dynamic from 'next/dynamic';
import WorkView from '@/components/WorkView';
import NotificationCenter from '@/components/NotificationCenter';
import apiClient from '@/lib/api';

type Timesheet = {
  id: number;
  employeeName: string;
  payPeriodStart: string;
  payPeriodEnd: string;
  totalHours: number;
  status: string;
};

function CaseWorkerDashboardComponent() {
  const { user, logout, loading: authLoading } = useAuth();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalCases: 0,
    pendingTimesheets: 0,
    evvViolations: 0,
    dueReassessments: 0
  });
  const [pendingTimesheets, setPendingTimesheets] = useState<Timesheet[]>([]);

  useEffect(() => {
    if (authLoading) return;
    if (!user || (user.role !== 'CASE_WORKER' && !user.roles?.includes('CASE_WORKER'))) {
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
        const submitted = timesheets.filter((ts: Timesheet) => ts.status === 'SUBMITTED');
        setPendingTimesheets(submitted);
        
        // Update stats
        setStats({
          totalCases: 145,
          pendingTimesheets: submitted.length,
          evvViolations: 3,
          dueReassessments: 5
        });
      } catch (err) {
        console.error('Error fetching timesheets:', err);
        setPendingTimesheets([]);
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
          <p className="text-muted mb-0">Loading Case Worker Dashboard...</p>
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
                  Case Worker Dashboard
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
        {/* Statistics Cards */}
        <div className="row mb-4">
          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card text-center">
              <div className="card-body">
                <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: 'var(--color-p2, #046b99)' }}>{stats.totalCases}</div>
                <p className="text-muted small mb-0">CASES</p>
              </div>
            </div>
          </div>
          
          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card text-center">
              <div className="card-body">
                <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#ffc107' }}>{stats.pendingTimesheets}</div>
                <p className="text-muted small mb-0">TIMESHEETS PENDING</p>
              </div>
            </div>
          </div>
          
          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card text-center">
              <div className="card-body">
                <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#dc3545' }}>{stats.evvViolations}</div>
                <p className="text-muted small mb-0">EVV VIOLATIONS</p>
              </div>
            </div>
          </div>
          
          <div className="col-lg-3 col-md-6 mb-3">
            <div className="card text-center">
              <div className="card-body">
                <div className="fw-bold mb-2" style={{ fontSize: '2.5rem', color: '#0dcaf0' }}>{stats.dueReassessments}</div>
                <p className="text-muted small mb-0">DUE REASSESSMENTS</p>
              </div>
            </div>
          </div>
        </div>

        {/* WorkView - Tasks */}
        <div className="mb-4">
          <WorkView username={user?.username || ''} />
        </div>

        {/* Priority Actions */}
        <div className="card mb-4">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>🚨 PRIORITY ACTIONS</h2>
          </div>
          <div className="card-body">
            <div>
              <div className="alert alert-warning mb-2" style={{ borderLeft: '4px solid #ffc107' }}>
                <p className="mb-0">• {stats.pendingTimesheets} Timesheets Pending Review</p>
              </div>
              <div className="alert alert-danger mb-2" style={{ borderLeft: '4px solid #dc3545' }}>
                <p className="mb-0">• {stats.evvViolations} EVV Violations Need Resolution</p>
              </div>
              <div className="alert alert-info mb-0" style={{ borderLeft: '4px solid #0dcaf0' }}>
                <p className="mb-0">• {stats.dueReassessments} Cases Due for Reassessment</p>
              </div>
            </div>
          </div>
        </div>

        {/* Pending Timesheets */}
        <div className="card">
          <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
            <h2 className="card-title mb-0" style={{ color: 'white' }}>📊 PENDING TIMESHEETS</h2>
          </div>
          <div className="card-body">
            {pendingTimesheets.length === 0 ? (
              <p className="text-center text-muted py-4">No timesheets pending review</p>
            ) : (
              <div className="table-responsive">
                <table className="table table-striped table-hover">
                  <thead>
                    <tr>
                      <th>Provider</th>
                      <th>Recipient</th>
                      <th>Pay Period</th>
                      <th>Hours</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pendingTimesheets.map((timesheet) => (
                      <tr key={timesheet.id}>
                        <td>{timesheet.employeeName}</td>
                        <td>-</td>
                        <td>{timesheet.payPeriodStart} to {timesheet.payPeriodEnd}</td>
                        <td className="fw-bold">{timesheet.totalHours}</td>
                        <td>
                          <span className="badge bg-warning">{timesheet.status}</span>
                        </td>
                        <td>
                          <button
                            onClick={() => router.push(`/caseworker/timesheet/${timesheet.id}`)}
                            className="btn btn-primary btn-sm"
                          >
                            Review
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

export default dynamic(() => Promise.resolve(CaseWorkerDashboardComponent), { ssr: false });
