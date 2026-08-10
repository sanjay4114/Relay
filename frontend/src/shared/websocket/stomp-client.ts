import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getAccessToken } from '@/shared/lib/auth-storage'

const WS_URL = import.meta.env.VITE_WS_URL ?? 'http://localhost:8080/ws'

let stompClient: Client | null = null

export function getStompClient(): Client {
  if (!stompClient) {
    stompClient = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${getAccessToken() ?? ''}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: import.meta.env.DEV ? (msg) => console.debug('[STOMP]', msg) : () => undefined,
    })
  }
  return stompClient
}

export function disconnectStomp(): void {
  if (stompClient?.active) {
    stompClient.deactivate()
  }
  stompClient = null
}
