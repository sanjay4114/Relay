-- =============================================================================
-- V21: File attachment performance index (supplements V20)
-- V20 intended a composite index on file_attachments but referenced the
-- non-existent column created_at. V16 defines uploaded_at.
-- =============================================================================

CREATE INDEX idx_file_attachments_message_uploaded
    ON file_attachments (message_id, uploaded_at DESC);
