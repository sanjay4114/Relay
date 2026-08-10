# Relay: Manual QA & User Acceptance Testing (UAT) Guide

## Introduction
This document serves as the comprehensive Manual Testing and User Acceptance Testing (UAT) guide for the **Relay** enterprise collaboration platform. The application is built using a Spring Boot backend, a React/Vite frontend, and a MySQL database managed via Flyway. This guide provides step-by-step instructions for local execution and detailed test cases for every module.

---

## SECTION 1: Environment Setup (Backend)

### Prerequisites
- **IDE:** Eclipse (with Spring Tools Suite recommended)
- **Java:** JDK 21 (or the version specified in `pom.xml`)
- **Database:** Local MySQL Server (version 8.x recommended)
- **Maven:** Installed and configured in Eclipse

### 1. Open Eclipse
Launch Eclipse and select your desired workspace.

### 2. Import Project
1. Go to `File` > `Import` > `Maven` > `Existing Maven Projects`.
2. Browse to the `backend` folder of the Relay project.
3. Select the `pom.xml` file and click **Finish**.

### 3. Maven Update Project
Right-click on the imported project > `Maven` > `Update Project...` (or Alt+F5). Ensure "Force Update of Snapshots/Releases" is checked and click **OK** to download all dependencies.

### 4. Required JDK
Verify that the project is using the correct JDK. Right-click the project > `Properties` > `Java Build Path` > `Libraries`. Ensure the JRE System Library matches your installed JDK.

### 5. `application.properties`
Navigate to `src/main/resources/application.properties` (or `application-dev.yml`). Ensure the following configurations are set correctly for your local environment:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/relay_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

### 6. MySQL Configuration
Ensure your local MySQL server is running. Create the database schema if it doesn't exist:
```sql
CREATE DATABASE relay_db;
```

### 7. Flyway Verification
Flyway will automatically run on application startup to create tables. Ensure `spring.flyway.enabled=true` in your properties file. 

### 8. Run RelayApplication
1. Locate `RelayApplication.java` in `src/main/java/com/relay/`.
2. Right-click > `Run As` > `Java Application` (or `Spring Boot App`).

### 9. How to Verify Backend Started Correctly
**Expected Console Output:**
- `INFO ... com.zaxxer.hikari.HikariDataSource : HikariPool-1 - Starting...`
- `INFO ... org.flywaydb.core.Flyway : Successfully applied X migrations to schema 'relay_db'`
- `INFO ... o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat started on port(s): 8080 (http)`
- `INFO ... com.relay.RelayApplication : Started RelayApplication in X seconds`

**Errors to Look For:**
- `Communications link failure`: MySQL is not running or credentials are wrong.
- `FlywayException`: A migration script failed. Check the SQL syntax in `src/main/resources/db/migration`.
- `PortInUseException`: Port 8080 is already in use by another application.

---

## SECTION 2: Environment Setup (Frontend)

### Prerequisites
- **IDE:** Visual Studio Code
- **Node.js:** v18+ or v20+ recommended

### 1. Open VS Code
Launch VS Code and open the `frontend` folder of the Relay project.

### 2. npm install
Open the integrated terminal (Ctrl+`) and run:
```bash
npm install
```
This will download all dependencies (React, Vite, TanStack Query, Zustand, etc.) into `node_modules`.

### 3. npm run dev
Start the local development server:
```bash
npm run dev
```

### 4. Verify Vite & Browser
- The terminal should display: `VITE vX.X.X  ready in X ms`
- Navigate to `http://localhost:5173` in your web browser.

### 5. Check Console
Open Chrome/Edge Developer Tools (F12) > **Console**.
- Look for any React runtime errors or warnings.
- Confirm Zustand stores and React Router initialized without error.

### 6. Check Network
Go to the **Network** tab in Developer Tools.
- Verify that API requests (e.g., fetching the current user) are successfully reaching `http://localhost:8080`.
- Verify the WebSocket/STOMP connection (`ws://localhost:8080/ws`) upgrades to `101 Switching Protocols` successfully.

---

## SECTION 3: Database Verification

### How to Verify
Use a database client like MySQL Workbench, DBeaver, or the CLI.

### 1. Database Exists
Run `SHOW DATABASES;` and verify `relay_db` is present. Connect to it: `USE relay_db;`

### 2. Flyway Executed
Run:
```sql
SELECT * FROM flyway_schema_history;
```
Verify that scripts up to `V20__add_performance_indexes.sql` are marked as `Success`.

### 3. Tables Created
Run `SHOW TABLES;`. You should see (among others):
`workspaces`, `users`, `channels`, `messages`, `tasks`, `files`, `notifications`, `search_documents`.

### 4. Indexes
To verify indexes (e.g., on the messages table):
```sql
SHOW INDEX FROM messages;
```

### 5. Sample SQL Queries
- Check for users: `SELECT * FROM users;`
- Check workspaces: `SELECT * FROM workspaces;`

---

## SECTION 4: Authentication

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| AUTH-01 | Auth | Register | Fill registration form with valid data. Submit. | User is created in DB. Redirected to login/dashboard. | | |
| AUTH-02 | Auth | Duplicate Email | Register with an email already in `users` table. | Validation error: "Email already exists". | | |
| AUTH-03 | Auth | Weak Password | Register with a password < 8 characters. | Validation error: "Password too weak". | | |
| AUTH-04 | Auth | Invalid Email | Register with `test@com` or `test`. | Validation error: "Invalid email format". | | |
| AUTH-05 | Auth | Login | Submit valid email and password. | JWT is generated. User redirected to Dashboard. | | |
| AUTH-06 | Auth | Logout | Click user avatar -> Logout. | JWT is cleared from storage. Redirected to Login. | | |
| AUTH-07 | Auth | Refresh Token | Wait for JWT expiration, or simulate it. Make API call. | Refresh token is used to seamlessly get a new JWT. | | |
| AUTH-08 | Auth | Protected Routes | Navigate directly to `/dashboard` while logged out. | Redirected to Login page immediately. | | |
| AUTH-09 | Auth | Session Expiration | Delete refresh token from storage. Wait. | User is forcibly logged out and shown a session expired message. | | |
| AUTH-10 | Auth | Invalid JWT | Modify JWT payload in browser storage. Refresh. | Backend returns 401 Unauthorized. App redirects to login. | | |

---

## SECTION 5: Workspace Module

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| WS-01 | Workspace | Create | Click '+' in sidebar. Enter name/details. | Workspace created. User becomes Owner. UI updates. | | |
| WS-02 | Workspace | Update | Go to Workspace Settings. Change name. | Name updates in DB and reflects instantly in sidebar. | | |
| WS-03 | Workspace | Delete | As Owner, click Delete Workspace. Confirm. | Workspace soft/hard deleted. Redirected to default/empty state. | | |
| WS-04 | Workspace | Switch | Click a different workspace icon in sidebar. | Context changes. Channels, tasks, and members reload. | | |
| WS-05 | Workspace | Invite | Generate invite link or send email invite. | Invite record created in DB. Recipient can join workspace. | | |
| WS-06 | Workspace | Remove | Admin removes a user from Workspace. | User loses access immediately. Redirected if currently active. | | |
| WS-07 | Workspace | Transfer Ownership | Owner assigns Owner role to another admin. | Roles updated. Previous owner becomes Admin/Member. | | |
| WS-08 | Workspace | RBAC | Non-admin tries to access Workspace Settings. | "403 Forbidden" or UI element is hidden completely. | | |
| WS-09 | Workspace | Persistence | Switch workspace, refresh browser. | App remembers and loads the last active workspace. | | |

---

## SECTION 6: Channels

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| CH-01 | Channels | Public | Create a channel, toggle "Private" off. | Appears in channel browser for all workspace members. | | |
| CH-02 | Channels | Private | Create a channel, toggle "Private" on. Add User A. | Only Creator and User A can see/join the channel. | | |
| CH-03 | Channels | Archive | Admin clicks "Archive Channel". | Channel becomes read-only. Removed from active sidebar. | | |
| CH-04 | Channels | Restore | Admin clicks "Restore" on archived channel. | Channel returns to active state. Messaging enabled. | | |
| CH-05 | Channels | Delete | Admin clicks "Delete Channel". | Channel and messages permanently removed/soft-deleted. | | |
| CH-06 | Channels | Join | User clicks "Join" on a public channel. | Added to `channel_members`. Redirected to channel view. | | |
| CH-07 | Channels | Leave | User clicks "Leave Channel". | Removed from sidebar. No longer receives notifications. | | |
| CH-08 | Channels | Permissions | Non-admin tries to delete a channel. | Action blocked by backend. UI button is disabled/hidden. | | |

---

## SECTION 7: Messaging

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| MSG-01 | Messaging | Send | Type in chat box. Hit Enter. | Message appears instantly. Broadcasted via STOMP/WebSocket. | | |
| MSG-02 | Messaging | Receive | Open App in Incognito (User B). User A sends msg. | User B sees message instantly without refreshing. | | |
| MSG-03 | Messaging | Edit | Hover message > Edit. Change text. Save. | Text updates. "(edited)" label appears next to timestamp. | | |
| MSG-04 | Messaging | Delete | Hover message > Delete. | Message disappears for all users. | | |
| MSG-05 | Messaging | Restore | (If soft-delete implemented) Undo delete. | Message reappears in the chat flow. | | |
| MSG-06 | Messaging | Typing | User A types in chat box. | User B sees "User A is typing..." indicator. | | |
| MSG-07 | Messaging | Pagination | Load a channel with 100+ messages. | Initially loads ~20-50 messages. | | |
| MSG-08 | Messaging | Infinite Scroll | Scroll to the top of the message list. | Older messages load seamlessly via TanStack Query. | | |
| MSG-09 | Messaging | Auto Scroll | Receive a new message while at the bottom. | Scrollbar automatically pushes down to show new message. | | |
| MSG-10 | Messaging | Message Grouping | User A sends 3 messages back-to-back. | Avatar/Name only shows on the first message. | | |
| MSG-11 | Messaging | Hover Menu | Hover over a message. | Action bar appears (Emoji, Reply, Edit, Delete). | | |
| MSG-12 | Messaging | Context Menu | Right-click a message. | Context menu appears with message actions. | | |

---

## SECTION 8: Threads

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| THR-01 | Threads | Reply | Click "Reply in Thread" on a message. | Thread sidebar opens. | | |
| THR-02 | Threads | Reply Counter | Send a message in the thread. | Parent message shows "1 reply" in the main channel view. | | |
| THR-03 | Threads | Thread Panel | Open thread panel. | Displays parent message and all chronological replies. | | |
| THR-04 | Threads | Pagination | Scroll up in a long thread. | Older thread replies load via infinite scroll. | | |
| THR-05 | Threads | Synchronization | Open thread in Window A. Reply in Window B. | Window A updates instantly via WebSocket. | | |

---

## SECTION 9: Collaboration

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| COL-01 | Collab | Emoji | Click reaction icon, select 👍. | Reaction appears under message. Counter increments. | | |
| COL-02 | Collab | Mentions | Type `@username` in chat. Submit. | Name is highlighted. Mentioned user gets a notification. | | |
| COL-03 | Collab | Pinned | Hover message > Pin to Channel. | Message appears in "Pinned Items" section of channel info. | | |
| COL-04 | Collab | Saved | Click "Save for later" on a message. | Message is added to user's personal "Saved Items" list. | | |
| COL-05 | Collab | Read Receipts | User B views channel. | Message status changes to 'Read' / checkmarks update. | | |
| COL-06 | Collab | Unread | User B has app closed. User A sends msg. | Channel in User B's sidebar becomes bold with a badge count. | | |
| COL-07 | Collab | Notifications | Receive a mention while tab is unfocused. | Browser push notification appears (if permissions granted). | | |

---

## SECTION 10: File Sharing

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| FS-01 | Files | Upload | Drag and drop a file into chat box. Send. | File uploads to Storage. Attachment renders in chat. | | |
| FS-02 | Files | Download | Click on an uploaded file. | File downloads to local machine successfully. | | |
| FS-03 | Files | Preview | Upload an image. | Image thumbnail renders inline in the message. | | |
| FS-04 | Files | PDF | Upload a PDF document. | PDF icon/thumbnail shows. Clicking opens preview/download. | | |
| FS-05 | Files | Multiple Upload | Select 3 files at once in the upload dialog. | All 3 files upload and attach to a single message. | | |
| FS-06 | Files | Large Files | Upload a file exceeding max size (e.g., 50MB+). | Graceful error: "File too large. Maximum size is X MB." | | |
| FS-07 | Files | Failure Recovery | Disconnect internet mid-upload. | Upload fails cleanly. Error message displayed to user. | | |

---

## SECTION 11: Notifications

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| NOT-01 | Notifs | Bell | Look at top navigation bar. | Bell icon shows unread count badge. | | |
| NOT-02 | Notifs | Dropdown | Click the Bell icon. | Dropdown list of recent notifications appears. | | |
| NOT-03 | Notifs | Read | Click on an unread notification. | Navigates to target (message/task). Badge count decrements. | | |
| NOT-04 | Notifs | Delete | Click "Clear" or "Delete" on a notification. | Item is removed from the notification list. | | |
| NOT-05 | Notifs | Grouping | Receive 5 messages in one channel while away. | Notification grouped: "5 new messages in #general". | | |
| NOT-06 | Notifs | Realtime | Keep dropdown open. Receive a mention. | New notification dynamically appears at top of the list. | | |

---

## SECTION 12: Global Search

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| SRC-01 | Search | Messages | Search for a specific word sent yesterday. | Relevant messages returned in search results dialog. | | |
| SRC-02 | Search | Tasks | Search for "Q3 Report" task name. | Task appears in results under the "Tasks" tab/section. | | |
| SRC-03 | Search | Channels | Search for "marketing". | #marketing channel appears. Clicking it joins/opens it. | | |
| SRC-04 | Search | Users | Search for "John". | John's profile appears. Clicking opens DM. | | |
| SRC-05 | Search | Files | Search for "budget.pdf". | File appears in results. | | |
| SRC-06 | Search | Keyboard Nav | Use Up/Down arrows in search results. | Focus moves between results. Enter selects item. | | |
| SRC-07 | Search | Cmd/Ctrl + K | Press Cmd+K (Mac) or Ctrl+K (Windows). | Global search modal opens instantly. Focus in input. | | |
| SRC-08 | Search | Highlighting | Search for "deploy". | The word "deploy" is highlighted bold in the result snippet. | | |
| SRC-09 | Search | Permissions | Search for a word only present in a Private Channel. | User not in the channel gets NO results for that word. | | |

---

## SECTION 13: Task Management

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| TSK-01 | Tasks | Create | Open Tasks. Click "New Task". Fill details. | Task created and appears on the Kanban board / list. | | |
| TSK-02 | Tasks | Edit | Click a task. Change description and due date. | Changes saved to DB. UI reflects new data. | | |
| TSK-03 | Tasks | Delete | Open Task Menu > Delete. | Task removed from board. | | |
| TSK-04 | Tasks | Drag Drop | Drag a task from "To Do" to "In Progress". | Column updates. Status change persists on page refresh. | | |
| TSK-05 | Tasks | Labels | Add "Urgent" label (red color) to a task. | Label renders on task card visually. | | |
| TSK-06 | Tasks | Assignees | Assign task to User B. | User B's avatar appears on task. | | |
| TSK-07 | Tasks | Comments | Open task. Add a comment in activity feed. | Comment saved and visible to other users opening task. | | |
| TSK-08 | Tasks | Activity | Change task status. | Audit log in task says "User A moved task to In Progress". | | |
| TSK-09 | Tasks | Message2Task | Hover message > "Create Task". | Modal opens with message pre-filled as description. | | |
| TSK-10 | Tasks | Kanban | View default board. | Columns render correctly. Scrollable horizontally if many. | | |
| TSK-11 | Tasks | Search | Use filter/search bar inside Tasks module. | Board filters down to matching tasks immediately. | | |
| TSK-12 | Tasks | Notifications | Assign a task to User B. | User B receives an in-app notification of assignment. | | |

---

## SECTION 14: Dashboard

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| DSH-01 | Dashboard| Widgets | Load Dashboard page. | Widgets (Task Stats, Recent Messages) render data. | | |
| DSH-02 | Dashboard| Timeline | View activity timeline widget. | Shows chronological list of recent workspace events. | | |
| DSH-03 | Dashboard| Metrics | Complete a task. Return to dashboard. | "Tasks Completed" metric increments by 1. | | |
| DSH-04 | Dashboard| Quick Actions| Click "New Channel" from Dashboard widget. | Modal opens bypassing sidebar navigation. | | |
| DSH-05 | Dashboard| Realtime | Keep dashboard open while someone else posts. | Activity timeline updates via WebSocket. | | |

---

## SECTION 15: Responsive Design

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| RES-01 | UI/UX | Desktop | Maximize browser window (1080p). | Sidebar fixed, main content area fills screen. No overlap. | | |
| RES-02 | UI/UX | Tablet | Resize window to ~768px width. | Sidebar may become collapsible. Fonts scale appropriately. | | |
| RES-03 | UI/UX | Mobile | Resize window to ~375px (or use DevTools device).| Hamburger menu replaces sidebar. Chat elements stack vertically. | | |

---

## SECTION 16: Performance Testing

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| PERF-01| Perf | Large Messages| Paste 50,000 characters of "Lorem Ipsum" and send. | Handled gracefully. Truncated with "Read More" or error if too big. | | |
| PERF-02| Perf | Large Tasks | Create a task board with 1,000 tasks. | Initial load < 2 seconds (using pagination/virtualization). | | |
| PERF-03| Perf | Many Files | Open a channel with 500 image attachments. | Images lazy-load. Page does not freeze on scroll. | | |
| PERF-04| Perf | Many Channels | A workspace has 200 channels. | Sidebar scrolls smoothly. Channel switching is near-instant. | | |
| PERF-05| Perf | Notifications | User has 10,000 unread notifications. | Notification bell query is fast. Dropdown loads instantly. | | |
| PERF-06| Perf | Browser Refresh| Hard refresh (Ctrl+F5) in the middle of a chat. | App reloads state from local storage/API < 1 second. | | |
| PERF-07| Perf | Reconnect | Stop backend server for 5s, start again. | Frontend attempts reconnect. Recovers WebSocket seamlessly. | | |

---

## SECTION 17: Security Testing

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| SEC-01 | Security | Unauthorized | Access `/api/v1/workspaces` via Postman without token. | 401 Unauthorized response. | | |
| SEC-02 | Security | Priv Channels | Try to hit the channel messages API for a private channel you don't belong to. | 403 Forbidden response. | | |
| SEC-03 | Security | RBAC | Call admin-only endpoints with a standard user token. | 403 Forbidden response. | | |
| SEC-04 | Security | Task Perms | Try to delete a task in a workspace you aren't a member of. | 403 Forbidden response. | | |
| SEC-05 | Security | WS Perms | Try to fetch Workspace A's data using an ID, while logged into Workspace B (and not a member of A). | 403 Forbidden / 404 Not Found. | | |
| SEC-06 | Security | File Perms | Attempt to access the direct download URL for a file in a private channel you don't have access to. | 403 Forbidden response. | | |

---

## SECTION 18: Browser Testing

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Pass/Fail |
|---|---|---|---|---|---|---|
| BROW-01| Browser| Chrome | Run through core flow (login, chat, tasks) in Chrome. | All UI renders correctly. No console errors. | | |
| BROW-02| Browser| Edge | Run through core flow in Microsoft Edge. | All UI renders correctly. WebSocket connects successfully. | | |
| BROW-03| Browser| Firefox | Run through core flow in Mozilla Firefox. | Flexbox layouts hold. CSS scrollbars function correctly. | | |

---

## SECTION 19: Bug Tracking Sheet

When executing the manual tests above, log all failures using the format below. 

### QA Checklist & Bug Log

| Test ID | Module | Feature | Steps | Expected Result | Actual Result | Status | Priority | Comments |
|---------|--------|---------|-------|-----------------|---------------|--------|----------|----------|
| e.g. AUTH-04 | Auth | Invalid Email | Submit test@test | Error validation | Allowed registration | FAIL | High | Regex for email validation is missing on backend |
| | | | | | | | | |
| | | | | | | | | |
| | | | | | | | | |
| | | | | | | | | |

***End of Document***
