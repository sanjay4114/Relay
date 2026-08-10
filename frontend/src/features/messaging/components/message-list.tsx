import { useEffect, useRef, useState } from 'react'
import { Message } from '../api/message-api'
import { MessageBubble } from './message-bubble'
import { Hash } from 'lucide-react'

interface MessageListProps {
  channelName: string
  channelDescription?: string
  messages: Message[]
  isLoading: boolean
  hasNextPage: boolean
  isFetchingNextPage: boolean
  fetchNextPage: () => void
  isThread?: boolean
  onOpenThread?: (messageId: string) => void
}

export function MessageList({
  channelName,
  channelDescription,
  messages,
  isLoading,
  hasNextPage,
  isFetchingNextPage,
  fetchNextPage,
  isThread,
  onOpenThread
}: MessageListProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const [shouldAutoScroll, setShouldAutoScroll] = useState(true)

  // Auto-scroll to bottom on new messages if we are already at the bottom
  useEffect(() => {
    if (shouldAutoScroll && containerRef.current) {
      containerRef.current.scrollTop = containerRef.current.scrollHeight
    }
  }, [messages, shouldAutoScroll])

  // Handle scroll events for infinite loading and auto-scroll logic
  const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
    const target = e.currentTarget
    
    // Determine if we are near the bottom to enable auto-scroll
    const isNearBottom = target.scrollHeight - target.scrollTop - target.clientHeight < 50
    setShouldAutoScroll(isNearBottom)

    // Load more when reaching the top
    if (target.scrollTop < 100 && hasNextPage && !isFetchingNextPage) {
      // Save current scroll height to restore scroll position after load
      const currentScrollHeight = target.scrollHeight
      
      fetchNextPage()
      
      // We'd ideally wait for the load to finish to restore scroll position.
      // A simple timeout or layout effect can handle it.
      setTimeout(() => {
        if (containerRef.current) {
          const newScrollHeight = containerRef.current.scrollHeight
          containerRef.current.scrollTop = newScrollHeight - currentScrollHeight
        }
      }, 50)
    }
  }

  if (isLoading) {
    return (
      <div className="flex-1 flex flex-col justify-end p-4 opacity-50">
        <div className="space-y-6 max-w-4xl mx-auto w-full">
          {[1, 2, 3].map(i => (
            <div key={i} className="flex gap-4">
              <div className="w-10 h-10 rounded-md bg-muted animate-pulse shrink-0" />
              <div className="space-y-2 flex-1">
                <div className="h-4 w-32 bg-muted animate-pulse rounded" />
                <div className="h-4 w-3/4 bg-muted animate-pulse rounded" />
              </div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div 
      ref={containerRef}
      onScroll={handleScroll}
      className="flex-1 overflow-y-auto"
    >
      <div className="min-h-full flex flex-col justify-end">
        <div className="max-w-4xl mx-auto w-full pt-8 pb-4">
          
          {/* Channel Intro Header */}
          {!hasNextPage && !isThread && (
            <div className="px-4 pb-8 mb-4 border-b border-muted/50">
              <div className="h-16 w-16 rounded-2xl bg-primary/10 flex items-center justify-center mb-6">
                <Hash className="h-8 w-8 text-primary" />
              </div>
              <h1 className="text-3xl font-bold tracking-tight mb-2">Welcome to #{channelName}!</h1>
              <p className="text-muted-foreground text-[15px]">
                This is the start of the <strong className="text-foreground">#{channelName}</strong> channel. 
                {channelDescription ? ` ${channelDescription}` : ''}
              </p>
            </div>
          )}

          {isFetchingNextPage && (
            <div className="text-center py-4 text-xs text-muted-foreground animate-pulse">
              Loading older messages...
            </div>
          )}

          {/* Messages */}
          <div className="flex flex-col pb-2">
            {messages.map((message, i) => {
              const previousMessage = i > 0 ? messages[i - 1] : null
              
              // Group consecutive messages from the same sender within 5 minutes
              const isConsecutive = previousMessage && 
                previousMessage.sender.publicId === message.sender.publicId &&
                (new Date(message.createdAt).getTime() - new Date(previousMessage.createdAt).getTime()) < 5 * 60 * 1000

              return (
                <MessageBubble 
                  key={message.publicId} 
                  message={message} 
                  isConsecutive={!!isConsecutive}
                  onOpenThread={onOpenThread}
                />
              )
            })}
          </div>
        </div>
      </div>
    </div>
  )
}
