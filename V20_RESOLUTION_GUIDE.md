# V20 Flyway Checksum Mismatch - Resolution Guide

## Problem Summary
The Flyway migration V20 (`add_performance_indexes.sql`) has a checksum mismatch:
- **Database (Applied):** -815006746
- **Source Code (Current):** Various attempts don't match

This prevents the Spring Boot backend from starting.

## Root Cause Analysis
1. The source code in the repository (`V20__add_performance_indexes.sql`) was modified at some point
2. The database has the migration marked as successfully applied with a different checksum  
3. Flyway validates checksums to prevent accidental SQL modification
4. The mismatch indicates either:
   - The source was changed after being applied to the database
   - The database history was modified
   - There's a line-ending or encoding difference

## Resolution Strategies

### Option 1: Check Current Database State (SAFE - Read-only)
Execute this query in MySQL Workbench to see what's in the Flyway history:

```sql
SELECT version, description, type, installed_by, installed_on, success, checksum
FROM flyway_schema_history 
WHERE version IN ('19', '20', '21')
ORDER BY version;
```

This shows:
- If V20 is truly marked as successful
- What checksum the database has for V20
- If V19 and V21 are also there

### Option 2: Update Database Checksum (RECOMMENDED - Safe if indexes exist)
The current `V20__add_performance_indexes.sql` source code has checksum: **-1336466733**

To fix:
```sql
UPDATE flyway_schema_history 
SET checksum = -1336466733
WHERE version = '20';
```

After this:
1. Restart Spring Boot
2. Verify health: `curl http://localhost:8080/actuator/health`
3. Check data is intact: `SELECT COUNT(*) FROM users;`

### Option 3: Alternative Checksum Values
If Option 2 doesn't work, try these alternatives:

```sql
-- Without detailed comments (442 bytes)
UPDATE flyway_schema_history 
SET checksum = -1803654654
WHERE version = '20';

-- Mixed format (680 bytes)
UPDATE flyway_schema_history 
SET checksum = -305899943  
WHERE version = '20';
```


Only if you want to re-apply the migration:

```sql
-- Delete V20 from history (WILL NEED to drop the indexes it created)
DELETE FROM flyway_schema_history WHERE version = '20';

-- Then drop the indexes it would recreate
DROP INDEX IF EXISTS idx_messages_channel_created_desc ON messages;
DROP INDEX IF EXISTS idx_messages_parent_created_desc ON messages;
DROP INDEX IF EXISTS idx_tasks_workspace_status ON tasks;
DROP INDEX IF EXISTS idx_notifications_recipient_created ON notifications;
DROP INDEX IF EXISTS idx_task_activity_workspace ON task_activity;
DROP INDEX IF EXISTS idx_file_attachments_message ON file_attachments;  
```

Then restart backend - Flyway will re-apply V20.

### Option 4: Baseline Fresh Database (NUCLEAR - Loses everything)
If you want to start fresh:

```bash
1. Backup current database (if you want to keep data)
2. Delete 'relay' database in MySQL
3. Restart backend - Flyway will apply all migrations from scratch
```

This causes Flyway to run V1-V21 sequentially on a clean database.

## Recommended Approach

### For Development (Get Working Quickly)
1. Check database state: Run Option 1 SQL query above  
2. If V20 is successful in DB, update source to match:
   - Run: `java CheckV20Checksum` to get current source checksum
   - Update DB: `UPDATE flyway_schema_history SET checksum = <VALUE> WHERE version = '20';`
   - Restart backend

### For Production (Preserve Data)
1. Check database state (Option 1)
2. Export database backup first
3. Update flyway_schema_history to accept current source
4. Test thoroughly before pushing to production

## Immediate Recovery Steps

### If backend won't start:

**Step 1:** Open MySQL Workbench → Connect to localhost:3306

**Step 2:** Run diagnostic query:
```sql
USE relay;
SELECT version, checksum, success FROM flyway_schema_history WHERE version IN ('19', '20', '21');
```

**Step 3:** Apply the fix (RECOMMENDED):
```sql
UPDATE flyway_schema_history 
SET checksum = -1336466733
WHERE version = '20';
```

**Step 4:** Verify the fix took:
```sql
SELECT version, checksum, success FROM flyway_schema_history WHERE version = '20';
-- Should show checksum = -1336466733 and success = 1
```

**Step 5:** Restart Spring Boot:
- In Eclipse: Right-click relay-api → Run As → Spring Boot App
- Or terminal: `java -jar backend/target/relay-api-0.1.0-SNAPSHOT.jar`

**Step 6:** Verify health:
```bash
curl http://localhost:8080/actuator/health
```

Expected output: `{"status":"UP", ...}`

## If that doesn't work:

Try the alternative checksums from Option 3 above, or follow Option 5 (delete and re-apply) if checksums still don't match.

## Testing the Fix

After applying one of the above solutions:

1. Rebuild backend JAR (if using Docker or executable):
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```

2. Restart Spring Boot (Eclipse: Right-click → Run As → Spring Boot App)

3. Verify startup:
   ```bash
   curl http://localhost:8080/actuator/health
   ```
   
   Expected: `{"status":"UP", ...}`

4. Verify database is intact:
   ```bash
   -- Check migrations again
   SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1;  -- Should be 21
   
   -- Check data still exists  
   SELECT COUNT(*) FROM users;
   SELECT COUNT(*) FROM workspaces;
   SELECT COUNT(*) FROM messages;
   ```

## If All Else Fails

Reset to a known-good state:

```sql
-- Full database reset
DROP DATABASE relay;
CREATE DATABASE relay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Then restart backend - Flyway will migrate from V1 to V21
```

**⚠️ WARNING:** This deletes ALL data. Only use if you have a backup or don't need the data.

---

**Next Steps:**
1. Choose a resolution strategy above
2. Run the SQL/commands  
3. Restart backend
4. Verify with health check
5. Test application functionality

If you encounter issues, check `backend/target/logs/` for Spring Boot startup logs with Flyway details.
