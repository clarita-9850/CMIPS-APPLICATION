'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Link from 'next/link';
import {
  Search,
  Plus,
  MoreHorizontal,
  Play,
  Pause,
  Square,
  Snowflake,
  Loader2,
  Filter,
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { jobApi, triggerApi } from '@/lib/api';
import { formatRelativeTime, getStatusColor, parseCronExpression } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import type { JobDefinition, Page } from '@/types';

export default function JobsPage() {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();
  const { toast } = useToast();

  const { data: jobsData, isLoading } = useQuery<Page<JobDefinition>>({
    queryKey: ['jobs', page, search],
    queryFn: () => (search ? jobApi.search(search, page) : jobApi.getAll(page)),
  });

  const triggerMutation = useMutation({
    mutationFn: (jobId: number) => triggerApi.trigger(jobId),
    onSuccess: (_, jobId) => {
      toast({ title: 'Job triggered successfully' });
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
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
    mutationFn: (jobId: number) => jobApi.hold(jobId),
    onSuccess: () => {
      toast({ title: 'Job put on hold' });
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
    },
  });

  const resumeMutation = useMutation({
    mutationFn: (jobId: number) => jobApi.resume(jobId),
    onSuccess: () => {
      toast({ title: 'Job resumed' });
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
    },
  });

  const iceMutation = useMutation({
    mutationFn: (jobId: number) => jobApi.ice(jobId),
    onSuccess: () => {
      toast({ title: 'Job put on ice' });
      queryClient.invalidateQueries({ queryKey: ['jobs'] });
    },
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Jobs</h1>
          <p className="text-muted-foreground">Manage batch job definitions</p>
        </div>
        <Link href="/jobs/new">
          <Button>
            <Plus className="mr-2 h-4 w-4" />
            New Job
          </Button>
        </Link>
      </div>

      {/* Search and Filter */}
      <div className="flex items-center gap-4">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search jobs..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-10"
          />
        </div>
        <Button variant="outline">
          <Filter className="mr-2 h-4 w-4" />
          Filter
        </Button>
      </div>

      {/* Jobs List */}
      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      ) : (
        <div className="space-y-4">
          {jobsData?.content.map((job) => (
            <Card key={job.id} className="hover:shadow-md transition-shadow">
              <CardContent className="p-6">
                <div className="flex items-start justify-between">
                  <div className="space-y-2">
                    <div className="flex items-center gap-3">
                      <Link href={`/jobs/${job.id}`} className="hover:underline">
                        <h3 className="text-lg font-semibold">{job.jobName}</h3>
                      </Link>
                      <Badge className={getStatusColor(job.status)}>{job.status}</Badge>
                      <Badge variant="outline">{job.jobType}</Badge>
                      {!job.enabled && <Badge variant="secondary">Disabled</Badge>}
                    </div>
                    {job.description && (
                      <p className="text-sm text-muted-foreground">{job.description}</p>
                    )}
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      {job.cronExpression && (
                        <span>Schedule: {parseCronExpression(job.cronExpression)}</span>
                      )}
                      <span>Priority: {job.priority}</span>
                      <span>Updated {formatRelativeTime(job.updatedAt)}</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => triggerMutation.mutate(job.id)}
                      disabled={triggerMutation.isPending}
                    >
                      <Play className="h-4 w-4" />
                    </Button>
                    {job.status === 'ACTIVE' && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => holdMutation.mutate(job.id)}
                      >
                        <Pause className="h-4 w-4" />
                      </Button>
                    )}
                    {job.status === 'ON_HOLD' && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => resumeMutation.mutate(job.id)}
                      >
                        <Play className="h-4 w-4" />
                      </Button>
                    )}
                    {job.status === 'ACTIVE' && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => iceMutation.mutate(job.id)}
                      >
                        <Snowflake className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}

          {/* Pagination */}
          {jobsData && jobsData.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 pt-4">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(page - 1)}
                disabled={page === 0}
              >
                Previous
              </Button>
              <span className="text-sm text-muted-foreground">
                Page {page + 1} of {jobsData.totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(page + 1)}
                disabled={page >= jobsData.totalPages - 1}
              >
                Next
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
