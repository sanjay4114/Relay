import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { notificationApi, type Notification } from '../api/notification-api'
import { useEffect } from 'react'
import { wsService } from '@/shared/lib/websocket'

export function useNotifications() {
  const queryClient = useQueryClient()
  const queryKey = ['notifications']
  const countKey = ['notifications-unread-count']

  const { data: notifications = [], isLoading } = useQuery({
    queryKey,
    queryFn: () => notificationApi.getNotifications(50),
  })

  const { data: unreadCount = 0 } = useQuery({
    queryKey: countKey,
    queryFn: () => notificationApi.getUnreadCount(),
  })

  useEffect(() => {
    const subscription = wsService.subscribe(
      '/user/queue/notifications',
      (event: any) => {
        const { type, unreadCount: newUnreadCount } = event

        // Always update the unread count if provided
        if (newUnreadCount !== undefined) {
          queryClient.setQueryData(countKey, newUnreadCount)
        }

        queryClient.setQueryData(queryKey, (old: Notification[] = []) => {
          if (type === 'NOTIFICATION_CREATED') {
            return [event.notification, ...old]
          } else if (type === 'NOTIFICATION_READ') {
            return old.map(n => n.publicId === event.publicId ? { ...n, isRead: true } : n)
          } else if (type === 'NOTIFICATIONS_ALL_READ') {
            return old.map(n => ({ ...n, isRead: true }))
          } else if (type === 'NOTIFICATION_DELETED') {
            return old.filter(n => n.publicId !== event.publicId)
          }
          return old
        })
      }
    )

    return () => {
      subscription?.unsubscribe()
    }
  }, [queryClient, countKey, queryKey])

  const { mutate: markAsRead } = useMutation({
    mutationFn: (publicId: string) => notificationApi.markAsRead(publicId),
    onMutate: async (publicId) => {
      await queryClient.cancelQueries({ queryKey })
      const previous = queryClient.getQueryData<Notification[]>(queryKey)
      queryClient.setQueryData<Notification[]>(queryKey, old => 
        old?.map(n => n.publicId === publicId ? { ...n, isRead: true } : n)
      )
      return { previous }
    },
    onError: (err, newTodo, context) => {
      queryClient.setQueryData(queryKey, context?.previous)
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: countKey })
    }
  })

  const { mutate: markAllAsRead } = useMutation({
    mutationFn: () => notificationApi.markAllAsRead(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey })
      queryClient.invalidateQueries({ queryKey: countKey })
    }
  })

  const { mutate: deleteNotification } = useMutation({
    mutationFn: (publicId: string) => notificationApi.deleteNotification(publicId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey })
      queryClient.invalidateQueries({ queryKey: countKey })
    }
  })

  return {
    notifications,
    unreadCount,
    isLoading,
    markAsRead,
    markAllAsRead,
    deleteNotification
  }
}
