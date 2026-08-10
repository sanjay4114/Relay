-- =============================================================================
-- V7: Platform (cross-cutting)
-- Files, entity links, notifications, audit log, search index
-- =============================================================================

CREATE TABLE files (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    public_id           CHAR(36)        NOT NULL,
    workspace_id        BIGINT UNSIGNED NOT NULL,
    uploaded_by_user_id BIGINT UNSIGNED NULL,
    filename            VARCHAR(255)    NOT NULL,
    mime_type           VARCHAR(127)    NOT NULL,
    size_bytes          BIGINT UNSIGNED NOT NULL,
    storage_key         VARCHAR(512)    NOT NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_files_public_id (public_id),
    KEY idx_files_workspace_id (workspace_id),
    KEY idx_files_uploaded_by (uploaded_by_user_id),
    CONSTRAINT fk_files_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_files_uploaded_by
        FOREIGN KEY (uploaded_by_user_id) REFERENCES users (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE file_attachments (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    file_id         BIGINT UNSIGNED NOT NULL,
    attachable_type ENUM('MESSAGE', 'TASK_COMMENT', 'PAGE') NOT NULL,
    attachable_id   BIGINT UNSIGNED NOT NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_attachments_unique (file_id, attachable_type, attachable_id),
    KEY idx_file_attachments_target (attachable_type, attachable_id),
    CONSTRAINT fk_file_attachments_file
        FOREIGN KEY (file_id) REFERENCES files (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE entity_links (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    workspace_id        BIGINT UNSIGNED NOT NULL,
    source_type         ENUM('MESSAGE', 'TASK', 'PAGE') NOT NULL,
    source_id           BIGINT UNSIGNED NOT NULL,
    target_type         ENUM('MESSAGE', 'TASK', 'PAGE') NOT NULL,
    target_id           BIGINT UNSIGNED NOT NULL,
    created_by_user_id  BIGINT UNSIGNED NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_entity_links_pair (
        workspace_id, source_type, source_id, target_type, target_id
    ),
    KEY idx_entity_links_source (workspace_id, source_type, source_id),
    KEY idx_entity_links_target (workspace_id, target_type, target_id),
    CONSTRAINT fk_entity_links_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_entity_links_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id         BIGINT UNSIGNED NOT NULL,
    workspace_id    BIGINT UNSIGNED NOT NULL,
    notification_type ENUM('MENTION', 'TASK_ASSIGNED', 'TASK_COMMENT', 'PAGE_COMMENT', 'SYSTEM') NOT NULL,
    title           VARCHAR(255)    NOT NULL,
    body            TEXT            NULL,
    entity_type     ENUM('MESSAGE', 'TASK', 'PAGE', 'CHANNEL') NOT NULL,
    entity_id       BIGINT UNSIGNED NOT NULL,
    actor_user_id   BIGINT UNSIGNED NULL,
    is_read         TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_notifications_user_unread (user_id, is_read, created_at),
    KEY idx_notifications_workspace (workspace_id),
    KEY idx_notifications_entity (entity_type, entity_id),
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_notifications_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_notifications_actor
        FOREIGN KEY (actor_user_id) REFERENCES users (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE audit_events (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    organization_id BIGINT UNSIGNED NOT NULL,
    workspace_id    BIGINT UNSIGNED NULL,
    actor_user_id   BIGINT UNSIGNED NULL,
    action          VARCHAR(100)    NOT NULL,
    entity_type     VARCHAR(50)     NULL,
    entity_id       BIGINT UNSIGNED NULL,
    metadata        JSON            NULL,
    ip_address      VARCHAR(45)     NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_audit_events_org_created (organization_id, created_at),
    KEY idx_audit_events_workspace (workspace_id, created_at),
    KEY idx_audit_events_actor (actor_user_id),
    KEY idx_audit_events_action (action),
    CONSTRAINT fk_audit_events_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_audit_events_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_audit_events_actor
        FOREIGN KEY (actor_user_id) REFERENCES users (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE search_documents (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    workspace_id    BIGINT UNSIGNED NOT NULL,
    entity_type     ENUM('MESSAGE', 'TASK', 'PAGE', 'USER') NOT NULL,
    entity_id       BIGINT UNSIGNED NOT NULL,
    title           VARCHAR(500)    NOT NULL DEFAULT '',
    body            TEXT            NULL,
    url_path        VARCHAR(512)    NOT NULL,
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_search_documents_entity (workspace_id, entity_type, entity_id),
    KEY idx_search_documents_workspace (workspace_id),
    FULLTEXT KEY ft_search_documents (title, body),
    CONSTRAINT fk_search_documents_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Remove scaffold placeholder table from V1
DROP TABLE IF EXISTS relay_schema_info;
