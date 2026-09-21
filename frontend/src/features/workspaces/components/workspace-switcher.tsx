import { useState, useEffect } from 'react'
import { Check, ChevronsUpDown, Plus } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { workspaceApi } from '@/features/workspaces/api/workspace-api'
import { cn } from '@/shared/lib/utils'
import { useWorkspaceStore } from '../store/workspace-store'
import { CreateWorkspaceWizard } from '../pages/workspace-pages'
import { Dialog, DialogContent, DialogTrigger } from '@/shared/ui/dialog'

export function WorkspaceSwitcher() {
  const [isOpen, setIsOpen] = useState(false)
  const { activeWorkspace, setActiveWorkspace } = useWorkspaceStore()
  const { data: workspaces, isLoading } = useQuery({
    queryKey: ['workspaces'],
    queryFn: workspaceApi.list,
  })

  useEffect(() => {
    if (workspaces && workspaces.length > 0) {
      const isValidActive = activeWorkspace && workspaces.some((ws) => ws.publicId === activeWorkspace.publicId)
      if (!isValidActive) {
        setActiveWorkspace(workspaces[0])
      }
    }
  }, [workspaces, activeWorkspace, setActiveWorkspace])

  if (isLoading) return <div className="h-10 w-full animate-pulse bg-sidebar-accent rounded-md" />

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex w-full items-center justify-between rounded-md p-2 hover:bg-sidebar-accent transition-colors"
      >
        <div className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-teal-600 text-white font-bold">
            {activeWorkspace?.name.charAt(0).toUpperCase() || 'W'}
          </div>
          <div className="flex flex-col items-start text-sm">
            <span className="font-semibold truncate w-32 text-left">{activeWorkspace?.name || 'Workspace'}</span>
            <span className="text-xs text-muted-foreground capitalize">{activeWorkspace?.role?.toLowerCase() || 'owner'}</span>
          </div>
        </div>
        <ChevronsUpDown className="h-4 w-4 text-muted-foreground" />
      </button>

      {isOpen && (
        <div className="absolute top-14 left-0 w-full rounded-md border border-sidebar-border bg-sidebar p-1 shadow-md z-50">
          {workspaces?.map((ws) => (
            <button
              key={ws.publicId}
              onClick={() => {
                setActiveWorkspace(ws)
                setIsOpen(false)
              }}
              className="flex w-full items-center justify-between rounded-sm px-2 py-1.5 text-sm hover:bg-sidebar-accent"
            >
              <span className="truncate">{ws.name}</span>
              {ws.publicId === activeWorkspace?.publicId && <Check className="h-4 w-4 text-teal-500" />}
            </button>
          ))}
          <div className="my-1 h-px bg-sidebar-border" />
          <Dialog>
            <DialogTrigger asChild>
              <button className="flex w-full items-center gap-2 rounded-sm px-2 py-1.5 text-sm hover:bg-sidebar-accent text-muted-foreground">
                <Plus className="h-4 w-4" />
                Create Workspace
              </button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[425px]">
              <CreateWorkspaceWizard onSuccess={() => setIsOpen(false)} />
            </DialogContent>
          </Dialog>
        </div>
      )}
    </div>
  )
}
