import { useNotifications } from '../hooks/use-notifications'
import { formatDistanceToNow, isToday, isYesterday } from 'date-fns'
import { Bell, Check, Trash2, MessageSquare, AtSign, UserPlus, Hash, Paperclip, CheckSquare } from 'lucide-react'
import { Button } from '@/shared/ui/button'
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/ui/avatar'
import type { Notification } from '../api/notification-api'
import { Loader2 } from 'lucide-react'

export function NotificationsPage() {
  const { notifications, isLoading, markAsRead, markAllAsRead, deleteNotification } = useNotifications()

  const getIcon = (type: string) => {
    switch (type) {
      case 'MENTION': return <AtSign className="h-4 w-4 text-blue-500" />
      case 'THREAD_REPLY': return <MessageSquare className="h-4 w-4 text-indigo-500" />
      case 'MESSAGE_REACTION': return <Check className="h-4 w-4 text-emerald-500" />
      case 'WORKSPACE_INVITE': return <UserPlus className="h-4 w-4 text-amber-500" />
      case 'CHANNEL_INVITE': return <Hash className="h-4 w-4 text-amber-500" />
      case 'FILE_UPLOAD': return <Paperclip className="h-4 w-4 text-sky-500" />
      case 'TASK_ASSIGNMENT': return <CheckSquare className="h-4 w-4 text-purple-500" />
      default: return <Bell className="h-4 w-4 text-muted-foreground" />
    }
  }

  // Group notifications by date
  const grouped = notifications.reduce((acc, notif) => {
    const date = new Date(notif.createdAt)
    let group = 'Earlier'
    if (isToday(date)) group = 'Today'
    else if (isYesterday(date)) group = 'Yesterday'

    if (!acc[group]) acc[group] = []
    acc[group].push(notif)
    return acc
  }, {} as Record<string, Notification[]>)

  const groupOrder = ['Today', 'Yesterday', 'Earlier']

  return (
    <div className="max-w-3xl mx-auto py-6 px-4">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Notifications</h1>
          <p className="text-muted-foreground">Stay updated on everything happening in your workspaces.</p>
        </div>
        {notifications.some(n => !n.isRead) && (
          <Button onClick={() => markAllAsRead()}>Mark all as read</Button>
        )}
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      ) : notifications.length === 0 ? (
        <div className="flex flex-col items-center justify-center h-64 text-muted-foreground bg-muted/20 rounded-xl border border-dashed">
          <Bell className="h-12 w-12 mb-4 opacity-20" />
          <p className="text-lg font-medium">You're all caught up!</p>
          <p className="text-sm">No new notifications right now.</p>
        </div>
      ) : (
        <div className="space-y-8">
          {groupOrder.map(group => {
            if (!grouped[group] || grouped[group].length === 0) return null
            
            return (
              <div key={group}>
                <h3 className="font-semibold text-sm text-muted-foreground uppercase tracking-wider mb-3 px-2">
                  {group}
                </h3>
                <div className="bg-background rounded-xl border shadow-sm overflow-hidden">
                  {grouped[group].map((notif, i) => (
                    <div 
                      key={notif.publicId}
                      className={`p-4 flex gap-4 transition-colors group relative ${!notif.isRead ? 'bg-primary/5' : 'hover:bg-muted/30'} ${i !== grouped[group].length - 1 ? 'border-b' : ''}`}
                    >
                      <div className="relative shrink-0 mt-1">
                        <Avatar className="h-12 w-12 border">
                          {notif.actor?.avatarUrl ? <AvatarImage src={notif.actor.avatarUrl} /> : null}
                          <AvatarFallback>{notif.actor?.displayName?.[0]?.toUpperCase() || '?'}</AvatarFallback>
                        </Avatar>
                        <div className="absolute -bottom-1 -right-1 bg-background rounded-full p-1 border shadow-sm">
                          {getIcon(notif.type)}
                        </div>
                      </div>
                      
                      <div className="flex-1 min-w-0">
                        <div className="flex justify-between items-start mb-1">
                          <p className="text-sm font-semibold pr-8">{notif.title}</p>
                          <span className="text-xs text-muted-foreground whitespace-nowrap">
                            {formatDistanceToNow(new Date(notif.createdAt), { addSuffix: true })}
                          </span>
                        </div>
                        <p className="text-sm text-muted-foreground">
                          {notif.body}
                        </p>
                      </div>
                      
                      <div className="absolute right-4 top-1/2 -translate-y-1/2 flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                        {!notif.isRead && (
                          <Button size="sm" variant="outline" className="h-8" onClick={() => markAsRead(notif.publicId)}>
                            Mark read
                          </Button>
                        )}
                        <Button size="icon" variant="ghost" className="h-8 w-8 text-muted-foreground hover:text-destructive" onClick={() => deleteNotification(notif.publicId)}>
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
