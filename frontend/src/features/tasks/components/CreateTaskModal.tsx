import React from 'react';
import * as Dialog from '@radix-ui/react-dialog';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Button } from '@/shared/ui/button';
import { X, Loader2 } from 'lucide-react';
import { useCreateTask } from '../hooks/use-tasks';
import type { CreateTaskRequest } from '../api/task-api';

const taskSchema = z.object({
  title: z.string().min(2, 'Title must be at least 2 characters'),
  description: z.string().optional(),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']).default('MEDIUM'),
  storyPoints: z.coerce.number().optional(),
});

type TaskFormValues = z.infer<typeof taskSchema>;

export function CreateTaskModal({ open, onOpenChange, workspaceId, initialData }: { 
  open: boolean; 
  onOpenChange: (open: boolean) => void;
  workspaceId: string;
  initialData?: Partial<CreateTaskRequest>;
}) {
  const { mutate: createTask, isPending } = useCreateTask(workspaceId);
  
  const { register, handleSubmit, reset, formState: { errors } } = useForm<TaskFormValues>({
    resolver: zodResolver(taskSchema),
    defaultValues: {
      title: initialData?.title || '',
      description: initialData?.description || '',
      priority: (initialData?.priority as any) || 'MEDIUM',
      storyPoints: initialData?.storyPoints,
    }
  });

  const onSubmit = (data: TaskFormValues) => {
    createTask({
      ...data,
      linkedMessagePublicId: initialData?.linkedMessagePublicId
    }, {
      onSuccess: () => {
        reset();
        onOpenChange(false);
      }
    });
  };

  return (
    <Dialog.Root open={open} onOpenChange={onOpenChange}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 animate-in fade-in" />
        <Dialog.Content className="fixed top-[50%] left-[50%] translate-x-[-50%] translate-y-[-50%] w-full max-w-lg bg-white dark:bg-[#1a1b1e] rounded-xl shadow-xl border border-slate-200 dark:border-slate-800 z-50 p-6">
          <div className="flex items-center justify-between mb-5">
            <Dialog.Title className="text-xl font-semibold">
              Create New Issue
            </Dialog.Title>
            <Dialog.Close asChild>
              <button className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-300">
                <X className="w-5 h-5" />
              </button>
            </Dialog.Close>
          </div>
          
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1 text-slate-700 dark:text-slate-300">Title</label>
              <input
                {...register('title')}
                className="w-full px-3 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500"
                placeholder="Issue title"
                autoFocus
              />
              {errors.title && <p className="text-red-500 text-xs mt-1">{errors.title.message}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium mb-1 text-slate-700 dark:text-slate-300">Description</label>
              <textarea
                {...register('description')}
                className="w-full px-3 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500 min-h-[100px]"
                placeholder="Add more details..."
              />
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1 text-slate-700 dark:text-slate-300">Priority</label>
                <select 
                  {...register('priority')}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500"
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-1 text-slate-700 dark:text-slate-300">Story Points</label>
                <input
                  type="number"
                  {...register('storyPoints')}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500"
                  placeholder="e.g. 3"
                />
              </div>
            </div>
            
            {initialData?.linkedMessagePublicId && (
              <div className="p-3 bg-teal-50 dark:bg-teal-900/20 border border-teal-200 dark:border-teal-800 rounded text-sm text-teal-800 dark:text-teal-300 flex items-center">
                This task will be linked to the selected message.
              </div>
            )}

            <div className="flex justify-end gap-3 mt-6 pt-4 border-t border-slate-100 dark:border-slate-800">
              <Dialog.Close asChild>
                <Button variant="outline" type="button">Cancel</Button>
              </Dialog.Close>
              <Button type="submit" className="bg-teal-600 hover:bg-teal-700 text-white" disabled={isPending}>
                {isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                Create Issue
              </Button>
            </div>
          </form>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
