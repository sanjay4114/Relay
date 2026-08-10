import { Navigate, Outlet } from 'react-router-dom'
import { isAuthenticated } from '@/shared/lib/auth-storage'

export function ProtectedRoute() {
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}

export function PublicOnlyRoute() {
  if (isAuthenticated()) {
    return <Navigate to="/dashboard" replace />
  }
  return <Outlet />
}

import { useWorkspaceStore } from '@/features/workspaces/store/workspace-store'
import { EmptyState } from '@/shared/components/empty-state'
import { Button } from '@/shared/ui/button'
import { Dialog, DialogContent, DialogTrigger } from '@/shared/ui/dialog'
import { CreateWorkspaceWizard } from '@/features/workspaces/pages/workspace-pages'
import { useState } from 'react'

export function WorkspaceGuard() {
  const { activeWorkspace } = useWorkspaceStore()
  const [isOpen, setIsOpen] = useState(false)

  if (!activeWorkspace) {
    return (
      <div className="flex h-screen w-full items-center justify-center">
        <EmptyState 
          title="No Workspace Selected"
          description="You need to select or create a workspace to continue."
          action={
            <Dialog open={isOpen} onOpenChange={setIsOpen}>
              <DialogTrigger asChild>
                <Button>Create a Workspace</Button>
              </DialogTrigger>
              <DialogContent>
                <CreateWorkspaceWizard onSuccess={() => setIsOpen(false)} />
              </DialogContent>
            </Dialog>
          }
        />
      </div>
    )
  }

  return <Outlet />
}
