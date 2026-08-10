import { createBrowserRouter, Navigate } from 'react-router-dom'
import { AppLayout } from '@/app/layout/app-layout'
import { AuthLayout } from '@/app/layout/auth-layout'
import { ProtectedRoute, PublicOnlyRoute, WorkspaceGuard } from '@/app/router/guards'
import { LoginPage } from '@/features/auth/pages/login-page'
import { RegisterPage } from '@/features/auth/pages/register-page'
import { ForgotPasswordPage } from '@/features/auth/pages/forgot-password-page'
import { ResetPasswordPage } from '@/features/auth/pages/reset-password-page'
import { DashboardPage } from '@/features/dashboard/pages/dashboard-page'
import { ChannelsDirectoryPage } from '@/features/messaging/pages/channels-directory-page'
import { ChannelDetailsPage } from '@/features/messaging/pages/channel-details-page'
import { MessagesPage } from '@/features/messaging/pages/messages-page'
import { TasksPage } from '@/features/tasks/pages/tasks-page'
import { DocsPage } from '@/features/docs/pages/docs-page'
import { SearchPage } from '@/features/search/pages/search-page'
import { NotificationsPage } from '@/features/notifications/pages/notifications-page'
import { SettingsPage } from '@/features/settings/pages/settings-page'
import { NotFoundPage } from '@/app/pages/not-found-page'
import { ErrorBoundaryPage } from '@/app/pages/error-boundary-page'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <ProtectedRoute />,
    errorElement: <ErrorBoundaryPage />,
    children: [
      {
        element: <WorkspaceGuard />,
        children: [
          {
            element: <AppLayout />,
            children: [
              { index: true, element: <Navigate to="/dashboard" replace /> },
              { path: 'dashboard', element: <DashboardPage /> },
              { path: 'channels', element: <ChannelsDirectoryPage /> },
              { path: 'channels/:channelId', element: <ChannelDetailsPage /> },
              { path: 'messages', element: <MessagesPage /> },
              { path: 'tasks', element: <TasksPage /> },
              { path: 'docs', element: <DocsPage /> },
              { path: 'search', element: <SearchPage /> },
              { path: 'notifications', element: <NotificationsPage /> },
              { path: 'settings', element: <SettingsPage /> },
            ],
          },
        ],
      },
    ],
  },
  {
    element: <PublicOnlyRoute />,
    errorElement: <ErrorBoundaryPage />,
    children: [
      {
        element: <AuthLayout />,
        children: [
          { path: 'login', element: <LoginPage /> },
          { path: 'register', element: <RegisterPage /> },
        ],
      },
    ],
  },
  {
    element: <AuthLayout />,
    children: [
      { path: 'forgot-password', element: <ForgotPasswordPage /> },
      { path: 'reset-password', element: <ResetPasswordPage /> },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
])
