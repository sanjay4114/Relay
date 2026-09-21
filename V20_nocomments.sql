CREATE INDEX idx_messages_channel_created_desc ON messages(channel_id, created_at DESC);
CREATE INDEX idx_messages_parent_created_desc ON messages(parent_message_id, created_at DESC);
CREATE INDEX idx_tasks_workspace_status ON tasks(workspace_id, status);
CREATE INDEX idx_notifications_recipient_created ON notifications(recipient_id, created_at DESC);
CREATE INDEX idx_task_activity_workspace ON task_activity(task_id, created_at DESC);