import { Moon, Sun, LogOut, Search, User as UserIcon, Settings } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import type { ReactNode } from 'react'
import { Button } from '@/shared/ui/button'
import { Avatar, AvatarFallback } from '@/shared/ui/avatar'
import { 
  DropdownMenu, 
  DropdownMenuContent, 
  DropdownMenuItem, 
  DropdownMenuLabel, 
  DropdownMenuSeparator, 
  DropdownMenuTrigger 
} from '@/shared/ui/dropdown-menu'
import { useTheme } from '@/app/providers/theme-provider'
import { logout } from '@/features/auth/api/auth-api'
import { clearAuth } from '@/shared/lib/auth-storage'
import { NotificationBell } from '@/features/notifications/components/notification-bell'
import { GlobalSearchDialog } from '@/features/search/components/GlobalSearchDialog'

interface AppHeaderProps {
  title: string
  description?: string
  mobileTrigger?: ReactNode
}

export function AppHeader({ title, description, mobileTrigger }: AppHeaderProps) {
  const { resolvedTheme, setTheme } = useTheme()
  const navigate = useNavigate()

  const handleLogout = async () => {
    try {
      await logout()
    } finally {
      clearAuth()
      navigate('/login')
    }
  }

  return (
    <header className="flex h-14 items-center justify-between border-b bg-background/80 px-4 md:px-6 backdrop-blur">
      <div className="flex items-center">
        {mobileTrigger}
        <div>
          <h1 className="text-lg font-semibold tracking-tight">{title}</h1>
          {description ? <p className="hidden md:block text-sm text-muted-foreground">{description}</p> : null}
        </div>
      </div>

      <div className="flex items-center gap-1 md:gap-2">
        <div className="hidden sm:block mr-2">
          <GlobalSearchDialog />
        </div>
        <div className="hidden sm:block">
          <NotificationBell />
        </div>
        <Button
          variant="ghost"
          size="icon"
          onClick={() => setTheme(resolvedTheme === 'dark' ? 'light' : 'dark')}
          aria-label="Toggle theme"
        >
          {resolvedTheme === 'dark' ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
        </Button>
        
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="relative h-8 w-8 rounded-full ml-2">
              <Avatar className="h-8 w-8">
                <AvatarFallback>U</AvatarFallback>
              </Avatar>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent className="w-56" align="end" forceMount>
            <DropdownMenuLabel className="font-normal">
              <div className="flex flex-col space-y-1">
                <p className="text-sm font-medium leading-none">User</p>
                <p className="text-xs leading-none text-muted-foreground">user@example.com</p>
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={() => navigate('/profile')}>
              <UserIcon className="mr-2 h-4 w-4" />
              <span>Profile</span>
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => navigate('/settings')}>
              <Settings className="mr-2 h-4 w-4" />
              <span>Settings</span>
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={handleLogout}>
              <LogOut className="mr-2 h-4 w-4" />
              <span>Log out</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}
