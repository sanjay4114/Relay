import { Channel } from '../api/channel-api'
import { useMessages } from '../hooks/use-messages'
import { useChannelSubscription } from '../hooks/use-channel-subscription'
import { MessageList } from './message-list'
import { MessageComposer } from './message-composer'
import { ThreadPanel } from './thread-panel'
import { useState, useCallback } from 'react'
import { Message } from '../api/message-api'

interface ChatWindowProps {
  channel: Channel
}

export function ChatWindow({ channel }: ChatWindowProps) {
  const {
    messages,
    isLoading,
    hasNextPage,
    isFetchingNextPage,
    fetchNextPage,
    sendMessage,
  } = useMessages(channel.publicId)

  const [typingUsers, setTypingUsers] = useState<Set<string>>(new Set())

  const { sendTypingStatus } = useChannelSubscription({
    channelPublicId: channel.publicId,
    onTypingEvent: (event) => {
      setTypingUsers(prev => {
        const next = new Set(prev)
        if (event.isTyping) next.add(event.userPublicId)
        else next.delete(event.userPublicId)
        return next
      })
    }
  })

  const [activeThreadId, setActiveThreadId] = useState<string | null>(null)
  
  const handleOpenThread = useCallback((messageId: string) => {
    setActiveThreadId(messageId)
  }, [])
  
  const activeThreadMessage = messages.find(m => m.publicId === activeThreadId) || null

  const handleSend = (content: string, attachmentIds?: string[]) => {
    sendMessage(content, attachmentIds)
    sendTypingStatus(false)
  }

  return (
    <div className="flex-1 flex overflow-hidden relative">
      <main className="flex-1 flex flex-col bg-background overflow-hidden relative">
        <MessageList 
          channelName={channel.name}
          channelDescription={channel.description}
          messages={messages}
          isLoading={isLoading}
          hasNextPage={!!hasNextPage}
          isFetchingNextPage={isFetchingNextPage}
          fetchNextPage={fetchNextPage}
          onOpenThread={handleOpenThread}
        />
        
        {/* Typing Indicator */}
      {typingUsers.size > 0 && (
        <div className="absolute bottom-[80px] left-8 text-xs text-muted-foreground/70 animate-pulse">
          {typingUsers.size === 1 ? 'Someone is typing...' : 'Multiple people are typing...'}
        </div>
      )}

      {/* Composer */}
      {channel.archived ? (
        <div className="p-4 bg-muted/20 text-center border-t border-muted/50">
          <p className="text-sm text-muted-foreground">This channel is archived and read-only.</p>
        </div>
      ) : (
        <MessageComposer 
          channelName={channel.name}
          onSend={handleSend}
        />
      )}
      </main>

      {/* Thread Panel */}
      {activeThreadMessage && (
        <ThreadPanel 
          channelPublicId={channel.publicId} 
          parentMessage={activeThreadMessage} 
          onClose={() => setActiveThreadId(null)} 
        />
      )}
    </div>
  )
}
