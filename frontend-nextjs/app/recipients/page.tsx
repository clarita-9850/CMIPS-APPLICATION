'use client';

import React, { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api';
import styles from './recipients.module.css';

interface Recipient {
  id: number;
  cin: string;
  firstName: string;
  lastName: string;
  middleName?: string;
  dateOfBirth: string;
  email: string;
  primaryPhone: string;
  personType: string;
  countyCode: string;
  residenceCounty: string;
  primaryLanguage?: string;
  interpreterNeeded?: boolean;
  espRegistered?: boolean;
}

interface SearchParams {
  recipientId: string;
  cin: string;
  firstName: string;
  lastName: string;
  countyCode: string;
  status: string;
}

interface Stats {
  total: number;
  eligible: number;
  pending: number;
  needsReassessment: number;
}

export default function RecipientManagementPage() {
  const { t } = useTranslation();
  const router = useRouter();
  const { user, loading: authLoading } = useAuth();

  const [loading, setLoading] = useState(true);
  const [recipients, setRecipients] = useState<Recipient[]>([]);
  const [filteredRecipients, setFilteredRecipients] = useState<Recipient[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize] = useState(50);
  const [searchParams, setSearchParams] = useState<SearchParams>({
    recipientId: '',
    cin: '',
    firstName: '',
    lastName: '',
    countyCode: '',
    status: ''
  });
  const [stats, setStats] = useState<Stats>({
    total: 0,
    eligible: 0,
    pending: 0,
    needsReassessment: 0
  });

  useEffect(() => {
    if (!authLoading && !user) {
      router.replace('/login');
    }
  }, [authLoading, user, router]);

  const fetchRecipients = useCallback(async (page: number = 0) => {
    try {
      setLoading(true);
      // Filter by user's county if available
      const countyCode = user?.county || '';
      const response = await apiClient.get(`/recipients?page=${page}&size=${pageSize}${countyCode ? `&countyCode=${countyCode}` : ''}`);
      const data = response.data;

      // Handle both paginated and non-paginated responses
      const recipientData = data.content || data || [];
      setRecipients(recipientData);
      setFilteredRecipients(recipientData);
      setTotalPages(data.totalPages || Math.ceil(recipientData.length / pageSize) || 1);
      setTotalElements(data.totalElements || recipientData.length || 0);
      setCurrentPage(data.currentPage || page || 0);

      // Calculate statistics - estimate from total based on current page ratios
      const eligibleCount = recipientData.filter((r: Recipient) => r.personType === 'RECIPIENT').length;
      const pendingCount = recipientData.filter((r: Recipient) => r.personType === 'APPLICANT').length;
      const openReferralCount = recipientData.filter((r: Recipient) => r.personType === 'OPEN_REFERRAL').length;
      const pageTotal = recipientData.length || 1;
      const total = data.totalElements || recipientData.length || 0;

      setStats({
        total: total,
        eligible: total > 0 ? Math.round((eligibleCount / pageTotal) * total) : eligibleCount,
        pending: total > 0 ? Math.round((pendingCount / pageTotal) * total) : pendingCount,
        needsReassessment: total > 0 ? Math.round((openReferralCount / pageTotal) * total) : openReferralCount
      });
    } catch (err) {
      console.error('Error fetching recipients:', err);
      setRecipients([]);
      setFilteredRecipients([]);
    } finally {
      setLoading(false);
    }
  }, [pageSize, user?.county]);

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      fetchRecipients(currentPage + 1);
    }
  };

  const handlePrevPage = () => {
    if (currentPage > 0) {
      fetchRecipients(currentPage - 1);
    }
  };

  useEffect(() => {
    if (user) {
      fetchRecipients();
    }
  }, [user, fetchRecipients]);

  const handleSearch = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams();
      if (searchParams.cin) params.append('cin', searchParams.cin);
      if (searchParams.firstName) params.append('firstName', searchParams.firstName);
      if (searchParams.lastName) params.append('lastName', searchParams.lastName);
      if (searchParams.countyCode) params.append('countyCode', searchParams.countyCode);
      if (searchParams.status) params.append('personType', searchParams.status);

      const response = await apiClient.get(`/recipients/search?${params.toString()}`);
      setFilteredRecipients(response.data || []);
    } catch (err) {
      console.error('Error searching recipients:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleClearSearch = () => {
    setSearchParams({
      recipientId: '',
      cin: '',
      firstName: '',
      lastName: '',
      countyCode: '',
      status: ''
    });
    setFilteredRecipients(recipients);
  };

  const getPersonTypeBadgeClass = (personType: string): string => {
    switch (personType) {
      case 'RECIPIENT': return styles.badgeActive;
      case 'APPLICANT': return styles.badgePending;
      case 'OPEN_REFERRAL': return styles.badgeOnLeave;
      case 'CLOSED_REFERRAL': return styles.badgeTerminated;
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
        <Breadcrumb path={['Home']} currentPage="Recipient Management" />

        <div className={styles.content}>
          {/* Header */}
          <div className={styles.header}>
            <div className={styles.headerTitle}>
              <h1>Recipient Management</h1>
              <p>Search, view, and manage IHSS recipients</p>
            </div>
            <button
              className="btn btn-primary"
              onClick={() => router.push('/recipients/new')}
            >
              <span className="ca-gov-icon-plus-line" aria-hidden="true"></span>
              New Recipient
            </button>
          </div>

          {/* Statistics Cards */}
          <div className={styles.statsGrid}>
            <div className={styles.statCard}>
              <div className={`${styles.statValue} ${styles.primary}`}>{stats.total}</div>
              <div className={styles.statLabel}>Total Recipients</div>
            </div>
            <div className={styles.statCard}>
              <div className={`${styles.statValue} ${styles.success}`}>{stats.eligible}</div>
              <div className={styles.statLabel}>Eligible</div>
            </div>
            <div className={styles.statCard}>
              <div className={`${styles.statValue} ${styles.warning}`}>{stats.pending}</div>
              <div className={styles.statLabel}>Pending</div>
            </div>
            <div className={styles.statCard}>
              <div className={`${styles.statValue} ${styles.info}`}>{stats.needsReassessment}</div>
              <div className={styles.statLabel}>Needs Reassessment</div>
            </div>
          </div>

          {/* Search Panel */}
          <div className={styles.searchPanel}>
            <div className={styles.searchHeader}>
              <h3>Search Recipients</h3>
            </div>
            <div className={styles.searchBody}>
              <div className={styles.searchGrid}>
                <div className={styles.searchField}>
                  <label>Recipient ID</label>
                  <input
                    type="text"
                    value={searchParams.recipientId}
                    onChange={(e) => setSearchParams({ ...searchParams, recipientId: e.target.value })}
                    placeholder="Enter recipient ID"
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
                  <label>Person Type</label>
                  <select
                    value={searchParams.status}
                    onChange={(e) => setSearchParams({ ...searchParams, status: e.target.value })}
                  >
                    <option value="">All Types</option>
                    <option value="RECIPIENT">Recipient</option>
                    <option value="APPLICANT">Applicant</option>
                    <option value="OPEN_REFERRAL">Open Referral</option>
                    <option value="CLOSED_REFERRAL">Closed Referral</option>
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
                setFilteredRecipients(recipients.filter(r => r.personType === 'RECIPIENT'));
              }}>
                <span className="ca-gov-icon-check-list" aria-hidden="true"></span>
                View All Recipients
              </button>
              <button
                className={styles.quickActionBtn}
                onClick={() => {
                  setFilteredRecipients(recipients.filter(r => r.personType === 'APPLICANT'));
                }}
              >
                <span className="ca-gov-icon-calendar" aria-hidden="true"></span>
                Applicants
              </button>
              <button className={styles.quickActionBtn} onClick={() => {
                setFilteredRecipients(recipients.filter(r => r.personType === 'OPEN_REFERRAL'));
              }}>
                <span className="ca-gov-icon-file-certificate" aria-hidden="true"></span>
                Open Referrals
              </button>
              <button className={styles.quickActionBtn} onClick={() => {
                setFilteredRecipients(recipients.filter(r => r.personType === 'CLOSED_REFERRAL'));
              }}>
                <span className="ca-gov-icon-close-line" aria-hidden="true"></span>
                Closed Referrals
              </button>
            </div>
          </div>

          {/* Results Table */}
          <div className={styles.resultsPanel}>
            <div className={styles.resultsHeader}>
              <h3>Recipients</h3>
              <span className={styles.resultsCount}>{filteredRecipients.length} results</span>
            </div>
            <div className={styles.resultsBody}>
              {loading ? (
                <div className={styles.loadingContainer}>
                  <div className={styles.spinner}></div>
                </div>
              ) : filteredRecipients.length === 0 ? (
                <div className={styles.emptyState}>
                  <div className={styles.emptyIcon}>👥</div>
                  <h4>No recipients found</h4>
                  <p>Try adjusting your search criteria or add a new recipient.</p>
                </div>
              ) : (
                <div className={styles.tableWrapper}>
                  <table className={styles.dataTable}>
                    <thead>
                      <tr>
                        <th>CIN</th>
                        <th>Name</th>
                        <th>Date of Birth</th>
                        <th>Type</th>
                        <th>County</th>
                        <th>Phone</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredRecipients.map((recipient) => (
                        <tr key={recipient.id}>
                          <td>
                            <span
                              className={styles.recipientLink}
                              onClick={() => router.push(`/recipients/${recipient.id}`)}
                            >
                              {recipient.cin || '-'}
                            </span>
                          </td>
                          <td>{`${recipient.firstName || ''} ${recipient.lastName || ''}`.trim() || '-'}</td>
                          <td>{recipient.dateOfBirth || '-'}</td>
                          <td>
                            <span className={`${styles.badge} ${getPersonTypeBadgeClass(recipient.personType)}`}>
                              {recipient.personType?.replace(/_/g, ' ') || '-'}
                            </span>
                          </td>
                          <td>{recipient.residenceCounty || recipient.countyCode || '-'}</td>
                          <td>{recipient.primaryPhone || '-'}</td>
                          <td>
                            <div className={styles.actionButtons}>
                              <button
                                className={styles.actionBtn}
                                onClick={() => router.push(`/recipients/${recipient.id}`)}
                                title="View"
                              >
                                <span className="ca-gov-icon-user" aria-hidden="true"></span>
                              </button>
                              <button
                                className={styles.actionBtn}
                                onClick={() => router.push(`/recipients/${recipient.id}/edit`)}
                                title="Edit"
                              >
                                <span className="ca-gov-icon-edit" aria-hidden="true"></span>
                              </button>
                              {recipient.personType === 'OPEN_REFERRAL' && (
                                <button
                                  className={styles.actionBtn}
                                  onClick={() => router.push(`/cases/new?recipientId=${recipient.id}`)}
                                  title="Create Case"
                                >
                                  <span className="ca-gov-icon-folder-plus" aria-hidden="true"></span>
                                </button>
                              )}
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
