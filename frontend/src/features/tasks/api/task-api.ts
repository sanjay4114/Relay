import { axiosInstance } from '@/shared/api/axios-instance';

export interface TaskDto {
  publicId: string;
  title: string;
  description: string;
  status: 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'DONE';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  startDate?: string;
  dueDate?: string;
  completedAt?: string;
  estimate?: string;
  storyPoints?: number;
  creatorPublicId: string;
  linkedMessagePublicId?: string;
  assigneePublicIds: string[];
  labels: { name: string; color: string }[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  priority?: string;
  startDate?: string;
  dueDate?: string;
  estimate?: string;
  storyPoints?: number;
  assigneePublicIds?: string[];
  labelNames?: string[];
  linkedMessagePublicId?: string;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  status?: string;
  priority?: string;
  startDate?: string;
  dueDate?: string;
  estimate?: string;
  storyPoints?: number;
  assigneePublicIds?: string[];
  labelNames?: string[];
}

export const taskApi = {
  listTasks: async (workspaceId: string, status?: string): Promise<TaskDto[]> => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    const response = await axiosInstance.get(`/workspaces/${workspaceId}/tasks?${params.toString()}`);
    return response.data.data.content; // Spring Page<TaskDto>
  },

  createTask: async (workspaceId: string, data: CreateTaskRequest): Promise<TaskDto> => {
    const response = await axiosInstance.post(`/workspaces/${workspaceId}/tasks`, data);
    return response.data.data;
  },

  updateTask: async (workspaceId: string, taskId: string, data: UpdateTaskRequest): Promise<TaskDto> => {
    const response = await axiosInstance.patch(`/workspaces/${workspaceId}/tasks/${taskId}`, data);
    return response.data.data;
  },

  deleteTask: async (workspaceId: string, taskId: string): Promise<void> => {
    await axiosInstance.delete(`/workspaces/${workspaceId}/tasks/${taskId}`);
  }
};
