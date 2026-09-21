import React, { useState, useEffect, useRef } from 'react';
import * as Dialog from '@radix-ui/react-dialog';
import { Search, Loader2, MessageSquare, Hash, User, Briefcase, File, CheckSquare } from 'lucide-react';
import { useGlobalSearch } from '../hooks/use-search';
import { useNavigate } from 'react-router-dom';
import { useWorkspaceStore } from '@/features/workspaces/store/workspace-store';

const iconMap: Record<string, React.ReactNode> = {
  MESSAGE: <MessageSquare className="w-4 h-4 text-blue-500" />,
  CHANNEL: <Hash className="w-4 h-4 text-green-500" />,
  USER: <User className="w-4 h-4 text-purple-500" />,
  WORKSPACE: <Briefcase className="w-4 h-4 text-orange-500" />,
  FILE: <File className="w-4 h-4 text-red-500" />,
  TASK: <CheckSquare className="w-4 h-4 text-emerald-500" />,
};

export const GlobalSearchDialog = () => {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [type, setType] = useState<string | undefined>();
  const inputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();
  
  const activeWorkspace = useWorkspaceStore((s) => s.activeWorkspace);

  // Debounce query
  const [debouncedQuery, setDebouncedQuery] = useState('');
  useEffect(() => {
    const t = setTimeout(() => setDebouncedQuery(query), 300);
    return () => clearTimeout(t);
  }, [query]);

  const { data, isLoading } = useGlobalSearch(debouncedQuery, activeWorkspace?.publicId, type);

  useEffect(() => {
    const down = (e: KeyboardEvent) => {
      if (e.key === 'k' && (e.metaKey || e.ctrlKey)) {
        e.preventDefault();
        setOpen((open) => !open);
      }
    };
    document.addEventListener('keydown', down);
    return () => document.removeEventListener('keydown', down);
  }, []);

  const handleSelect = (result: any) => {
    setOpen(false);
    if (result.type === 'CHANNEL') {
      navigate(`/app/workspace/${result.metadata.workspaceId}/channel/${result.id}`);
    } else if (result.type === 'MESSAGE') {
      navigate(`/app/workspace/${result.metadata.workspaceId}/channel/${result.metadata.channelId}?message=${result.id}`);
    } else if (result.type === 'WORKSPACE') {
      navigate(`/app/workspace/${result.id}`);
    } else if (result.type === 'TASK') {
      navigate(`/app/workspace/${result.metadata.workspaceId}/tasks/${result.id}`);
    } else if (result.url) {
      window.open(result.url, '_blank');
    }
  };

  return (
    <>
      <button
        onClick={() => setOpen(true)}
        className="flex items-center gap-2 px-3 py-1.5 text-sm text-gray-500 bg-gray-100 dark:bg-gray-800 rounded-md hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors border border-transparent dark:border-gray-700 w-64"
      >
        <Search className="w-4 h-4" />
        <span className="flex-1 text-left">Search {activeWorkspace?.name || 'Relay'}...</span>
        <kbd className="hidden sm:inline-flex items-center gap-1 px-1.5 font-mono text-[10px] font-medium text-gray-500 bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 rounded opacity-100">
          <span className="text-xs">⌘</span>K
        </kbd>
      </button>

      <Dialog.Root open={open} onOpenChange={setOpen}>
        <Dialog.Portal>
          <Dialog.Overlay className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 animate-in fade-in" />
          <Dialog.Content className="fixed top-[20%] left-1/2 -translate-x-1/2 w-full max-w-2xl bg-white dark:bg-gray-900 rounded-xl shadow-2xl border border-gray-200 dark:border-gray-800 z-50 flex flex-col overflow-hidden animate-in fade-in zoom-in-95">
            
            <div className="flex items-center px-4 py-3 border-b border-gray-200 dark:border-gray-800">
              <Search className="w-5 h-5 text-gray-400 mr-3" />
              <input
                ref={inputRef}
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search messages, channels, files, and users..."
                className="flex-1 bg-transparent border-none outline-none text-gray-900 dark:text-gray-100 placeholder:text-gray-400 text-lg"
                autoFocus
              />
              {isLoading && <Loader2 className="w-5 h-5 text-gray-400 animate-spin" />}
            </div>

            <div className="flex gap-2 px-4 py-2 bg-gray-50 dark:bg-gray-900/50 border-b border-gray-200 dark:border-gray-800 overflow-x-auto">
              {['All', 'MESSAGE', 'CHANNEL', 'USER', 'FILE', 'TASK'].map((t) => (
                <button
                  key={t}
                  onClick={() => setType(t === 'All' ? undefined : t)}
                  className={`px-3 py-1 text-xs font-medium rounded-full whitespace-nowrap transition-colors ${
                    (type === t || (t === 'All' && !type))
                      ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/50 dark:text-blue-300'
                      : 'text-gray-600 hover:bg-gray-200 dark:text-gray-400 dark:hover:bg-gray-800'
                  }`}
                >
                  {t === 'All' ? 'All Results' : t.charAt(0) + t.slice(1).toLowerCase() + 's'}
                </button>
              ))}
            </div>

            <div className="max-h-[60vh] overflow-y-auto p-2">
              {!query ? (
                <div className="px-4 py-12 text-center text-gray-500">
                  <p>Type to start searching...</p>
                  <div className="flex justify-center gap-4 mt-4">
                    <span className="flex items-center gap-2"><Hash className="w-4 h-4"/> Channels</span>
                    <span className="flex items-center gap-2"><MessageSquare className="w-4 h-4"/> Messages</span>
                    <span className="flex items-center gap-2"><User className="w-4 h-4"/> Users</span>
                    <span className="flex items-center gap-2"><File className="w-4 h-4"/> Files</span>
                  </div>
                </div>
              ) : data?.results?.length === 0 ? (
                <div className="px-4 py-12 text-center text-gray-500">
                  No results found for "{query}"
                </div>
              ) : (
                <div className="flex flex-col gap-1">
                  {data?.results?.map((result, idx) => (
                    <button
                      key={`${result.type}-${result.id}-${idx}`}
                      onClick={() => handleSelect(result)}
                      className="flex items-start gap-4 p-3 text-left rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors focus:bg-gray-100 dark:focus:bg-gray-800 outline-none group"
                    >
                      <div className="mt-1 p-2 bg-gray-100 dark:bg-gray-800 rounded-md group-hover:bg-white dark:group-hover:bg-gray-700 shadow-sm transition-colors">
                        {iconMap[result.type]}
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <span className="font-medium text-gray-900 dark:text-gray-100 truncate">
                            {result.title}
                          </span>
                          <span className="text-xs text-gray-500 border border-gray-200 dark:border-gray-700 px-1.5 py-0.5 rounded">
                            {result.type}
                          </span>
                        </div>
                        <p className="text-sm text-gray-500 dark:text-gray-400 line-clamp-2 mt-0.5" 
                           dangerouslySetInnerHTML={{
                             __html: result.subtitle.replace(
                               new RegExp(`(${query})`, 'gi'),
                               '<span class="bg-yellow-200 dark:bg-yellow-900/50 text-gray-900 dark:text-yellow-200 font-semibold rounded px-0.5">$1</span>'
                             )
                           }} 
                        />
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
            
            <div className="px-4 py-2 border-t border-gray-200 dark:border-gray-800 text-xs text-gray-500 flex justify-between">
              <span>Use arrows to navigate, Enter to select</span>
              <span>ESC to close</span>
            </div>

          </Dialog.Content>
        </Dialog.Portal>
      </Dialog.Root>
    </>
  );
};
