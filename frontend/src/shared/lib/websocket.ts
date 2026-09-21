import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { getAccessToken } from './auth-storage'

/** STOMP/SockJS endpoint (Spring registers `/api/ws`). */
export function resolveWebSocketUrl(): string {
  const configured = import.meta.env.VITE_WS_URL
  if (typeof configured === 'string' && configured.trim() !== '') {
    return configured.trim()
  }
  if (import.meta.env.PROD) {
    return '/api/ws'
  }
  return 'http://localhost:8080/api/ws'
}

export class WebSocketService {
  private static instance: WebSocketService
  private client: Client | null = null
  private connectionPromise: Promise<void> | null = null

  private constructor() {}

  public static getInstance(): WebSocketService {
    if (!WebSocketService.instance) {
      WebSocketService.instance = new WebSocketService()
    }
    return WebSocketService.instance
  }

  public connect(): Promise<void> {
    if (this.connectionPromise) {
      return this.connectionPromise
    }

    this.connectionPromise = new Promise((resolve, reject) => {
      const token = getAccessToken()
      if (!token) {
        reject(new Error('No auth token available'))
        this.connectionPromise = null
        return
      }

      this.client = new Client({
        webSocketFactory: () => new SockJS(resolveWebSocketUrl()),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: (str) => {
          if (import.meta.env.DEV) {
            console.log(str)
          }
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          resolve()
        },
        onStompError: (frame) => {
          console.error('Broker reported error: ' + frame.headers['message'])
          console.error('Additional details: ' + frame.body)
          reject(new Error(frame.headers['message']))
        },
        onWebSocketError: (event) => {
          console.error('WebSocket error: ', event)
        },
      })

      this.client.activate()
    })

    return this.connectionPromise
  }

  public disconnect(): void {
    if (this.client) {
      this.client.deactivate()
      this.client = null
      this.connectionPromise = null
    }
  }

  public subscribe(
    destination: string,
    callback: (message: any) => void
  ): StompSubscription | null {
    if (!this.client || !this.client.connected) {
      console.warn('Cannot subscribe. WebSocket is not connected.')
      return null
    }

    return this.client.subscribe(destination, (message: IMessage) => {
      try {
        const body = JSON.parse(message.body)
        callback(body)
      } catch (e) {
        callback(message.body)
      }
    })
  }

  public publish(destination: string, body: any): void {
    if (!this.client || !this.client.connected) {
      console.warn('Cannot publish. WebSocket is not connected.')
      return
    }

    this.client.publish({
      destination,
      body: typeof body === 'string' ? body : JSON.stringify(body),
    })
  }

  public isConnected(): boolean {
    return !!this.client?.connected
  }
}

export const wsService = WebSocketService.getInstance()
