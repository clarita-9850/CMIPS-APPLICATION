'use client';

import { useParams, useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Link from 'next/link';
import {
  ArrowLeft,
  Play,
  Pause,
  Snowflake,
  Clock,
  Calendar,
  Settings,
  History,
  GitBranch,
  AlertCircle,
  CheckCircle2,
  XCircle,
  Loader2,
  RefreshCw,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { jobApi, triggerApi } from '@/lib/api';
import { formatRelativeTime, getStatusColor, parseCronExpression } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import type { JobDefinition, ExecutionSummary, DependencyInfo, Page } from '@/types';

function getExecutionStatusIcon(status: string) {
  switch (status) {
    case 'COMPLETED':
      return <CheckCircle2 className="h-4 w-4 text-green-500" />;
    case 'FAILED':
    case 'ABANDONED':
      return <XCircle className="h-4 w-4 text-red-500" />;
    case 'RUNNING':
    case 'STARTING':
      return <Loader2 className="h-4 w-4 text-blue-500 animate-spin" />;
    case 'STOPPED':
      return <AlertCircle className="h-4 w-4 text-yellow-500" />;
    default:
      return <Clock className="h-4 w-4 text-gray-500" />;
  }
}

function getExecutionStatusColor(status: string) {
  switch (status) {
    case 'COMPLETED':
      return 'bg-green-100 text-green-800';
    case 'FAILED':
    case 'ABANDONED':
      return 'bg-red-100 text-red-800';
    case 'RUNNING':
    case 'STARTING':
      return 'bg-blue-100 text-blue-800';
    case 'STOPPED':
      return 'bg-yellow-100 text-yellow-800';
    case 'QUEUED':
    case 'TRIGGERED':
      return 'bg-purple-100 text-purple-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
}

function formatDuration(seconds?: number) {
  if (!seconds) return '-';
  if (seconds < 60) return `${seconds}s`;
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ${seconds % 60}s`;
  return `${Math.floor(seconds / 3600)}h ${Math.floor((seconds % 3600) / 60)}m`;
}

function formatDateTime(dateStr?: string) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString();
}

export default function JobDetailsPage() {
  const params = useParams();
  const router = useRouter();
  const jobId = Number(params.id);
  const queryClient = useQueryClient();
  const { toast } = useToast();

  // Fetch job details
  const { data: job, isLoading: jobLoading } = useQuery<JobDefinition>({
    queryKey: ['job', jobId],
    queryFn: () => jobApi.getById(jobId),
    enabled: !!jobId,
  });

  // Fetch dependencies
  const { data: dependencies } = useQuery<DependencyInfo[]>({
    queryKey: ['job-dependencies', jobId],
    queryFn: () => jobApi.getDependencies(jobId),
    enabled: !!jobId,
  });

  // Fetch dependents
  const { data: dependents } = useQuery<DependencyInfo[]>({
    queryKey: ['job-dependents', jobId],
    queryFn: () => jobApi.getDependents(jobId),
    enabled: !!jobId,
  });

  // Fetch execution history
  const { data: executionsData } = useQuery<Page<ExecutionSummary>>({
    queryKey: ['job-executions', jobId],
    queryFn: () => jobApi.getExecutions(jobId, 0, 10),
    enabled: !!jobId,
    refetchInterval: 5000, // Refresh every 5 seconds for progress updates
  });

  // Mutations
  const triggerMutation = useMutation({
    mutationFn: () => triggerApi.trigger(jobId),
    onSuccess: () => {
      toast({ title: 'Job triggered successfully' });
      queryClient.invalidateQueries({ queryKey: ['job', jobId] });
      queryClient.invalidateQueries({ queryKey: ['job-executions', jobId] });
    },
    onError: (error: any) => {
      toast({
        title: 'Failed to trigger job',
        description: error.response?.data?.message || error.message,
        variant: 'destructive',
      });
    },
  });

  const holdMutation = useMutation({
    mutationFn: () => jobApi.hold(jobId),
    onSuccess: () => {
      toast({ title: 'Job put on hold' });
      queryClient.invalidateQueries({ queryKey: ['job', jobId] });
    },
  });

  const resumeMutation = useMutation({
    mutationFn: () => jobApi.resume(jobId),
    onSuccess: () => {
      toast({ title: 'Job resumed' });
      queryClient.invalidateQueries({ queryKey: ['job', jobId] });
    },
  });

  const iceMutation = useMutation({
    mutationFn: () => jobApi.ice(jobId),
    onSuccess: () => {
      toast({ title: 'Job put on ice' });
      queryClient.invalidateQueries({ queryKey: ['job', jobId] });
    },
  });

  if (jobLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!job) {
    return (
      <div className="flex flex-col items-center justify-center h-64 space-y-4">
        <AlertCircle className="h-12 w-12 text-muted-foreground" />
        <p className="text-lg text-muted-foreground">Job not found</p>
        <Link href="/jobs">
          <Button variant="outline">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Jobs
          </Button>
        </Link>
      </div>
    );
  }

  const executions = executionsData?.content || [];
  const latestExecution = executions[0];
  const isRunning = latestExecution && ['RUNNING', 'STARTING', 'TRIGGERED', 'QUEUED'].includes(latestExecution.status);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link href="/jobs">
            <Button variant="ghost" size="icon">
              <ArrowLeft className="h-5 w-5" />
            </Button>
          </Link>
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-bold tracking-tight">{job.jobName}</h1>
              <Badge className={getStatusColor(job.status)}>{job.status}</Badge>
              <Badge variant="outline">{job.jobType}</Badge>
              {!job.enabled && <Badge variant="secondary">Disabled</Badge>}
            </div>
            {job.description && (
              <p className="text-muted-foreground mt-1">{job.description}</p>
            )}
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button
            onClick={() => triggerMutation.mutate()}
            disabled={triggerMutation.isPending || isRunning}
          >
            {triggerMutation.isPending ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Play className="mr-2 h-4 w-4" />
            )}
            Run Now
          </Button>
          {job.status === 'ACTIVE' && (
            <>
              <Button variant="outline" onClick={() => holdMutation.mutate()}>
                <Pause className="mr-2 h-4 w-4" />
                Hold
              </Button>
              <Button variant="outline" onClick={() => iceMutation.mutate()}>
                <Snowflake className="mr-2 h-4 w-4" />
                Ice
              </Button>
            </>
          )}
          {job.status === 'ON_HOLD' && (
            <Button variant="outline" onClick={() => resumeMutation.mutate()}>
              <RefreshCw className="mr-2 h-4 w-4" />
              Resume
            </Button>
          )}
        </div>
      </div>

      {/* Running Execution Progress */}
      {isRunning && latestExecution && (
        <Card className="border-blue-200 bg-blue-50">
          <CardHeader className="pb-2">
            <CardTitle className="text-lg flex items-center gap-2">
              <Loader2 className="h-5 w-5 animate-spin text-blue-600" />
              Job Running
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Progress</span>
                <span className="font-medium">{latestExecution.progressPercentage}%</span>
              </div>
              <div className="w-full bg-blue-200 rounded-full h-2">
                <div
                  className="bg-blue-600 h-2 rounded-full transition-all duration-500"
                  style={{ width: `${latestExecution.progressPercentage}%` }}
                />
              </div>
              {latestExecution.progressMessage && (
                <p className="text-sm text-muted-foreground">{latestExecution.progressMessage}</p>
              )}
              <div className="flex gap-4 text-sm text-muted-foreground">
                <span>Started: {formatDateTime(latestExecution.startedAt)}</span>
                <span>Trigger: {latestExecution.triggerType}</span>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid gap-6 md:grid-cols-2">
        {/* Job Configuration */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Settings className="h-5 w-5" />
              Configuration
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-muted-foreground">Schedule</p>
                <p className="font-medium">
                  {job.cronExpression ? parseCronExpression(job.cronExpression) : 'Manual only'}
                </p>
                {job.cronExpression && (
                  <p className="text-xs text-muted-foreground font-mono">{job.cronExpression}</p>
                )}
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Timezone</p>
                <p className="font-medium">{job.timezone}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Priority</p>
                <p className="font-medium">{job.priority}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Max Retries</p>
                <p className="font-medium">{job.maxRetries}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Timeout</p>
                <p className="font-medium">{formatDuration(job.timeoutSeconds)}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Next Run</p>
                <p className="font-medium">
                  {job.nextFireTime ? formatDateTime(job.nextFireTime) : '-'}
                </p>
              </div>
            </div>
            <div className="pt-2 border-t">
              <p className="text-sm text-muted-foreground mb-1">Created</p>
              <p className="text-sm">
                {formatDateTime(job.createdAt)} by {job.createdBy || 'system'}
              </p>
              <p className="text-sm text-muted-foreground mb-1 mt-2">Last Updated</p>
              <p className="text-sm">
                {formatDateTime(job.updatedAt)} by {job.updatedBy || 'system'}
              </p>
            </div>
          </CardContent>
        </Card>

        {/* Dependencies */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <GitBranch className="h-5 w-5" />
              Dependencies
            </CardTitle>
            <CardDescription>Jobs that must complete before this job runs</CardDescription>
          </CardHeader>
          <CardContent>
            {dependencies && dependencies.length > 0 ? (
              <div className="space-y-2">
                {dependencies.map((dep) => (
                  <Link
                    key={dep.id}
                    href={`/jobs/${dep.dependsOnJobId}`}
                    className="flex items-center justify-between p-2 rounded-lg hover:bg-muted transition-colors"
                  >
                    <div className="flex items-center gap-2">
                      <GitBranch className="h-4 w-4 text-muted-foreground" />
                      <span className="font-medium">{dep.dependsOnJobName}</span>
                    </div>
                    <Badge variant="outline" className="text-xs">
                      {dep.dependencyType}
                    </Badge>
                  </Link>
                ))}
              </div>
            ) : (
              <p className="text-sm text-muted-foreground text-center py-4">
                No dependencies configured
              </p>
            )}

            {dependents && dependents.length > 0 && (
              <>
                <div className="border-t my-4" />
                <p className="text-sm font-medium mb-2">Dependent Jobs</p>
                <p className="text-xs text-muted-foreground mb-2">
                  Jobs that depend on this job
                </p>
                <div className="space-y-2">
                  {dependents.map((dep) => (
                    <Link
                      key={dep.id}
                      href={`/jobs/${dep.jobId}`}
                      className="flex items-center justify-between p-2 rounded-lg hover:bg-muted transition-colors"
                    >
                      <div className="flex items-center gap-2">
                        <GitBranch className="h-4 w-4 text-muted-foreground rotate-180" />
                        <span className="font-medium">{dep.jobName}</span>
                      </div>
                      <Badge variant="outline" className="text-xs">
                        {dep.dependencyType}
                      </Badge>
                    </Link>
                  ))}
                </div>
              </>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Execution History */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <History className="h-5 w-5" />
            Execution History
          </CardTitle>
          <CardDescription>Recent job executions and their status</CardDescription>
        </CardHeader>
        <CardContent>
          {executions.length > 0 ? (
            <div className="space-y-3">
              {executions.map((execution) => (
                <div
                  key={execution.id}
                  className="flex items-center justify-between p-3 rounded-lg border hover:bg-muted/50 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    {getExecutionStatusIcon(execution.status)}
                    <div>
                      <div className="flex items-center gap-2">
                        <Badge className={getExecutionStatusColor(execution.status)}>
                          {execution.status}
                        </Badge>
                        <span className="text-sm text-muted-foreground">
                          {execution.triggerType}
                        </span>
                      </div>
                      <p className="text-sm text-muted-foreground mt-1">
                        {execution.progressMessage || 'No message'}
                      </p>
                      {execution.errorMessage && (
                        <p className="text-sm text-red-600 mt-1">{execution.errorMessage}</p>
                      )}
                    </div>
                  </div>
                  <div className="text-right text-sm">
                    <p className="font-medium">{formatDateTime(execution.triggeredAt)}</p>
                    <div className="flex items-center gap-2 text-muted-foreground">
                      <Clock className="h-3 w-3" />
                      <span>{formatDuration(execution.durationSeconds)}</span>
                      {execution.retryCount > 0 && (
                        <span className="text-orange-600">
                          (Retry #{execution.retryCount})
                        </span>
                      )}
                    </div>
                    <p className="text-xs text-muted-foreground">
                      by {execution.triggeredBy || 'system'}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-muted-foreground">
              <History className="h-12 w-12 mx-auto mb-2 opacity-50" />
              <p>No executions yet</p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
