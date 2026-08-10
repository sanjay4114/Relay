import { apiClient } from '@/shared/api/client'
import type { ApiResponse } from '@/shared/types/api'

export interface NotificationActor {
  publicId: string
  displayName: string
  avatarUrl?: string
}

export interface Notification {
  publicId: string
  type: 'MENTION' | 'THREAD_REPLY' | 'MESSAGE_REACTION' | 'WORKSPACE_INVITE' | 'CHANNEL_INVITE' | 'FILE_UPLOAD' | 'TASK_ASSIGNMENT'
  title: string
  body: string
  entityType: string
  entityPublicId: string
  isRead: boolean
  createdAt: string
  actor?: NotificationActor
}

export const notificationApi = {
  getNotifications: async (limit = 50): Promise<Notification[]> => {
    const { data } = await apiClient.get<ApiResponse<Notification[]>>(`/notifications?limit=${limit}`)
    return data.data
  },
  
  getUnreadCount: async (): Promise<number> => {
    const { data } = await apiClient.get<ApiResponse<number>>('/notifications/unread-count')
    return data.data
  },
  
  markAsRead: async (publicId: string): Promise<void> => {
    await apiClient.post(`/notifications/${publicId}/read`)
  },
  
  markAllAsRead: async (): Promise<void> => {
    await apiClient.post('/notifications/read-all')
  },
  
  deleteNotification: async (publicId: string): Promise<void> => {
    await apiClient.delete(`/notifications/${publicId}`)
  }
}
