import { apiClient } from '@/shared/api/client'
import type { ApiResponse } from '@/shared/types/api'

export interface Workspace {
  publicId: string
  name: string
  slug: string
  description?: string
  role: 'OWNER' | 'ADMIN' | 'MEMBER'
  createdAt: string
  updatedAt: string
}

export interface WorkspaceMember {
  publicId: string
  email: string
  displayName: string
  avatarUrl?: string
  role: 'OWNER' | 'ADMIN' | 'MEMBER'
  joinedAt: string
}

export const workspaceApi = {
  list: async () => {
    const { data } = await apiClient.get<ApiResponse<Workspace[]>>('/workspaces')
    return data.data
  },
  get: async (id: string) => {
    const { data } = await apiClient.get<ApiResponse<Workspace>>(`/workspaces/${id}`)
    return data.data
  },
  create: async (payload: { name: string; slug?: string; description?: string }) => {
    const { data } = await apiClient.post<ApiResponse<Workspace>>('/workspaces', payload)
    return data.data
  },
  update: async (id: string, payload: { name: string; slug: string; description?: string }) => {
    const { data } = await apiClient.patch<ApiResponse<Workspace>>(`/workspaces/${id}`, payload)
    return data.data
  },
  delete: async (id: string) => {
    await apiClient.delete(`/workspaces/${id}`)
  },
  listMembers: async (workspaceId: string) => {
    const { data } = await apiClient.get<ApiResponse<WorkspaceMember[]>>(`/workspaces/${workspaceId}/members`)
    return data.data
  },
  inviteMember: async (workspaceId: string, email: string, role: string) => {
    await apiClient.post(`/workspaces/${workspaceId}/members/invite`, { email, role })
  },
  removeMember: async (workspaceId: string, userId: string) => {
    await apiClient.delete(`/workspaces/${workspaceId}/members/${userId}`)
  },
  leaveWorkspace: async (workspaceId: string) => {
    await apiClient.post(`/workspaces/${workspaceId}/members/leave`)
  },
  transferOwnership: async (workspaceId: string, newOwnerPublicId: string) => {
    await apiClient.post(`/workspaces/${workspaceId}/members/transfer-ownership`, { newOwnerPublicId })
  },
}
