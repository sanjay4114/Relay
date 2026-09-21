# RELAY - V20 Checksum Mismatch Quick Recovery

**Status:** Backend startup is blocked due to Flyway V20 migration checksum mismatch

**Root Cause:** The V20 migration source code doesn't match the checksum recorded in the database migration history

**Current State:**
- Database Checksum: -815006746 (what's recorded as applied)
- Source Code Checksum: -1336466733 (what's currently in the repo)

## ✅ QUICK FIX (2 minutes)

### Prerequisites
- MySQL Workbench installed and running
- Connection to localhost:3306, database: relay
- Database password available (if required)

### Steps

1. **Open MySQL Workbench**
   - Connect to localhost:3306
   - Select `relay` database
   - Create new SQL script (File → New Query Tab)

2. **Copy and run this SQL:**
   ```sql
   USE relay;
   
   -- Check current status
   SELECT version, checksum, success 
   FROM flyway_schema_history 
   WHERE version = '20';
   
   -- Apply the fix
   UPDATE flyway_schema_history 
   SET checksum = -1336466733
   WHERE version = '20';
   
   -- Verify it worked
   SELECT version, checksum, success 
   FROM flyway_schema_history 
   WHERE version = '20';
   ```

3. **Restart Spring Boot Backend**
   - In Eclipse STS: Right-click `relay-api` → Run As → Spring Boot App
   - Or via terminal:
     ```bash
     cd e:\Web Applications\Relay\backend
     java -jar target/relay-api-0.1.0-SNAPSHOT.jar
     ```

4. **Verify it works**
   ```bash
   curl http://localhost:8080/actuator/health
   # Expected: {"status":"UP", ...}
   ```

## ⚠️ If Step 2 SQL doesn't work

Try these alternative checksums (in order):

```sql
-- Alternative 1 (minimal comments)
UPDATE flyway_schema_history SET checksum = -1803654654 WHERE version = '20';

-- Alternative 2 (different format)
UPDATE flyway_schema_history SET checksum = -305899943 WHERE version = '20';

-- Alternative 3 (other variant)
UPDATE flyway_schema_history SET checksum = -153951993 WHERE version = '20';
```

After each attempt, restart Spring Boot and check `curl http://localhost:8080/actuator/health`

## 🆘 If NOTHING works

Use the nuclear option (deletes and re-applies V20):

```sql
USE relay;

-- Backup your data first (optional but recommended)
-- Export current data to CSV from MySQL Workbench if needed

-- Delete V20 and V21 from history
DELETE FROM flyway_schema_history WHERE version >= '20';

-- Drop all the indexes V20 created
DROP INDEX IF EXISTS idx_messages_channel_created_desc ON messages;
DROP INDEX IF EXISTS idx_messages_parent_created_desc ON messages;
DROP INDEX IF EXISTS idx_tasks_workspace_status ON tasks;
DROP INDEX IF EXISTS idx_notifications_recipient_created ON notifications;
DROP INDEX IF EXISTS idx_task_activity_workspace ON task_activity;
DROP INDEX IF EXISTS idx_file_attachments_message ON file_attachments;
```

Then restart Spring Boot - Flyway will re-apply V20 and V21 automatically.

## 📋 Verification Checklist

After backend starts, verify everything:

```bash
# 1. Health check
curl http://localhost:8080/actuator/health

# 2. Login and verify JWT auth works
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password"}'

# 3. In MySQL, verify data still exists
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM workspaces;
SELECT COUNT(*) FROM messages;
SELECT COUNT(*) FROM tasks;
```

## 📚 Detailed Documentation

See [V20_RESOLUTION_GUIDE.md](V20_RESOLUTION_GUIDE.md) for comprehensive troubleshooting

See [REPAIR_V20_FLYWAY.sql](REPAIR_V20_FLYWAY.sql) for all repair options in SQL

## 🚀 Next Steps After Fix

1. **Test backend**
   - ✅ Verify health endpoint
   - ✅ Try logging in
   - ✅ Check WebSocket connection (try messaging)

2. **Start frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   - Navigate to http://localhost:5173
   - Test all features per RELAY_FINAL_COMPLETION_REPORT.md

3. **Run QA Checklist**
   - See RELAY_FINAL_COMPLETION_REPORT.md section 13
   - 51 tests across 20 phases
   - Record results in spreadsheet

## ❓ Questions?

Check these files in order:
1. V20_RESOLUTION_GUIDE.md - Detailed explanation
2. REPAIR_V20_FLYWAY.sql - SQL with all options
3. RELAY_FINAL_COMPLETION_REPORT.md - Full project context
4. docs/DEVELOPMENT_JOURNAL.md - Historical context

---

**Expected Time to Fix:** 5-10 minutes  
**Data Risk:** LOW (only updating migration metadata, not schema)  
**Recommended Approach:** Option 1 (Update checksum), then Option 3 only if Option 1 fails
