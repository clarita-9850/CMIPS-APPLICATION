'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import apiClient from '@/lib/api';
import dynamic from 'next/dynamic';
import AnalyticsTab from './components/AnalyticsTab';
import BatchJobsTab from './components/BatchJobsTab';

type Task = {
  id: number;
  title: string;
  description?: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'CLOSED' | 'ESCALATED' | string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW' | string;
  assignedTo?: string;
  workQueue?: string;
  dueDate?: string;
  createdAt?: string;
  actionLink?: string;
};

type QueueInfo = {
  name: string;
  displayName: string;
  description: string;
  supervisorOnly: boolean;
};

type QueueSubscription = {
  id: number;
  username: string;
  workQueue: string;
  subscribedBy: string;
  createdAt: string;
};

type User = {
  id: string;
  username: string;
  email?: string;
  firstName?: string;
  lastName?: string;
};

type UserQueueData = {
  username: string;
  queueName: string;
  queueDisplayName: string;
  tasks: Task[];
  taskCount: number;
};

type ViewType = 'myWorkspace' | 'workQueues' | 'queueTasks' | 'queueSubscriptions';

function SupervisorDashboardComponent() {
  const { user, logout, loading: authLoading, isAuthenticated } = useAuth();
  
  // Debug: Log user state on mount and changes
  useEffect(() => {
    console.log('=== SupervisorDashboard Component ===');
    console.log('User state:', { 
      user, 
      authLoading, 
      role: user?.role, 
      roles: user?.roles,
      hasUser: !!user,
      userString: JSON.stringify(user, null, 2)
    });
  }, [user, authLoading]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  
  // Navigation state
  const [activeTab, setActiveTab] = useState<'myWorkspace' | 'workQueues' | 'analytics' | 'batchJobs'>('myWorkspace');
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  
  // Work Queues state
  const [queues, setQueues] = useState<QueueInfo[]>([]);
  const [selectedQueue, setSelectedQueue] = useState<QueueInfo | null>(null);
  const [queueView, setQueueView] = useState<ViewType>('myWorkspace' as ViewType);
  
  // My Workspace state
  const [userQueueData, setUserQueueData] = useState<UserQueueData[]>([]);
  const [escalatedTasks, setEscalatedTasks] = useState<Task[]>([]);
  const [allSubscriptions, setAllSubscriptions] = useState<QueueSubscription[]>([]);
  
  // Queue Tasks state
  const [queueTasks, setQueueTasks] = useState<Task[]>([]);
  const [selectedTasks, setSelectedTasks] = useState<Set<number>>(new Set());
  
  // Queue Subscriptions state
  const [queueSubscriptions, setQueueSubscriptions] = useState<QueueSubscription[]>([]);
  const [allUsers, setAllUsers] = useState<User[]>([]);
  const [showAddSubscriptionModal, setShowAddSubscriptionModal] = useState(false);
  const [selectedUserToAdd, setSelectedUserToAdd] = useState<string>('');

  useEffect(() => {
    // Wait for auth to finish loading
    if (authLoading) {
      console.log('Auth still loading...');
      return;
    }
    
    // If no user after auth loads, redirect to login
    if (!user) {
      console.log('No user after auth load, redirecting to login');
      window.location.href = '/login';
      return;
    }
    
    console.log('User check:', { 
      user, 
      role: user?.role, 
      roles: user?.roles,
      username: user?.username 
    });
    
    // Allow both ADMIN and SUPERVISOR roles
    // Check both user.role and user.roles array
    const isSupervisor = 
      user?.role === 'SUPERVISOR' || 
      user?.roles?.includes('SUPERVISOR') ||
      user?.roles?.some((r: string) => r.toUpperCase() === 'SUPERVISOR');
      
    const isAdmin = 
      user?.role === 'ADMIN' || 
      user?.roles?.includes('ADMIN') ||
      user?.roles?.some((r: string) => r.toUpperCase() === 'ADMIN');
    
    console.log('Role check:', { 
      isSupervisor, 
      isAdmin, 
      hasUser: !!user,
      userRole: user?.role,
      userRoles: user?.roles
    });
    
    if (!isSupervisor && !isAdmin) {
      console.log('User does not have SUPERVISOR or ADMIN role, redirecting to login');
      console.log('User object:', JSON.stringify(user, null, 2));
      window.location.href = '/login';
      return;
    }
    
    console.log('User authorized, loading dashboard data');
    // Load dashboard data for both roles
    loadDashboardData();
  }, [user, authLoading]);

  useEffect(() => {
    if (activeTab === 'myWorkspace') {
      loadMyWorkspaceData();
    } else if (selectedQueue) {
      loadQueueTasks();
      loadQueueSubscriptions();
    }
  }, [activeTab, selectedQueue]);

  const loadDashboardData = async () => {
    setLoading(true);
    setError('');
    try {
      const catalogRes = await apiClient.get('/work-queues/catalog');
      const catalog: QueueInfo[] = catalogRes.data || [];
      setQueues(catalog);
      
      const usersRes = await apiClient.get('/work-queues/users');
      setAllUsers(usersRes.data || []);
      
      if (activeTab === 'myWorkspace') {
        await loadMyWorkspaceData();
      }
    } catch (err: any) {
      console.error('Error loading dashboard data:', err);
      setError(err?.response?.data?.error || err.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const loadMyWorkspaceData = async () => {
    try {
      // Get all queues
      const catalogRes = await apiClient.get('/work-queues/catalog');
      const allQueues: QueueInfo[] = catalogRes.data || [];
      
      // Get all subscriptions
      const allSubs: QueueSubscription[] = [];
      for (const queue of allQueues) {
        if (!queue.supervisorOnly) {
          try {
            const subsRes = await apiClient.get(`/work-queues/queue/${queue.name}/details`);
            allSubs.push(...(subsRes.data || []));
          } catch (err) {
            console.log(`No subscriptions for ${queue.name}`);
          }
        }
      }
      setAllSubscriptions(allSubs);
      
      // Get escalated tasks (supervisor-only queue)
      try {
        const escalatedRes = await apiClient.get('/work-queues/ESCALATED/tasks');
        setEscalatedTasks(escalatedRes.data || []);
      } catch (err) {
        console.log('No escalated tasks');
        setEscalatedTasks([]);
      }
      
      // Group subscriptions by user and get their tasks
      const userQueueMap = new Map<string, UserQueueData>();
      
      for (const sub of allSubs) {
        const key = `${sub.username}_${sub.workQueue}`;
        if (!userQueueMap.has(key)) {
          const queueInfo = allQueues.find(q => q.name === sub.workQueue);
          userQueueMap.set(key, {
            username: sub.username,
            queueName: sub.workQueue,
            queueDisplayName: queueInfo?.displayName || sub.workQueue,
            tasks: [],
            taskCount: 0,
          });
        }
      }
      
      // Get tasks for each user-queue combination
      for (const [key, userQueue] of userQueueMap.entries()) {
        try {
          const tasksRes = await apiClient.get(`/tasks?username=${userQueue.username}`);
          const allUserTasks: Task[] = tasksRes.data || [];
          const queueTasks = allUserTasks.filter(t => t.workQueue === userQueue.queueName);
          userQueue.tasks = queueTasks;
          userQueue.taskCount = queueTasks.length;
        } catch (err) {
          console.log(`Error loading tasks for ${userQueue.username} in ${userQueue.queueName}`);
        }
      }
      
      setUserQueueData(Array.from(userQueueMap.values()));
    } catch (err: any) {
      console.error('Error loading workspace data:', err);
      setError(err?.response?.data?.error || err.message || 'Failed to load workspace data');
    }
  };

  const loadQueueTasks = async () => {
    if (!selectedQueue) return;
    try {
      const tasksRes = await apiClient.get(`/work-queues/${selectedQueue.name}/tasks`);
      setQueueTasks(tasksRes.data || []);
    } catch (err: any) {
      console.error('Error loading queue tasks:', err);
      setQueueTasks([]);
    }
  };

  const loadQueueSubscriptions = async () => {
    if (!selectedQueue) return;
    try {
      const subsRes = await apiClient.get(`/work-queues/queue/${selectedQueue.name}/details`);
      setQueueSubscriptions(subsRes.data || []);
    } catch (err: any) {
      console.error('Error loading queue subscriptions:', err);
      setQueueSubscriptions([]);
    }
  };

  const handleViewQueue = (queue: QueueInfo) => {
    setSelectedQueue(queue);
    setQueueView('queueTasks');
  };

  const handleReserveTasks = async (count?: number) => {
    if (!selectedQueue) return;
    
    try {
      const tasksToReserve = count 
        ? queueTasks.filter(t => t.status === 'OPEN').slice(0, count)
        : Array.from(selectedTasks).map(id => queueTasks.find(t => t.id === id)).filter(Boolean) as Task[];
      
      if (tasksToReserve.length === 0) {
        alert('No tasks selected or available to reserve');
        return;
      }
      
      for (const task of tasksToReserve) {
        await apiClient.put(`/tasks/${task.id}`, {
          ...task,
          assignedTo: user?.username,
          status: 'IN_PROGRESS'
        });
      }
      
      alert(`Reserved ${tasksToReserve.length} task(s) successfully!`);
      setSelectedTasks(new Set());
      loadQueueTasks();
    } catch (err: any) {
      console.error('Error reserving tasks:', err);
      alert('Failed to reserve tasks: ' + (err?.response?.data?.error || err.message));
    }
  };

  const handleForwardTasks = async () => {
    if (selectedTasks.size === 0) {
      alert('Please select tasks to forward');
      return;
    }
    
    const forwardTo = prompt('Enter username to forward tasks to:');
    if (!forwardTo) return;
    
    try {
      for (const taskId of selectedTasks) {
        const task = queueTasks.find(t => t.id === taskId);
        if (task) {
          await apiClient.put(`/tasks/${taskId}`, {
            ...task,
            assignedTo: forwardTo
          });
        }
      }
      
      alert(`Forwarded ${selectedTasks.size} task(s) to ${forwardTo}`);
      setSelectedTasks(new Set());
      loadQueueTasks();
    } catch (err: any) {
      console.error('Error forwarding tasks:', err);
      alert('Failed to forward tasks: ' + (err?.response?.data?.error || err.message));
    }
  };

  const handleAddSubscription = async () => {
    if (!selectedQueue || !selectedUserToAdd) {
      alert('Please select a user');
      return;
    }
    
    try {
      await apiClient.post('/work-queues/subscribe', {
        username: selectedUserToAdd,
        workQueue: selectedQueue.name,
        subscribedBy: user?.username,
      });
      
      alert('User added to queue successfully!');
      setShowAddSubscriptionModal(false);
      setSelectedUserToAdd('');
      loadQueueSubscriptions();
      if (activeTab === 'myWorkspace') {
        loadMyWorkspaceData();
      }
    } catch (err: any) {
      console.error('Error adding subscription:', err);
      alert('Failed to add user: ' + (err?.response?.data?.error || err.message));
    }
  };

  const handleRemoveSubscription = async (username: string) => {
    if (!selectedQueue) return;
    
    if (!confirm(`Remove ${username} from ${selectedQueue.displayName}?`)) {
      return;
    }
    
    try {
      await apiClient.delete('/work-queues/unsubscribe', {
        data: {
          username,
          workQueue: selectedQueue.name,
        },
      });
      
      alert('User removed from queue successfully!');
      loadQueueSubscriptions();
      if (activeTab === 'myWorkspace') {
        loadMyWorkspaceData();
      }
    } catch (err: any) {
      console.error('Error removing subscription:', err);
      alert('Failed to remove user: ' + (err?.response?.data?.error || err.message));
    }
  };

  const toggleTaskSelection = (taskId: number) => {
    const newSelected = new Set(selectedTasks);
    if (newSelected.has(taskId)) {
      newSelected.delete(taskId);
    } else {
      newSelected.add(taskId);
    }
    setSelectedTasks(newSelected);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return '#ffc107';
      case 'IN_PROGRESS': return '#17a2b8';
      case 'CLOSED': return '#28a745';
      case 'ESCALATED': return '#dc3545';
      default: return '#6c757d';
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH': return '#dc3545';
      case 'MEDIUM': return '#ffc107';
      case 'LOW': return '#17a2b8';
      default: return '#6c757d';
    }
  };

  if (loading || authLoading || !user) {
    return (
      <div className="min-h-screen d-flex align-items-center justify-content-center" style={{ backgroundColor: 'var(--gray-50, #fafafa)' }}>
        <div className="text-center card p-5">
          <div className="spinner-border text-primary mb-3" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="text-muted mb-0">Loading Supervisor Dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header role="banner" style={{ backgroundColor: 'white', borderBottom: '1px solid #e5e7eb', position: 'sticky', top: 0, zIndex: 100 }}>
        {/* Site Header */}
        <div style={{ padding: '1rem 0', borderBottom: '1px solid #e5e7eb' }}>
          <div className="container">
            <div className="d-flex justify-content-between align-items-center">
              <div>
                <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--color-p2, #046b99)', margin: 0 }}>
                  CMIPSII Case Management Information Payroll System II
                </h1>
                <p className="text-muted mb-0" style={{ fontSize: '0.875rem', margin: '0.25rem 0 0 0' }}>
                  Supervisor Dashboard
                </p>
              </div>
              {(user || isAuthenticated) && (
                <div className="d-flex align-items-center" style={{ gap: '1rem' }}>
                  <span className="text-muted" style={{ fontSize: '0.9rem', whiteSpace: 'nowrap' }}>
                    Welcome, <strong style={{ color: '#333' }}>{user?.name || user?.username || user?.firstName || 'User'}</strong>
                  </span>
                  <button 
                    type="button" 
                    onClick={logout}
                    className="btn btn-danger"
                    style={{ 
                      padding: '0.375rem 0.75rem',
                      fontSize: '0.875rem',
                      whiteSpace: 'nowrap',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '0.5rem'
                    }}
                  >
                    Logout
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Navigation */}
        <div style={{ backgroundColor: 'white' }}>
          <div className="container">
            <div className="d-flex align-items-center" style={{ padding: '0.75rem 0', gap: '0.5rem', flexWrap: 'wrap' }}>
              <button
                onClick={() => {
                  setActiveTab('myWorkspace');
                  setQueueView('myWorkspace' as ViewType);
                }}
                className={`btn btn-sm ${activeTab === 'myWorkspace' ? 'btn-primary' : 'btn-secondary'}`}
              >
                My Workspace
              </button>
              <button
                onClick={() => {
                  setActiveTab('workQueues');
                  setQueueView('workQueues' as ViewType);
                }}
                className={`btn btn-sm ${activeTab === 'workQueues' ? 'btn-primary' : 'btn-secondary'}`}
              >
                Work Queues
              </button>
              <button
                onClick={() => setActiveTab('analytics')}
                className={`btn btn-sm ${activeTab === 'analytics' ? 'btn-primary' : 'btn-secondary'}`}
              >
                Analytics
              </button>
              <button
                onClick={() => setActiveTab('batchJobs')}
                className={`btn btn-sm ${activeTab === 'batchJobs' ? 'btn-primary' : 'btn-secondary'}`}
              >
                Batch Jobs
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main id="main-content" className="container" style={{ padding: '1.5rem 0' }}>
        <div>
          {activeTab === 'myWorkspace' && (
            <div>
              {/* Header */}
              <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                  <h2 className="card-title mb-1">My Workspace</h2>
                  <p className="text-muted">Overview of users, queues, and tasks</p>
                </div>
                <button
                  onClick={loadMyWorkspaceData}
                  className="btn btn-primary"
                >
                  🔄 Refresh
                </button>
              </div>

              {error && (
                <div className="alert alert-danger mb-4">
                  {error}
                </div>
              )}

              {/* Escalated Tasks Section */}
              <div className="card mb-4">
                <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                  <h3 className="card-title mb-0" style={{ color: 'white' }}>⚠️ Escalated Tasks (Supervisor Only)</h3>
                  <p className="text-muted mb-0" style={{ color: 'rgba(255,255,255,0.9)', fontSize: '0.875rem' }}>Tasks requiring immediate supervisor attention</p>
                </div>
                <div className="card-body">
                  {escalatedTasks.length === 0 ? (
                    <p className="text-center text-muted py-4">No escalated tasks at this time</p>
                  ) : (
                    <div>
                      {escalatedTasks.map((task) => (
                        <div
                          key={task.id}
                          className="card mb-3"
                          style={{ borderLeft: '4px solid ' + getStatusColor(task.status) }}
                        >
                          <div className="card-body">
                            <div className="d-flex justify-content-between align-items-start mb-2">
                              <h5 className="fw-semibold mb-0">{task.title}</h5>
                              <div className="d-flex gap-2">
                                <span className="badge" style={{ backgroundColor: getStatusColor(task.status), color: 'white' }}>
                                  {task.status}
                                </span>
                                <span className="badge" style={{ backgroundColor: getPriorityColor(task.priority), color: 'white' }}>
                                  {task.priority}
                                </span>
                              </div>
                            </div>
                            {task.description && (
                              <p className="text-muted mb-2 small">{task.description}</p>
                            )}
                            <div className="d-flex gap-4 small text-muted">
                              <span>👤 Assigned: {task.assignedTo || 'Unassigned'}</span>
                              {task.dueDate && (
                                <span>📅 Due: {new Date(task.dueDate).toLocaleDateString()}</span>
                              )}
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              {/* Users by Queue Section */}
              <div className="card">
                <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                  <h3 className="card-title mb-0" style={{ color: 'white' }}>👥 Users by Work Queue</h3>
                  <p className="text-muted mb-0" style={{ color: 'rgba(255,255,255,0.9)', fontSize: '0.875rem' }}>View which users are in which queues and their active tasks</p>
                </div>
                <div className="card-body">
                  {userQueueData.length === 0 ? (
                    <p className="text-center text-muted py-4">No users assigned to work queues yet</p>
                  ) : (
                    <div>
                      {userQueueData.map((userQueue, idx) => (
                        <div
                          key={`${userQueue.username}_${userQueue.queueName}_${idx}`}
                          className="card mb-3"
                        >
                          <div className="card-body">
                            <div className="d-flex justify-content-between align-items-start mb-3">
                              <div>
                                <h5 className="fw-bold mb-1">
                                  👤 {userQueue.username}
                                </h5>
                                <p className="text-muted mb-0 small">
                                  📋 Queue: {userQueue.queueDisplayName}
                                </p>
                              </div>
                              <span className="badge bg-primary" style={{ fontSize: '1rem', padding: '0.5rem 1rem' }}>
                                {userQueue.taskCount} Task{userQueue.taskCount !== 1 ? 's' : ''}
                              </span>
                            </div>
                            
                            {userQueue.tasks.length === 0 ? (
                              <p className="text-muted small mb-0">No active tasks</p>
                            ) : (
                              <div className="mt-3">
                                {userQueue.tasks.map((task) => (
                                  <div
                                    key={task.id}
                                    className="card mb-2"
                                    style={{ borderLeft: '3px solid ' + getStatusColor(task.status) }}
                                  >
                                    <div className="card-body py-2">
                                      <div className="d-flex justify-content-between align-items-start">
                                        <div className="flex-grow-1">
                                          <h6 className="fw-semibold mb-1 small">{task.title}</h6>
                                          {task.description && (
                                            <p className="text-muted small mb-1">{task.description}</p>
                                          )}
                                          {task.dueDate && (
                                            <p className="text-muted small mb-0">
                                              📅 Due: {new Date(task.dueDate).toLocaleDateString()}
                                            </p>
                                          )}
                                        </div>
                                        <div className="d-flex gap-2 ms-3">
                                          <span className="badge" style={{ backgroundColor: getStatusColor(task.status), color: 'white' }}>
                                            {task.status}
                                          </span>
                                          <span className="badge" style={{ backgroundColor: getPriorityColor(task.priority), color: 'white' }}>
                                            {task.priority}
                                          </span>
                                        </div>
                                      </div>
                                    </div>
                                  </div>
                                ))}
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {activeTab === 'workQueues' && (
            <>
              {queueView === 'workQueues' && (
                <div>
                  <div className="d-flex justify-content-between align-items-center mb-4">
                    <div>
                      <h2 className="card-title mb-1">Work Queues</h2>
                      <p className="text-muted">Manage work queues and subscriptions</p>
                    </div>
                    <button
                      onClick={loadDashboardData}
                      className="btn btn-primary"
                    >
                      <span className="ca-gov-icon-refresh" aria-hidden="true"></span>
                      Refresh
                    </button>
                  </div>

                  {error && (
                    <div className="alert alert-danger mb-4">
                      {error}
                    </div>
                  )}

                  {/* Work Queues Table */}
                  <div className="card">
                    <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                      <h3 className="card-title mb-0" style={{ color: 'white' }}>Work Queues List</h3>
                    </div>
                    <div className="card-body">
                      {queues.length === 0 ? (
                        <p className="text-center text-muted py-4">No work queues found</p>
                      ) : (
                        <div className="table-responsive">
                          <table className="table table-striped">
                            <thead>
                              <tr>
                                <th>Action</th>
                                <th>Name</th>
                                <th>Administrator</th>
                                <th>User Subscription</th>
                              </tr>
                            </thead>
                            <tbody>
                              {queues.map((queue) => (
                                <tr key={queue.name}>
                                  <td>
                                    <button
                                      onClick={() => handleViewQueue(queue)}
                                      className="btn btn-primary btn-sm"
                                    >
                                      View
                                    </button>
                                  </td>
                                  <td>{queue.displayName}</td>
                                  <td>
                                    <span className="badge bg-primary">ADMIN</span>
                                  </td>
                                  <td>
                                    <span className="badge bg-success">Yes</span>
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
              )}

              {queueView === 'queueTasks' && selectedQueue && (
                <div>
                  <div className="d-flex justify-content-between align-items-center mb-4">
                    <div>
                      <h2 className="card-title mb-1">
                        Work Queue Tasks: {selectedQueue.displayName}
                      </h2>
                      <p className="text-muted">{selectedQueue.description}</p>
                    </div>
                    <button
                      onClick={() => setQueueView('workQueues' as ViewType)}
                      className="btn btn-secondary"
                    >
                      <span className="ca-gov-icon-arrow-prev" aria-hidden="true"></span>
                      Back to Queues
                    </button>
                  </div>

                  {/* Action Buttons */}
                  <div className="card mb-4">
                    <div className="card-body">
                      <h3 className="card-title mb-4">Task Actions</h3>
                      <div className="d-flex gap-2 flex-wrap">
                        <button
                          onClick={() => handleReserveTasks(5)}
                          className="btn btn-primary"
                        >
                          Reserve Next 5 Tasks
                        </button>
                        <button
                          onClick={() => handleReserveTasks(20)}
                          className="btn btn-primary"
                        >
                          Reserve Next 20 Tasks
                        </button>
                        <button
                          onClick={() => handleReserveTasks()}
                          className="btn btn-primary"
                          disabled={selectedTasks.size === 0}
                        >
                          Reserve Selected ({selectedTasks.size})
                        </button>
                        <button
                          onClick={handleForwardTasks}
                          className="btn btn-primary"
                          disabled={selectedTasks.size === 0}
                        >
                          Forward Selected ({selectedTasks.size})
                        </button>
                      </div>
                    </div>
                  </div>

                  {/* Tasks Table */}
                  <div className="card">
                    <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                      <h3 className="card-title mb-0" style={{ color: 'white' }}>Total Tasks: {queueTasks.length}</h3>
                    </div>
                    <div className="card-body">
                      {queueTasks.length === 0 ? (
                        <p className="text-center text-muted py-4">No tasks found in this queue</p>
                      ) : (
                        <div className="table-responsive">
                          <table className="table table-striped">
                            <thead>
                              <tr>
                                <th>Action</th>
                                <th>Task ID</th>
                                <th>Subject</th>
                                <th>Priority</th>
                                <th>Status</th>
                                <th>Deadline</th>
                              </tr>
                            </thead>
                            <tbody>
                              {queueTasks.map((task) => (
                                <tr key={task.id}>
                                  <td>
                                    <input
                                      type="checkbox"
                                      checked={selectedTasks.has(task.id)}
                                      onChange={() => toggleTaskSelection(task.id)}
                                      className="me-2"
                                    />
                                    <button className="btn btn-link btn-sm p-0" style={{ textDecoration: 'none' }}>
                                      Reserve...
                                    </button>
                                  </td>
                                  <td>
                                    <button className="btn btn-link btn-sm p-0" style={{ textDecoration: 'none' }}>
                                      {task.id}
                                    </button>
                                  </td>
                                  <td>{task.title}</td>
                                  <td>
                                    <span className="badge" style={{ backgroundColor: getPriorityColor(task.priority), color: 'white' }}>
                                      {task.priority}
                                    </span>
                                  </td>
                                  <td>
                                    <span className="badge" style={{ backgroundColor: getStatusColor(task.status), color: 'white' }}>
                                      {task.status}
                                    </span>
                                  </td>
                                  <td>{task.dueDate ? new Date(task.dueDate).toLocaleDateString() : '-'}</td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* View Tabs */}
                  <div className="mt-4 border-b border-gray-300 mb-6">
                    <nav className="flex">
                      <button
                        onClick={() => setQueueView('queueTasks' as ViewType)}
                        className={`px-4 py-2 text-sm font-medium border-t border-l border-r rounded-t ${
                          (queueView as string) === 'queueTasks'
                            ? 'bg-white border-gray-300 text-gray-900'
                            : 'bg-gray-100 border-gray-300 text-gray-600 hover:bg-gray-50'
                        }`}
                      >
                        View Work Queue
                      </button>
                      <button
                        onClick={() => setQueueView('queueSubscriptions' as ViewType)}
                        className={`px-4 py-2 text-sm font-medium border-t border-l border-r rounded-t ${
                          (queueView as string) === 'queueSubscriptions'
                            ? 'bg-white border-gray-300 text-gray-900'
                            : 'bg-gray-100 border-gray-300 text-gray-600 hover:bg-gray-50'
                        }`}
                      >
                        Work Queues Subscriptions
                      </button>
                    </nav>
                  </div>
                </div>
              )}

              {queueView === 'queueSubscriptions' && selectedQueue && (
                <div>
                  <div className="d-flex justify-content-between align-items-center mb-4">
                    <div>
                      <h2 className="card-title mb-1">
                        Work Queue Subscriptions: {selectedQueue.displayName}
                      </h2>
                      <p className="text-muted">Manage user subscriptions for this work queue</p>
                    </div>
                    <button
                      onClick={() => setQueueView('workQueues' as ViewType)}
                      className="btn btn-secondary"
                    >
                      <span className="ca-gov-icon-arrow-prev" aria-hidden="true"></span>
                      Back to Queues
                    </button>
                  </div>

                  {/* View Tabs */}
                  <div className="mb-4">
                    <ul className="nav nav-tabs" role="tablist">
                      <li className="nav-item" role="presentation">
                        <button
                          className={`nav-link ${(queueView as string) === 'queueTasks' ? 'active' : ''}`}
                          onClick={() => setQueueView('queueTasks' as ViewType)}
                          type="button"
                        >
                          View Work Queue
                        </button>
                      </li>
                      <li className="nav-item" role="presentation">
                        <button
                          className={`nav-link ${(queueView as string) === 'queueSubscriptions' ? 'active' : ''}`}
                          onClick={() => setQueueView('queueSubscriptions' as ViewType)}
                          type="button"
                        >
                          Work Queues Subscriptions
                        </button>
                      </li>
                    </ul>
                  </div>

                  {/* Subscriptions Table */}
                  <div className="card">
                    <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                      <h3 className="card-title mb-0" style={{ color: 'white' }}>Subscribed Users</h3>
                    </div>
                    <div className="card-body">
                      <div className="mb-4">
                        <button
                          onClick={() => setShowAddSubscriptionModal(true)}
                          className="btn btn-primary"
                        >
                          Add User Subscription
                        </button>
                      </div>

                      {queueSubscriptions.length === 0 ? (
                        <p className="text-center text-muted py-4">No users subscribed to this queue</p>
                      ) : (
                        <div className="table-responsive">
                          <table className="table table-striped table-hover">
                            <thead>
                              <tr>
                                <th>Action</th>
                                <th>Name</th>
                                <th>Subscription Date</th>
                              </tr>
                            </thead>
                            <tbody>
                              {queueSubscriptions.map((sub) => (
                                <tr key={sub.id}>
                                  <td>
                                    <button
                                      onClick={() => handleRemoveSubscription(sub.username)}
                                      className="btn btn-link btn-sm p-0"
                                    >
                                      Remove...
                                    </button>
                                  </td>
                                  <td>{sub.username}</td>
                                  <td>
                                    {new Date(sub.createdAt).toLocaleDateString('en-US', { 
                                      year: 'numeric', 
                                      month: '2-digit', 
                                      day: '2-digit',
                                      hour: '2-digit',
                                      minute: '2-digit'
                                    })}
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
              )}
            </>
          )}

          {activeTab === 'analytics' && <AnalyticsTab />}

          {activeTab === 'batchJobs' && <BatchJobsTab />}
        </div>

        {/* Add Subscription Modal */}
        {showAddSubscriptionModal && selectedQueue && (
        <div
          className="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center"
          style={{ backgroundColor: 'rgba(0, 0, 0, 0.5)', zIndex: 1050 }}
          onClick={() => setShowAddSubscriptionModal(false)}
        >
            <div
              className="card"
              style={{ maxWidth: '500px', width: '90%' }}
              onClick={(e) => e.stopPropagation()}
            >
              {/* Modal Header */}
              <div className="card-header" style={{ backgroundColor: 'var(--color-p2, #046b99)', color: 'white' }}>
                <div className="d-flex justify-content-between align-items-center">
                  <h3 className="card-title mb-0" style={{ color: 'white' }}>
                    Add Work Queue Subscription: {selectedQueue.displayName}
                  </h3>
                  <button
                    onClick={() => setShowAddSubscriptionModal(false)}
                    className="btn-close btn-close-white"
                    aria-label="Close"
                  ></button>
                </div>
              </div>
            
            {/* Modal Body */}
            <div className="card-body">
              <div className="mb-3">
                <label htmlFor="user-select" className="form-label fw-bold">
                  User: <span className="text-danger">*</span>
                </label>
                <select
                  id="user-select"
                  value={selectedUserToAdd}
                  onChange={(e) => setSelectedUserToAdd(e.target.value)}
                  className="form-select"
                >
                  <option value="">-- Select User --</option>
                  {allUsers.map((u) => (
                    <option key={u.id} value={u.username}>
                      {u.username} {u.firstName && u.lastName && `(${u.firstName} ${u.lastName})`}
                    </option>
                  ))}
                </select>
                <p className="text-muted small mt-2">
                  <span className="text-danger">*</span> required field
                </p>
              </div>
            </div>
            
            {/* Modal Footer */}
            <div className="card-footer d-flex justify-content-end gap-2">
              <button
                onClick={() => {
                  setShowAddSubscriptionModal(false);
                  setSelectedUserToAdd('');
                }}
                className="btn btn-secondary"
              >
                Cancel
              </button>
              <button
                onClick={handleAddSubscription}
                disabled={!selectedUserToAdd}
                className="btn btn-primary"
              >
                Save
              </button>
            </div>
          </div>
        </div>
      )}
      </main>
    </div>
  );
}

export default dynamic(() => Promise.resolve(SupervisorDashboardComponent), { ssr: false });
