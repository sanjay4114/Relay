import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { channelApi, Channel } from '../api/channel-api'
import { useWorkspaceStore } from '@/features/workspaces/store/workspace-store'
import { DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Label } from '@/shared/ui/label'
import { toast } from 'sonner'
import { Hash, Lock } from 'lucide-react'

const updateChannelSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters').max(80),
  slug: z.string().max(100),
  description: z.string().optional(),
  visibility: z.enum(['PUBLIC', 'PRIVATE']),
})

type UpdateChannelValues = z.infer<typeof updateChannelSchema>

export function ChannelSettingsModal({ channel, onSuccess }: { channel: Channel, onSuccess?: () => void }) {
  const queryClient = useQueryClient()
  const { activeWorkspace } = useWorkspaceStore()
  
  const form = useForm<UpdateChannelValues>({
    resolver: zodResolver(updateChannelSchema),
    defaultValues: {
      name: channel.name,
      slug: channel.slug,
      description: channel.description || '',
      visibility: channel.visibility,
    },
  })

  const { mutate, isPending } = useMutation({
    mutationFn: (data: UpdateChannelValues) => channelApi.update(activeWorkspace!.publicId, channel.publicId, data),
    onSuccess: (updatedChannel) => {
      queryClient.invalidateQueries({ queryKey: ['channel', activeWorkspace?.publicId, channel.publicId] })
      queryClient.invalidateQueries({ queryKey: ['channels', activeWorkspace?.publicId] })
      toast.success(`Channel "#${updatedChannel.name}" updated successfully`)
      onSuccess?.()
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update channel')
    }
  })

  const onSubmit = (data: UpdateChannelValues) => {
    mutate(data)
  }
  
  const watchVisibility = form.watch('visibility')

  return (
    <>
      <DialogHeader>
        <DialogTitle>Channel Settings</DialogTitle>
        <DialogDescription>
          Update the settings for #{channel.name}
        </DialogDescription>
      </DialogHeader>
      
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4 py-4">
        <div className="space-y-2">
          <Label htmlFor="name">Name</Label>
          <div className="relative">
            {watchVisibility === 'PRIVATE' ? (
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            ) : (
              <Hash className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            )}
            <Input 
              id="name" 
              className="pl-9"
              {...form.register('name')} 
            />
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="slug">Slug</Label>
          <Input 
            id="slug" 
            {...form.register('slug')} 
          />
        </div>
        
        <div className="space-y-2">
          <Label htmlFor="description">Description (optional)</Label>
          <Input 
            id="description" 
            {...form.register('description')} 
          />
        </div>

        <div className="space-y-3 pt-2">
          <Label>Visibility</Label>
          <div className="grid gap-3 sm:grid-cols-2">
            <label className={`flex cursor-pointer items-start gap-3 rounded-lg border p-3 transition-colors hover:bg-accent ${watchVisibility === 'PUBLIC' ? 'border-primary bg-accent/50' : ''}`}>
              <input 
                type="radio" 
                value="PUBLIC" 
                className="mt-1 sr-only"
                {...form.register('visibility')}
              />
              <Hash className="mt-0.5 h-5 w-5 text-muted-foreground shrink-0" />
              <div className="space-y-1">
                <p className="text-sm font-medium leading-none">Public</p>
                <p className="text-xs text-muted-foreground">Anyone in workspace</p>
              </div>
            </label>
            
            <label className={`flex cursor-pointer items-start gap-3 rounded-lg border p-3 transition-colors hover:bg-accent ${watchVisibility === 'PRIVATE' ? 'border-primary bg-accent/50' : ''}`}>
              <input 
                type="radio" 
                value="PRIVATE" 
                className="mt-1 sr-only"
                {...form.register('visibility')}
              />
              <Lock className="mt-0.5 h-5 w-5 text-muted-foreground shrink-0" />
              <div className="space-y-1">
                <p className="text-sm font-medium leading-none">Private</p>
                <p className="text-xs text-muted-foreground">Only invited members</p>
              </div>
            </label>
          </div>
        </div>

        <DialogFooter className="pt-4">
          <Button type="button" variant="outline" onClick={onSuccess}>
            Cancel
          </Button>
          <Button type="submit" disabled={isPending}>
            {isPending ? 'Saving...' : 'Save Changes'}
          </Button>
        </DialogFooter>
      </form>
    </>
  )
}
