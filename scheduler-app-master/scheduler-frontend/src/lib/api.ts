import axios from 'axios';
import type {
  JobDefinition,
  ExecutionSummary,
  DashboardStats,
  DependencyGraph,
  JobCalendar,
  AuditLog,
  Page,
  CreateJobRequest,
  UpdateJobRequest,
  TriggerJobRequest,
  AddDependencyRequest,
  DependencyInfo,
  JobStatus,
} from '@/types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8084';

const api = axios.create({
  baseURL: `${API_BASE_URL}/api/scheduler`,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Job APIs
export const jobApi = {
  getAll: async (page = 0, size = 20): Promise<Page<JobDefinition>> => {
    const { data } = await api.get('/jobs', { params: { page, size } });
    return data;
  },

  search: async (q: string, page = 0, size = 20): Promise<Page<JobDefinition>> => {
    const { data } = await api.get('/jobs/search', { params: { q, page, size } });
    return data;
  },

  filter: async (
    status?: JobStatus,
    jobType?: string,
    enabled?: boolean,
    page = 0,
    size = 20
  ): Promise<Page<JobDefinition>> => {
    const { data } = await api.get('/jobs/filter', {
      params: { status, jobType, enabled, page, size },
    });
    return data;
  },

  getById: async (id: number): Promise<JobDefinition> => {
    const { data } = await api.get(`/jobs/${id}`);
    return data;
  },

  getByName: async (name: string): Promise<JobDefinition> => {
    const { data } = await api.get(`/jobs/name/${name}`);
    return data;
  },

  getTypes: async (): Promise<string[]> => {
    const { data } = await api.get('/jobs/types');
    return data;
  },

  create: async (request: CreateJobRequest): Promise<JobDefinition> => {
    const { data } = await api.post('/jobs', request);
    return data;
  },

  update: async (id: number, request: UpdateJobRequest): Promise<JobDefinition> => {
    const { data } = await api.put(`/jobs/${id}`, request);
    return data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/jobs/${id}`);
  },

  hold: async (id: number): Promise<JobDefinition> => {
    const { data } = await api.post(`/jobs/${id}/hold`);
    return data;
  },

  ice: async (id: number): Promise<JobDefinition> => {
    const { data } = await api.post(`/jobs/${id}/ice`);
    return data;
  },

  resume: async (id: number): Promise<JobDefinition> => {
    const { data } = await api.post(`/jobs/${id}/resume`);
    return data;
  },

  enable: async (id: number): Promise<JobDefinition> => {
    const { data } = await api.post(`/jobs/${id}/enable`);
    return data;
  },

  disable: async (id: number): Promise<JobDefinition> => {
    const { data } = await api.post(`/jobs/${id}/disable`);
    return data;
  },

  // Dependencies
  getDependencies: async (id: number): Promise<DependencyInfo[]> => {
    const { data } = await api.get(`/jobs/${id}/dependencies`);
    return data;
  },

  getDependents: async (id: number): Promise<DependencyInfo[]> => {
    const { data } = await api.get(`/jobs/${id}/dependents`);
    return data;
  },

  addDependency: async (id: number, request: AddDependencyRequest): Promise<DependencyInfo> => {
    const { data } = await api.post(`/jobs/${id}/dependencies`, request);
    return data;
  },

  removeDependency: async (id: number, dependsOnJobId: number): Promise<void> => {
    await api.delete(`/jobs/${id}/dependencies/${dependsOnJobId}`);
  },

  // Executions
  getExecutions: async (id: number, page = 0, size = 20): Promise<Page<ExecutionSummary>> => {
    const { data } = await api.get(`/jobs/${id}/executions`, { params: { page, size } });
    return data;
  },
};

// Trigger APIs
export const triggerApi = {
  trigger: async (jobId: number, request?: TriggerJobRequest): Promise<ExecutionSummary> => {
    const { data } = await api.post(`/trigger/${jobId}`, request || {});
    return data;
  },

  stop: async (triggerId: string): Promise<void> => {
    await api.post(`/trigger/stop/${triggerId}`);
  },

  getStatus: async (triggerId: string): Promise<ExecutionSummary> => {
    const { data } = await api.get(`/trigger/status/${triggerId}`);
    return data;
  },

  getRunning: async (): Promise<ExecutionSummary[]> => {
    const { data } = await api.get('/trigger/running');
    return data;
  },
};

// Dashboard APIs
export const dashboardApi = {
  getStats: async (): Promise<DashboardStats> => {
    const { data } = await api.get('/dashboard/stats');
    return data;
  },

  getRecent: async (limit = 10): Promise<ExecutionSummary[]> => {
    const { data } = await api.get('/dashboard/recent', { params: { limit } });
    return data;
  },

  getRunning: async (): Promise<ExecutionSummary[]> => {
    const { data } = await api.get('/dashboard/running');
    return data;
  },
};

// Graph APIs
export const graphApi = {
  getFullGraph: async (): Promise<DependencyGraph> => {
    const { data } = await api.get('/graph');
    return data;
  },

  getSubgraph: async (jobId: number, depth = 2): Promise<DependencyGraph> => {
    const { data } = await api.get(`/graph/subgraph/${jobId}`, { params: { depth } });
    return data;
  },

  getExecutionOrder: async (jobIds: number[]): Promise<number[]> => {
    const { data } = await api.get('/graph/execution-order', {
      params: { jobIds: jobIds.join(',') },
    });
    return data;
  },
};

// Calendar APIs
export const calendarApi = {
  getAll: async (): Promise<JobCalendar[]> => {
    const { data } = await api.get('/calendars');
    return data;
  },

  getById: async (id: number): Promise<JobCalendar> => {
    const { data } = await api.get(`/calendars/${id}`);
    return data;
  },

  create: async (name: string, description: string, type: string): Promise<JobCalendar> => {
    const { data } = await api.post('/calendars', { name, description, type });
    return data;
  },

  getDates: async (id: number, start: string, end: string): Promise<string[]> => {
    const { data } = await api.get(`/calendars/${id}/dates`, { params: { start, end } });
    return data;
  },

  addDate: async (id: number, date: string, description?: string): Promise<void> => {
    await api.post(`/calendars/${id}/dates`, { date, description });
  },

  getCalendarsForJob: async (jobId: number): Promise<JobCalendar[]> => {
    const { data } = await api.get(`/calendars/job/${jobId}`);
    return data;
  },
};

// Audit APIs
export const auditApi = {
  getEntityHistory: async (entityType: string, entityId: number, page = 0, size = 20): Promise<Page<AuditLog>> => {
    const { data } = await api.get(`/audit/entity/${entityType}/${entityId}`, { params: { page, size } });
    return data;
  },

  getRecentOperations: async (hours = 24, page = 0, size = 50): Promise<Page<AuditLog>> => {
    const { data } = await api.get('/audit/operations/recent', { params: { hours, page, size } });
    return data;
  },
};

// Admin APIs
export const adminApi = {
  getStatus: async (): Promise<Record<string, unknown>> => {
    const { data } = await api.get('/admin/status');
    return data;
  },

  pause: async (): Promise<Record<string, string>> => {
    const { data } = await api.post('/admin/pause');
    return data;
  },

  resume: async (): Promise<Record<string, string>> => {
    const { data } = await api.post('/admin/resume');
    return data;
  },

  checkCmipsBackendHealth: async (): Promise<Record<string, unknown>> => {
    const { data } = await api.get('/admin/health/cmips-backend');
    return data;
  },
};

export default api;
