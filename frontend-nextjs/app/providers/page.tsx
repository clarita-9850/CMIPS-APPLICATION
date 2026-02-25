'use client';

import React, { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api';
import styles from './providers.module.css';

interface Provider {
  id: number;
  providerNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  primaryPhone: string;
  providerStatus: string;
  dojCountyCode: string;
  effectiveDate: string;
  backgroundCheckCompleted: boolean;
  sickLeaveEligibleDate: string;
  sickLeaveAccruedHours: number;
  sickLeaveEligible?: boolean;
  sickLeaveHoursAccrued?: number;
  enrollmentDate?: string;
  orientationCompleted?: boolean;
  eligible?: string;
}

interface SearchParams {
  providerId: string;
  firstName: string;
  lastName: string;
  countyCode: string;
  status: string;
}

interface Stats {
  total: number;
  active: number;
  onLeave: number;
  pendingCertification: number;
}

export default function ProviderManagementPage() {
  const { t } = useTranslation();
  const router = useRouter();
  const { user, loading: authLoading } = useAuth();

  const [loading, setLoading] = useState(true);
  const [providers, setProviders] = useState<Provider[]>([]);
  const [filteredProviders, setFilteredProviders] = useState<Provider[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize] = useState(50);
  const [searchParams, setSearchParams] = useState<SearchParams>({
    providerId: '',
    firstName: '',
    lastName: '',
    countyCode: '',
    status: ''
  });
  const [stats, setStats] = useState<Stats>({
    total: 0,
    active: 0,
    onLeave: 0,
    pendingCertification: 0
  });

  useEffect(() => {
    if (!authLoading && !user) {
      router.replace('/login');
    }
  }, [authLoading, user, router]);

  const fetchProviders = useCallback(async (page: number = 0) => {
    try {
      setLoading(true);
      // Filter by user's county if available
      const countyCode = user?.county || '';
      const response = await apiClient.get(`/providers?page=${page}&size=${pageSize}${countyCode ? `&countyCode=${countyCode}` : ''}`);
      const data = response.data;

      // Handle both paginated and non-paginated responses
      const providerData = data.content || data || [];
      setProviders(providerData);
      setFilteredProviders(providerData);
      setTotalPages(data.totalPages || Math.ceil(providerData.length / pageSize) || 1);
      setTotalElements(data.totalElements || providerData.length || 0);
      setCurrentPage(data.currentPage || page || 0);

      // Calculate statistics - estimate from total based on current page ratios
      const activeCount = providerData.filter((p: Provider) => p.providerStatus === 'ACTIVE').length;
      const onLeaveCount = providerData.filter((p: Provider) => p.providerStatus === 'ON_LEAVE').length;
      const pendingCount = providerData.filter((p: Provider) => !p.backgroundCheckCompleted && !p.enrollmentDate).length;
      const pageTotal = providerData.length || 1;
      const total = data.totalElements || providerData.length || 0;

      setStats({
        total: total,
        active: total > 0 ? Math.round((activeCount / pageTotal) * total) : activeCount,
        onLeave: total > 0 ? Math.round((onLeaveCount / pageTotal) * total) : onLeaveCount,
        pendingCertification: total > 0 ? Math.round((pendingCount / pageTotal) * total) : pendingCount
      });
    } catch (err) {
      console.error('Error fetching providers:', err);
      setProviders([]);
      setFilteredProviders([]);
    } finally {
      setLoading(false);
    }
  }, [pageSize, user?.county]);

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      fetchProviders(currentPage + 1);
    }
  };

  const handlePrevPage = () => {
    if (currentPage > 0) {
      fetchProviders(currentPage - 1);
    }
  };

  useEffect(() => {
    if (user) {
      fetchProviders();
    }
  }, [user, fetchProviders]);

  const handleSearch = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      if (searchParams.providerId) params.append('providerNumber', searchParams.providerId);
      if (searchParams.firstName) params.append('firstName', searchParams.firstName);
      if (searchParams.lastName) params.append('lastName', searchParams.lastName);
      if (searchParams.countyCode) params.append('countyCode', searchParams.countyCode);

      const response = await apiClient.get(`/providers/search?${params.toString()}`);
      let results = response.data || [];

      // Filter by status if specified (client-side filtering)
      if (searchParams.status) {
        results = results.filter((p: Provider) => p.providerStatus === searchParams.status);
      }

      setFilteredProviders(results);
    } catch (err) {
      console.error('Error searching providers:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchParams({
      providerId: '',
      firstName: '',
      lastName: '',
      countyCode: '',
      status: ''
    });
    setFilteredProviders(providers);
  };

  const getStatusBadgeClass = (status: string): string => {
    switch (status) {
      case 'ACTIVE': return styles.badgeActive;
      case 'ON_LEAVE': return styles.badgeOnLeave;
      case 'TERMINATED': return styles.badgeTerminated;
      default: return styles.badgeDefault;
    }
  };

  const getCertBadgeClass = (completed: boolean): string => {
    return completed ? styles.badgeYes : styles.badgePending;
  };

  if (authLoading || !user) {
    return (
      <div className={styles.pageContainer}>
        <div className="container">
          <div className={styles.loadingContainer}>
            <div className={styles.spinner}></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.pageContainer}>
      <div className="container">
        <Breadcrumb path={['Home']} currentPage="Provider Management" />

        <div className={styles.content}>
          {/* Header */}
          <div className={styles.header}>
            <div className={styles.headerTitle}>
              <h1>Provider Management</h1>
              <p>Search, view, and manage IHSS providers</p>
            </div>
            <button
              className="btn btn-primary"
              onClick={() => router.push('/providers/new')}
            >
              <span className="ca-gov-icon-plus-line" aria-hidden="true"></span>
              New Provider
            </button>
          </div>

          {/* Statistics Cards */}
          <div className={styles.statsGrid}>
            <div className={styles.statCard}>
              <div className={`${styles.statValue} ${styles.primary}`}>{stats.total}</div>
              <div className={styles.statLabel}>Total Providers</div>
            </div>
            <div className={styles.statCard}>
              <div className={`${styles.statValue} ${styles.success}`}>{stats.active}</div>
              <div className={styles.statLabel}>Active</div>
            </div>
            <div className={styles.statCard}>
              <div className={`${styles.statValue} ${styles.warning}`}>{stats.onLeave}</div>
              <div className={styles.statLabel}>On Leave</div>
            </div>
            <div className={styles.statCard}>
              <div className={`${styles.statValue} ${styles.info}`}>{stats.pendingCertification}</div>
              <div className={styles.statLabel}>Pending Certification</div>
            </div>
          </div>

          {/* Search Panel */}
          <div className={styles.searchPanel}>
            <div className={styles.searchHeader}>
              <h3>Search Providers</h3>
            </div>
            <div className={styles.searchBody}>
              <div className={styles.searchGrid}>
                <div className={styles.searchField}>
                  <label>Provider ID</label>
                  <input
                    type="text"
                    value={searchParams.providerId}
                    onChange={(e) => setSearchParams({ ...searchParams, providerId: e.target.value })}
                    placeholder="Enter provider ID"
                  />
                </div>
                <div className={styles.searchField}>
                  <label>First Name</label>
                  <input
                    type="text"
                    value={searchParams.firstName}
                    onChange={(e) => setSearchParams({ ...searchParams, firstName: e.target.value })}
                    placeholder="Enter first name"
                  />
                </div>
                <div className={styles.searchField}>
                  <label>Last Name</label>
                  <input
                    type="text"
                    value={searchParams.lastName}
                    onChange={(e) => setSearchParams({ ...searchParams, lastName: e.target.value })}
                    placeholder="Enter last name"
                  />
                </div>
                <div className={styles.searchField}>
                  <label>County Code</label>
                  <input
                    type="text"
                    value={searchParams.countyCode}
                    onChange={(e) => setSearchParams({ ...searchParams, countyCode: e.target.value })}
                    placeholder="e.g., 19"
                  />
                </div>
                <div className={styles.searchField}>
                  <label>Status</label>
                  <select
                    value={searchParams.status}
                    onChange={(e) => setSearchParams({ ...searchParams, status: e.target.value })}
                  >
                    <option value="">All Statuses</option>
                    <option value="ACTIVE">Active</option>
                    <option value="ON_LEAVE">On Leave</option>
                    <option value="TERMINATED">Terminated</option>
                  </select>
                </div>
              </div>
              <div className={styles.searchActions}>
                <button className="btn btn-primary" onClick={handleSearch} disabled={loading}>
                  <span className="ca-gov-icon-search" aria-hidden="true"></span>
                  {loading ? 'Searching...' : 'Search'}
                </button>
                <button className="btn btn-secondary" onClick={handleClearSearch}>
                  <span className="ca-gov-icon-close-line" aria-hidden="true"></span>
                  Clear
                </button>
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className={styles.quickActions}>
            <div className={styles.quickActionsHeader}>
              <h3>Quick Actions</h3>
            </div>
            <div className={styles.quickActionsBody}>
              <button className={styles.quickActionBtn} onClick={() => {
                setFilteredProviders(providers.filter(p => p.providerStatus === 'ACTIVE'));
              }}>
                <span className="ca-gov-icon-check-list" aria-hidden="true"></span>
                View All Active
              </button>
              <button
                className={styles.quickActionBtn}
                onClick={() => {
                  setFilteredProviders(providers.filter(p => p.providerStatus === 'ON_LEAVE'));
                }}
              >
                <span className="ca-gov-icon-calendar" aria-hidden="true"></span>
                On Leave
              </button>
              <button className={styles.quickActionBtn} onClick={() => {
                setFilteredProviders(providers.filter(p => !p.backgroundCheckCompleted));
              }}>
                <span className="ca-gov-icon-file-certificate" aria-hidden="true"></span>
                Pending Certification
              </button>
              <button className={styles.quickActionBtn} onClick={() => {
                setFilteredProviders(providers.filter(p => p.sickLeaveEligibleDate || p.sickLeaveEligible));
              }}>
                <span className="ca-gov-icon-medical-plus" aria-hidden="true"></span>
                Sick Leave Eligible
              </button>
              <button className={styles.quickActionBtn} onClick={() => router.push('/providers/violations/pending')}>
                <span className="ca-gov-icon-warning-triangle" aria-hidden="true"></span>
                Pending Violations
              </button>
            </div>
          </div>

          {/* Results Table */}
          <div className={styles.resultsPanel}>
            <div className={styles.resultsHeader}>
              <h3>Providers</h3>
              <span className={styles.resultsCount}>{filteredProviders.length} results</span>
            </div>
            <div className={styles.resultsBody}>
              {loading ? (
                <div className={styles.loadingContainer}>
                  <div className={styles.spinner}></div>
                </div>
              ) : filteredProviders.length === 0 ? (
                <div className={styles.emptyState}>
                  <div className={styles.emptyIcon}>👤</div>
                  <h4>No providers found</h4>
                  <p>Try adjusting your search criteria or add a new provider.</p>
                </div>
              ) : (
                <div className={styles.tableWrapper}>
                  <table className={styles.dataTable}>
                    <thead>
                      <tr>
                        <th>Provider ID</th>
                        <th>Name</th>
                        <th>Type</th>
                        <th>Status</th>
                        <th>County</th>
                        <th>Certification</th>
                        <th>Sick Leave</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredProviders.map((provider) => (
                        <tr key={provider.id}>
                          <td>
                            <span
                              className={styles.providerLink}
                              onClick={() => router.push(`/providers/${provider.id}`)}
                            >
                              {provider.providerNumber || '-'}
                            </span>
                          </td>
                          <td>{`${provider.firstName || ''} ${provider.lastName || ''}`.trim() || '-'}</td>
                          <td>Individual Provider</td>
                          <td>
                            <span className={`${styles.badge} ${getStatusBadgeClass(provider.providerStatus)}`}>
                              {provider.providerStatus?.replace(/_/g, ' ') || '-'}
                            </span>
                          </td>
                          <td>{provider.dojCountyCode || '-'}</td>
                          <td>
                            <span className={`${styles.badge} ${getCertBadgeClass(provider.backgroundCheckCompleted)}`}>
                              {provider.backgroundCheckCompleted ? 'CERTIFIED' : 'PENDING'}
                            </span>
                          </td>
                          <td>
                            {provider.sickLeaveEligibleDate || provider.sickLeaveEligible ? (
                              <span className={styles.sickLeaveHours}>
                                {provider.sickLeaveAccruedHours || provider.sickLeaveHoursAccrued || 0} hrs
                              </span>
                            ) : (
                              <span className={styles.sickLeaveNA}>N/A</span>
                            )}
                          </td>
                          <td>
                            <div className={styles.actionButtons}>
                              <button
                                className={styles.actionBtn}
                                onClick={() => router.push(`/providers/${provider.id}`)}
                                title="View"
                              >
                                <span className="ca-gov-icon-user" aria-hidden="true"></span>
                              </button>
                              <button
                                className={styles.actionBtn}
                                onClick={() => router.push(`/providers/${provider.id}/edit`)}
                                title="Edit"
                              >
                                <span className="ca-gov-icon-edit" aria-hidden="true"></span>
                              </button>
                              <button
                                className={styles.actionBtn}
                                onClick={() => router.push(`/providers/${provider.id}/cori`)}
                                title="CORI"
                              >
                                <span className="ca-gov-icon-shield-check" aria-hidden="true"></span>
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
              {/* Pagination Controls */}
              {totalPages > 1 && (
                <div className={styles.pagination}>
                  <button
                    className={styles.paginationBtn}
                    onClick={handlePrevPage}
                    disabled={currentPage === 0}
                  >
                    &larr; Previous
                  </button>
                  <span className={styles.pageInfo}>
                    Page {currentPage + 1} of {totalPages} ({totalElements} total)
                  </span>
                  <button
                    className={styles.paginationBtn}
                    onClick={handleNextPage}
                    disabled={currentPage >= totalPages - 1}
                  >
                    Next &rarr;
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
