import { axiosInstance } from '@/shared/api/axios-instance';

export interface ActivityItemDto {
  id: string;
  type: 'MESSAGE' | 'TASK_UPDATE' | 'FILE_UPLOAD' | 'CHANNEL_CREATED';
  title: string;
  description: string;
  actorName: string;
  actorAvatar: string;
  timestamp: string;
  metadata: { meta1: string; meta2: string };
}

export interface DashboardStatsDto {
  activeTasks: number;
  overdueTasks: number;
  dueTodayTasks: number;
  unreadMessages: number;
  activeChannels: number;
  onlineMembers: number;
}

export const dashboardApi = {
  getTimeline: async (workspaceId: string, limit = 20): Promise<ActivityItemDto[]> => {
    const response = await axiosInstance.get(`/workspaces/${workspaceId}/dashboard/timeline?limit=${limit}`);
    return response.data.data;
  },

  getStats: async (workspaceId: string): Promise<DashboardStatsDto> => {
    const response = await axiosInstance.get(`/workspaces/${workspaceId}/dashboard/stats`);
    return response.data.data;
  }
};
