import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Hash, Lock, Users, Info, Settings } from 'lucide-react'
import { channelApi } from '../api/channel-api'
import { useWorkspaceStore } from '@/features/workspaces/store/workspace-store'
import { Button } from '@/shared/ui/button'
import { EmptyState } from '@/shared/components/empty-state'
import { Skeleton } from '@/shared/ui/skeleton'
import { Dialog, DialogContent, DialogTrigger } from '@/shared/ui/dialog'
import { ChannelSettingsModal } from '../components/channel-settings-modal'
import { ChannelMembersModal } from '../components/channel-members-modal'
import { ConfirmationDialog } from '@/shared/components/confirmation-dialog'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { ChatWindow } from '../components/chat-window'

export function ChannelDetailsPage() {
  const { channelId } = useParams<{ channelId: string }>()
  const { activeWorkspace } = useWorkspaceStore()
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  
  const [isSettingsOpen, setIsSettingsOpen] = useState(false)
  const [isMembersOpen, setIsMembersOpen] = useState(false)

  const { data: channel, isLoading, isError } = useQuery({
    queryKey: ['channel', activeWorkspace?.publicId, channelId],
    queryFn: () => channelApi.get(activeWorkspace!.publicId, channelId!),
    enabled: !!activeWorkspace && !!channelId,
  })

  const { mutate: archiveChannel, isPending: isArchiving } = useMutation({
    mutationFn: () => channelApi.archive(activeWorkspace!.publicId, channelId!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['channel', activeWorkspace?.publicId, channelId] })
      queryClient.invalidateQueries({ queryKey: ['channels', activeWorkspace?.publicId] })
      toast.success('Channel archived')
    },
    onError: (error: any) => toast.error(error.response?.data?.message || 'Failed to archive channel')
  })

  const { mutate: deleteChannel, isPending: isDeleting } = useMutation({
    mutationFn: () => channelApi.delete(activeWorkspace!.publicId, channelId!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['channels', activeWorkspace?.publicId] })
      toast.success('Channel deleted')
      navigate('/channels')
    },
    onError: (error: any) => toast.error(error.response?.data?.message || 'Failed to delete channel')
  })

  if (isLoading) {
    return (
      <div className="flex flex-col h-full">
        <header className="h-14 border-b flex items-center px-6 shrink-0">
          <Skeleton className="h-6 w-48" />
        </header>
        <div className="flex-1 p-6">
          <Skeleton className="h-full w-full rounded-xl" />
        </div>
      </div>
    )
  }

  if (isError || !channel) {
    return (
      <div className="flex items-center justify-center h-full">
        <EmptyState title="Channel not found" description="The channel may have been deleted or you don't have access." />
      </div>
    )
  }

  return (
    <div className="flex flex-col h-full">
      {/* Channel Header */}
      <header className="h-14 border-b flex items-center justify-between px-4 md:px-6 shrink-0 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="flex items-center gap-2">
          {channel.visibility === 'PRIVATE' ? (
            <Lock className="h-4 w-4 text-muted-foreground" />
          ) : (
            <Hash className="h-4 w-4 text-muted-foreground" />
          )}
          <h2 className="font-semibold">{channel.name}</h2>
          {channel.archived && (
            <span className="ml-2 px-1.5 py-0.5 rounded-sm bg-secondary text-secondary-foreground text-xs font-medium">
              Archived
            </span>
          )}
        </div>
        
        <div className="flex items-center gap-1 md:gap-2">
          <Dialog open={isMembersOpen} onOpenChange={setIsMembersOpen}>
            <DialogTrigger asChild>
              <Button variant="ghost" size="sm" className="h-8 hidden md:flex">
                <Users className="h-4 w-4 mr-2" />
                Members
              </Button>
            </DialogTrigger>
            <DialogContent>
              <ChannelMembersModal channel={channel} />
            </DialogContent>
          </Dialog>

          <Dialog open={isSettingsOpen} onOpenChange={setIsSettingsOpen}>
            <DialogTrigger asChild>
              <Button variant="ghost" size="icon" className="h-8 w-8">
                <Settings className="h-4 w-4" />
              </Button>
            </DialogTrigger>
            <DialogContent>
              <ChannelSettingsModal channel={channel} onSuccess={() => setIsSettingsOpen(false)} />
            </DialogContent>
          </Dialog>
          
          {!channel.archived && (
            <ConfirmationDialog
              title="Archive Channel"
              description={`Are you sure you want to archive #${channel.name}? It will become read-only for everyone.`}
              onConfirm={() => archiveChannel()}
            >
              <Button variant="ghost" size="sm" className="h-8 text-destructive" disabled={isArchiving}>Archive</Button>
            </ConfirmationDialog>
          )}

          {channel.archived && (
            <ConfirmationDialog
              title="Delete Channel"
              description={`Are you sure you want to delete #${channel.name}? This cannot be undone.`}
              onConfirm={() => deleteChannel()}
              isDestructive
            >
              <Button variant="ghost" size="sm" className="h-8 text-destructive" disabled={isDeleting}>Delete</Button>
            </ConfirmationDialog>
          )}
        </div>
      </header>

      <ChatWindow channel={channel} />
    </div>
  )
}
