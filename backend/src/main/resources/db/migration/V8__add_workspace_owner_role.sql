-- =============================================================================
-- V8: Add Workspace Owner Role and Soft Deletes
-- =============================================================================

-- 1. Alter Enums in workspace_members and invitations
ALTER TABLE workspace_members 
    MODIFY COLUMN workspace_role ENUM('OWNER', 'ADMIN', 'MEMBER') NOT NULL DEFAULT 'MEMBER';

ALTER TABLE invitations 
    MODIFY COLUMN workspace_role ENUM('OWNER', 'ADMIN', 'MEMBER') NOT NULL DEFAULT 'MEMBER';

-- 2. Add soft delete fields to workspaces
ALTER TABLE workspaces 
    ADD COLUMN deleted_at DATETIME(6) NULL,
    ADD COLUMN deleted_by BIGINT UNSIGNED NULL,
    ADD CONSTRAINT fk_workspaces_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX idx_workspaces_deleted_at ON workspaces (deleted_at);
