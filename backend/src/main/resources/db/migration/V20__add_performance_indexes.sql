-- Performance Indexes for high-throughput queries
-- These indexes optimize common query patterns without blocking queries

-- Messages pagination by channel
CREATE INDEX idx_messages_channel_created_desc ON messages(channel_id, created_at DESC);

-- Messages pagination by thread/parent  
CREATE INDEX idx_messages_parent_created_desc ON messages(parent_message_id, created_at DESC);

-- Tasks filtering by workspace and status
CREATE INDEX idx_tasks_workspace_status ON tasks(workspace_id, status);

-- Notifications by recipient (uses recipient_id, not user_id)
CREATE INDEX idx_notifications_recipient_created ON notifications(recipient_id, created_at DESC);

-- Dashboard timeline filtering by task activity
CREATE INDEX idx_task_activity_workspace ON task_activity(task_id, created_at DESC);
