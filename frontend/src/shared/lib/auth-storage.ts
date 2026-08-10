import { clearStoredWorkspace } from '@/shared/lib/workspace-storage'

const ACCESS_TOKEN_KEY = 'relay_access_token'

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

export function setAccessToken(token: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
}

export function clearAccessToken(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
}

export function clearAuth(): void {
  clearAccessToken()
  clearStoredWorkspace()
}

export function isAuthenticated(): boolean {
  return Boolean(getAccessToken())
}
