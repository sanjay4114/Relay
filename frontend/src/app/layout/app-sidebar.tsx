import { NavLink } from 'react-router-dom'
import {
  Bell,
  FileText,
  Hash,
  Home,
  LayoutDashboard,
  MessageSquare,
  Search,
  Settings,
  SquareKanban,
} from 'lucide-react'
import { cn } from '@/shared/lib/utils'
import { ScrollArea } from '@/shared/ui/scroll-area'
import { Separator } from '@/shared/ui/separator'
import { Badge } from '@/shared/ui/badge'
import { WorkspaceSwitcher } from '@/features/workspaces/components/workspace-switcher'
import { ChannelSidebarList } from '@/features/messaging/components/channel-sidebar-list'

const mainNav = [
  { to: '/dashboard', label: 'Home', icon: Home },
  { to: '/channels', label: 'Channels', icon: Hash },
  { to: '/messages', label: 'Messages', icon: MessageSquare },
  { to: '/tasks', label: 'Tasks', icon: SquareKanban },
  { to: '/docs', label: 'Docs', icon: FileText },
  { to: '/search', label: 'Search', icon: Search },
]

const secondaryNav = [
  { to: '/notifications', label: 'Notifications', icon: Bell, badge: '0' },
  { to: '/settings', label: 'Settings', icon: Settings },
]

export function AppSidebar({ onNavigate }: { onNavigate?: () => void }) {
  return (
    <aside className="flex h-full w-full flex-col text-sidebar-foreground">
      <div className="border-b border-sidebar-border p-2">
        <WorkspaceSwitcher />
      </div>

      <ScrollArea className="flex-1 px-2 py-3">
        <nav className="space-y-1">
          {mainNav.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={onNavigate}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                    : 'text-sidebar-foreground/80 hover:bg-sidebar-accent/60 hover:text-sidebar-accent-foreground',
                )
              }
            >
              <Icon className="h-4 w-4" />
              {label}
            </NavLink>
          ))}
        </nav>

        <ChannelSidebarList onNavigate={onNavigate} />

        <Separator className="my-3" />

        <nav className="space-y-1">
          {secondaryNav.map(({ to, label, icon: Icon, badge }) => (
            <NavLink
              key={to}
              to={to}
              onClick={onNavigate}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                    : 'text-sidebar-foreground/80 hover:bg-sidebar-accent/60 hover:text-sidebar-accent-foreground',
                )
              }
            >
              <Icon className="h-4 w-4" />
              <span className="flex-1">{label}</span>
              {badge ? (
                <Badge variant="secondary" className="h-5 min-w-5 justify-center px-1.5">
                  {badge}
                </Badge>
              ) : null}
            </NavLink>
          ))}
        </nav>
      </ScrollArea>
    </aside>
  )
}
