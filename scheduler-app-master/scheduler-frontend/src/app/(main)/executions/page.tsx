'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Loader2, StopCircle, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { triggerApi, dashboardApi } from '@/lib/api';
import { formatRelativeTime, formatDuration, getStatusColor } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import type { ExecutionSummary } from '@/types';

export default function ExecutionsPage() {
  const queryClient = useQueryClient();
  const { toast } = useToast();

  const { data: runningExecutions, isLoading: runningLoading } = useQuery<ExecutionSummary[]>({
    queryKey: ['executions', 'running'],
    queryFn: () => triggerApi.getRunning(),
    refetchInterval: 3000,
  });

  const { data: recentExecutions, isLoading: recentLoading } = useQuery<ExecutionSummary[]>({
    queryKey: ['executions', 'recent'],
    queryFn: () => dashboardApi.getRecent(50),
    refetchInterval: 10000,
  });

  const stopMutation = useMutation({
    mutationFn: (triggerId: string) => triggerApi.stop(triggerId),
    onSuccess: () => {
      toast({ title: 'Execution stopped' });
      queryClient.invalidateQueries({ queryKey: ['executions'] });
    },
    onError: (error: any) => {
      toast({
        title: 'Failed to stop execution',
        description: error.response?.data?.message || error.message,
        variant: 'destructive',
      });
    },
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Executions</h1>
          <p className="text-muted-foreground">Monitor job executions</p>
        </div>
        <Button
          variant="outline"
          onClick={() => {
            queryClient.invalidateQueries({ queryKey: ['executions'] });
          }}
        >
          <RefreshCw className="mr-2 h-4 w-4" />
          Refresh
        </Button>
      </div>

      {/* Running Executions */}
      <Card>
        <CardHeader>
          <CardTitle>Running Executions</CardTitle>
          <CardDescription>Currently executing jobs</CardDescription>
        </CardHeader>
        <CardContent>
          {runningLoading ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin" />
            </div>
          ) : runningExecutions?.length === 0 ? (
            <p className="text-center text-muted-foreground py-8">No jobs currently running</p>
          ) : (
            <div className="space-y-4">
              {runningExecutions?.map((execution) => (
                <div
                  key={execution.id}
                  className="flex items-center justify-between p-4 border rounded-lg"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold">{execution.jobName}</span>
                      <Badge className={getStatusColor(execution.status)}>
                        {execution.status}
                      </Badge>
                      <Badge variant="outline">{execution.triggerType}</Badge>
                    </div>
                    <div className="text-sm text-muted-foreground">
                      {execution.progressMessage || 'Running...'}
                    </div>
                    <div className="text-xs text-muted-foreground">
                      Started {formatRelativeTime(execution.triggeredAt)}
                      {execution.triggeredBy && ` by ${execution.triggeredBy}`}
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <div className="w-40">
                      <div className="flex items-center justify-between text-sm mb-1">
                        <span>Progress</span>
                        <span>{execution.progressPercentage}%</span>
                      </div>
                      <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                        <div
                          className="h-full bg-blue-500 transition-all duration-300"
                          style={{ width: `${execution.progressPercentage}%` }}
                        />
                      </div>
                    </div>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => stopMutation.mutate(execution.triggerId)}
                      disabled={stopMutation.isPending}
                    >
                      <StopCircle className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Recent Executions */}
      <Card>
        <CardHeader>
          <CardTitle>Recent Executions</CardTitle>
          <CardDescription>Last 50 job executions</CardDescription>
        </CardHeader>
        <CardContent>
          {recentLoading ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin" />
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b">
                    <th className="text-left p-2">Job</th>
                    <th className="text-left p-2">Status</th>
                    <th className="text-left p-2">Trigger</th>
                    <th className="text-left p-2">Started</th>
                    <th className="text-left p-2">Duration</th>
                    <th className="text-left p-2">By</th>
                  </tr>
                </thead>
                <tbody>
                  {recentExecutions?.map((execution) => (
                    <tr key={execution.id} className="border-b hover:bg-gray-50">
                      <td className="p-2 font-medium">{execution.jobName}</td>
                      <td className="p-2">
                        <Badge className={getStatusColor(execution.status)}>
                          {execution.status}
                        </Badge>
                      </td>
                      <td className="p-2">
                        <Badge variant="outline">{execution.triggerType}</Badge>
                      </td>
                      <td className="p-2 text-sm text-muted-foreground">
                        {formatRelativeTime(execution.triggeredAt)}
                      </td>
                      <td className="p-2 text-sm">
                        {execution.durationSeconds
                          ? formatDuration(execution.durationSeconds)
                          : '-'}
                      </td>
                      <td className="p-2 text-sm text-muted-foreground">
                        {execution.triggeredBy || '-'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
