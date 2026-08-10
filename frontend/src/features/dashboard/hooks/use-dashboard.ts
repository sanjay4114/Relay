import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '../api/dashboard-api';

export const useDashboardTimeline = (workspaceId: string | undefined, limit = 20) => {
  return useQuery({
    queryKey: ['dashboard-timeline', workspaceId, limit],
    queryFn: () => dashboardApi.getTimeline(workspaceId!, limit),
    enabled: !!workspaceId,
  });
};

export const useDashboardStats = (workspaceId: string | undefined) => {
  return useQuery({
    queryKey: ['dashboard-stats', workspaceId],
    queryFn: () => dashboardApi.getStats(workspaceId!),
    enabled: !!workspaceId,
    refetchInterval: 30000, // Refresh every 30 seconds
  });
};
