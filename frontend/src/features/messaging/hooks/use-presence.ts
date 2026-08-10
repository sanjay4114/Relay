import { useEffect, useState } from 'react'
import { wsService } from '@/shared/lib/websocket'
import { useWebSocket } from '@/shared/hooks/use-websocket'

export interface PresenceEvent {
  userPublicId: string
  status: 'ONLINE' | 'OFFLINE'
  lastSeenAt: string | null
}

export function usePresence() {
  const { isConnected } = useWebSocket()
  const [onlineUsers, setOnlineUsers] = useState<Set<string>>(new Set())

  useEffect(() => {
    if (!isConnected) return

    const subscription = wsService.subscribe('/topic/presence', (event: PresenceEvent) => {
      setOnlineUsers(prev => {
        const next = new Set(prev)
        if (event.status === 'ONLINE') {
          next.add(event.userPublicId)
        } else {
          next.delete(event.userPublicId)
        }
        return next
      })
    })

    return () => {
      subscription?.unsubscribe()
    }
  }, [isConnected])

  const isUserOnline = (userPublicId: string) => onlineUsers.has(userPublicId)

  return { onlineUsers, isUserOnline }
}
