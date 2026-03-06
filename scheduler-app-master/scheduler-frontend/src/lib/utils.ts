import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { formatDistanceToNow, format, parseISO } from 'date-fns';
import type { ExecutionStatus, JobStatus } from '@/types';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatDate(dateString: string): string {
  try {
    return format(parseISO(dateString), 'PPp');
  } catch {
    return dateString;
  }
}

export function formatRelativeTime(dateString: string): string {
  try {
    return formatDistanceToNow(parseISO(dateString), { addSuffix: true });
  } catch {
    return dateString;
  }
}

export function formatDuration(seconds: number): string {
  if (seconds < 60) {
    return `${seconds}s`;
  }
  if (seconds < 3600) {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}m ${secs}s`;
  }
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  return `${hours}h ${minutes}m`;
}

export function getStatusColor(status: ExecutionStatus | JobStatus): string {
  const colors: Record<string, string> = {
    // Execution statuses
    TRIGGERED: 'bg-yellow-100 text-yellow-800',
    QUEUED: 'bg-blue-100 text-blue-800',
    STARTING: 'bg-cyan-100 text-cyan-800',
    RUNNING: 'bg-blue-100 text-blue-800',
    COMPLETED: 'bg-green-100 text-green-800',
    FAILED: 'bg-red-100 text-red-800',
    STOPPED: 'bg-gray-100 text-gray-800',
    ABANDONED: 'bg-orange-100 text-orange-800',
    UNKNOWN: 'bg-gray-100 text-gray-800',
    // Job statuses
    ACTIVE: 'bg-green-100 text-green-800',
    INACTIVE: 'bg-gray-100 text-gray-800',
    ON_HOLD: 'bg-yellow-100 text-yellow-800',
    ON_ICE: 'bg-cyan-100 text-cyan-800',
  };
  return colors[status] || 'bg-gray-100 text-gray-800';
}

export function getStatusIcon(status: ExecutionStatus | JobStatus): string {
  const icons: Record<string, string> = {
    TRIGGERED: 'clock',
    QUEUED: 'loader',
    STARTING: 'play',
    RUNNING: 'loader',
    COMPLETED: 'check-circle',
    FAILED: 'x-circle',
    STOPPED: 'stop-circle',
    ABANDONED: 'alert-circle',
    UNKNOWN: 'help-circle',
    ACTIVE: 'check',
    INACTIVE: 'x',
    ON_HOLD: 'pause',
    ON_ICE: 'snowflake',
  };
  return icons[status] || 'circle';
}

export function parseCronExpression(cron: string): string {
  // Simple cron parser for display
  const parts = cron.split(' ');
  if (parts.length !== 6) return cron;

  const [second, minute, hour, dayOfMonth, month, dayOfWeek] = parts;

  if (minute === '0' && hour === '0' && dayOfMonth === '*' && month === '*' && dayOfWeek === '*') {
    return 'Daily at midnight';
  }
  if (dayOfMonth === '*' && month === '*' && dayOfWeek === '*') {
    return `Daily at ${hour}:${minute.padStart(2, '0')}`;
  }
  if (dayOfMonth === '*' && month === '*' && dayOfWeek === '1-5') {
    return `Weekdays at ${hour}:${minute.padStart(2, '0')}`;
  }

  return cron;
}
