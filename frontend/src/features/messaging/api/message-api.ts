import { apiClient } from '@/shared/api/client'
import type { ApiResponse } from '@/shared/types/api'

export interface MessageSender {
  publicId: string
  displayName: string
  avatarUrl?: string
}

export interface Message {
  publicId: string
  channelPublicId: string
  content: string
  messageType: 'TEXT'
  sender: MessageSender
  createdAt: string
  updatedAt: string
  editedAt?: string
  deletedAt?: string
  parentMessageId?: string
  replyCount: number
  lastReplyAt?: string
  isPinned?: boolean
  reactions?: Record<string, string[]>
  attachments?: import('./file-api').FileAttachment[]
}

export const messageApi = {
  getChannelMessages: async (channelId: string, cursor?: string, limit = 50) => {
    const params = new URLSearchParams()
    params.append('limit', limit.toString())
    if (cursor) {
      params.append('cursor', cursor)
    }
    const { data } = await apiClient.get<ApiResponse<Message[]>>(`/channels/${channelId}/messages?${params.toString()}`)
    return data.data
  },
  getThreadMessages: async (channelId: string, messageId: string, cursor?: string, limit = 50) => {
    const params = new URLSearchParams()
    params.append('limit', limit.toString())
    if (cursor) {
      params.append('cursor', cursor)
    }
    const { data } = await apiClient.get<ApiResponse<Message[]>>(`/channels/${channelId}/messages/${messageId}/replies?${params.toString()}`)
    return data.data
  },
  getPinnedMessages: async (channelId: string) => {
    const { data } = await apiClient.get<ApiResponse<Message[]>>(`/channels/${channelId}/messages/pinned`)
    return data.data
  },
  getSavedMessages: async (channelId: string, cursor?: string, limit = 50) => {
    const params = new URLSearchParams()
    params.append('limit', limit.toString())
    if (cursor) {
      params.append('cursor', cursor)
    }
    const { data } = await apiClient.get<ApiResponse<Message[]>>(`/channels/${channelId}/messages/saved?${params.toString()}`)
    return data.data
  },
  editMessage: async (channelId: string, messageId: string, content: string) => {
    const { data } = await apiClient.patch<ApiResponse<Message>>(`/channels/${channelId}/messages/${messageId}`, { content })
    return data.data
  },
  deleteMessage: async (channelId: string, messageId: string) => {
    await apiClient.delete(`/channels/${channelId}/messages/${messageId}`)
  },
  restoreMessage: async (channelId: string, messageId: string) => {
    await apiClient.post(`/channels/${channelId}/messages/${messageId}/restore`)
  },
  addReaction: async (channelId: string, messageId: string, emoji: string) => {
    await apiClient.post(`/channels/${channelId}/messages/${messageId}/reactions?emoji=${encodeURIComponent(emoji)}`)
  },
  removeReaction: async (channelId: string, messageId: string, emoji: string) => {
    await apiClient.delete(`/channels/${channelId}/messages/${messageId}/reactions?emoji=${encodeURIComponent(emoji)}`)
  },
  pinMessage: async (channelId: string, messageId: string) => {
    await apiClient.post(`/channels/${channelId}/messages/${messageId}/pin`)
  },
  unpinMessage: async (channelId: string, messageId: string) => {
    await apiClient.delete(`/channels/${channelId}/messages/${messageId}/pin`)
  },
  saveMessage: async (channelId: string, messageId: string) => {
    await apiClient.post(`/channels/${channelId}/messages/${messageId}/save`)
  },
  unsaveMessage: async (channelId: string, messageId: string) => {
    await apiClient.delete(`/channels/${channelId}/messages/${messageId}/save`)
  },
  markMessageDelivered: async (channelId: string, messageId: string) => {
    await apiClient.post(`/channels/${channelId}/messages/${messageId}/delivered`)
  },
  markMessageRead: async (channelId: string, messageId: string) => {
    await apiClient.post(`/channels/${channelId}/messages/${messageId}/read`)
  },
  markChannelRead: async (channelId: string, lastMessageId: string) => {
    await apiClient.post(`/channels/${channelId}/messages/read-all?lastMessageId=${lastMessageId}`)
  },
  getUnreadCount: async (channelId: string): Promise<number> => {
    const { data } = await apiClient.get<ApiResponse<number>>(`/channels/${channelId}/messages/unread-count`)
    return data.data
  },
}
