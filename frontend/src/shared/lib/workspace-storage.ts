const WORKSPACE_KEY = 'relay_workspace'

export interface StoredWorkspace {
  publicId: string
  name: string
  slug: string
}

export function getStoredWorkspace(): StoredWorkspace | null {
  const raw = localStorage.getItem(WORKSPACE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as StoredWorkspace
  } catch {
    return null
  }
}

export function setStoredWorkspace(workspace: StoredWorkspace): void {
  localStorage.setItem(WORKSPACE_KEY, JSON.stringify(workspace))
}

export function clearStoredWorkspace(): void {
  localStorage.removeItem(WORKSPACE_KEY)
}
