'use client';

import { useQuery } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { auditApi } from '@/lib/api';
import { formatRelativeTime, formatDate } from '@/lib/utils';
import type { Page, AuditLog } from '@/types';

const actionColors: Record<string, string> = {
  CREATE: 'bg-green-100 text-green-800',
  UPDATE: 'bg-blue-100 text-blue-800',
  DELETE: 'bg-red-100 text-red-800',
  TRIGGER: 'bg-purple-100 text-purple-800',
  STOP: 'bg-orange-100 text-orange-800',
  HOLD: 'bg-yellow-100 text-yellow-800',
  RESUME: 'bg-cyan-100 text-cyan-800',
  ICE: 'bg-sky-100 text-sky-800',
  ENABLE: 'bg-emerald-100 text-emerald-800',
  DISABLE: 'bg-gray-100 text-gray-800',
};

export default function AuditPage() {
  const { data: auditData, isLoading } = useQuery<Page<AuditLog>>({
    queryKey: ['audit', 'recent'],
    queryFn: () => auditApi.getRecentOperations(168, 0, 100), // Last 7 days
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Audit Log</h1>
        <p className="text-muted-foreground">Track all administrative actions</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Recent Activity</CardTitle>
          <CardDescription>Last 7 days of operations</CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin" />
            </div>
          ) : auditData?.content.length === 0 ? (
            <p className="text-center text-muted-foreground py-8">No audit entries found</p>
          ) : (
            <div className="space-y-4">
              {auditData?.content.map((entry) => (
                <div
                  key={entry.id}
                  className="flex items-start gap-4 p-4 border rounded-lg"
                >
                  <Badge className={actionColors[entry.action] || 'bg-gray-100 text-gray-800'}>
                    {entry.action}
                  </Badge>
                  <div className="flex-1 space-y-1">
                    <p className="font-medium">
                      {entry.changeSummary || `${entry.action} on ${entry.entityType}`}
                    </p>
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <span>By: {entry.performedBy}</span>
                      <span>Entity: {entry.entityType} #{entry.entityId}</span>
                      <span>{formatRelativeTime(entry.actionTimestamp)}</span>
                    </div>
                  </div>
                  <div className="text-xs text-muted-foreground">
                    {formatDate(entry.actionTimestamp)}
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
