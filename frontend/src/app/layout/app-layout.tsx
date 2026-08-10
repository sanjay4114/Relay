import { Outlet } from 'react-router-dom'
import { AppHeader } from '@/app/layout/app-header'
import { AppSidebar } from '@/app/layout/app-sidebar'
import { Toaster } from '@/shared/ui/sonner'
import { Sheet, SheetContent, SheetTrigger } from '@/shared/ui/sheet'
import { Button } from '@/shared/ui/button'
import { Menu } from 'lucide-react'
import { useState } from 'react'
import { useWebSocket } from '@/shared/hooks/use-websocket'

interface AppLayoutProps {
  title?: string
  description?: string
}

export function AppLayout({ title = 'Relay', description }: AppLayoutProps) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  useWebSocket() // Initialize global websocket connection

  return (
    <div className="flex h-screen overflow-hidden bg-background">
      {/* Desktop Sidebar */}
      <div className="hidden md:flex h-full w-64 flex-col border-r border-sidebar-border bg-sidebar">
        <AppSidebar />
      </div>

      <div className="flex min-w-0 flex-1 flex-col">
        <AppHeader 
          title={title} 
          description={description} 
          mobileTrigger={
            <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
              <SheetTrigger asChild>
                <Button variant="ghost" size="icon" className="md:hidden mr-2">
                  <Menu className="h-5 w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent side="left" className="w-72 p-0">
                <AppSidebar onNavigate={() => setMobileMenuOpen(false)} />
              </SheetContent>
            </Sheet>
          }
        />
        <main className="flex-1 overflow-auto p-4 md:p-6">
          <Outlet />
        </main>
      </div>
      <Toaster />
    </div>
  )
}
