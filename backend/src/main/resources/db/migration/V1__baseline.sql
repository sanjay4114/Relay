-- Relay v1.0 baseline migration placeholder
-- Entity tables will be added in subsequent phases.

CREATE TABLE IF NOT EXISTS relay_schema_info (
    id INT NOT NULL PRIMARY KEY,
    version_label VARCHAR(32) NOT NULL
) ENGINE=InnoDB;

INSERT IGNORE INTO relay_schema_info (id, version_label) VALUES (1, 'v1.0-scaffold');
