import { motion } from 'framer-motion'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { useWorkspaceStore } from '@/features/workspaces/store/workspace-store'
import { useDashboardStats, useDashboardTimeline } from '../hooks/use-dashboard'
import { useCurrentUser } from '@/features/auth/hooks/use-current-user'
import { Loader2, CheckSquare, Clock, AlertTriangle, MessageSquare, Hash, Users, Plus, Upload, UserPlus, Activity, FileText } from 'lucide-react'
import { Avatar, AvatarFallback, AvatarImage } from '@/shared/ui/avatar'
import { formatDistanceToNow } from 'date-fns'
import { useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { CreateTaskModal } from '@/features/tasks/components/CreateTaskModal'
import { CreateChannelModal } from '@/features/messaging/components/create-channel-modal'

import { Dialog, DialogContent } from '@/shared/ui/dialog'

export function DashboardPage() {
  const activeWorkspace = useWorkspaceStore(s => s.activeWorkspace)
  const { data: user } = useCurrentUser()
  const { data: stats, isLoading: statsLoading } = useDashboardStats(activeWorkspace?.publicId)
  const { data: timeline, isLoading: timelineLoading } = useDashboardTimeline(activeWorkspace?.publicId)
  const navigate = useNavigate()

  const [createTaskOpen, setCreateTaskOpen] = useState(false)
  const [createChannelOpen, setCreateChannelOpen] = useState(false)

  if (!activeWorkspace || !user) return null

  const renderIcon = (type: string) => {
    switch (type) {
      case 'MESSAGE': return <MessageSquare className="w-4 h-4 text-blue-500" />
      case 'TASK_UPDATE': return <CheckSquare className="w-4 h-4 text-emerald-500" />
      case 'FILE_UPLOAD': return <FileText className="w-4 h-4 text-orange-500" />
      default: return <Activity className="w-4 h-4 text-gray-500" />
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="space-y-6 max-w-6xl mx-auto pb-8"
    >
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 bg-gradient-to-r from-teal-900/20 to-transparent p-6 rounded-xl border border-teal-500/10">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Welcome back, {user.displayName}</h2>
          <p className="text-muted-foreground mt-1">Here's what's happening in <span className="font-semibold text-foreground">{activeWorkspace.name}</span></p>
        </div>
        <div className="flex gap-2">
          <Button onClick={() => setCreateTaskOpen(true)} className="bg-teal-600 hover:bg-teal-700 text-white shadow-md">
            <Plus className="w-4 h-4 mr-2" /> New Task
          </Button>
          <Button onClick={() => setCreateChannelOpen(true)} variant="outline" className="shadow-sm">
            <Hash className="w-4 h-4 mr-2" /> New Channel
          </Button>
        </div>
      </div>

      <div className="grid gap-4 grid-cols-2 md:grid-cols-3 lg:grid-cols-6">
        <Card className="col-span-1 shadow-sm border-slate-200 dark:border-slate-800">
          <CardHeader className="p-4 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground flex items-center justify-between">
              My Tasks <CheckSquare className="w-4 h-4 text-teal-500" />
            </CardTitle>
          </CardHeader>
          <CardContent className="p-4 pt-0">
            {statsLoading ? <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" /> : (
              <div className="text-2xl font-bold">{stats?.activeTasks || 0}</div>
            )}
          </CardContent>
        </Card>
        
        <Card className="col-span-1 shadow-sm border-slate-200 dark:border-slate-800">
          <CardHeader className="p-4 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground flex items-center justify-between">
              Due Today <Clock className="w-4 h-4 text-orange-500" />
            </CardTitle>
          </CardHeader>
          <CardContent className="p-4 pt-0">
            {statsLoading ? <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" /> : (
              <div className="text-2xl font-bold">{stats?.dueTodayTasks || 0}</div>
            )}
          </CardContent>
        </Card>

        <Card className="col-span-1 shadow-sm border-slate-200 dark:border-slate-800 bg-red-50/50 dark:bg-red-950/10 border-red-100 dark:border-red-900/30">
          <CardHeader className="p-4 pb-2">
            <CardTitle className="text-sm font-medium text-red-600 dark:text-red-400 flex items-center justify-between">
              Overdue <AlertTriangle className="w-4 h-4" />
            </CardTitle>
          </CardHeader>
          <CardContent className="p-4 pt-0">
            {statsLoading ? <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" /> : (
              <div className="text-2xl font-bold text-red-600 dark:text-red-400">{stats?.overdueTasks || 0}</div>
            )}
          </CardContent>
        </Card>

        <Card className="col-span-1 shadow-sm border-slate-200 dark:border-slate-800">
          <CardHeader className="p-4 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground flex items-center justify-between">
              Unread <MessageSquare className="w-4 h-4 text-blue-500" />
            </CardTitle>
          </CardHeader>
          <CardContent className="p-4 pt-0">
            {statsLoading ? <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" /> : (
              <div className="text-2xl font-bold">{stats?.unreadMessages || 0}</div>
            )}
          </CardContent>
        </Card>

        <Card className="col-span-1 shadow-sm border-slate-200 dark:border-slate-800">
          <CardHeader className="p-4 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground flex items-center justify-between">
              Channels <Hash className="w-4 h-4 text-indigo-500" />
            </CardTitle>
          </CardHeader>
          <CardContent className="p-4 pt-0">
            {statsLoading ? <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" /> : (
              <div className="text-2xl font-bold">{stats?.activeChannels || 0}</div>
            )}
          </CardContent>
        </Card>

        <Card className="col-span-1 shadow-sm border-slate-200 dark:border-slate-800">
          <CardHeader className="p-4 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground flex items-center justify-between">
              Online <Users className="w-4 h-4 text-green-500" />
            </CardTitle>
          </CardHeader>
          <CardContent className="p-4 pt-0">
            {statsLoading ? <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" /> : (
              <div className="text-2xl font-bold flex items-center gap-2">
                <div className="w-2.5 h-2.5 rounded-full bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.6)]"></div>
                {stats?.onlineMembers || 0}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        <Card className="md:col-span-2 shadow-sm border-slate-200 dark:border-slate-800 h-[500px] flex flex-col">
          <CardHeader>
            <CardTitle>Workspace Activity</CardTitle>
            <CardDescription>Unified timeline of messages, tasks, and files</CardDescription>
          </CardHeader>
          <CardContent className="flex-1 overflow-y-auto pr-2 custom-scrollbar">
            {timelineLoading ? (
              <div className="flex justify-center py-12"><Loader2 className="w-8 h-8 animate-spin text-teal-500" /></div>
            ) : timeline?.length === 0 ? (
              <div className="text-center py-12 text-muted-foreground">No recent activity</div>
            ) : (
              <div className="space-y-6 relative before:absolute before:inset-0 before:ml-5 before:-translate-x-px md:before:mx-auto md:before:translate-x-0 before:h-full before:w-0.5 before:bg-gradient-to-b before:from-transparent before:via-slate-200 dark:before:via-slate-800 before:to-transparent">
                {timeline?.map((item, i) => (
                  <div key={`${item.id}-${i}`} className="relative flex items-center justify-between md:justify-normal md:odd:flex-row-reverse group is-active">
                    <div className="flex items-center justify-center w-10 h-10 rounded-full border-4 border-background bg-slate-100 dark:bg-slate-800 text-slate-500 shrink-0 md:order-1 md:group-odd:-translate-x-1/2 md:group-even:translate-x-1/2 shadow-sm z-10">
                      {renderIcon(item.type)}
                    </div>
                    <div className="w-[calc(100%-4rem)] md:w-[calc(50%-2.5rem)] bg-white dark:bg-slate-900/50 border border-slate-200 dark:border-slate-800 p-4 rounded-xl shadow-sm hover:shadow-md transition-shadow">
                      <div className="flex items-center gap-2 mb-2">
                        <Avatar className="w-5 h-5">
                          <AvatarImage src={item.actorAvatar} />
                          <AvatarFallback className="text-[9px]">{item.actorName.charAt(0)}</AvatarFallback>
                        </Avatar>
                        <span className="font-semibold text-sm">{item.actorName}</span>
                        <span className="text-xs text-muted-foreground ml-auto">{formatDistanceToNow(new Date(item.timestamp), { addSuffix: true })}</span>
                      </div>
                      <h4 className="text-sm font-medium text-foreground mb-1">{item.title}</h4>
                      <p className="text-sm text-muted-foreground line-clamp-2">{item.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <div className="space-y-6">
          <Card className="shadow-sm border-slate-200 dark:border-slate-800">
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
            </CardHeader>
            <CardContent className="grid grid-cols-1 gap-2">
              <Button variant="outline" className="justify-start h-12" onClick={() => setCreateTaskOpen(true)}>
                <CheckSquare className="w-4 h-4 mr-3 text-teal-500" /> Create Task
              </Button>
              <Button variant="outline" className="justify-start h-12" onClick={() => navigate('/app/channels')}>
                <MessageSquare className="w-4 h-4 mr-3 text-blue-500" /> Browse Channels
              </Button>
              <Button variant="outline" className="justify-start h-12" onClick={() => setCreateChannelOpen(true)}>
                <Hash className="w-4 h-4 mr-3 text-indigo-500" /> New Channel
              </Button>
              <Button variant="outline" className="justify-start h-12" onClick={() => window.alert('Invite Member coming soon')}>
                <UserPlus className="w-4 h-4 mr-3 text-purple-500" /> Invite Member
              </Button>
              <Button variant="outline" className="justify-start h-12" onClick={() => window.alert('Upload coming soon')}>
                <Upload className="w-4 h-4 mr-3 text-orange-500" /> Upload File
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
      
      {createTaskOpen && <CreateTaskModal open={createTaskOpen} onOpenChange={setCreateTaskOpen} workspaceId={activeWorkspace.publicId} />}
      <Dialog open={createChannelOpen} onOpenChange={setCreateChannelOpen}>
        <DialogContent>
          <CreateChannelModal onSuccess={() => setCreateChannelOpen(false)} />
        </DialogContent>
      </Dialog>
    </motion.div>
  )
}
