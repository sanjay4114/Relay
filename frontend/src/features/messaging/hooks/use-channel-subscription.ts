import { useEffect, useCallback } from 'react'
import { wsService } from '@/shared/lib/websocket'
import { useWebSocket } from '@/shared/hooks/use-websocket'

export interface TypingEvent {
  userPublicId: string
  channelPublicId: string
  isTyping: boolean
}

interface UseChannelSubscriptionProps {
  channelPublicId?: string
  onTypingEvent?: (event: TypingEvent) => void
  onMessageEvent?: (event: any) => void // Placeholder for phase 5
}

export function useChannelSubscription({ 
  channelPublicId, 
  onTypingEvent,
  onMessageEvent 
}: UseChannelSubscriptionProps) {
  const { isConnected } = useWebSocket()

  useEffect(() => {
    if (!isConnected || !channelPublicId) return

    // Subscribe to channel events (typing, messages, etc)
    const subscription = wsService.subscribe(
      `/topic/channels.${channelPublicId}.events`, 
      (event: any) => {
        // Basic routing based on event structure
        if (event.hasOwnProperty('isTyping') && onTypingEvent) {
          onTypingEvent(event as TypingEvent)
        } else if (onMessageEvent) {
          onMessageEvent(event)
        }
      }
    )

    return () => {
      subscription?.unsubscribe()
    }
  }, [isConnected, channelPublicId, onTypingEvent, onMessageEvent])

  const sendTypingStatus = useCallback((isTyping: boolean) => {
    if (!isConnected || !channelPublicId) return
    wsService.publish(`/app/channels.${channelPublicId}.typing`, { isTyping })
  }, [isConnected, channelPublicId])

  return { sendTypingStatus }
}
