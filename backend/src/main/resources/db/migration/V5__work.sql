-- =============================================================================
-- V5: Work Management
-- Projects, tasks, comments, activity log
-- =============================================================================

CREATE TABLE projects (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    public_id           CHAR(36)        NOT NULL,
    workspace_id        BIGINT UNSIGNED NOT NULL,
    name                VARCHAR(255)    NOT NULL,
    description         TEXT            NULL,
    status              ENUM('ACTIVE', 'ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
    color               VARCHAR(7)      NOT NULL DEFAULT '#6366f1',
    created_by_user_id  BIGINT UNSIGNED NULL,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_projects_public_id (public_id),
    UNIQUE KEY uk_projects_workspace_name (workspace_id, name),
    KEY idx_projects_workspace_id (workspace_id),
    KEY idx_projects_status (status),
    CONSTRAINT fk_projects_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_projects_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tasks (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    public_id           CHAR(36)        NOT NULL,
    workspace_id        BIGINT UNSIGNED NOT NULL,
    project_id          BIGINT UNSIGNED NULL,
    title               VARCHAR(500)    NOT NULL,
    description         TEXT            NULL,
    status              ENUM('BACKLOG', 'TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'CANCELLED') NOT NULL DEFAULT 'BACKLOG',
    priority            ENUM('NONE', 'LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NONE',
    assignee_user_id    BIGINT UNSIGNED NULL,
    reporter_user_id    BIGINT UNSIGNED NOT NULL,
    due_date            DATE            NULL,
    position            DECIMAL(12, 4)  NOT NULL DEFAULT 0.0000,
    created_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at          DATETIME(6)     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tasks_public_id (public_id),
    KEY idx_tasks_workspace_status (workspace_id, status),
    KEY idx_tasks_project_position (project_id, status, position),
    KEY idx_tasks_assignee (assignee_user_id),
    KEY idx_tasks_reporter (reporter_user_id),
    KEY idx_tasks_due_date (due_date),
    KEY idx_tasks_deleted_at (deleted_at),
    FULLTEXT KEY ft_tasks_title_description (title, description),
    CONSTRAINT fk_tasks_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tasks_project
        FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_tasks_assignee
        FOREIGN KEY (assignee_user_id) REFERENCES users (id)
        ON DELETE SET NULL,
    CONSTRAINT fk_tasks_reporter
        FOREIGN KEY (reporter_user_id) REFERENCES users (id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE task_comments (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    task_id         BIGINT UNSIGNED NOT NULL,
    author_user_id  BIGINT UNSIGNED NOT NULL,
    body            TEXT            NOT NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at      DATETIME(6)     NULL,
    PRIMARY KEY (id),
    KEY idx_task_comments_task_created (task_id, created_at),
    KEY idx_task_comments_author (author_user_id),
    CONSTRAINT fk_task_comments_task
        FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_task_comments_author
        FOREIGN KEY (author_user_id) REFERENCES users (id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE task_activity (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    task_id         BIGINT UNSIGNED NOT NULL,
    actor_user_id   BIGINT UNSIGNED NULL,
    action          VARCHAR(50)     NOT NULL,
    metadata        JSON            NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_task_activity_task_created (task_id, created_at),
    KEY idx_task_activity_actor (actor_user_id),
    CONSTRAINT fk_task_activity_task
        FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_task_activity_actor
        FOREIGN KEY (actor_user_id) REFERENCES users (id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
