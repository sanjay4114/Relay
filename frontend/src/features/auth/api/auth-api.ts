import { apiClient } from '@/shared/api/client'
import { setAccessToken } from '@/shared/lib/auth-storage'
import { setStoredWorkspace } from '@/shared/lib/workspace-storage'
import type {
  AccessTokenResponse,
  ApiResponse,
  AuthResponse,
  MessageResponse,
} from '@/shared/types/api'

export interface RegisterPayload {
  displayName: string
  email: string
  password: string
}

export interface LoginPayload {
  email: string
  password: string
}

export interface ResetPasswordPayload {
  token: string
  newPassword: string
}

import { useWorkspaceStore } from '@/features/workspaces/store/workspace-store'

function persistAuth(data: AuthResponse) {
  setAccessToken(data.accessToken)
  setStoredWorkspace(data.workspace)
  if (data.workspace) {
    useWorkspaceStore.getState().setActiveWorkspace({
      publicId: data.workspace.publicId,
      name: data.workspace.name,
      slug: data.workspace.slug,
      description: data.workspace.description,
      role: 'OWNER',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    })
  }
}

export async function register(payload: RegisterPayload): Promise<AuthResponse> {
  const { data } = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', payload)
  persistAuth(data.data)
  return data.data
}

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  const { data } = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', payload)
  persistAuth(data.data)
  return data.data
}

export async function refreshAccessToken(): Promise<string> {
  const { data } = await apiClient.post<ApiResponse<AccessTokenResponse>>('/auth/refresh')
  setAccessToken(data.data.accessToken)
  return data.data.accessToken
}

export async function logout(): Promise<void> {
  try {
    await apiClient.post('/auth/logout')
  } finally {
    useWorkspaceStore.getState().clearActiveWorkspace()
  }
}

export async function forgotPassword(email: string): Promise<string> {
  const { data } = await apiClient.post<ApiResponse<MessageResponse>>('/auth/forgot-password', { email })
  return data.data.message
}

export async function resetPassword(payload: ResetPasswordPayload): Promise<string> {
  const { data } = await apiClient.post<ApiResponse<MessageResponse>>('/auth/reset-password', payload)
  return data.data.message
}

export async function getMe(): Promise<AuthResponse['user']> {
  const { data } = await apiClient.get<ApiResponse<AuthResponse['user']>>('/auth/me')
  return data.data
}
