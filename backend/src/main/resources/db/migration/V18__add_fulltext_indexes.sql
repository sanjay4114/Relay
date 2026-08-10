ALTER TABLE users ADD FULLTEXT INDEX ft_users_name_email (display_name, email);
ALTER TABLE workspaces ADD FULLTEXT INDEX ft_workspaces_name_desc (name, description);
ALTER TABLE channels ADD FULLTEXT INDEX ft_channels_name_desc (name, description);
ALTER TABLE messages ADD FULLTEXT INDEX ft_messages_content (content);
ALTER TABLE file_attachments ADD FULLTEXT INDEX ft_files_name (original_name);
