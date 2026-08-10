import { useQuery } from '@tanstack/react-query'
import { NavLink } from 'react-router-dom'
import { Hash, Lock, Plus } from 'lucide-react'
import { channelApi } from '../api/channel-api'
import { useWorkspaceStore } from '@/features/workspaces/store/workspace-store'
import { cn } from '@/shared/lib/utils'
import { Dialog, DialogContent, DialogTrigger } from '@/shared/ui/dialog'
import { CreateChannelModal } from './create-channel-modal'
import { useState } from 'react'

export function ChannelSidebarList({ onNavigate }: { onNavigate?: () => void }) {
  const { activeWorkspace } = useWorkspaceStore()
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)

  const { data: channels = [], isLoading } = useQuery({
    queryKey: ['channels', activeWorkspace?.publicId],
    queryFn: () => channelApi.list(activeWorkspace!.publicId),
    enabled: !!activeWorkspace,
  })

  if (!activeWorkspace) return null

  return (
    <div className="mt-6">
      <div className="flex items-center justify-between px-3 mb-1">
        <h3 className="text-xs font-semibold text-sidebar-foreground/50 uppercase tracking-wider">
          Channels
        </h3>
        <Dialog open={isCreateModalOpen} onOpenChange={setIsCreateModalOpen}>
          <DialogTrigger asChild>
            <button className="h-5 w-5 rounded-md flex items-center justify-center text-sidebar-foreground/50 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground transition-colors">
              <Plus className="h-3.5 w-3.5" />
            </button>
          </DialogTrigger>
          <DialogContent>
            <CreateChannelModal onSuccess={() => setIsCreateModalOpen(false)} />
          </DialogContent>
        </Dialog>
      </div>

      <nav className="space-y-0.5">
        {isLoading ? (
          <div className="px-3 py-2 text-sm text-sidebar-foreground/50">Loading channels...</div>
        ) : channels.length === 0 ? (
          <div className="px-3 py-2 text-sm text-sidebar-foreground/50">No channels yet</div>
        ) : (
          channels.map((channel) => (
            <NavLink
              key={channel.publicId}
              to={`/channels/${channel.publicId}`}
              onClick={onNavigate}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-2 rounded-md px-3 py-1.5 text-sm transition-colors',
                  isActive
                    ? 'bg-sidebar-accent text-sidebar-accent-foreground font-medium'
                    : 'text-sidebar-foreground/80 hover:bg-sidebar-accent/60 hover:text-sidebar-accent-foreground',
                )
              }
            >
              {channel.visibility === 'PRIVATE' ? (
                <Lock className="h-3.5 w-3.5 opacity-70" />
              ) : (
                <Hash className="h-3.5 w-3.5 opacity-70" />
              )}
              <span className="truncate">{channel.name}</span>
            </NavLink>
          ))
        )}
      </nav>
    </div>
  )
}
