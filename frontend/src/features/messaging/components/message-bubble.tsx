import { format } from 'date-fns'
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/ui/avatar'
import { Pencil, Trash2, Undo2, MoreHorizontal, MessageSquare, Pin, Bookmark, SmilePlus, Paperclip, FileText, Download, CheckSquare } from 'lucide-react'
import { Button } from '@/shared/ui/button'
import { Popover, PopoverContent, PopoverTrigger } from '@/shared/ui/popover'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger, DropdownMenuSeparator } from '@/shared/ui/dropdown-menu'
import { useCurrentUser } from '@/features/auth/hooks/use-current-user'
import { useWorkspaceStore } from '@/features/workspaces/store/workspace-store'
import { toast } from 'sonner'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { CreateTaskModal } from '@/features/tasks/components/CreateTaskModal'

interface MessageBubbleProps {
  message: Message
  isConsecutive?: boolean // If true, hide avatar and name
  onOpenThread?: (messageId: string) => void
}

export const MessageBubble = memo(function MessageBubble({ message, isConsecutive, onOpenThread }: MessageBubbleProps) {
  const { data: currentUser } = useCurrentUser()
  const { activeWorkspace } = useWorkspaceStore()
  const queryClient = useQueryClient()
  
  const [isEditing, setIsEditing] = useState(false)
  const [editContent, setEditContent] = useState(message.content)
  const [isTaskModalOpen, setIsTaskModalOpen] = useState(false)
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  const isSender = currentUser?.publicId === message.sender.publicId
  const isAdminOrOwner = activeWorkspace?.role === 'ADMIN' || activeWorkspace?.role === 'OWNER'
  const isDeleted = !!message.deletedAt

  const COMMON_EMOJIS = ['👍', '👎', '❤️', '😂', '🔥', '👀', '🎉', '🚀']

  // Mutations
  const { mutate: editMessage, isPending: isSaving } = useMutation({
    mutationFn: (newContent: string) => messageApi.editMessage(message.channelPublicId, message.publicId, newContent),
    onSuccess: () => setIsEditing(false),
    onError: (err: any) => toast.error(err.response?.data?.message || 'Failed to edit message')
  })

  const { mutate: deleteMessage } = useMutation({
    mutationFn: () => messageApi.deleteMessage(message.channelPublicId, message.publicId),
    onError: (err: any) => toast.error(err.response?.data?.message || 'Failed to delete message')
  })

  const { mutate: restoreMessage } = useMutation({
    mutationFn: () => messageApi.restoreMessage(message.channelPublicId, message.publicId),
    onError: (err: any) => toast.error(err.response?.data?.message || 'Failed to restore message')
  })

  const { mutate: addReaction } = useMutation({
    mutationFn: (emoji: string) => messageApi.addReaction(message.channelPublicId, message.publicId, emoji),
    onError: (err: any) => toast.error(err.response?.data?.message || 'Failed to add reaction')
  })

  const { mutate: removeReaction } = useMutation({
    mutationFn: (emoji: string) => messageApi.removeReaction(message.channelPublicId, message.publicId, emoji),
    onError: (err: any) => toast.error(err.response?.data?.message || 'Failed to remove reaction')
  })

  const { mutate: togglePin } = useMutation({
    mutationFn: () => message.isPinned ? messageApi.unpinMessage(message.channelPublicId, message.publicId) : messageApi.pinMessage(message.channelPublicId, message.publicId),
    onError: (err: any) => toast.error(err.response?.data?.message || 'Failed to pin/unpin message')
  })

  const { mutate: toggleSave } = useMutation({
    // We don't have isSaved in message yet, so this will just trigger save for now (or unsave if we track it).
    // Let's just assume we trigger save. We'll handle save state in a future iteration or dedicated panel.
    mutationFn: () => messageApi.saveMessage(message.channelPublicId, message.publicId),
    onSuccess: () => toast.success('Message saved to bookmarks'),
    onError: (err: any) => toast.error(err.response?.data?.message || 'Failed to save message')
  })

  const handleSaveEdit = () => {
    const trimmed = editContent.trim()
    if (!trimmed) return
    if (trimmed === message.content) {
      setIsEditing(false)
      return
    }
    editMessage(trimmed)
  }

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Escape') {
      setIsEditing(false)
      setEditContent(message.content)
    } else if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSaveEdit()
    }
  }

  const timeString = format(new Date(message.createdAt), 'h:mm a')

  // Parse mentions in the text
  const renderContent = (content: string) => {
    if (!content) return null
    // Replace <@publicId> with a styled mention. In a real app we'd map publicId to displayName.
    return content.split(/(<@[a-zA-Z0-9-]+>)/g).map((part, i) => {
      if (part.startsWith('<@') && part.endsWith('>')) {
        const id = part.slice(2, -1)
        const isMe = id === currentUser?.publicId
        return (
          <span key={i} className={`font-semibold rounded px-1 text-sm cursor-pointer hover:underline ${isMe ? 'bg-primary/20 text-primary' : 'bg-muted text-foreground'}`}>
            @{id}
          </span>
        )
      }
      return part
    })
  }

  return (
    <motion.div 
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.2 }}
      className={`group relative flex gap-3 px-4 py-1.5 hover:bg-muted/40 transition-colors ${isConsecutive ? 'mt-0' : 'mt-4'}`}
    >
      {/* Avatar or Timestamp Gutter */}
      <div className="w-10 shrink-0 flex justify-center">
        {!isConsecutive ? (
          <Avatar className="h-10 w-10 mt-1 rounded-md">
            <AvatarImage src={message.sender.avatarUrl} />
            <AvatarFallback className="rounded-md bg-primary/10 text-primary font-medium">
              {message.sender.displayName.charAt(0).toUpperCase()}
            </AvatarFallback>
          </Avatar>
        ) : (
          <div className="w-full text-[10px] text-muted-foreground/0 group-hover:text-muted-foreground transition-colors self-center text-center mt-1 select-none">
            {timeString}
          </div>
        )}
      </div>

      {/* Content */}
      <div className="flex-1 min-w-0 flex flex-col">
        {/* Pinned Indicator */}
        {!isConsecutive && message.isPinned && (
          <div className="flex items-center gap-1 text-xs text-muted-foreground mb-1 mt-1">
            <Pin className="h-3 w-3" /> Pinned
          </div>
        )}

        {!isConsecutive && (
          <div className="flex items-baseline gap-2">
            <span className="font-semibold text-[15px] hover:underline cursor-pointer">
              {message.sender.displayName}
            </span>
            <span className="text-xs text-muted-foreground">
              {timeString}
            </span>
          </div>
        )}
        
        {isEditing ? (
          <div className="mt-1 bg-background border rounded-lg p-2 shadow-sm focus-within:border-primary focus-within:ring-1 focus-within:ring-primary transition-all">
            <textarea
              ref={textareaRef}
              value={editContent}
              onChange={(e) => {
                setEditContent(e.target.value)
                e.target.style.height = 'auto'
                e.target.style.height = `${Math.min(e.target.scrollHeight, 200)}px`
              }}
              onKeyDown={handleKeyDown}
              className="w-full resize-none bg-transparent text-[15px] outline-none"
              rows={1}
              autoFocus
            />
            <div className="flex justify-between items-center mt-2">
              <span className="text-[10px] text-muted-foreground">esc to cancel • enter to save</span>
              <div className="flex gap-2">
                <Button variant="ghost" size="sm" onClick={() => setIsEditing(false)} className="h-7 text-xs">Cancel</Button>
                <Button size="sm" onClick={handleSaveEdit} disabled={!editContent.trim() || isSaving} className="h-7 text-xs">Save</Button>
              </div>
            </div>
          </div>
        ) : (
          <div className={`text-[15px] leading-relaxed break-words whitespace-pre-wrap ${isDeleted ? 'text-muted-foreground italic opacity-70' : 'text-foreground'}`}>
            {isDeleted ? "This message was deleted." : renderContent(message.content)}
            {!isDeleted && message.editedAt && (
              <span className="text-[11px] text-muted-foreground ml-2 select-none hover:underline cursor-help" title={`Edited at ${format(new Date(message.editedAt), 'PP p')}`}>
                (edited)
              </span>
            )}
          </div>
        )}

        {/* Attachments */}
        {!isDeleted && message.attachments && message.attachments.length > 0 && (
          <div className="flex flex-wrap gap-2 mt-2">
            {message.attachments.map(att => {
              const isImage = att.mimeType.startsWith('image/')
              return (
                <div key={att.publicId} className="border rounded-md overflow-hidden bg-muted/30 group relative max-w-sm">
                  {isImage && att.thumbnailUrl ? (
                    <div className="relative cursor-pointer" onClick={() => window.open(att.url, '_blank')}>
                      <img src={att.thumbnailUrl} alt={att.originalName} className="max-h-64 object-contain" />
                      <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                        <Download className="h-8 w-8 text-white" />
                      </div>
                    </div>
                  ) : (
                    <div className="p-3 flex items-center gap-3 w-64 cursor-pointer hover:bg-muted/50 transition-colors" onClick={() => window.open(att.url, '_blank')}>
                      <div className="p-2 bg-primary/10 rounded text-primary">
                        <FileText className="h-6 w-6" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate" title={att.originalName}>{att.originalName}</p>
                        <p className="text-xs text-muted-foreground">{(att.fileSize / 1024 / 1024).toFixed(2)} MB • {att.extension.toUpperCase()}</p>
                      </div>
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        )}

        {/* Reactions Bar */}
        {!isDeleted && message.reactions && Object.keys(message.reactions).length > 0 && (
          <div className="flex flex-wrap gap-1 mt-1">
            {Object.entries(message.reactions).map(([emoji, users]) => {
              const hasReacted = currentUser && users.includes(currentUser.publicId)
              return (
                <button
                  key={emoji}
                  onClick={() => hasReacted ? removeReaction(emoji) : addReaction(emoji)}
                  className={`flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs border transition-colors ${
                    hasReacted 
                      ? 'bg-primary/10 border-primary/20 text-primary hover:bg-primary/20' 
                      : 'bg-muted/50 border-transparent hover:bg-muted/80 text-muted-foreground'
                  }`}
                  title={`${users.length} reaction${users.length === 1 ? '' : 's'}`}
                >
                  <span>{emoji}</span>
                  <span className="font-medium">{users.length}</span>
                </button>
              )
            })}
          </div>
        )}

        {/* Reply Count Button */}
        {!isDeleted && message.replyCount > 0 && !isEditing && (
          <div className="mt-2 flex items-center">
            <Button 
              variant="ghost" 
              size="sm" 
              className="h-7 text-xs text-primary font-medium px-2 py-0 hover:bg-primary/10 rounded"
              onClick={() => onOpenThread?.(message.publicId)}
            >
              {message.replyCount} {message.replyCount === 1 ? 'reply' : 'replies'}
              {message.lastReplyAt && (
                <span className="text-muted-foreground font-normal ml-2">
                  Last reply {format(new Date(message.lastReplyAt), 'MMM d, h:mm a')}
                </span>
              )}
            </Button>
          </div>
        )}
      </div>

      {/* Hover Actions (Desktop) / Long press equivalent (Mobile can use dropdown) */}
      {!isEditing && (
        <div className="absolute right-4 top-2 opacity-0 group-hover:opacity-100 transition-opacity bg-background border rounded-md shadow-sm flex items-center overflow-hidden z-10">
          {!isDeleted && (
            <Popover>
              <PopoverTrigger asChild>
                <Button variant="ghost" size="icon" className="h-8 w-8 rounded-none" title="Add Reaction">
                  <SmilePlus className="h-4 w-4 text-muted-foreground" />
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-auto p-2 flex gap-1" align="end" side="top">
                {COMMON_EMOJIS.map(emoji => (
                  <button
                    key={emoji}
                    onClick={() => addReaction(emoji)}
                    className="h-8 w-8 rounded hover:bg-muted flex items-center justify-center text-lg transition-colors"
                  >
                    {emoji}
                  </button>
                ))}
              </PopoverContent>
            </Popover>
          )}

          {isSender && !isDeleted && (
            <>
              <Button variant="ghost" size="icon" className="h-8 w-8 rounded-none" onClick={() => {
                setIsEditing(true)
                setEditContent(message.content)
              }} title="Edit Message">
                <Pencil className="h-4 w-4 text-muted-foreground" />
              </Button>
            </>
          )}

          {!isDeleted && onOpenThread && (
            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-none" onClick={() => onOpenThread(message.publicId)} title="Reply in thread">
              <MessageSquare className="h-4 w-4 text-muted-foreground" />
            </Button>
          )}
          
          {isDeleted && isAdminOrOwner && (
            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-none" onClick={() => restoreMessage()} title="Restore Message">
              <Undo2 className="h-4 w-4 text-muted-foreground" />
            </Button>
          )}

          {/* More options placeholder */}
          {!isDeleted && (
             <DropdownMenu>
               <DropdownMenuTrigger asChild>
                 <Button variant="ghost" size="icon" className="h-8 w-8 rounded-none">
                   <MoreHorizontal className="h-4 w-4 text-muted-foreground" />
                 </Button>
               </DropdownMenuTrigger>
               <DropdownMenuContent align="end" className="w-40">
                 {onOpenThread && (
                   <DropdownMenuItem onClick={() => onOpenThread(message.publicId)}>
                     <MessageSquare className="h-4 w-4 mr-2" /> Reply in thread
                   </DropdownMenuItem>
                 )}
                 <DropdownMenuItem onClick={() => togglePin()}>
                   <Pin className="h-4 w-4 mr-2" /> {message.isPinned ? 'Unpin message' : 'Pin message'}
                 </DropdownMenuItem>
                 <DropdownMenuItem onClick={() => toggleSave()}>
                   <Bookmark className="h-4 w-4 mr-2" /> Save message
                 </DropdownMenuItem>
                 <DropdownMenuSeparator />
                 <DropdownMenuItem onClick={() => setIsTaskModalOpen(true)}>
                   <CheckSquare className="h-4 w-4 mr-2 text-emerald-500" /> Create Task
                 </DropdownMenuItem>
                 <DropdownMenuSeparator />
                 {isSender && (
                   <>
                     <DropdownMenuItem onClick={() => { setIsEditing(true); setEditContent(message.content); }}>
                       <Pencil className="h-4 w-4 mr-2" /> Edit message
                     </DropdownMenuItem>
                     <DropdownMenuItem onClick={() => deleteMessage()} className="text-destructive focus:text-destructive">
                       <Trash2 className="h-4 w-4 mr-2" /> Delete message
                     </DropdownMenuItem>
                   </>
                 )}
               </DropdownMenuContent>
             </DropdownMenu>
          )}
        </div>
      )}

      {/* Modals */}
      {activeWorkspace && isTaskModalOpen && (
        <CreateTaskModal 
          open={isTaskModalOpen} 
          onOpenChange={setIsTaskModalOpen}
          workspaceId={activeWorkspace.id}
          initialData={{
            title: `Task from message by ${message.sender.displayName}`,
            description: message.content,
            linkedMessagePublicId: message.publicId
          }}
        />
      )}
    </motion.div>
  )
})
