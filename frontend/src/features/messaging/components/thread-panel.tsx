import { X } from 'lucide-react'
import { Button } from '@/shared/ui/button'
import type { Message } from '../api/message-api'
import { useThreadMessages } from '../hooks/use-thread-messages'
import { MessageList } from './message-list'
import { MessageComposer } from './message-composer'
import { MessageBubble } from './message-bubble'
import { ScrollArea } from '@/shared/ui/scroll-area'

interface ThreadPanelProps {
  channelPublicId: string
  parentMessage: Message
  onClose: () => void
}

export function ThreadPanel({ channelPublicId, parentMessage, onClose }: ThreadPanelProps) {
  const {
    messages,
    isLoading,
    hasNextPage,
    isFetchingNextPage,
    fetchNextPage,
    sendReply,
  } = useThreadMessages(channelPublicId, parentMessage.publicId)

  return (
    <div className="w-full md:w-[400px] lg:w-[450px] border-l bg-background flex flex-col h-full absolute md:relative right-0 top-0 bottom-0 z-20 shadow-xl md:shadow-none transition-all">
      {/* Header */}
      <div className="h-14 border-b flex items-center justify-between px-4 shrink-0 bg-background/80 backdrop-blur z-10">
        <div>
          <h2 className="font-semibold tracking-tight text-[15px]">Thread</h2>
          <p className="text-xs text-muted-foreground truncate max-w-[200px]">
            #{parentMessage.channelPublicId} {/* Normally we'd pass the channel name */}
          </p>
        </div>
        <Button variant="ghost" size="icon" onClick={onClose} className="h-8 w-8 shrink-0">
          <X className="h-4 w-4" />
        </Button>
      </div>

      {/* Main Scrollable Area */}
      <div className="flex-1 flex flex-col min-h-0 overflow-hidden relative">
        <div className="flex-1 overflow-y-auto">
          {/* Parent Message Context */}
          <div className="p-2 border-b border-muted/50 bg-muted/10">
            <MessageBubble message={parentMessage} isConsecutive={false} />
          </div>

          {/* Replies */}
          <div className="flex flex-col justify-end min-h-full">
             <MessageList 
                channelName="" 
                messages={messages}
                isLoading={isLoading}
                hasNextPage={!!hasNextPage}
                isFetchingNextPage={isFetchingNextPage}
                fetchNextPage={fetchNextPage}
                isThread={true}
             />
          </div>
        </div>
      </div>

      {/* Composer */}
      <div className="shrink-0 border-t bg-background p-2">
        <MessageComposer 
          channelName="thread" 
          onSend={(content, attachmentIds) => sendReply(content, attachmentIds)} 
        />
      </div>
    </div>
  )
}
