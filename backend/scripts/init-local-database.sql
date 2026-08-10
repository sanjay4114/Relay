-- Create the Relay database on your local MySQL server.
-- Run with:  mysql -u root -p < scripts/init-local-database.sql

CREATE DATABASE IF NOT EXISTS relay
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Optional: dedicated application user (recommended)
-- CREATE USER IF NOT EXISTS 'relay'@'localhost' IDENTIFIED BY 'your_password';
-- GRANT ALL PRIVILEGES ON relay.* TO 'relay'@'localhost';
-- FLUSH PRIVILEGES;

-- Then set spring.datasource.username and spring.datasource.password in application.properties
