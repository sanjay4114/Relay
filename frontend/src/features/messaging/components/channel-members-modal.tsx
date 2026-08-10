import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { channelApi, Channel } from '../api/channel-api'
import { useWorkspaceStore } from '@/features/workspaces/store/workspace-store'
import { DialogHeader, DialogTitle, DialogDescription } from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/ui/avatar'
import { toast } from 'sonner'
import { Trash2, UserPlus } from 'lucide-react'
import { useState } from 'react'
import { Input } from '@/shared/ui/input'
import { ConfirmationDialog } from '@/shared/components/confirmation-dialog'

export function ChannelMembersModal({ channel }: { channel: Channel }) {
  const queryClient = useQueryClient()
  const { activeWorkspace } = useWorkspaceStore()
  const [inviteId, setInviteId] = useState('')

  const { data: members = [], isLoading } = useQuery({
    queryKey: ['channelMembers', activeWorkspace?.publicId, channel.publicId],
    queryFn: () => channelApi.listMembers(activeWorkspace!.publicId, channel.publicId),
    enabled: !!activeWorkspace && !!channel.publicId,
  })

  const { mutate: removeMember, isPending: isRemoving } = useMutation({
    mutationFn: (userId: string) => channelApi.removeMember(activeWorkspace!.publicId, channel.publicId, userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['channelMembers', activeWorkspace?.publicId, channel.publicId] })
      toast.success('Member removed')
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to remove member')
    }
  })

  const { mutate: inviteMember, isPending: isInviting } = useMutation({
    mutationFn: (userId: string) => channelApi.inviteMember(activeWorkspace!.publicId, channel.publicId, userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['channelMembers', activeWorkspace?.publicId, channel.publicId] })
      toast.success('Member invited')
      setInviteId('')
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to invite member')
    }
  })

  return (
    <>
      <DialogHeader>
        <DialogTitle>Channel Members</DialogTitle>
        <DialogDescription>
          Manage members of #{channel.name}
        </DialogDescription>
      </DialogHeader>
      
      <div className="py-4 space-y-4">
        {channel.visibility === 'PRIVATE' && (
          <div className="flex gap-2">
            <Input 
              placeholder="User Public ID to invite" 
              value={inviteId}
              onChange={(e) => setInviteId(e.target.value)}
            />
            <Button 
              onClick={() => inviteMember(inviteId)} 
              disabled={!inviteId || isInviting}
            >
              <UserPlus className="h-4 w-4 mr-2" />
              Invite
            </Button>
          </div>
        )}

        <div className="space-y-2 max-h-[300px] overflow-y-auto">
          {isLoading ? (
            <p className="text-sm text-muted-foreground text-center py-4">Loading members...</p>
          ) : members.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-4">No members found</p>
          ) : (
            members.map(member => (
              <div key={member.userPublicId} className="flex items-center justify-between p-2 rounded-md hover:bg-muted/50">
                <div className="flex items-center gap-3">
                  <Avatar className="h-8 w-8">
                    <AvatarImage src={member.avatarUrl} />
                    <AvatarFallback>{member.displayName.charAt(0).toUpperCase()}</AvatarFallback>
                  </Avatar>
                  <div>
                    <p className="text-sm font-medium leading-none">{member.displayName}</p>
                    <p className="text-xs text-muted-foreground">{member.role}</p>
                  </div>
                </div>
                {member.role !== 'OWNER' && (
                  <ConfirmationDialog
                    title="Remove Member"
                    description={`Are you sure you want to remove ${member.displayName} from this channel?`}
                    onConfirm={() => removeMember(member.userPublicId)}
                    isDestructive
                  >
                    <Button variant="ghost" size="icon" className="h-8 w-8 text-destructive opacity-50 hover:opacity-100">
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </ConfirmationDialog>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </>
  )
}
