import { useRouteError, useNavigate } from 'react-router-dom';
import { Button } from '@/shared/ui/button';
import { AlertTriangle, Home, RefreshCw } from 'lucide-react';
import { motion } from 'framer-motion';

export function ErrorBoundaryPage() {
  const error = useRouteError() as any;
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <motion.div 
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        className="max-w-md w-full bg-card p-8 rounded-xl shadow-lg border border-border text-center space-y-6"
      >
        <div className="mx-auto w-16 h-16 bg-red-100 dark:bg-red-900/20 rounded-full flex items-center justify-center">
          <AlertTriangle className="w-8 h-8 text-red-600 dark:text-red-500" />
        </div>
        
        <div className="space-y-2">
          <h1 className="text-2xl font-bold tracking-tight">Something went wrong</h1>
          <p className="text-muted-foreground text-sm">
            An unexpected error occurred in the application. Our team has been notified.
          </p>
        </div>

        {error?.message && (
          <div className="bg-muted p-4 rounded-lg overflow-x-auto text-left">
            <code className="text-xs text-red-600 dark:text-red-400 font-mono">
              {error.message}
            </code>
          </div>
        )}

        <div className="flex flex-col sm:flex-row gap-3 justify-center pt-2">
          <Button onClick={() => window.location.reload()} variant="default" className="w-full sm:w-auto">
            <RefreshCw className="w-4 h-4 mr-2" /> Reload Page
          </Button>
          <Button onClick={() => navigate('/')} variant="outline" className="w-full sm:w-auto">
            <Home className="w-4 h-4 mr-2" /> Go Home
          </Button>
        </div>
      </motion.div>
    </div>
  );
}
