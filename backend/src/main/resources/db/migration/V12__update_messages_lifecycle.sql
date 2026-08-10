-- =============================================================================
-- V12: Update Messages for Lifecycle (Phase 5B)
-- =============================================================================

ALTER TABLE messages
    ADD COLUMN edited_by BIGINT UNSIGNED NULL,
    ADD COLUMN deleted_by BIGINT UNSIGNED NULL,
    ADD CONSTRAINT fk_messages_edited_by FOREIGN KEY (edited_by) REFERENCES users(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_messages_deleted_by FOREIGN KEY (deleted_by) REFERENCES users(id) ON DELETE SET NULL;
