import { TooltipProvider } from '@/shared/ui/tooltip'
import { AppProviders } from '@/app/providers/app-providers'
import { ThemeProvider } from '@/app/providers/theme-provider'
import type { ReactNode } from 'react'

export function Providers({ children }: { children: ReactNode }) {
  return (
    <ThemeProvider>
      <AppProviders>
        <TooltipProvider delayDuration={200}>{children}</TooltipProvider>
      </AppProviders>
    </ThemeProvider>
  )
}
