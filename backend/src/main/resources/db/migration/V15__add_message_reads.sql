-- =============================================================================
-- V15: Read Receipts & Unread System (Phase 5E)
-- =============================================================================

CREATE TABLE message_reads (
    message_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    delivered_at DATETIME(6) NULL,
    read_at DATETIME(6) NULL,
    PRIMARY KEY (message_id, user_id),
    CONSTRAINT fk_reads_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_reads_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_message_reads_user ON message_reads(user_id);

-- Add watermark cursor to channel_members to optimize "unread count" queries
ALTER TABLE channel_members
    ADD COLUMN last_read_message_id BIGINT UNSIGNED NULL,
    ADD CONSTRAINT fk_channel_member_last_read FOREIGN KEY (last_read_message_id) REFERENCES messages(id) ON DELETE SET NULL;
