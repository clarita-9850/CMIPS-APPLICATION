'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Loader2, Power, Pause, Play, Server, Activity } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { adminApi } from '@/lib/api';
import { useToast } from '@/hooks/use-toast';

export default function SettingsPage() {
  const queryClient = useQueryClient();
  const { toast } = useToast();

  const { data: status, isLoading } = useQuery({
    queryKey: ['admin', 'status'],
    queryFn: () => adminApi.getStatus(),
    refetchInterval: 5000,
  });

  const pauseMutation = useMutation({
    mutationFn: () => adminApi.pause(),
    onSuccess: () => {
      toast({ title: 'Scheduler paused' });
      queryClient.invalidateQueries({ queryKey: ['admin', 'status'] });
    },
  });

  const resumeMutation = useMutation({
    mutationFn: () => adminApi.resume(),
    onSuccess: () => {
      toast({ title: 'Scheduler resumed' });
      queryClient.invalidateQueries({ queryKey: ['admin', 'status'] });
    },
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Settings</h1>
        <p className="text-muted-foreground">Manage scheduler configuration</p>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      ) : (
        <div className="grid gap-6 md:grid-cols-2">
          {/* Scheduler Status */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Power className="h-5 w-5" />
                Scheduler Status
              </CardTitle>
              <CardDescription>Current state of the job scheduler</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <span>Status</span>
                <Badge variant={status?.isStarted ? 'success' : 'secondary'}>
                  {status?.isStandby ? 'Paused' : status?.isStarted ? 'Running' : 'Stopped'}
                </Badge>
              </div>
              <div className="flex items-center justify-between">
                <span>Instance</span>
                <span className="text-sm text-muted-foreground">
                  {String(status?.schedulerName || 'Unknown')}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span>Instance ID</span>
                <span className="text-sm text-muted-foreground font-mono">
                  {String(status?.schedulerInstanceId || 'Unknown')}
                </span>
              </div>
              <div className="pt-4 border-t flex gap-2">
                {status?.isStandby ? (
                  <Button
                    onClick={() => resumeMutation.mutate()}
                    disabled={resumeMutation.isPending}
                  >
                    <Play className="mr-2 h-4 w-4" />
                    Resume Scheduler
                  </Button>
                ) : (
                  <Button
                    variant="outline"
                    onClick={() => pauseMutation.mutate()}
                    disabled={pauseMutation.isPending}
                  >
                    <Pause className="mr-2 h-4 w-4" />
                    Pause Scheduler
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>

          {/* CMIPS Backend Connection */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Server className="h-5 w-5" />
                CMIPS Backend Connection
              </CardTitle>
              <CardDescription>Connection status to the CMIPS backend application</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <span>Health Status</span>
                <Badge variant={status?.cmipsBackendHealthy ? 'success' : 'destructive'}>
                  {status?.cmipsBackendHealthy ? 'Healthy' : 'Unhealthy'}
                </Badge>
              </div>
              <div className="flex items-center justify-between">
                <span>Last Check</span>
                <span className="text-sm text-muted-foreground">Just now</span>
              </div>
            </CardContent>
          </Card>

          {/* System Info */}
          <Card className="md:col-span-2">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Activity className="h-5 w-5" />
                System Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 md:grid-cols-3">
                <div className="p-4 bg-gray-50 rounded-lg">
                  <div className="text-sm text-muted-foreground">Scheduler Backend</div>
                  <div className="text-lg font-semibold">Port 8084</div>
                </div>
                <div className="p-4 bg-gray-50 rounded-lg">
                  <div className="text-sm text-muted-foreground">CMIPS Backend</div>
                  <div className="text-lg font-semibold">Port 8081</div>
                </div>
                <div className="p-4 bg-gray-50 rounded-lg">
                  <div className="text-sm text-muted-foreground">Keycloak</div>
                  <div className="text-lg font-semibold">Port 8085</div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
