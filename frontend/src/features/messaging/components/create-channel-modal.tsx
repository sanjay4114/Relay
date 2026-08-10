import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { channelApi } from '../api/channel-api'
import { useWorkspaceStore } from '@/features/workspaces/store/workspace-store'
import { DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Label } from '@/shared/ui/label'
import { toast } from 'sonner'
import { Hash, Lock } from 'lucide-react'

const createChannelSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters').max(80),
  slug: z.string().max(100).optional(),
  description: z.string().optional(),
  visibility: z.enum(['PUBLIC', 'PRIVATE']),
})

type CreateChannelValues = z.infer<typeof createChannelSchema>

export function CreateChannelModal({ onSuccess }: { onSuccess?: () => void }) {
  const queryClient = useQueryClient()
  const { activeWorkspace } = useWorkspaceStore()
  
  const form = useForm<CreateChannelValues>({
    resolver: zodResolver(createChannelSchema),
    defaultValues: {
      name: '',
      slug: '',
      description: '',
      visibility: 'PUBLIC',
    },
  })

  const { mutate, isPending } = useMutation({
    mutationFn: (data: CreateChannelValues) => channelApi.create(activeWorkspace!.publicId, data),
    onSuccess: (newChannel) => {
      queryClient.invalidateQueries({ queryKey: ['channels', activeWorkspace?.publicId] })
      toast.success(`Channel "#${newChannel.name}" created successfully`)
      onSuccess?.()
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || 'Failed to create channel'
      toast.error(message)
    }
  })

  const onSubmit = (data: CreateChannelValues) => {
    mutate(data)
  }
  
  const watchName = form.watch('name')
  const watchVisibility = form.watch('visibility')
  const generatedSlug = form.watch('slug') || watchName.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '')

  return (
    <>
      <DialogHeader>
        <DialogTitle>Create a channel</DialogTitle>
        <DialogDescription>
          Channels are where your team communicates. They're best when organized around a topic.
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
              placeholder="e.g. plan-budget" 
              {...form.register('name')} 
            />
          </div>
          {form.formState.errors.name && (
            <p className="text-sm text-destructive">{form.formState.errors.name.message}</p>
          )}
        </div>
        
        <div className="space-y-2">
          <Label htmlFor="description">Description (optional)</Label>
          <Input 
            id="description" 
            placeholder="What's this channel about?" 
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
                <p className="text-xs text-muted-foreground">Anyone in {activeWorkspace?.name} can join</p>
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
                <p className="text-xs text-muted-foreground">Only invited members can view or join</p>
              </div>
            </label>
          </div>
        </div>

        <DialogFooter className="pt-4">
          <Button type="button" variant="outline" onClick={onSuccess}>
            Cancel
          </Button>
          <Button type="submit" disabled={isPending || !watchName}>
            {isPending ? 'Creating...' : 'Create Channel'}
          </Button>
        </DialogFooter>
      </form>
    </>
  )
}
