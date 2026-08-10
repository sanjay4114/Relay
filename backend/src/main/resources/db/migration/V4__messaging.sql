-- =============================================================================
-- V4: Messaging
-- Channels, messages, threads, reactions, mentions, pins
-- =============================================================================

CREATE TABLE channels (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    public_id           CHAR(36)        NOT NULL,
    workspace_id        BIGINT UNSIGNED NOT NULL,
    name                VARCHAR(80)     NOT NULL,
    description         TEXT            NULL,
    channel_type        ENUM('PUBLIC', 'PRIVATE', 'DM') NOT NULL DEFAULT 'PUBLIC',
    created_by_user_id  BIGINT UNSIGNED NULL,
    is_archived         TINYINT(1)      NOT NULL DEFAULT 0,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_channels_public_id (public_id),
    UNIQUE KEY uk_channels_workspace_name (workspace_id, name),
    KEY idx_channels_workspace_id (workspace_id),
    KEY idx_channels_type (channel_type),
    KEY idx_channels_archived (is_archived),
    CONSTRAINT fk_channels_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_channels_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE channel_members (
    channel_id      BIGINT UNSIGNED NOT NULL,
    user_id         BIGINT UNSIGNED NOT NULL,
    joined_at       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_read_at    DATETIME(6)     NULL,
    PRIMARY KEY (channel_id, user_id),
    KEY idx_channel_members_user_id (user_id),
    KEY idx_channel_members_last_read (last_read_at),
    CONSTRAINT fk_channel_members_channel
        FOREIGN KEY (channel_id) REFERENCES channels (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_channel_members_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE messages (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    public_id           CHAR(36)        NOT NULL,
    channel_id          BIGINT UNSIGNED NOT NULL,
    author_user_id      BIGINT UNSIGNED NOT NULL,
    parent_message_id   BIGINT UNSIGNED NULL,
    thread_root_id      BIGINT UNSIGNED NULL,
    body                TEXT            NOT NULL,
    body_search         TEXT            NULL,
    is_edited           TINYINT(1)      NOT NULL DEFAULT 0,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at          DATETIME(6)     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_messages_public_id (public_id),
    KEY idx_messages_channel_created (channel_id, created_at),
    KEY idx_messages_author (author_user_id),
    KEY idx_messages_thread_root (thread_root_id),
    KEY idx_messages_parent (parent_message_id),
    KEY idx_messages_deleted_at (deleted_at),
    FULLTEXT KEY ft_messages_body_search (body_search),
    CONSTRAINT fk_messages_channel
        FOREIGN KEY (channel_id) REFERENCES channels (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_messages_author
        FOREIGN KEY (author_user_id) REFERENCES users (id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_messages_parent
        FOREIGN KEY (parent_message_id) REFERENCES messages (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_messages_thread_root
        FOREIGN KEY (thread_root_id) REFERENCES messages (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE message_reactions (
    message_id      BIGINT UNSIGNED NOT NULL,
    user_id         BIGINT UNSIGNED NOT NULL,
    emoji           VARCHAR(32)     NOT NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (message_id, user_id, emoji),
    KEY idx_message_reactions_user_id (user_id),
    CONSTRAINT fk_message_reactions_message
        FOREIGN KEY (message_id) REFERENCES messages (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_message_reactions_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE message_mentions (
    id                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    message_id              BIGINT UNSIGNED NOT NULL,
    mention_type            ENUM('USER', 'CHANNEL', 'HERE') NOT NULL,
    mentioned_user_id       BIGINT UNSIGNED NULL,
    mentioned_channel_id    BIGINT UNSIGNED NULL,
    created_at              DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_message_mentions_message_id (message_id),
    KEY idx_message_mentions_user_id (mentioned_user_id),
    CONSTRAINT fk_message_mentions_message
        FOREIGN KEY (message_id) REFERENCES messages (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_message_mentions_user
        FOREIGN KEY (mentioned_user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_message_mentions_channel
        FOREIGN KEY (mentioned_channel_id) REFERENCES channels (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pinned_messages (
    channel_id          BIGINT UNSIGNED NOT NULL,
    message_id          BIGINT UNSIGNED NOT NULL,
    pinned_by_user_id   BIGINT UNSIGNED NULL,
    pinned_at           DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (channel_id, message_id),
    KEY idx_pinned_messages_message_id (message_id),
    CONSTRAINT fk_pinned_messages_channel
        FOREIGN KEY (channel_id) REFERENCES channels (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_pinned_messages_message
        FOREIGN KEY (message_id) REFERENCES messages (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_pinned_messages_pinned_by
        FOREIGN KEY (pinned_by_user_id) REFERENCES users (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
