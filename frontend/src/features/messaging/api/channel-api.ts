import { apiClient } from '@/shared/api/client'
import type { ApiResponse } from '@/shared/types/api'

export interface Channel {
  publicId: string
  name: string
  slug: string
  description?: string
  visibility: 'PUBLIC' | 'PRIVATE'
  archived: boolean
  createdAt: string
  updatedAt: string
}

export interface ChannelMember {
  userPublicId: string
  email: string
  displayName: string
  avatarUrl?: string
  role: 'OWNER' | 'MODERATOR' | 'MEMBER'
  joinedAt: string
  lastReadAt?: string
}

export const channelApi = {
  list: async (workspaceId: string) => {
    const { data } = await apiClient.get<ApiResponse<Channel[]>>(`/workspaces/${workspaceId}/channels`)
    return data.data
  },
  get: async (workspaceId: string, channelId: string) => {
    const { data } = await apiClient.get<ApiResponse<Channel>>(`/workspaces/${workspaceId}/channels/${channelId}`)
    return data.data
  },
  create: async (workspaceId: string, payload: { name: string; slug?: string; description?: string; visibility: 'PUBLIC' | 'PRIVATE' }) => {
    const { data } = await apiClient.post<ApiResponse<Channel>>(`/workspaces/${workspaceId}/channels`, payload)
    return data.data
  },
  update: async (workspaceId: string, channelId: string, payload: Partial<{ name: string; slug: string; description: string; visibility: 'PUBLIC' | 'PRIVATE' }>) => {
    const { data } = await apiClient.patch<ApiResponse<Channel>>(`/workspaces/${workspaceId}/channels/${channelId}`, payload)
    return data.data
  },
  archive: async (workspaceId: string, channelId: string) => {
    await apiClient.post(`/workspaces/${workspaceId}/channels/${channelId}/archive`)
  },
  restore: async (workspaceId: string, channelId: string) => {
    await apiClient.post(`/workspaces/${workspaceId}/channels/${channelId}/restore`)
  },
  delete: async (workspaceId: string, channelId: string) => {
    await apiClient.delete(`/workspaces/${workspaceId}/channels/${channelId}`)
  },
  join: async (workspaceId: string, channelId: string) => {
    await apiClient.post(`/workspaces/${workspaceId}/channels/${channelId}/join`)
  },
  leave: async (workspaceId: string, channelId: string) => {
    await apiClient.post(`/workspaces/${workspaceId}/channels/${channelId}/leave`)
  },
  listMembers: async (workspaceId: string, channelId: string) => {
    const { data } = await apiClient.get<ApiResponse<ChannelMember[]>>(`/workspaces/${workspaceId}/channels/${channelId}/members`)
    return data.data
  },
  inviteMember: async (workspaceId: string, channelId: string, userPublicId: string) => {
    await apiClient.post(`/workspaces/${workspaceId}/channels/${channelId}/members/invite`, { userPublicId })
  },
  removeMember: async (workspaceId: string, channelId: string, userPublicId: string) => {
    await apiClient.delete(`/workspaces/${workspaceId}/channels/${channelId}/members/${userPublicId}`)
  },
}
