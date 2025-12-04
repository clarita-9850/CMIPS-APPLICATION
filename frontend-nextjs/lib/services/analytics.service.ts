import apiClient from '../api';

export interface AnalyticsFilters {
  districtId?: string;
  county?: string;
  statusFilter?: string;
  priorityLevel?: string;
  serviceType?: string;
  startYear?: string;
  endYear?: string;
}

export interface AnalyticsSummary {
  totalTimesheetsToday: number;
  pendingApprovals: number;
  totalParticipants: number;
  distinctEmployees: number;
  distinctProviders: number;
  distinctRecipients: number;
  totalApprovedAmountToday: number;
  totalApprovedAmountThisWeek: number;
  avgApprovalTimeHours: number;
  lastUpdated?: string;
  status?: string;
}

export const analyticsService = {
  /**
   * Get real-time metrics
   */
  async getRealTimeMetrics(filters?: AnalyticsFilters): Promise<AnalyticsSummary> {
    const response = await apiClient.get<AnalyticsSummary>('/analytics/realtime-metrics', {
      params: filters,
    });
    return response.data;
  },

  /**
   * Get filter options
   */
  async getFilterOptions(): Promise<any> {
    const response = await apiClient.get('/analytics/filters');
    return response.data;
  },

  /**
   * Get ad-hoc filters
   */
  async getAdhocFilters(): Promise<any> {
    const response = await apiClient.get('/analytics/filters');
    return response.data;
  },

  /**
   * Get ad-hoc statistics
   */
  async getAdhocStats(params?: Record<string, string>): Promise<any> {
    const response = await apiClient.get('/analytics/adhoc-data', { params });
    return response.data;
  },

  /**
   * Get ad-hoc data
   */
  async getAdhocData(params?: Record<string, any>): Promise<any> {
    const response = await apiClient.get('/analytics/adhoc-data', { params });
    return response.data;
  },
};

