import apiClient from './api';

export interface Task {
  id: number;
  title: string;
  description?: string;
  subject?: string;
  due_date?: string;
  dueDate?: string;
  priority: 'urgent' | 'high' | 'normal' | 'low' | 'HIGH' | 'MEDIUM' | 'LOW';
  status: 'completed' | 'in_progress' | 'pending' | 'cancelled' | 'OPEN' | 'IN_PROGRESS' | 'CLOSED' | 'ESCALATED';
  tags?: string;
  created_at?: string;
  createdAt?: string;
  workQueue?: string;
  assignedTo?: string;
  actionLink?: string;
  relatedEntityType?: string;
  relatedEntityId?: number;
}

export interface TasksResponse {
  tasks: Task[];
}

export interface TaskCounts {
  open: number;
  inProgress: number;
  closed: number;
}

export const taskService = {
  /**
   * Get all tasks
   */
  async getAllTasks(): Promise<Task[]> {
    const response = await apiClient.get<TasksResponse>('/api/tasks');
    return response.data.tasks || response.data || [];
  },

  /**
   * Get tasks for a specific user (with optional work queue subscription)
   */
  async getTasksForUser(username: string, includeSubscribedQueues: boolean = false): Promise<Task[]> {
    const url = includeSubscribedQueues
      ? `/api/tasks?username=${username}&includeSubscribedQueues=true`
      : `/api/tasks?username=${username}`;
    const response = await apiClient.get(url);
    return response.data.tasks || response.data || [];
  },

  /**
   * Get task counts for a user
   */
  async getTaskCounts(username: string): Promise<TaskCounts> {
    try {
      const response = await apiClient.get<TaskCounts>(`/api/tasks/count/${username}`);
      return response.data || { open: 0, inProgress: 0, closed: 0 };
    } catch (error) {
      console.error('Error fetching task counts:', error);
      return { open: 0, inProgress: 0, closed: 0 };
    }
  },

  /**
   * Get task by ID
   */
  async getTaskById(taskId: number): Promise<Task> {
    const response = await apiClient.get<Task>(`/api/tasks/${taskId}`);
    return response.data;
  },

  /**
   * Create a new task
   */
  async createTask(task: Partial<Task>): Promise<Task> {
    const response = await apiClient.post<Task>('/api/tasks', task);
    return response.data;
  },

  /**
   * Update a task
   */
  async updateTask(taskId: number, task: Partial<Task>): Promise<Task> {
    const response = await apiClient.put<Task>(`/api/tasks/${taskId}`, task);
    return response.data;
  },

  /**
   * Update task status
   */
  async updateTaskStatus(taskId: number, status: string): Promise<Task> {
    const response = await apiClient.put<Task>(`/api/tasks/${taskId}/status`, { status });
    return response.data;
  },

  /**
   * Delete a task
   */
  async deleteTask(taskId: number): Promise<void> {
    await apiClient.delete(`/api/tasks/${taskId}`);
  },
};
