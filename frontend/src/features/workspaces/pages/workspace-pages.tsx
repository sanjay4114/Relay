import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { workspaceApi } from '../api/workspace-api'
import { useWorkspaceStore } from '../store/workspace-store'
import { DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Label } from '@/shared/ui/label'
import { toast } from 'sonner'

export function WorkspaceSettingsPage() {
  return <div className="p-6">Workspace Settings (Implementation Pending)</div>
}

export function MemberManagementScreen() {
  return <div className="p-6">Member Management (Implementation Pending)</div>
}

const createWorkspaceSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters').max(255),
  slug: z.string().max(100).optional(),
  description: z.string().optional(),
})

type CreateWorkspaceValues = z.infer<typeof createWorkspaceSchema>

export function CreateWorkspaceWizard({ onSuccess }: { onSuccess?: () => void }) {
  const queryClient = useQueryClient()
  const { setActiveWorkspace } = useWorkspaceStore()
  
  const form = useForm<CreateWorkspaceValues>({
    resolver: zodResolver(createWorkspaceSchema),
    defaultValues: {
      name: '',
      slug: '',
      description: '',
    },
  })

  const { mutate, isPending } = useMutation({
    mutationFn: workspaceApi.create,
    onSuccess: (newWorkspace) => {
      // 1. Refresh workspace list
      queryClient.invalidateQueries({ queryKey: ['workspaces'] })
      // 2. Automatically switch to the newly created workspace
      setActiveWorkspace(newWorkspace)
      toast.success(`Workspace "${newWorkspace.name}" created successfully`)
      onSuccess?.()
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || 'Failed to create workspace'
      toast.error(message)
      console.error(error)
    }
  })

  const onSubmit = (data: CreateWorkspaceValues) => {
    mutate(data)
  }
  
  const watchName = form.watch('name')
  const generatedSlug = form.watch('slug') || watchName.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '')

  return (
    <>
      <DialogHeader>
        <DialogTitle>Create a Workspace</DialogTitle>
        <DialogDescription>
          Create a new workspace to collaborate with your team.
        </DialogDescription>
      </DialogHeader>
      
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4 py-4">
        <div className="space-y-2">
          <Label htmlFor="name">Workspace Name</Label>
          <Input 
            id="name" 
            placeholder="e.g. Acme Corp" 
            {...form.register('name')} 
          />
          {form.formState.errors.name && (
            <p className="text-sm text-destructive">{form.formState.errors.name.message}</p>
          )}
        </div>
        
        <div className="space-y-2">
          <Label htmlFor="slug">Workspace URL (Slug)</Label>
          <Input 
            id="slug" 
            placeholder="Leave empty to auto-generate" 
            {...form.register('slug')} 
          />
          <p className="text-xs text-muted-foreground">
            Preview: relay.app/w/{generatedSlug || 'acme-corp'}
          </p>
          {form.formState.errors.slug && (
            <p className="text-sm text-destructive">{form.formState.errors.slug.message}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="description">Description (Optional)</Label>
          <Input 
            id="description" 
            placeholder="What is this workspace for?" 
            {...form.register('description')} 
          />
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={onSuccess}>
            Cancel
          </Button>
          <Button type="submit" disabled={isPending}>
            {isPending ? 'Creating...' : 'Create Workspace'}
          </Button>
        </DialogFooter>
      </form>
    </>
  )
}

