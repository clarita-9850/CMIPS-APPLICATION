'use client';

import { useQuery } from '@tanstack/react-query';
import {
  CheckCircle,
  XCircle,
  Clock,
  Activity,
  ListTodo,
  Loader2,
} from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { dashboardApi } from '@/lib/api';
import { formatRelativeTime, getStatusColor } from '@/lib/utils';
import type { DashboardStats, ExecutionSummary } from '@/types';

function StatCard({
  title,
  value,
  icon: Icon,
  description,
}: {
  title: string;
  value: number | string;
  icon: React.ComponentType<{ className?: string }>;
  description?: string;
}) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        <Icon className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        {description && <p className="text-xs text-muted-foreground">{description}</p>}
      </CardContent>
    </Card>
  );
}

function RecentExecutions({ executions }: { executions: ExecutionSummary[] }) {
  if (!executions.length) {
    return (
      <div className="text-center text-muted-foreground py-8">No recent executions</div>
    );
  }

  return (
    <div className="space-y-4">
      {executions.map((execution) => (
        <div
          key={execution.id}
          className="flex items-center justify-between border-b pb-4 last:border-0"
        >
          <div className="flex items-center gap-3">
            <div>
              <p className="font-medium">{execution.jobName}</p>
              <p className="text-sm text-muted-foreground">
                {formatRelativeTime(execution.triggeredAt)}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Badge className={getStatusColor(execution.status)}>{execution.status}</Badge>
            {execution.status === 'RUNNING' && (
              <span className="text-sm text-muted-foreground">
                {execution.progressPercentage}%
              </span>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}

function RunningJobs({ executions }: { executions: ExecutionSummary[] }) {
  if (!executions.length) {
    return (
      <div className="text-center text-muted-foreground py-8">No jobs currently running</div>
    );
  }

  return (
    <div className="space-y-4">
      {executions.map((execution) => (
        <div
          key={execution.id}
          className="flex items-center justify-between border-b pb-4 last:border-0"
        >
          <div>
            <p className="font-medium">{execution.jobName}</p>
            <p className="text-sm text-muted-foreground">{execution.progressMessage || 'Running...'}</p>
          </div>
          <div className="flex items-center gap-3">
            <div className="w-32 h-2 bg-gray-200 rounded-full overflow-hidden">
              <div
                className="h-full bg-blue-500 transition-all"
                style={{ width: `${execution.progressPercentage}%` }}
              />
            </div>
            <span className="text-sm font-medium">{execution.progressPercentage}%</span>
          </div>
        </div>
      ))}
    </div>
  );
}

export default function DashboardPage() {
  const { data: stats, isLoading: statsLoading } = useQuery<DashboardStats>({
    queryKey: ['dashboard', 'stats'],
    queryFn: () => dashboardApi.getStats(),
    refetchInterval: 10000, // Refresh every 10 seconds
  });

  const { data: recentExecutions, isLoading: recentLoading } = useQuery<ExecutionSummary[]>({
    queryKey: ['dashboard', 'recent'],
    queryFn: () => dashboardApi.getRecent(10),
    refetchInterval: 5000,
  });

  const { data: runningExecutions, isLoading: runningLoading } = useQuery<ExecutionSummary[]>({
    queryKey: ['dashboard', 'running'],
    queryFn: () => dashboardApi.getRunning(),
    refetchInterval: 3000,
  });

  if (statsLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
        <p className="text-muted-foreground">Overview of batch job scheduling</p>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Total Jobs"
          value={stats?.totalJobs || 0}
          icon={ListTodo}
          description="Configured job definitions"
        />
        <StatCard
          title="Active Jobs"
          value={stats?.activeJobs || 0}
          icon={Activity}
          description="Jobs ready to run"
        />
        <StatCard
          title="Running Now"
          value={stats?.runningExecutions || 0}
          icon={Clock}
          description="Currently executing"
        />
        <StatCard
          title="Completed Today"
          value={stats?.completedToday || 0}
          icon={CheckCircle}
          description={`${stats?.failedToday || 0} failed`}
        />
      </div>

      {/* Two Column Layout */}
      <div className="grid gap-6 md:grid-cols-2">
        {/* Running Jobs */}
        <Card>
          <CardHeader>
            <CardTitle>Running Jobs</CardTitle>
            <CardDescription>Currently executing jobs</CardDescription>
          </CardHeader>
          <CardContent>
            {runningLoading ? (
              <div className="flex justify-center py-4">
                <Loader2 className="h-6 w-6 animate-spin" />
              </div>
            ) : (
              <RunningJobs executions={runningExecutions || []} />
            )}
          </CardContent>
        </Card>

        {/* Recent Executions */}
        <Card>
          <CardHeader>
            <CardTitle>Recent Executions</CardTitle>
            <CardDescription>Latest job runs</CardDescription>
          </CardHeader>
          <CardContent>
            {recentLoading ? (
              <div className="flex justify-center py-4">
                <Loader2 className="h-6 w-6 animate-spin" />
              </div>
            ) : (
              <RecentExecutions executions={recentExecutions || []} />
            )}
          </CardContent>
        </Card>
      </div>

      {/* Job Status Distribution */}
      {stats?.jobsByStatus && (
        <Card>
          <CardHeader>
            <CardTitle>Jobs by Status</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-4">
              {Object.entries(stats.jobsByStatus).map(([status, count]) => (
                <div key={status} className="flex items-center gap-2">
                  <Badge className={getStatusColor(status as any)}>{status}</Badge>
                  <span className="font-medium">{count}</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
