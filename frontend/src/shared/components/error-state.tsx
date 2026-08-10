import { AlertTriangle } from 'lucide-react'
import { Button } from '@/shared/ui/button'

interface ErrorStateProps {
  title?: string
  message?: string
  onRetry?: () => void
}

export function ErrorState({ 
  title = "Something went wrong", 
  message = "An error occurred while loading this content.", 
  onRetry 
}: ErrorStateProps) {
  return (
    <div className="flex flex-col items-center justify-center p-8 text-center border rounded-lg border-destructive/20 bg-destructive/5 animate-in fade-in-50">
      <AlertTriangle className="mb-4 h-10 w-10 text-destructive" />
      <h3 className="mb-2 text-lg font-semibold text-destructive">{title}</h3>
      <p className="mb-4 max-w-sm text-sm text-destructive/80">{message}</p>
      {onRetry && (
        <Button variant="outline" onClick={onRetry}>
          Try Again
        </Button>
      )}
    </div>
  )
}
