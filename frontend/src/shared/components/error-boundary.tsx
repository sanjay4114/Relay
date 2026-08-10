import { ErrorBoundary as ReactErrorBoundary, FallbackProps } from 'react-error-boundary'
import { ErrorState } from './error-state'

function ErrorFallback({ error, resetErrorBoundary }: FallbackProps) {
  return (
    <div className="flex h-full w-full items-center justify-center p-6">
      <ErrorState 
        title="Application Error"
        message={error.message || 'An unexpected error occurred in the application.'}
        onRetry={resetErrorBoundary}
      />
    </div>
  )
}

export function ErrorBoundary({ children }: { children: React.ReactNode }) {
  return (
    <ReactErrorBoundary FallbackComponent={ErrorFallback}>
      {children}
    </ReactErrorBoundary>
  )
}
