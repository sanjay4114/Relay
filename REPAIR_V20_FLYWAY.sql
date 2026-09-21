-- Relay - Flyway V20 Checksum Mismatch Recovery
-- ==================================================
-- 
-- This script diagnoses and repairs V20 migration checksum mismatches
-- Run in MySQL Workbench or mysql CLI after connecting to the relay database
--
-- Step 1: DIAGNOSTIC QUERIES (READ-ONLY - Safe to run)
-- =====================================================

-- Check current V20 status
SELECT 
    version, 
    description, 
    installed_by, 
    installed_on, 
    success,
    checksum
FROM flyway_schema_history 
WHERE version IN ('19', '20', '21')
ORDER BY version;

-- Count total successful migrations
SELECT COUNT(*) as successful_migrations 
FROM flyway_schema_history 
WHERE success = 1;

-- Verify indexes exist in database (that V20 should have created)
SHOW INDEX FROM messages WHERE Key_name LIKE 'idx_messages%desc';
SHOW INDEX FROM notifications WHERE Key_name LIKE 'idx_notifications%';
SHOW INDEX FROM tasks WHERE Key_name LIKE 'idx_tasks%';
SHOW INDEX FROM task_activity WHERE Key_name LIKE 'idx_task%';

-- =====================================================
-- Step 2: FIX CHECKSUM (Choose ONE based on your situation)
-- =====================================================

-- OPTION A: Update database to accept current source code
-- This is SAFE if the indexes actually exist in the database
-- Update to match the current V20__add_performance_indexes.sql source
UPDATE flyway_schema_history 
SET checksum = -1336466733
WHERE version = '20' 
AND installed_on IS NOT NULL;

-- Verify update
SELECT version, checksum, success FROM flyway_schema_history WHERE version = '20';

-- OPTION B: If OPTION A doesn't work, try with no-comment version
-- UPDATE flyway_schema_history 
-- SET checksum = -1803654654
-- WHERE version = '20';

-- OPTION C: Reset V20 and everything after it to re-apply
-- DELETE FROM flyway_schema_history WHERE version >= '20';
-- Then restart backend - Flyway will re-apply V20 and V21

-- =====================================================
-- Step 3: VERIFY DATA INTEGRITY (After applying fix)
-- =====================================================

-- Verify critical tables still exist and have data
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as workspace_count FROM workspaces;
SELECT COUNT(*) as channel_count FROM channels;
SELECT COUNT(*) as message_count FROM messages;
SELECT COUNT(*) as task_count FROM tasks;
SELECT COUNT(*) as notification_count FROM notifications;
SELECT COUNT(*) as file_count FROM file_attachments;

-- =====================================================
-- Step 4: After running above, restart Spring Boot backend
-- =====================================================
-- The backend should now start successfully:
-- - In Eclipse STS: Right-click relay-api → Run As → Spring Boot App
-- - Via terminal: java -jar backend/target/relay-api-0.1.0-SNAPSHOT.jar
-- - Verify: curl http://localhost:8080/actuator/health

-- =====================================================
-- EMERGENCY RECOVERY (Only if data loss is acceptable)
-- =====================================================

-- DANGER: This COMPLETELY resets the database
-- Only run if you have a backup or don't need your data
-- DROP DATABASE relay;
-- CREATE DATABASE relay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Then restart backend to apply all migrations from scratch
