-- Performance Indexes for high-throughput queries

-- Messages pagination
CREATE INDEX idx_messages_channel_created ON messages(channel_id, created_at DESC);
CREATE INDEX idx_messages_parent_created ON messages(parent_message_id, created_at DESC);

-- Tasks filtering
CREATE INDEX idx_tasks_workspace_status ON tasks(workspace_id, status);

-- Notifications
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at DESC);

-- Dashboard Activity Feed optimization
CREATE INDEX idx_task_activity_workspace ON task_activity(task_id, created_at DESC);
CREATE INDEX idx_file_attachments_message ON file_attachments(message_id, created_at DESC);
