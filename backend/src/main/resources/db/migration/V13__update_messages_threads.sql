-- =============================================================================
-- V13: Update Messages for Threads (Phase 5C)
-- =============================================================================

ALTER TABLE messages
    ADD COLUMN parent_message_id BIGINT UNSIGNED NULL,
    ADD COLUMN reply_count INT NOT NULL DEFAULT 0,
    ADD COLUMN last_reply_at DATETIME(6) NULL,
    ADD CONSTRAINT fk_messages_parent FOREIGN KEY (parent_message_id) REFERENCES messages(id) ON DELETE CASCADE;

CREATE INDEX idx_messages_parent_created ON messages(parent_message_id, created_at);
