import React from 'react';
import type { TaskDto } from '../api/task-api';
import { TaskCard } from './TaskCard';

interface TaskColumnProps {
  title: string;
  status: string;
  tasks: TaskDto[];
  onDragStart: (e: React.DragEvent, taskId: string) => void;
  onDrop: (e: React.DragEvent, status: string) => void;
  onDragOver: (e: React.DragEvent) => void;
}

export function TaskColumn({ title, status, tasks, onDragStart, onDrop, onDragOver }: TaskColumnProps) {
  
  const getStatusColor = () => {
    switch(status) {
      case 'TODO': return 'bg-slate-200 dark:bg-slate-800 text-slate-700 dark:text-slate-300';
      case 'IN_PROGRESS': return 'bg-amber-100 dark:bg-amber-900/40 text-amber-700 dark:text-amber-400';
      case 'REVIEW': return 'bg-purple-100 dark:bg-purple-900/40 text-purple-700 dark:text-purple-400';
      case 'DONE': return 'bg-teal-100 dark:bg-teal-900/40 text-teal-700 dark:text-teal-400';
      default: return 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300';
    }
  };

  return (
    <div 
      className="flex-1 min-w-[300px] flex flex-col rounded-lg bg-slate-50 dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800 p-3 shadow-sm h-fit max-h-full"
      onDrop={(e) => onDrop(e, status)}
      onDragOver={onDragOver}
    >
      <div className="flex items-center justify-between mb-3 px-1">
        <h3 className="text-sm font-semibold flex items-center gap-2">
          {title}
          <span className={`text-[10px] px-1.5 py-0.5 rounded-full font-medium ${getStatusColor()}`}>
            {tasks.length}
          </span>
        </h3>
      </div>

      <div className="flex flex-col gap-3 overflow-y-auto min-h-[150px] pb-2">
        {tasks.map(task => (
          <TaskCard 
            key={task.publicId} 
            task={task} 
            onDragStart={(e) => onDragStart(e, task.publicId)} 
          />
        ))}
      </div>
    </div>
  );
}
