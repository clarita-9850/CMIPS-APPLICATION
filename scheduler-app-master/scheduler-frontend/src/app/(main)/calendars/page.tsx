'use client';

import { useQuery } from '@tanstack/react-query';
import { Calendar, Plus, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { calendarApi } from '@/lib/api';
import { formatRelativeTime } from '@/lib/utils';
import type { JobCalendar } from '@/types';

const calendarTypeColors: Record<string, string> = {
  BLACKOUT: 'bg-red-100 text-red-800',
  HOLIDAY: 'bg-purple-100 text-purple-800',
  MAINTENANCE: 'bg-yellow-100 text-yellow-800',
  CUSTOM: 'bg-blue-100 text-blue-800',
};

export default function CalendarsPage() {
  const { data: calendars, isLoading } = useQuery<JobCalendar[]>({
    queryKey: ['calendars'],
    queryFn: () => calendarApi.getAll(),
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Calendars</h1>
          <p className="text-muted-foreground">Manage scheduling calendars and blackout dates</p>
        </div>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          New Calendar
        </Button>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      ) : calendars?.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-16">
            <Calendar className="h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">No calendars yet</h3>
            <p className="text-muted-foreground mb-4">
              Create calendars to manage blackout dates and holidays
            </p>
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              Create First Calendar
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {calendars?.map((calendar) => (
            <Card key={calendar.id} className="hover:shadow-md transition-shadow cursor-pointer">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg">{calendar.calendarName}</CardTitle>
                  <Badge className={calendarTypeColors[calendar.calendarType]}>
                    {calendar.calendarType}
                  </Badge>
                </div>
                {calendar.description && (
                  <CardDescription>{calendar.description}</CardDescription>
                )}
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between text-sm text-muted-foreground">
                  <span>Updated {formatRelativeTime(calendar.updatedAt)}</span>
                  {!calendar.isActive && <Badge variant="secondary">Inactive</Badge>}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
