-- =============================================================================
-- V9: Update Channels Schema for Phase 4
-- =============================================================================

-- 1. Modify channels table
ALTER TABLE channels
    DROP FOREIGN KEY fk_channels_created_by,
    DROP INDEX uk_channels_workspace_name,
    DROP INDEX idx_channels_type,
    DROP INDEX idx_channels_archived;

ALTER TABLE channels
    CHANGE COLUMN channel_type visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    CHANGE COLUMN is_archived archived TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN slug VARCHAR(100) NULL AFTER name,
    ADD COLUMN deleted_at DATETIME(6) NULL,
    ADD COLUMN deleted_by BIGINT UNSIGNED NULL,
    CHANGE COLUMN created_by_user_id created_by BIGINT UNSIGNED NOT NULL;

-- Populate existing slugs (if any data exists)
UPDATE channels SET slug = CONCAT(LOWER(REPLACE(name, ' ', '-')), '-', id) WHERE slug IS NULL;

ALTER TABLE channels
    MODIFY COLUMN slug VARCHAR(100) NOT NULL,
    ADD UNIQUE KEY uk_channels_workspace_name (workspace_id, name),
    ADD UNIQUE KEY uk_channels_workspace_slug (workspace_id, slug),
    ADD KEY idx_channels_visibility (visibility),
    ADD KEY idx_channels_archived (archived),
    ADD KEY idx_channels_deleted_at (deleted_at),
    ADD CONSTRAINT fk_channels_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_channels_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL;

-- 2. Modify channel_members table
ALTER TABLE channel_members
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' AFTER joined_at;
