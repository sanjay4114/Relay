-- =============================================================================
-- V16: File Attachments (Phase 6)
-- =============================================================================

DROP TABLE IF EXISTS file_attachments;

CREATE TABLE file_attachments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    public_id VARCHAR(36) NOT NULL UNIQUE,
    message_id BIGINT UNSIGNED NULL,
    uploaded_by BIGINT UNSIGNED NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(128) NOT NULL,
    extension VARCHAR(32) NOT NULL,
    file_size BIGINT UNSIGNED NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    storage_provider VARCHAR(64) NOT NULL,
    storage_path VARCHAR(1024) NOT NULL,
    thumbnail_path VARCHAR(1024) NULL,
    uploaded_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6) NULL,
    
    CONSTRAINT fk_attachment_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE SET NULL,
    CONSTRAINT fk_attachment_uploader FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_attachment_message ON file_attachments(message_id);
CREATE INDEX idx_attachment_checksum ON file_attachments(checksum);
