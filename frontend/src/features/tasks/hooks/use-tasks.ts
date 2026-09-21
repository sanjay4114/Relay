import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { taskApi, type CreateTaskRequest, type UpdateTaskRequest } from '../api/task-api';

export const useWorkspaceTasks = (workspaceId: string) => {
  return useQuery({
    queryKey: ['workspace-tasks', workspaceId],
    queryFn: () => taskApi.listTasks(workspaceId),
    enabled: !!workspaceId,
  });
};

export const useCreateTask = (workspaceId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateTaskRequest) => taskApi.createTask(workspaceId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workspace-tasks', workspaceId] });
    },
  });
};

export const useUpdateTask = (workspaceId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ taskId, data }: { taskId: string; data: UpdateTaskRequest }) => 
      taskApi.updateTask(workspaceId, taskId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workspace-tasks', workspaceId] });
    },
  });
};

export const useDeleteTask = (workspaceId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (taskId: string) => taskApi.deleteTask(workspaceId, taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workspace-tasks', workspaceId] });
    },
  });
};
