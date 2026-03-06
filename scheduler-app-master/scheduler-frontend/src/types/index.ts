// Job Definition Types
export type JobStatus = 'ACTIVE' | 'INACTIVE' | 'ON_HOLD' | 'ON_ICE';
export type DependencyType = 'SUCCESS' | 'COMPLETION' | 'FAILURE';
export type ExecutionStatus = 'TRIGGERED' | 'QUEUED' | 'STARTING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'STOPPED' | 'ABANDONED' | 'UNKNOWN';
export type TriggerType = 'SCHEDULED' | 'MANUAL' | 'DEPENDENCY' | 'RETRY' | 'API';
export type CalendarType = 'BLACKOUT' | 'HOLIDAY' | 'MAINTENANCE' | 'CUSTOM';
export type AssignmentType = 'EXCLUDE' | 'INCLUDE_ONLY';

export interface JobDefinition {
  id: number;
  jobName: string;
  jobType: string;
  description?: string;
  cronExpression?: string;
  timezone: string;
  status: JobStatus;
  enabled: boolean;
  priority: number;
  maxRetries: number;
  timeoutSeconds: number;
  jobParameters?: Record<string, unknown>;
  targetRoles?: string[];
  targetCounties?: string[];
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
  dependencies?: DependencyInfo[];
  dependents?: DependencyInfo[];
  nextFireTime?: string;
  lastExecution?: ExecutionSummary;
}

export interface DependencyInfo {
  id: number;
  jobId: number;
  jobName: string;
  dependsOnJobId: number;
  dependsOnJobName: string;
  dependencyType: DependencyType;
  isActive: boolean;
}

export interface ExecutionSummary {
  id: number;
  triggerId: string;
  jobDefinitionId: number;
  jobName: string;
  status: ExecutionStatus;
  triggerType: TriggerType;
  triggeredAt: string;
  startedAt?: string;
  completedAt?: string;
  progressPercentage: number;
  progressMessage?: string;
  errorMessage?: string;
  retryCount: number;
  triggeredBy?: string;
  durationSeconds?: number;
}

export interface DashboardStats {
  totalJobs: number;
  activeJobs: number;
  runningExecutions: number;
  completedToday: number;
  failedToday: number;
  executionsByStatus: Record<string, number>;
  jobsByType: Record<string, number>;
  jobsByStatus: Record<string, number>;
}

export interface JobCalendar {
  id: number;
  calendarName: string;
  description?: string;
  calendarType: CalendarType;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface AuditLog {
  id: number;
  entityType: string;
  entityId: number;
  action: string;
  actionTimestamp: string;
  performedBy: string;
  performedByRole?: string;
  previousState?: Record<string, unknown>;
  newState?: Record<string, unknown>;
  changeSummary?: string;
  ipAddress?: string;
  userAgent?: string;
}

// Graph Types for React Flow
export interface GraphNode {
  id: string;
  label: string;
  type?: string;
  status?: string;
  enabled?: boolean;
}

export interface GraphEdge {
  source: string;
  target: string;
}

export interface DependencyGraph {
  nodes: GraphNode[];
  edges: GraphEdge[];
}

// Request Types
export interface CreateJobRequest {
  jobName: string;
  jobType: string;
  description?: string;
  cronExpression?: string;
  timezone?: string;
  enabled?: boolean;
  priority?: number;
  maxRetries?: number;
  timeoutSeconds?: number;
  jobParameters?: Record<string, unknown>;
  targetRoles?: string[];
  targetCounties?: string[];
}

export interface UpdateJobRequest {
  jobType?: string;
  description?: string;
  cronExpression?: string;
  timezone?: string;
  enabled?: boolean;
  priority?: number;
  maxRetries?: number;
  timeoutSeconds?: number;
  jobParameters?: Record<string, unknown>;
  targetRoles?: string[];
  targetCounties?: string[];
}

export interface TriggerJobRequest {
  parameters?: Record<string, unknown>;
  skipDependencyCheck?: boolean;
}

export interface AddDependencyRequest {
  dependsOnJobId: number;
  dependencyType?: DependencyType;
}

// Paginated Response
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
