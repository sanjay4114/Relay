# RELAY - Complete Recovery & Testing Checklist

## 🎯 OVERALL STATUS

**Application:** RELAY (Real-time Collaboration Platform)  
**Current Issue:** Flyway V20 checksum mismatch blocking backend startup  
**Expected Resolution Time:** 30-45 minutes (fix + test + verify)  
**Data Status:** ✅ All preserved (only metadata issue)

---

## PHASE 1: Fix Flyway V20 Checksum Mismatch (10-15 min)

**Goal:** Get Spring Boot backend starting successfully

### Checklist

- [ ] **1.1** Open MySQL Workbench
  - Connect to `localhost:3306`
  - Select database: `relay`
  
- [ ] **1.2** Run diagnostic SQL (copy from REPAIR_V20_FLYWAY.sql)
  - Check V19, V20, V21 status
  - Verify all show `success = 1`

- [ ] **1.3** Apply checksum fix (primary option)
  ```sql
  UPDATE flyway_schema_history 
  SET checksum = -1336466733
  WHERE version = '20';
  ```
  
- [ ] **1.4** Verify fix applied
  ```sql
  SELECT version, checksum FROM flyway_schema_history WHERE version = '20';
  -- Should show checksum = -1336466733
  ```

- [ ] **1.5** Restart Spring Boot backend
  - Eclipse STS: Right-click `relay-api` → Run As → Spring Boot App
  - Watch console for: `Tomcat started on port 8080`

- [ ] **1.6** Verify backend health
  ```bash
  curl http://localhost:8080/actuator/health
  # Expected: {"status":"UP"}
  ```

**If checksum fix doesn't work:**
- [ ] Try Alternative 1 checksum: `-1803654654`
- [ ] Try Alternative 2 checksum: `-305899943`
- [ ] If none work → see V20_RESOLUTION_GUIDE.md Option 5

---

## PHASE 2: Start Frontend (5 min)

**Goal:** Get React frontend running and connected to backend

### Checklist

- [ ] **2.1** Open new terminal
  ```bash
  cd "e:\Web Applications\Relay\frontend"
  npm install  # Only needed first time
  npm run dev
  ```

- [ ] **2.2** Wait for "Local: http://localhost:5173"

- [ ] **2.3** Open browser to http://localhost:5173
  - Should see Relay login page
  - No errors in browser console (F12)

- [ ] **2.4** Check network connectivity
  - Backend on port 8080 ✅
  - Frontend on port 5173 ✅
  - WebSocket at /api/ws ✅

---

## PHASE 3: Functional Testing (15-20 min)

**Goal:** Verify all 15 features work correctly

See detailed checklist in **RELAY_FINAL_COMPLETION_REPORT.md** Section 13

### Quick Test Sequence

- [ ] **3.1** Authentication
  - [ ] Register new account (or login with test account)
  - [ ] Verify JWT token received
  - [ ] Try refresh token (should extend session)

- [ ] **3.2** Workspace Management
  - [ ] Create new workspace
  - [ ] View all workspaces
  - [ ] Switch workspace (verify isolation)

- [ ] **3.3** Messaging
  - [ ] Create public channel
  - [ ] Send message in channel
  - [ ] Message appears real-time (WebSocket)
  - [ ] Edit and delete message

- [ ] **3.4** Threads
  - [ ] Reply to message (creates thread)
  - [ ] View message in context
  - [ ] Verify thread count

- [ ] **3.5** Files
  - [ ] Upload file attachment
  - [ ] Download file  
  - [ ] File appears in channel/search

- [ ] **3.6** Search
  - [ ] Cmd+K to open search
  - [ ] Search for message content
  - [ ] Search for file name
  - [ ] Results highlight keywords

- [ ] **3.7** Tasks
  - [ ] Create task from message context menu
  - [ ] Move task between columns (drag-drop)
  - [ ] Assign user to task
  - [ ] Task appears on Dashboard

- [ ] **3.8** Dashboard
  - [ ] View stats (Active Tasks, Unread Messages, etc.)
  - [ ] View Activity Timeline
  - [ ] Timeline shows recent messages, tasks, files

- [ ] **3.9** Notifications
  - [ ] Mention user (@username)
  - [ ] Notification appears real-time
  - [ ] Click notification → navigate to message
  - [ ] Mark as read

- [ ] **3.10** Browser Features
  - [ ] Test on Chrome, Firefox, Safari (if available)
  - [ ] Resize window → verify responsive design
  - [ ] Keyboard navigation works (Tab key)
  - [ ] Accessibility features functional (screen reader friendly)

---

## PHASE 4: Database Verification (5 min)

**Goal:** Confirm database integrity and no data loss

### MySQL Queries

```sql
USE relay;

-- Verify all migrations applied
SELECT COUNT(*) as total_migrations FROM flyway_schema_history WHERE success = 1;
-- Expected: 21

-- Verify data preserved
SELECT 
  (SELECT COUNT(*) FROM users) as users,
  (SELECT COUNT(*) FROM workspaces) as workspaces,
  (SELECT COUNT(*) FROM channels) as channels,
  (SELECT COUNT(*) FROM messages) as messages,
  (SELECT COUNT(*) FROM tasks) as tasks,
  (SELECT COUNT(*) FROM notifications) as notifications,
  (SELECT COUNT(*) FROM file_attachments) as files;

-- Verify indexes exist (V20 should have created these)
SHOW INDEX FROM messages WHERE Key_name LIKE '%desc%';
-- Expected: idx_messages_channel_created_desc, idx_messages_parent_created_desc

SHOW INDEX FROM notifications WHERE Key_name = 'idx_notifications_recipient_created';
-- Expected: one result with column = recipient_id

-- Verify V21 file attachment index
SHOW INDEX FROM file_attachments WHERE Key_name = 'idx_file_attachments_message_uploaded';
-- Expected: one result
```

### Checklist

- [ ] **4.1** Run migration count query
  - [ ] Result should be: 21 migrations

- [ ] **4.2** Run data preservation query
  - [ ] All counts > 0 (or 0 is OK if fresh database)
  - [ ] No NULL results

- [ ] **4.3** Verify V20 indexes exist
  - [ ] `idx_messages_channel_created_desc` ✅
  - [ ] `idx_messages_parent_created_desc` ✅  
  - [ ] `idx_tasks_workspace_status` ✅
  - [ ] `idx_notifications_recipient_created` ✅
  - [ ] `idx_task_activity_workspace` ✅

- [ ] **4.4** Verify V21 index exists
  - [ ] `idx_file_attachments_message_uploaded` ✅

---

## PHASE 5: Performance & Load Testing (Optional, 10-15 min)

**Goal:** Verify backend performance under load

### Checklist

- [ ] **5.1** Check backend metrics
  ```bash
  curl http://localhost:8080/actuator/prometheus | grep -i jvm
  # Verify JVM metrics look normal (no memory leaks)
  ```

- [ ] **5.2** Load test (optional but recommended)
  ```bash
  # Terminal 1: Run test script
  cd backend
  mvn test -Dgroups=integration  # If integration tests configured
  # Or use Apache JMeter for load testing
  ```

- [ ] **5.3** Check startup time
  - Verify backend starts in < 30 seconds
  - Flyway migration validation completes
  - All beans initialized

- [ ] **5.4** Monitor resource usage
  - Watch memory usage (should stabilize)
  - CPU usage reasonable when idle
  - Database connections pooled properly

---

## PHASE 6: Detailed QA (If Required, 30-45 min)

**Goal:** Complete comprehensive QA checklist

**Reference:** RELAY_FINAL_COMPLETION_REPORT.md Section 13

### Test Coverage Areas

- [ ] **6.1** Environment Setup (3 tests)
- [ ] **6.2** Authentication & Authorization (5 tests)
- [ ] **6.3** Workspace Management (3 tests)
- [ ] **6.4** Channel Management (4 tests)
- [ ] **6.5** WebSocket Real-time (3 tests)
- [ ] **6.6** Core Messaging (5 tests)
- [ ] **6.7** Message Threads (3 tests)
- [ ] **6.8** Emoji Reactions (3 tests)
- [ ] **6.9** Mentions & Tags (2 tests)
- [ ] **6.10** Message Pins & Saves (3 tests)
- [ ] **6.11** Read/Unread Status (2 tests)
- [ ] **6.12** File Attachments (4 tests)
- [ ] **6.13** Notifications (4 tests)
- [ ] **6.14** Global Search (3 tests)
- [ ] **6.15** Task Management (4 tests)
- [ ] **6.16** Dashboard (3 tests)
- [ ] **6.17** Responsive Design (3 tests)
- [ ] **6.18** Security (3 tests)
- [ ] **6.19** Error Handling (2 tests)
- [ ] **6.20** Regression Testing (2 tests)

**Total: 51 tests**

Record results in spreadsheet:
- Test Name | Status (PASS/FAIL) | Browser | Notes

---

## TROUBLESHOOTING

### Backend Won't Start
**Symptoms:** "Migration checksum mismatch for version 20"

**Solution:**
1. Run diagnostic query in MySQL
2. Update checksum to `-1336466733`
3. Restart backend
4. If fails, try alternative checksums (see V20_RESOLUTION_GUIDE.md)

### Frontend Can't Connect to Backend
**Symptoms:** CORS error or WebSocket connection failed

**Solution:**
1. Verify backend is running: `curl http://localhost:8080/actuator/health`
2. Check frontend .env file: `VITE_WS_URL=http://localhost:8080/api/ws`
3. Verify ports: 8080 (backend), 5173 (frontend)
4. Check browser console for exact error

### Messages Not Appearing Real-time
**Symptoms:** Need to refresh page to see new messages

**Solution:**
1. Verify WebSocket connected in browser DevTools (Network → WS)
2. Check JWT token valid: `curl http://localhost:8080/actuator/health`
3. Verify in Spring logs: no WebSocket handshake errors
4. Restart both frontend and backend

### Database Queries Slow
**Symptoms:** Dashboard loads slowly, search takes time

**Solution:**
1. Verify V20 indexes exist: `SHOW INDEX FROM messages`
2. Run `ANALYZE TABLE messages` in MySQL
3. Check query plans: `EXPLAIN SELECT ...`
4. Monitor disk I/O and memory

---

## ROLLBACK PROCEDURE (If Needed)

**If something breaks after fixing V20:**

```sql
-- Restore original Flyway history
UPDATE flyway_schema_history 
SET checksum = -815006746
WHERE version = '20';
-- This reverts back to original mismatch state

-- Delete corrupted data (if applicable)
-- BACKUP FIRST
```

---

## SUCCESS CRITERIA

You'll know everything is working when:

✅ Backend starts without errors  
✅ Frontend loads on http://localhost:5173  
✅ Can login with credentials  
✅ Can send/receive messages in real-time  
✅ WebSocket connection shows in DevTools  
✅ All 15 features accessible  
✅ Database has 21 migrations marked successful  
✅ All indexes from V20 and V21 exist  
✅ No console errors in browser  
✅ No ERROR logs in Spring Boot console  

---

## ESTIMATED TIMELINE

| Phase | Task | Time |
|-------|------|------|
| 1 | Fix Flyway V20 | 10-15 min |
| 2 | Start Frontend | 5 min |
| 3 | Quick Functional Test | 15-20 min |
| 4 | Database Verification | 5 min |
| 5 | Performance Check | 10-15 min (optional) |
| 6 | Full QA Checklist | 30-45 min (optional) |
| **TOTAL** | **Minimum (1-4)** | **35-40 min** |
| **TOTAL** | **Comprehensive (1-6)** | **75-120 min** |

---

## NEXT STEPS

1. **Read:** V20_QUICK_FIX.md (2 min)
2. **Execute:** REPAIR_V20_FLYWAY.sql queries (5 min)
3. **Test:** Phases 1-4 above (25-30 min)
4. **Deploy:** When all phases pass
5. **Document:** Any issues or customizations needed

---

## SUPPORT RESOURCES

All files referenced are in: `e:\Web Applications\Relay\`

- **Quick Start:** V20_QUICK_FIX.md
- **Detailed Reference:** V20_RESOLUTION_GUIDE.md
- **SQL Scripts:** REPAIR_V20_FLYWAY.sql
- **Project Status:** RELAY_FINAL_COMPLETION_REPORT.md
- **QA Checklist:** RELAY_FINAL_COMPLETION_REPORT.md (Section 13)
- **Technical History:** docs/DEVELOPMENT_JOURNAL.md
- **UAT Guide:** QA_UAT_Guide.md

---

**Last Updated:** [Current Session]  
**Version:** 1.0 - Initial Recovery Guide  
**Status:** Ready for Execution
