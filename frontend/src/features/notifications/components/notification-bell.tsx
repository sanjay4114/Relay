import { Bell, Check, Trash2, MessageSquare, AtSign, UserPlus, Hash, Paperclip, CheckSquare } from 'lucide-react'
import { Button } from '@/shared/ui/button'
import { Popover, PopoverContent, PopoverTrigger } from '@/shared/ui/popover'
import { useNotifications } from '../hooks/use-notifications'
import { Notification } from '../api/notification-api'
import { formatDistanceToNow } from 'date-fns'
import { ScrollArea } from '@/shared/ui/scroll-area'
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/ui/avatar'
import { Badge } from '@/shared/ui/badge'
import { useState } from 'react'

export function NotificationBell() {
  const { notifications, unreadCount, markAsRead, markAllAsRead, deleteNotification } = useNotifications()
  const [isOpen, setIsOpen] = useState(false)

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

  const handleNotificationClick = (notification: Notification) => {
    if (!notification.isRead) {
      markAsRead(notification.publicId)
    }
    setIsOpen(false)
    // Future: navigate to entity
  }

  return (
    <Popover open={isOpen} onOpenChange={setIsOpen}>
      <PopoverTrigger asChild>
        <Button variant="ghost" size="icon" className="relative h-9 w-9">
          <Bell className="h-5 w-5 text-muted-foreground" />
          {unreadCount > 0 && (
            <Badge 
              variant="destructive" 
              className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-[10px] rounded-full border-2 border-background"
            >
              {unreadCount > 99 ? '99+' : unreadCount}
            </Badge>
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-80 p-0 mr-4 mt-1" align="end">
        <div className="flex items-center justify-between p-4 border-b">
          <h4 className="font-semibold text-sm">Notifications</h4>
          {unreadCount > 0 && (
            <Button variant="ghost" size="sm" className="h-7 text-xs" onClick={() => markAllAsRead()}>
              Mark all as read
            </Button>
          )}
        </div>
        
        <ScrollArea className="h-[400px]">
          {notifications.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-40 text-muted-foreground">
              <Bell className="h-8 w-8 mb-2 opacity-20" />
              <p className="text-sm">No notifications</p>
            </div>
          ) : (
            <div className="flex flex-col">
              {notifications.map(notif => (
                <div 
                  key={notif.publicId} 
                  className={`p-3 border-b flex gap-3 hover:bg-muted/50 transition-colors cursor-pointer group ${!notif.isRead ? 'bg-primary/5' : ''}`}
                  onClick={() => handleNotificationClick(notif)}
                >
                  <div className="relative shrink-0">
                    <Avatar className="h-10 w-10 border">
                      {notif.actor?.avatarUrl ? <AvatarImage src={notif.actor.avatarUrl} /> : null}
                      <AvatarFallback>{notif.actor?.displayName?.[0]?.toUpperCase() || '?'}</AvatarFallback>
                    </Avatar>
                    <div className="absolute -bottom-1 -right-1 bg-background rounded-full p-0.5 border shadow-sm">
                      {getIcon(notif.type)}
                    </div>
                  </div>
                  
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium leading-tight">
                      {notif.title}
                    </p>
                    <p className="text-xs text-muted-foreground mt-1 line-clamp-2">
                      {notif.body}
                    </p>
                    <p className="text-[10px] text-muted-foreground mt-1.5 font-medium">
                      {formatDistanceToNow(new Date(notif.createdAt), { addSuffix: true })}
                    </p>
                  </div>
                  
                  <div className="shrink-0 flex flex-col items-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    {!notif.isRead && (
                      <div className="h-2 w-2 bg-primary rounded-full" />
                    )}
                    <Button 
                      variant="ghost" 
                      size="icon" 
                      className="h-6 w-6 text-muted-foreground hover:text-destructive"
                      onClick={(e) => {
                        e.stopPropagation();
                        deleteNotification(notif.publicId)
                      }}
                    >
                      <Trash2 className="h-3 w-3" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </ScrollArea>
      </PopoverContent>
    </Popover>
  )
}
