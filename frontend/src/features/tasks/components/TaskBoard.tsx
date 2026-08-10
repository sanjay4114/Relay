import React, { useState } from 'react';
import { useWorkspaceStore } from '@/features/workspaces/store/workspace-store';
import { useWorkspaceTasks, useUpdateTask } from '../hooks/use-tasks';
import { TaskColumn } from './TaskColumn';
import { Loader2, Plus } from 'lucide-react';
import { Button } from '@/shared/ui/button';
import { TaskDto } from '../api/task-api';
import { CreateTaskModal } from './CreateTaskModal';

const COLUMNS = [
  { id: 'TODO', title: 'To Do' },
  { id: 'IN_PROGRESS', title: 'In Progress' },
  { id: 'REVIEW', title: 'In Review' },
  { id: 'DONE', title: 'Done' }
] as const;

export function TaskBoard() {
  const currentWorkspace = useWorkspaceStore((s) => s.currentWorkspace);
  const { data: tasks, isLoading } = useWorkspaceTasks(currentWorkspace?.id || '');
  const { mutate: updateTask } = useUpdateTask(currentWorkspace?.id || '');
  
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [optimisticTasks, setOptimisticTasks] = useState<TaskDto[] | null>(null);

  // Derive tasks to show
  const displayTasks = optimisticTasks ?? tasks ?? [];

  const handleDragStart = (e: React.DragEvent, taskId: string) => {
    e.dataTransfer.setData('text/plain', taskId);
    e.dataTransfer.effectAllowed = 'move';
  };

  const handleDrop = (e: React.DragEvent, status: string) => {
    e.preventDefault();
    const taskId = e.dataTransfer.getData('text/plain');
    if (!taskId || !currentWorkspace) return;

    const task = displayTasks.find(t => t.publicId === taskId);
    if (!task || task.status === status) return;

    // Optimistic UI update
    const newTasks = displayTasks.map(t => 
      t.publicId === taskId ? { ...t, status: status as any } : t
    );
    setOptimisticTasks(newTasks);

    updateTask(
      { taskId, data: { status } },
      { 
        onSettled: () => setOptimisticTasks(null) // Revert optimistic on complete/fail to let react query take over
      }
    );
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
  };

  if (!currentWorkspace) return null;
  
  if (isLoading && !tasks) {
    return <div className="flex h-full items-center justify-center"><Loader2 className="h-8 w-8 animate-spin text-teal-500" /></div>;
  }

  return (
    <div className="flex flex-col h-full bg-background text-foreground">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Issues</h1>
          <p className="text-muted-foreground text-sm">Manage workspace tasks and issues</p>
        </div>
        <Button onClick={() => setCreateModalOpen(true)} className="bg-teal-600 hover:bg-teal-700 text-white shadow-md">
          <Plus className="mr-2 h-4 w-4" /> New Issue
        </Button>
      </div>

      <div className="flex-1 flex gap-6 overflow-x-auto pb-4">
        {COLUMNS.map(col => (
          <TaskColumn
            key={col.id}
            title={col.title}
            status={col.id}
            tasks={displayTasks.filter(t => t.status === col.id)}
            onDragStart={handleDragStart}
            onDrop={handleDrop}
            onDragOver={handleDragOver}
          />
        ))}
      </div>

      <CreateTaskModal open={createModalOpen} onOpenChange={setCreateModalOpen} workspaceId={currentWorkspace.id} />
    </div>
  );
}
