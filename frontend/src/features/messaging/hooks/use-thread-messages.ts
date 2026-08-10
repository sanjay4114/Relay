import { useInfiniteQuery, useQueryClient } from '@tanstack/react-query'
import { messageApi, Message } from '../api/message-api'
import { useEffect, useCallback } from 'react'
import { wsService } from '@/shared/lib/websocket'

export function useThreadMessages(channelPublicId: string, parentMessageId: string | null) {
  const queryClient = useQueryClient()
  const queryKey = ['thread-messages', channelPublicId, parentMessageId]

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError,
  } = useInfiniteQuery({
    queryKey,
    queryFn: ({ pageParam }) => messageApi.getThreadMessages(channelPublicId, parentMessageId!, pageParam as string | undefined),
    getNextPageParam: (lastPage) => {
      if (lastPage.length < 50) return undefined
      return lastPage[lastPage.length - 1].createdAt
    },
    initialPageParam: undefined as string | undefined,
    enabled: !!channelPublicId && !!parentMessageId,
    refetchOnWindowFocus: false, 
  })

  // Extract all messages into a flat array, reversed so oldest is top
  const messages = data?.pages.flatMap(page => page).reverse() || []

  // Setup Real-time listener for the thread
  // The main useMessages hook listens to /topic/channels.{id}.messages
  // If we receive a MESSAGE_CREATED for a reply, we should append it here as well.
  useEffect(() => {
    if (!channelPublicId || !parentMessageId) return

    const subscription = wsService.subscribe(
      `/topic/channels.${channelPublicId}.messages`,
      (event: { type: string, message: Message }) => {
        const { type, message: incomingMessage } = event

        // Only handle messages for THIS thread
        if (incomingMessage.parentMessageId !== parentMessageId && incomingMessage.publicId !== parentMessageId) {
          return
        }

        queryClient.setQueryData(queryKey, (oldData: any) => {
          if (!oldData) return oldData
          
          const newPages = [...oldData.pages]
          
          if (type === 'MESSAGE_CREATED' && incomingMessage.parentMessageId === parentMessageId) {
            newPages[0] = [incomingMessage, ...newPages[0]]
          } else if (['MESSAGE_UPDATED', 'MESSAGE_DELETED', 'MESSAGE_RESTORED', 'REACTION_ADDED', 'REACTION_REMOVED', 'MESSAGE_PINNED', 'MESSAGE_UNPINNED', 'MENTION_CREATED'].includes(type)) {
            // Find and replace the message in the pages (either a reply or the parent itself, though parent is usually not in this list, just in case)
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
  }, [channelPublicId, parentMessageId, queryClient, queryKey])

  // Sending a reply
  const sendReply = useCallback((content: string, attachmentIds?: string[]) => {
    if (!parentMessageId) return
    wsService.publish(`/app/channels.${channelPublicId}.send`, { 
      content,
      parentMessageId,
      attachmentIds
    })
  }, [channelPublicId, parentMessageId])

  return {
    messages,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
    isError,
    sendReply,
  }
}
