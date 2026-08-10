-- =============================================================================
-- V6: Knowledge (Docs)
-- Pages, blocks, permissions, comments
-- =============================================================================

CREATE TABLE pages (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    public_id           CHAR(36)        NOT NULL,
    workspace_id        BIGINT UNSIGNED NOT NULL,
    parent_page_id      BIGINT UNSIGNED NULL,
    title               VARCHAR(500)    NOT NULL DEFAULT 'Untitled',
    icon                VARCHAR(32)     NULL,
    position            INT             NOT NULL DEFAULT 0,
    created_by_user_id  BIGINT UNSIGNED NULL,
    updated_by_user_id  BIGINT UNSIGNED NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at          DATETIME(6)     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pages_public_id (public_id),
    KEY idx_pages_workspace_parent (workspace_id, parent_page_id),
    KEY idx_pages_position (workspace_id, parent_page_id, position),
    KEY idx_pages_deleted_at (deleted_at),
    FULLTEXT KEY ft_pages_title (title),
    CONSTRAINT fk_pages_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_pages_parent
        FOREIGN KEY (parent_page_id) REFERENCES pages (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_pages_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_pages_updated_by
        FOREIGN KEY (updated_by_user_id) REFERENCES users (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE page_blocks (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    page_id         BIGINT UNSIGNED NOT NULL,
    block_type      ENUM(
                        'PARAGRAPH', 'HEADING1', 'HEADING2', 'HEADING3',
                        'BULLET_LIST', 'NUMBERED_LIST', 'TODO',
                        'CODE', 'QUOTE', 'DIVIDER'
                    ) NOT NULL,
    content         JSON            NOT NULL,
    position        INT             NOT NULL DEFAULT 0,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_page_blocks_page_position (page_id, position),
    CONSTRAINT fk_page_blocks_page
        FOREIGN KEY (page_id) REFERENCES pages (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE page_permissions (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    page_id         BIGINT UNSIGNED NOT NULL,
    user_id         BIGINT UNSIGNED NULL,
    team_id         BIGINT UNSIGNED NULL,
    permission      ENUM('OWNER', 'EDITOR', 'VIEWER') NOT NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_page_permissions_page_user (page_id, user_id),
    UNIQUE KEY uk_page_permissions_page_team (page_id, team_id),
    KEY idx_page_permissions_page_id (page_id),
    CONSTRAINT fk_page_permissions_page
        FOREIGN KEY (page_id) REFERENCES pages (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_page_permissions_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_page_permissions_team
        FOREIGN KEY (team_id) REFERENCES teams (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_page_permissions_grantee CHECK (
        (user_id IS NOT NULL AND team_id IS NULL)
        OR (user_id IS NULL AND team_id IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE page_comments (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    page_id         BIGINT UNSIGNED NOT NULL,
    block_id        BIGINT UNSIGNED NULL,
    author_user_id  BIGINT UNSIGNED NOT NULL,
    body            TEXT            NOT NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at      DATETIME(6)     NULL,
    PRIMARY KEY (id),
    KEY idx_page_comments_page_created (page_id, created_at),
    KEY idx_page_comments_block_id (block_id),
    KEY idx_page_comments_author (author_user_id),
    CONSTRAINT fk_page_comments_page
        FOREIGN KEY (page_id) REFERENCES pages (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_page_comments_block
        FOREIGN KEY (block_id) REFERENCES page_blocks (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_page_comments_author
        FOREIGN KEY (author_user_id) REFERENCES users (id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
