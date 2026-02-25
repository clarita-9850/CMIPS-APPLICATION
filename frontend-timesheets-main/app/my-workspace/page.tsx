'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useTranslation } from 'react-i18next';
import Breadcrumb from '@/components/Breadcrumb';
import { useAuth } from '@/lib/contexts/AuthContext';
import styles from './my-workspace.module.css';
import Link from 'next/link';

interface Task {
  id: number;
  title: string;
  description?: string;
  subject?: string;
  due_date?: string;
  dueDate?: string;
  priority: string;
  status: string;
  workQueue?: string;
  assignedTo?: string;
  relatedEntityType?: string;
  relatedEntityId?: number;
}

interface TaskCounts {
  open: number;
  inProgress: number;
  closed: number;
}

type StatusFilter = 'ALL' | 'OPEN' | 'IN_PROGRESS' | 'CLOSED';

export default function MyWorkspacePage() {
  const { t } = useTranslation();
  const router = useRouter();
  const { isAuthenticated, loading: authLoading, user } = useAuth();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [filteredTasks, setFilteredTasks] = useState<Task[]>([]);
  const [taskCounts, setTaskCounts] = useState<TaskCounts>({ open: 0, inProgress: 0, closed: 0 });
  const [selectedStatus, setSelectedStatus] = useState<StatusFilter>('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);

  // Normalize status for comparison
  const normalizeStatus = (status: string): string => {
    const upper = status?.toUpperCase() || '';
    if (upper === 'COMPLETED' || upper === 'CLOSED') return 'CLOSED';
    if (upper === 'IN_PROGRESS' || upper === 'IN-PROGRESS') return 'IN_PROGRESS';
    if (upper === 'PENDING' || upper === 'OPEN') return 'OPEN';
    return upper;
  };

  const fetchTasks = useCallback(async () => {
    try {
      setLoading(true);
      const { taskService } = await import('@/lib/services/task.service');

      // Try to get tasks for user with work queues, fallback to all tasks
      let tasksData: Task[] = [];
      if (user?.username) {
        try {
          tasksData = await taskService.getTasksForUser(user.username, true);
        } catch {
          tasksData = await taskService.getAllTasks();
        }
      } else {
        tasksData = await taskService.getAllTasks();
      }

      setTasks(tasksData);

      // Calculate counts from tasks
      const counts = {
        open: tasksData.filter(t => normalizeStatus(t.status) === 'OPEN').length,
        inProgress: tasksData.filter(t => normalizeStatus(t.status) === 'IN_PROGRESS').length,
        closed: tasksData.filter(t => normalizeStatus(t.status) === 'CLOSED').length,
      };
      setTaskCounts(counts);

      // Also try to fetch counts from API
      if (user?.username) {
        try {
          const apiCounts = await taskService.getTaskCounts(user.username);
          if (apiCounts.open > 0 || apiCounts.inProgress > 0 || apiCounts.closed > 0) {
            setTaskCounts(apiCounts);
          }
        } catch {
          // Use calculated counts
        }
      }
    } catch (err) {
      setError('Network error while fetching tasks');
      console.error('Error fetching tasks:', err);
    } finally {
      setLoading(false);
    }
  }, [user?.username]);

  // Filter tasks when status filter changes
  useEffect(() => {
    if (selectedStatus === 'ALL') {
      setFilteredTasks(tasks);
    } else {
      setFilteredTasks(tasks.filter(task => normalizeStatus(task.status) === selectedStatus));
    }
  }, [tasks, selectedStatus]);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchTasks();
    }
  }, [isAuthenticated, fetchTasks]);

  const handleUpdateStatus = async (taskId: number, newStatus: string) => {
    try {
      const { taskService } = await import('@/lib/services/task.service');
      await taskService.updateTaskStatus(taskId, newStatus);
      fetchTasks();
      setSelectedTask(null);
    } catch (err) {
      console.error('Error updating task status:', err);
      alert('Failed to update task status');
    }
  };

  const getPriorityClass = (priority: string) => {
    const p = priority?.toLowerCase() || '';
    switch (p) {
      case 'urgent':
      case 'high':
        return styles.priorityHigh;
      case 'medium':
      case 'normal':
        return styles.priorityNormal;
      case 'low':
        return styles.priorityLow;
      default:
        return styles.priorityNormal;
    }
  };

  const getStatusClass = (status: string) => {
    const normalized = normalizeStatus(status);
    switch (normalized) {
      case 'CLOSED':
        return styles.statusCompleted;
      case 'IN_PROGRESS':
        return styles.statusInProgress;
      case 'OPEN':
        return styles.statusPending;
      case 'ESCALATED':
        return styles.statusEscalated;
      default:
        return styles.statusPending;
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return 'No due date';
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  const getStatusOptions = (currentStatus: string) => {
    const normalized = normalizeStatus(currentStatus);
    switch (normalized) {
      case 'OPEN':
        return ['IN_PROGRESS', 'CLOSED'];
      case 'IN_PROGRESS':
        return ['CLOSED'];
      default:
        return [];
    }
  };

  if (authLoading || !isAuthenticated) {
    return (
      <div className="container py-5 text-center">
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className={styles.pageContainer}>
      <div className="container">
        <Breadcrumb path={['Home']} currentPage="My Workspace" />

        <div className={styles.workspaceContent}>
          <div className={styles.workspaceHeader}>
            <div className={styles.workspaceTitle}>
              <h1>{t('myWorkspace.title')}</h1>
              <p>{t('myWorkspace.subtitle')}</p>
            </div>
            <button className={styles.helpBtn} title={t('myWorkspace.helpButton')}>
              ?
            </button>
          </div>

          <div className={styles.workspaceGrid}>
            {/* My Tasks Section */}
            <div className={styles.tasksSection}>
              <div className={styles.tasksHeader}>
                <h2>{t('myWorkspace.tasks.title')}</h2>
                <div className={styles.headerControls}>
                  <button className={styles.refreshBtn} title="Refresh tasks" onClick={fetchTasks}>
                    🔄
                  </button>
                </div>
              </div>

              {/* Task Counts */}
              <div className={styles.taskCountsContainer}>
                <div className={styles.taskCountCard}>
                  <div className={styles.taskCountValue}>{taskCounts.open}</div>
                  <div className={styles.taskCountLabel}>Open</div>
                </div>
                <div className={styles.taskCountCard}>
                  <div className={`${styles.taskCountValue} ${styles.inProgress}`}>{taskCounts.inProgress}</div>
                  <div className={styles.taskCountLabel}>In Progress</div>
                </div>
                <div className={styles.taskCountCard}>
                  <div className={`${styles.taskCountValue} ${styles.closed}`}>{taskCounts.closed}</div>
                  <div className={styles.taskCountLabel}>Closed</div>
                </div>
              </div>

              {/* Status Filters */}
              <div className={styles.statusFilters}>
                <button
                  className={`${styles.filterBtn} ${selectedStatus === 'ALL' ? styles.filterActive : ''}`}
                  onClick={() => setSelectedStatus('ALL')}
                >
                  All Tasks
                </button>
                <button
                  className={`${styles.filterBtn} ${selectedStatus === 'OPEN' ? styles.filterActive : ''}`}
                  onClick={() => setSelectedStatus('OPEN')}
                >
                  Open ({taskCounts.open})
                </button>
                <button
                  className={`${styles.filterBtn} ${selectedStatus === 'IN_PROGRESS' ? styles.filterActive : ''}`}
                  onClick={() => setSelectedStatus('IN_PROGRESS')}
                >
                  In Progress ({taskCounts.inProgress})
                </button>
                <button
                  className={`${styles.filterBtn} ${selectedStatus === 'CLOSED' ? styles.filterActive : ''}`}
                  onClick={() => setSelectedStatus('CLOSED')}
                >
                  Closed ({taskCounts.closed})
                </button>
              </div>

              <div className={styles.tasksContainer}>
                {/* Tasks Data Table */}
                <div className={styles.tasksTableSection}>
                  {loading ? (
                    <div className={styles.loading}>
                      <div className={styles.spinner}></div>
                      <p>Loading tasks...</p>
                    </div>
                  ) : error ? (
                    <div className={styles.error}>Error: {error}</div>
                  ) : filteredTasks.length === 0 ? (
                    <div className={styles.noTasks}>
                      <div className={styles.noTasksIcon}>📋</div>
                      <p>No tasks found</p>
                    </div>
                  ) : (
                    <div className={styles.tableContainer}>
                      <table className={styles.tasksTable}>
                        <thead>
                          <tr>
                            <th>{t('myWorkspace.tasks.columns.task')}</th>
                            <th>Work Queue</th>
                            <th>{t('myWorkspace.tasks.columns.dueDate')}</th>
                            <th>{t('myWorkspace.tasks.columns.priority')}</th>
                            <th>Status</th>
                            <th>Actions</th>
                          </tr>
                        </thead>
                        <tbody>
                          {filteredTasks.map((task) => (
                            <tr key={task.id} onClick={() => setSelectedTask(task)} className={styles.taskRow}>
                              <td>
                                <div className={styles.taskTitle}>{task.title}</div>
                                {task.description && (
                                  <div className={styles.taskDescription}>{task.description}</div>
                                )}
                              </td>
                              <td>{task.workQueue || task.subject || '-'}</td>
                              <td>{formatDate(task.due_date || task.dueDate || '')}</td>
                              <td>
                                <span className={`${styles.priorityBadge} ${getPriorityClass(task.priority)}`}>
                                  {task.priority}
                                </span>
                              </td>
                              <td>
                                <span className={`${styles.statusBadge} ${getStatusClass(task.status)}`}>
                                  {task.status?.replace(/_/g, ' ')}
                                </span>
                              </td>
                              <td>
                                <button
                                  className={styles.viewBtn}
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    setSelectedTask(task);
                                  }}
                                >
                                  View
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
            </div>

            {/* My Shortcuts Section */}
            <div className={styles.shortcutsSection}>
              <div className={styles.shortcutsHeader}>
                <h2>{t('myWorkspace.shortcuts.title')}</h2>
              </div>

              <div className={styles.shortcutsContainer}>
                <div className={styles.shortcutsList}>
                  <Link href="/cases" className={styles.shortcutLink}>
                    <span className={styles.shortcutIcon}>📁</span>
                    <span className={styles.shortcutText}>Manage Cases</span>
                  </Link>
                  <Link href="/providers" className={styles.shortcutLink}>
                    <span className={styles.shortcutIcon}>👥</span>
                    <span className={styles.shortcutText}>Manage Providers</span>
                  </Link>
                  <Link href="/recipients" className={styles.shortcutLink}>
                    <span className={styles.shortcutIcon}>👤</span>
                    <span className={styles.shortcutText}>Manage Recipients</span>
                  </Link>
                  <Link href="/new-application" className={styles.shortcutLink}>
                    <span className={styles.shortcutIcon}>📝</span>
                    <span className={styles.shortcutText}>{t('myWorkspace.shortcuts.newApplication')}</span>
                  </Link>
                  <Link href="#" className={styles.shortcutLink}>
                    <span className={styles.shortcutIcon}>📋</span>
                    <span className={styles.shortcutText}>{t('myWorkspace.shortcuts.newReferral')}</span>
                  </Link>
                  <Link href="#" className={styles.shortcutLink}>
                    <span className={styles.shortcutIcon}>🔄</span>
                    <span className={styles.shortcutText}>{t('myWorkspace.shortcuts.mergeSSN')}</span>
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Task Detail Modal */}
      {selectedTask && (
        <div className={styles.modalOverlay} onClick={() => setSelectedTask(null)}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2>{selectedTask.title}</h2>
              <button className={styles.modalClose} onClick={() => setSelectedTask(null)}>
                ×
              </button>
            </div>
            <div className={styles.modalBody}>
              {selectedTask.description && (
                <div className={styles.modalSection}>
                  <h4>Description</h4>
                  <p>{selectedTask.description}</p>
                </div>
              )}
              <div className={styles.modalSection}>
                <h4>Details</h4>
                <div className={styles.modalDetails}>
                  <div className={styles.detailItem}>
                    <label>Status:</label>
                    <span className={`${styles.statusBadge} ${getStatusClass(selectedTask.status)}`}>
                      {selectedTask.status?.replace(/_/g, ' ')}
                    </span>
                  </div>
                  <div className={styles.detailItem}>
                    <label>Priority:</label>
                    <span className={`${styles.priorityBadge} ${getPriorityClass(selectedTask.priority)}`}>
                      {selectedTask.priority}
                    </span>
                  </div>
                  <div className={styles.detailItem}>
                    <label>Work Queue:</label>
                    <span>{selectedTask.workQueue || selectedTask.subject || '-'}</span>
                  </div>
                  <div className={styles.detailItem}>
                    <label>Due Date:</label>
                    <span>{formatDate(selectedTask.due_date || selectedTask.dueDate || '')}</span>
                  </div>
                  {selectedTask.assignedTo && (
                    <div className={styles.detailItem}>
                      <label>Assigned To:</label>
                      <span>{selectedTask.assignedTo}</span>
                    </div>
                  )}
                </div>
              </div>
              {getStatusOptions(selectedTask.status).length > 0 && (
                <div className={styles.modalSection}>
                  <h4>Update Status</h4>
                  <div className={styles.statusActions}>
                    {getStatusOptions(selectedTask.status).map((status) => (
                      <button
                        key={status}
                        className={styles.statusUpdateBtn}
                        onClick={() => handleUpdateStatus(selectedTask.id, status)}
                      >
                        Mark as {status.replace(/_/g, ' ')}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
