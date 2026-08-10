import React from 'react';
import { TaskDto } from '../api/task-api';
import { AlertCircle, ArrowUp, ArrowDown, ArrowRight, Link as LinkIcon, MoreHorizontal } from 'lucide-react';
import { Avatar, AvatarFallback } from '@/shared/ui/avatar';

interface TaskCardProps {
  task: TaskDto;
  onDragStart: (e: React.DragEvent) => void;
}

const PriorityIcon = ({ priority }: { priority: string }) => {
  switch (priority) {
    case 'LOW': return <ArrowDown className="w-3.5 h-3.5 text-slate-400" />;
    case 'MEDIUM': return <ArrowRight className="w-3.5 h-3.5 text-blue-400" />;
    case 'HIGH': return <ArrowUp className="w-3.5 h-3.5 text-orange-400" />;
    case 'CRITICAL': return <AlertCircle className="w-3.5 h-3.5 text-red-500" />;
    default: return null;
  }
};

export function TaskCard({ task, onDragStart }: TaskCardProps) {
  return (
    <div
      draggable
      onDragStart={onDragStart}
      className="group bg-white dark:bg-[#1a1b1e] border border-slate-200 dark:border-slate-700/60 rounded-md p-3 shadow-sm hover:shadow-md cursor-grab active:cursor-grabbing transition-all hover:border-slate-300 dark:hover:border-slate-600"
    >
      <div className="flex items-start justify-between mb-2">
        <span className="text-xs font-mono text-slate-500 dark:text-slate-400">REL-{task.publicId.substring(0, 4).toUpperCase()}</span>
        <button className="opacity-0 group-hover:opacity-100 text-slate-400 hover:text-slate-600 transition-opacity">
          <MoreHorizontal className="w-4 h-4" />
        </button>
      </div>

      <h4 className="text-sm font-medium text-slate-900 dark:text-slate-100 mb-2 leading-snug">
        {task.title}
      </h4>

      <div className="flex flex-wrap gap-1 mb-3">
        {task.labels?.map((label, idx) => (
          <span 
            key={idx} 
            className="text-[10px] px-1.5 py-0.5 rounded-sm bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 font-medium"
            style={{ borderLeft: `2px solid ${label.color}` }}
          >
            {label.name}
          </span>
        ))}
      </div>

      <div className="flex items-center justify-between mt-3 pt-2 border-t border-slate-100 dark:border-slate-800">
        <div className="flex items-center gap-2">
          <PriorityIcon priority={task.priority} />
          {task.storyPoints !== null && task.storyPoints !== undefined && (
            <span className="text-xs font-medium text-slate-500 dark:text-slate-400 bg-slate-100 dark:bg-slate-800 px-1 rounded-sm">
              {task.storyPoints}
            </span>
          )}
          {task.linkedMessagePublicId && (
            <LinkIcon className="w-3.5 h-3.5 text-teal-500 opacity-70" title="Linked to Message" />
          )}
        </div>
        
        <div className="flex -space-x-1">
          {task.assigneePublicIds?.slice(0, 3).map((a, i) => (
            <Avatar key={a} className="w-5 h-5 border border-white dark:border-[#1a1b1e]">
              <AvatarFallback className="text-[9px]">U</AvatarFallback>
            </Avatar>
          ))}
        </div>
      </div>
    </div>
  );
}
