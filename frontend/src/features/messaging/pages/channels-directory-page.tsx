import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Hash, Lock, Users, Search as SearchIcon } from 'lucide-react'
import { channelApi, Channel } from '../api/channel-api'
import { useWorkspaceStore } from '@/features/workspaces/store/workspace-store'
import { PageHeader } from '@/shared/components/page-header'
import { EmptyState } from '@/shared/components/empty-state'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Dialog, DialogContent, DialogTrigger } from '@/shared/ui/dialog'
import { CreateChannelModal } from '../components/create-channel-modal'
import { useState } from 'react'
import { toast } from 'sonner'
import { useNavigate } from 'react-router-dom'
import { formatDistanceToNow } from 'date-fns'

export function ChannelsDirectoryPage() {
  const { activeWorkspace } = useWorkspaceStore()
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [search, setSearch] = useState('')
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)

  const { data: channels = [], isLoading } = useQuery({
    queryKey: ['channels', activeWorkspace?.publicId],
    queryFn: () => channelApi.list(activeWorkspace!.publicId),
    enabled: !!activeWorkspace,
  })

  const { mutate: joinChannel, isPending: isJoining } = useMutation({
    mutationFn: (channelId: string) => channelApi.join(activeWorkspace!.publicId, channelId),
    onSuccess: (_, channelId) => {
      queryClient.invalidateQueries({ queryKey: ['channels', activeWorkspace?.publicId] })
      toast.success('Joined channel')
      navigate(`/channels/${channelId}`)
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to join channel')
    }
  })

  const filteredChannels = channels.filter(c => 
    c.name.toLowerCase().includes(search.toLowerCase()) || 
    (c.description && c.description.toLowerCase().includes(search.toLowerCase()))
  )

  return (
    <div className="flex flex-col h-full max-w-5xl mx-auto">
      <PageHeader 
        title="All Channels" 
        description={`Browse all public channels in ${activeWorkspace?.name || 'this workspace'}`}
        actions={
          <Dialog open={isCreateModalOpen} onOpenChange={setIsCreateModalOpen}>
            <DialogTrigger asChild>
              <Button>Create Channel</Button>
            </DialogTrigger>
            <DialogContent>
              <CreateChannelModal onSuccess={() => setIsCreateModalOpen(false)} />
            </DialogContent>
          </Dialog>
        }
      />

      <div className="mb-6 relative">
        <SearchIcon className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input 
          placeholder="Search channels..." 
          className="pl-9 max-w-md"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <div className="flex-1 overflow-auto bg-card border rounded-lg shadow-sm">
        {isLoading ? (
          <div className="p-8 text-center text-muted-foreground">Loading channels...</div>
        ) : filteredChannels.length === 0 ? (
          <EmptyState 
            title="No channels found" 
            description={search ? "Try a different search term" : "There are no public channels to join yet."} 
          />
        ) : (
          <div className="divide-y">
            {filteredChannels.map((channel) => (
              <div key={channel.publicId} className="p-4 flex items-center justify-between hover:bg-muted/50 transition-colors">
                <div className="flex items-start gap-3">
                  <div className="mt-1 h-8 w-8 rounded bg-primary/10 flex items-center justify-center shrink-0">
                    {channel.visibility === 'PRIVATE' ? (
                      <Lock className="h-4 w-4 text-primary" />
                    ) : (
                      <Hash className="h-4 w-4 text-primary" />
                    )}
                  </div>
                  <div>
                    <h3 className="font-semibold">{channel.name}</h3>
                    {channel.description && (
                      <p className="text-sm text-muted-foreground mt-0.5 line-clamp-1">{channel.description}</p>
                    )}
                    <div className="flex items-center gap-4 mt-2 text-xs text-muted-foreground">
                      <span className="flex items-center gap-1">
                        <Users className="h-3 w-3" />
                        Created {formatDistanceToNow(new Date(channel.createdAt), { addSuffix: true })}
                      </span>
                      {channel.archived && (
                        <span className="px-1.5 py-0.5 rounded-sm bg-secondary text-secondary-foreground font-medium">
                          Archived
                        </span>
                      )}
                    </div>
                  </div>
                </div>
                
                <div className="ml-4 shrink-0">
                  <Button 
                    variant="outline" 
                    onClick={() => joinChannel(channel.publicId)}
                    disabled={isJoining}
                  >
                    View / Join
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
