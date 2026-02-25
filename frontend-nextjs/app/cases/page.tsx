'use client';

import React, { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api';
import styles from './cases.module.css';

interface CaseEntity {
  id: number;
  caseNumber: string;
  caseStatus: string;
  caseType: string;
  countyCode: string;
  caseOwnerId: string;
  recipientId: number;
  cin: string;
  referralDate: string;
  applicationDate: string;
  eligibilityDate: string;
  authorizedHoursMonthly: number;
  assessmentType: string;
  healthCareCertStatus: string;
  healthCareCertDueDate: string;
}

interface SearchParams {
  caseNumber: string;
  cin: string;
  countyCode: string;
  status: string;
  caseOwnerId: string;
}

interface Stats {
  total: number;
  pending: number;
  eligible: number;
  dueForReassessment: number;
}

export default function CaseManagementPage() {
  const { t } = useTranslation();
  const router = useRouter();
  const { user, loading: authLoading } = useAuth();

  const [loading, setLoading] = useState(true);
  const [cases, setCases] = useState<CaseEntity[]>([]);
  const [filteredCases, setFilteredCases] = useState<CaseEntity[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize] = useState(50);
  const [searchParams, setSearchParams] = useState<SearchParams>({
    caseNumber: '',
    cin: '',
    countyCode: '',
    status: '',
    caseOwnerId: ''
  });
  const [stats, setStats] = useState<Stats>({
    total: 0,
    pending: 0,
    eligible: 0,
    dueForReassessment: 0
  });

  useEffect(() => {
    if (!authLoading && !user) {
      router.replace('/login');
    }
  }, [authLoading, user, router]);

  const fetchCases = useCallback(async (page: number = 0) => {
    try {
      setLoading(true);
      // Filter by user's county if available
      const countyCode = user?.county || '';
      const response = await apiClient.get(`/cases?page=${page}&size=${pageSize}${countyCode ? `&countyCode=${countyCode}` : ''}`);
      const data = response.data;

      // Handle both paginated and non-paginated responses
      const caseData = data.content || data || [];
      setCases(caseData);
      setFilteredCases(caseData);
      setTotalPages(data.totalPages || Math.ceil(caseData.length / pageSize) || 1);
      setTotalElements(data.totalElements || caseData.length || 0);
      setCurrentPage(data.currentPage || page || 0);

      // Calculate statistics - estimate from total based on current page ratios
      const pendingCount = caseData.filter((c: CaseEntity) => c.caseStatus === 'PENDING').length;
      const eligibleCount = caseData.filter((c: CaseEntity) => c.caseStatus === 'ELIGIBLE').length;
      const dueForReassessmentCount = caseData.filter((c: CaseEntity) =>
        c.healthCareCertDueDate && new Date(c.healthCareCertDueDate) <= new Date(Date.now() + 30 * 24 * 60 * 60 * 1000)
      ).length;
      const pageTotal = caseData.length || 1;
      const total = data.totalElements || caseData.length || 0;

      setStats({
        total: total,
        pending: total > 0 ? Math.round((pendingCount / pageTotal) * total) : pendingCount,
        eligible: total > 0 ? Math.round((eligibleCount / pageTotal) * total) : eligibleCount,
        dueForReassessment: total > 0 ? Math.round((dueForReassessmentCount / pageTotal) * total) : dueForReassessmentCount
      });
    } catch (err) {
      console.error('Error fetching cases:', err);
      setCases([]);
      setFilteredCases([]);
    } finally {
      setLoading(false);
    }
  }, [pageSize, user?.county]);

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      fetchCases(currentPage + 1);
    }
  };

  const handlePrevPage = () => {
    if (currentPage > 0) {
      fetchCases(currentPage - 1);
    }
  };

  useEffect(() => {
    if (user) {
      fetchCases();
    }
  }, [user, fetchCases]);

  const handleSearch = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      if (searchParams.caseNumber) params.append('caseNumber', searchParams.caseNumber);
      if (searchParams.cin) params.append('cin', searchParams.cin);
      if (searchParams.countyCode) params.append('countyCode', searchParams.countyCode);
      if (searchParams.status) params.append('status', searchParams.status);
      if (searchParams.caseOwnerId) params.append('caseOwnerId', searchParams.caseOwnerId);

      const response = await apiClient.get(`/cases/search?${params.toString()}`);
      setFilteredCases(response.data || []);
    } catch (err) {
      console.error('Error searching cases:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchParams({
      caseNumber: '',
      cin: '',
      countyCode: '',
      status: '',
      caseOwnerId: ''
    });
    setFilteredCases(cases);
  };

  const getStatusBadgeClass = (status: string): string => {
    switch (status) {
      case 'PENDING': return styles.badgePending;
      case 'ELIGIBLE': return styles.badgeEligible;
      case 'PRESUMPTIVE_ELIGIBLE': return styles.badgePresumptive;
      case 'ON_LEAVE': return styles.badgeOnLeave;
      case 'DENIED': return styles.badgeDenied;
      case 'TERMINATED': return styles.badgeTerminated;
      case 'APPLICATION_WITHDRAWN': return styles.badgeDefault;
      default: return styles.badgeDefault;
    }
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
        <Breadcrumb path={['Home']} currentPage="Case Management" />

        <div className={styles.content}>
          {/* Header */}
          <div className={styles.header}>
            <div className={styles.headerTitle}>
              <h1>Case Management</h1>
              <p>Search, view, and manage IHSS cases</p>
            </div>
            <button
              className="btn btn-primary"
              onClick={() => router.push('/cases/new')}
            >
              <span className="ca-gov-icon-plus-line" aria-hidden="true"></span>
              New Case
            </button>
          </div>

          {/* Statistics Cards */}
          <div className={styles.statsGrid}>
            <div className={styles.statCard}>
              <div className={`${styles.statValue} ${styles.primary}`}>{stats.total}</div>
              <div className={styles.statLabel}>Total Cases</div>
            </div>
            <div className={styles.statCard}>
              <div className={`${styles.statValue} ${styles.warning}`}>{stats.pending}</div>
              <div className={styles.statLabel}>Pending</div>
            </div>
            <div className={styles.statCard}>
              <div className={`${styles.statValue} ${styles.success}`}>{stats.eligible}</div>
              <div className={styles.statLabel}>Eligible</div>
            </div>
            <div className={styles.statCard}>
              <div className={`${styles.statValue} ${styles.danger}`}>{stats.dueForReassessment}</div>
              <div className={styles.statLabel}>Due for Reassessment</div>
            </div>
          </div>

          {/* Search Panel */}
          <div className={styles.searchPanel}>
            <div className={styles.searchHeader}>
              <h3>Search Cases</h3>
            </div>
            <div className={styles.searchBody}>
              <div className={styles.searchGrid}>
                <div className={styles.searchField}>
                  <label>Case Number</label>
                  <input
                    type="text"
                    value={searchParams.caseNumber}
                    onChange={(e) => setSearchParams({ ...searchParams, caseNumber: e.target.value })}
                    placeholder="Enter case number"
                  />
                </div>
                <div className={styles.searchField}>
                  <label>CIN</label>
                  <input
                    type="text"
                    value={searchParams.cin}
                    onChange={(e) => setSearchParams({ ...searchParams, cin: e.target.value })}
                    placeholder="Enter CIN"
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
                    <option value="PENDING">Pending</option>
                    <option value="ELIGIBLE">Eligible</option>
                    <option value="PRESUMPTIVE_ELIGIBLE">Presumptive Eligible</option>
                    <option value="ON_LEAVE">On Leave</option>
                    <option value="DENIED">Denied</option>
                    <option value="TERMINATED">Terminated</option>
                    <option value="APPLICATION_WITHDRAWN">Application Withdrawn</option>
                  </select>
                </div>
                <div className={styles.searchField}>
                  <label>Case Owner</label>
                  <input
                    type="text"
                    value={searchParams.caseOwnerId}
                    onChange={(e) => setSearchParams({ ...searchParams, caseOwnerId: e.target.value })}
                    placeholder="Owner ID"
                  />
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

          {/* Results Table */}
          <div className={styles.resultsPanel}>
            <div className={styles.resultsHeader}>
              <h3>Cases</h3>
              <span className={styles.resultsCount}>{filteredCases.length} results</span>
            </div>
            <div className={styles.resultsBody}>
              {loading ? (
                <div className={styles.loadingContainer}>
                  <div className={styles.spinner}></div>
                </div>
              ) : filteredCases.length === 0 ? (
                <div className={styles.emptyState}>
                  <div className={styles.emptyIcon}>📁</div>
                  <h4>No cases found</h4>
                  <p>Try adjusting your search criteria or create a new case.</p>
                </div>
              ) : (
                <div className={styles.tableWrapper}>
                  <table className={styles.dataTable}>
                    <thead>
                      <tr>
                        <th>Case Number</th>
                        <th>CIN</th>
                        <th>Type</th>
                        <th>Status</th>
                        <th>County</th>
                        <th>Case Owner</th>
                        <th>Authorized Hours</th>
                        <th>Assessment</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredCases.map((caseEntity) => (
                        <tr key={caseEntity.id}>
                          <td>
                            <span
                              className={styles.caseLink}
                              onClick={() => router.push(`/cases/${caseEntity.id}`)}
                            >
                              {caseEntity.caseNumber || '-'}
                            </span>
                          </td>
                          <td>{caseEntity.cin || '-'}</td>
                          <td>{caseEntity.caseType || '-'}</td>
                          <td>
                            <span className={`${styles.badge} ${getStatusBadgeClass(caseEntity.caseStatus)}`}>
                              {caseEntity.caseStatus?.replace(/_/g, ' ') || '-'}
                            </span>
                          </td>
                          <td>{caseEntity.countyCode || '-'}</td>
                          <td>{caseEntity.caseOwnerId || '-'}</td>
                          <td>{caseEntity.authorizedHoursMonthly ? `${caseEntity.authorizedHoursMonthly}/mo` : '-'}</td>
                          <td>{caseEntity.assessmentType?.replace(/_/g, ' ') || '-'}</td>
                          <td>
                            <div className={styles.actionButtons}>
                              <button
                                className={styles.actionBtn}
                                onClick={() => router.push(`/cases/${caseEntity.id}`)}
                                title="View"
                              >
                                <span className="ca-gov-icon-user" aria-hidden="true"></span>
                              </button>
                              <button
                                className={styles.actionBtn}
                                onClick={() => router.push(`/cases/${caseEntity.id}/edit`)}
                                title="Edit"
                              >
                                <span className="ca-gov-icon-edit" aria-hidden="true"></span>
                              </button>
                              <button
                                className={styles.actionBtn}
                                onClick={() => router.push(`/cases/${caseEntity.id}/notes`)}
                                title="Notes"
                              >
                                <span className="ca-gov-icon-file-text" aria-hidden="true"></span>
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
