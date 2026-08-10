-- =============================================================================
-- V14: Collaboration Features (Phase 5D)
-- =============================================================================

-- Emoji Reactions
CREATE TABLE message_reactions (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    emoji VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_message_user_emoji (message_id, user_id, emoji),
    CONSTRAINT fk_reactions_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_reactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Message Mentions
CREATE TABLE message_mentions (
    message_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (message_id, user_id),
    CONSTRAINT fk_mentions_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_mentions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Pinned Messages (adding to messages table)
ALTER TABLE messages
    ADD COLUMN pinned_at DATETIME(6) NULL,
    ADD COLUMN pinned_by BIGINT UNSIGNED NULL,
    ADD CONSTRAINT fk_messages_pinned_by FOREIGN KEY (pinned_by) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_messages_pinned ON messages(channel_id, pinned_at);

-- Saved Messages (Personal bookmarks)
CREATE TABLE saved_messages (
    user_id BIGINT UNSIGNED NOT NULL,
    message_id BIGINT UNSIGNED NOT NULL,
    saved_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (user_id, message_id),
    CONSTRAINT fk_saved_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
);
