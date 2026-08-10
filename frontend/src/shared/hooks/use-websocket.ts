import { useEffect, useState } from 'react'
import { wsService } from '../lib/websocket'

export function useWebSocket() {
  const [isConnected, setIsConnected] = useState(false)
  const [error, setError] = useState<Error | null>(null)

  useEffect(() => {
    let mounted = true

    const connect = async () => {
      try {
        await wsService.connect()
        if (mounted) {
          setIsConnected(true)
          setError(null)
        }
      } catch (err) {
        if (mounted) {
          setError(err instanceof Error ? err : new Error('WebSocket connection failed'))
          setIsConnected(false)
        }
      }
    }

    connect()

    return () => {
      mounted = false
      // In a real app, you might not want to disconnect on every unmount 
      // if you want to keep the connection alive globally.
      // But for strict cleanup, we expose a way to disconnect or we leave it active globally.
      // We will let the global auth flow handle actual disconnects to preserve connection across pages.
    }
  }, [])

  return { isConnected, error }
}
