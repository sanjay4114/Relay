import { useInfiniteQuery, useQueryClient } from '@tanstack/react-query'
import { messageApi, type Message } from '../api/message-api'
import { useEffect, useCallback } from 'react'
import { useChannelSubscription } from './use-channel-subscription'
import { wsService } from '@/shared/lib/websocket'

export function useMessages(channelPublicId: string) {
  const queryClient = useQueryClient()
  const queryKey = ['messages', channelPublicId]

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError,
  } = useInfiniteQuery({
    queryKey,
    queryFn: ({ pageParam }) => messageApi.getChannelMessages(channelPublicId, pageParam as string | undefined),
    getNextPageParam: (lastPage) => {
      if (lastPage.length < 50) return undefined
      return lastPage[lastPage.length - 1].createdAt
    },
    initialPageParam: undefined as string | undefined,
    enabled: !!channelPublicId,
    // Refetch on window focus is often bad for chat apps as it causes jumps, but standard react-query behavior is ok for now.
    refetchOnWindowFocus: false, 
  })

  // Extract all messages into a flat array, reversed so oldest is top
  const messages = data?.pages.flatMap(page => page).reverse() || []

  // Setup Real-time listener
  useEffect(() => {
    if (!channelPublicId) return

    const subscription = wsService.subscribe(
      `/topic/channels.${channelPublicId}.messages`,
      (event: { type: string, message: Message }) => {
        const { type, message: incomingMessage } = event

        queryClient.setQueryData(queryKey, (oldData: any) => {
          if (!oldData) return oldData
          
          const newPages = [...oldData.pages]
          
          if (type === 'MESSAGE_CREATED' && !incomingMessage.parentMessageId) {
            newPages[0] = [incomingMessage, ...newPages[0]]
          } else if (['MESSAGE_UPDATED', 'MESSAGE_DELETED', 'MESSAGE_RESTORED', 'THREAD_REPLY_CREATED', 'REACTION_ADDED', 'REACTION_REMOVED', 'MESSAGE_PINNED', 'MESSAGE_UNPINNED', 'MENTION_CREATED'].includes(type)) {
            // Find and replace the message in the pages
            for (let i = 0; i < newPages.length; i++) {
              const msgIndex = newPages[i].findIndex((m: Message) => m.publicId === incomingMessage.publicId)
              if (msgIndex !== -1) {
                const pageCopy = [...newPages[i]]
                pageCopy[msgIndex] = incomingMessage
                newPages[i] = pageCopy
                break
              }
            }
          }
          
          return {
            ...oldData,
            pages: newPages,
          }
        })
      }
    )

    return () => {
      subscription?.unsubscribe()
    }
  }, [channelPublicId, queryClient, queryKey])

  // Sending a message
  const sendMessage = useCallback((content: string, attachmentIds?: string[]) => {
    wsService.publish(`/app/channels.${channelPublicId}.send`, { content, attachmentIds })
  }, [channelPublicId])

  return {
    messages,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError,
    sendMessage,
  }
}
