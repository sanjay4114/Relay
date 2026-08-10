export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
}

export interface ApiErrorResponse {
  timestamp: string
  status: number
  code: string
  message: string
  path: string
  fieldErrors?: Record<string, string>
}

export interface User {
  publicId: string
  email: string
  displayName: string
  avatarUrl?: string
  statusMessage?: string
  timezone: string
}

export interface Workspace {
  publicId: string
  name: string
  slug: string
  description?: string
}

export interface AuthResponse {
  accessToken: string
  user: User
  workspace: Workspace
}

export interface AccessTokenResponse {
  accessToken: string
}

export interface MessageResponse {
  message: string
}
