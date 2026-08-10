# Relay Development Journal

## Phase 7: Notification System (Current)

**Objectives Completed:**
- Centralized real-time notification system across the platform.

### Database
- Created Flyway migration `V17__add_notifications.sql`.
- Added `notifications` table with fields `id`, `public_id`, `recipient_id`, `actor_id`, `type`, `title`, `body`, `entity_type`, `entity_public_id`, `is_read`, `created_at`, `read_at`.
- Established indexes for performance.

### Backend (Spring Boot)
- Verified `NotificationService`, `NotificationRepository`, and `NotificationController` implementation.
- Added event triggers for:
  - **Mentions**: Broadcasts `NotificationType.MENTION` when parsing `@username`.
  - **Thread Replies**: Triggers `NotificationType.THREAD_REPLY` when a message has a parent.
  - **Message Reactions**: Added `NotificationType.MESSAGE_REACTION`.
  - **Workspace Invitations**: Integrated into `WorkspaceMemberService` as `WORKSPACE_INVITE`.
  - **Channel Invitations**: Integrated into `ChannelService` as `CHANNEL_INVITE`.
  - **File Uploads**: Sends `FILE_UPLOAD` when attachments are present.
- Updated `NotificationController` with Swagger/OpenAPI `@Operation` and `@Tag` annotations.
- Added `springdoc-openapi-starter-webmvc-ui` dependency to `pom.xml` for Swagger UI.

### WebSockets
- Hooked up `SimpMessagingTemplate` in `NotificationService`.
- Broadcasts real-time events to `/user/queue/notifications`.
- Supported event types: `NOTIFICATION_CREATED`, `NOTIFICATION_READ`, `NOTIFICATIONS_ALL_READ`, `NOTIFICATION_DELETED`.

### Frontend (React/Vite)
- Validated `use-notifications.ts` custom hook for websocket integration with React Query.
- `NotificationBell` component added with unread badge and dropdown popover.
- `NotificationsPage` displays comprehensive notification history grouped by "Today", "Yesterday", and "Earlier".
- End-to-end functionality implemented for reading and deleting notifications.

### Testing & Verification Checklist
- [x] Multiple browser tabs synchronized via WebSocket and React Query.
- [x] Multiple user isolation maintained using Spring Security context and `recipient_id`.
- [x] Read synchronization functional (`markAllAsRead` / `markAsRead`).
- [x] Cross-entity support (Mentions, Threads, Files, Workspaces, Channels).

---

*Status: Ready for Task Management Phase.*

---

## Phase 8: Global Search (Current)

**Objectives Completed:**
- Unified search experience across Messages, Channels, Users, Workspaces, and Files.

### Database
- Created Flyway migration `V18__add_fulltext_indexes.sql`.
- Added MySQL `FULLTEXT` indexes to `messages.content`, `channels.name`, `users.display_name`, `workspaces.name`, and `file_attachments.original_name`.

### Backend (Spring Boot)
- Created `SearchService` and `SearchProvider` interfaces.
- Implemented `MessageSearchProvider`, `ChannelSearchProvider`, `UserSearchProvider`, `WorkspaceSearchProvider`, and `FileSearchProvider`.
- Leveraged `JdbcClient` in `SearchRepository` for high-performance `MATCH() AGAINST()` native boolean mode queries.
- Created `SearchController` with unified `GET /api/v1/search` endpoint supporting pagination and type-filtering.
- Implemented workspace and channel membership authorization filtering directly inside the native SQL joins.

### Frontend (React/Vite)
- Added `searchApi` and `useGlobalSearch` React Query hook.
- Implemented `GlobalSearchDialog` providing a `Cmd+K` command palette interface.
- Added visual highlighting for matching text using `dangerouslySetInnerHTML`.
- Grouped results with specific icons for each entity type.
- Fully supports keyboard navigation and integrated into the `AppHeader` across the application.

### Testing & Verification Checklist
- [x] Authorization: users cannot see messages or files from private channels they don't belong to.
- [x] Highlighting: Search keywords are highlighted in yellow on the frontend.
- [x] Performance: Uses native MySQL fulltext indexes instead of slow LIKE queries.
- [x] Keyboard Navigation: Cmd+K opens the modal instantly.

---

*Status: Completed & Ready for Review.*

---

## Phase 9: Task Management (Current)

**Objectives Completed:**
- Fully integrated Kanban Task Board into the Relay collaboration platform.
- The interface features a sleek, Linear-inspired dark + teal design.

### Database
- Created Flyway migration `V19__add_tasks.sql`.
- Configured complex schema including `tasks`, `task_comments`, `task_labels`, `task_assignees`, and `task_activity` tables.

### Backend (Spring Boot)
- Created `Task` domain entities and enums (`TaskStatus`, `TaskPriority`, `TaskActivityType`).
- Implemented `TaskRepository`, `TaskCommentRepository`, `TaskActivityRepository`, and `LabelRepository`.
- Built `TaskService` handling business logic: creation, updates, deletions, assignments, label association, and comprehensive activity logging.
- Automatically generates robust system notifications for Task Assignments.
- Created `TaskController` exposing workspace-bound task endpoints (`GET`, `POST`, `PATCH`, `DELETE`) with full Swagger documentation.
- Integrated tasks into Global Search by adding `TaskSearchProvider` and updating `SearchRepository` with native MySQL `MATCH()` fulltext indexing.

### Frontend (React/Vite)
- Developed robust React Query hooks (`useWorkspaceTasks`, `useCreateTask`, `useUpdateTask`) with Optimistic UI updates for ultra-fast drag-and-drop.
- Engineered a pristine Kanban `TaskBoard` featuring distinct columns (To Do, In Progress, In Review, Done).
- Created `TaskCard` component that displays priorities, story points, multiple assignees, colored tags, and a linked message indicator.
- Designed a polished `CreateTaskModal` with strict form validation via `zod`.
- **Core Workflow Integration:** Modified `MessageBubble.tsx` to include an inline context menu option: `"Create Task"`. Selecting it automatically passes the message context into the modal, establishing a hard bidirectional link between chat and workflow!

### Testing & Verification Checklist
- [x] Board: Columns render correctly, Drag-and-drop visually functions using HTML5 Drag events.
- [x] Context Menu: Messages can be converted to Tasks directly from the chat pane.
- [x] Persistence: Task updates correctly trigger the API and invalidate caches.
- [x] Global Search: The Command Palette (`Cmd+K`) now indexes and navigates directly to Tasks.

---

*Status: Completed & Ready for Review.*

---

## Phase 10: Dashboard (Current)

**Objectives Completed:**
- Created a beautiful, highly informative dashboard summarizing everything in the workspace.
- Kept the system lightweight by reusing existing frontend modules and utilizing native SQL aggregations to minimize overhead.

### Backend (Spring Boot)
- Created a dedicated `DashboardController` (`/api/v1/workspaces/{workspaceId}/dashboard`).
- Implemented an ultra-performant `DashboardRepository` utilizing `JdbcClient`.
- **Timeline Endpoint:** Utilized a massive `UNION ALL` SQL query to chronologically aggregate Messages, Task Updates, and File Uploads natively at the database level, ensuring lightning-fast performance across thousands of records.
- **Stats Endpoint:** Utilized subqueries to concurrently compute live metrics: Active Tasks, Overdue Tasks, Tasks Due Today, Unread Messages, Active Channels, and Online Members.

### Frontend (React/Vite)
- Fully redesigned `DashboardPage.tsx` using `framer-motion` for smooth entrance animations.
- Engineered dynamic metric cards (Widgets) featuring:
    - Task stats (Active, Due Today, Overdue).
    - Engagement stats (Unread Messages, Active Channels, Online Members).
- Developed a **Unified Activity Timeline** featuring vertical chronological tracking with custom icons, displaying cross-module events in a cohesive feed.
- Added **Quick Actions** sidebar natively integrating our previously built `CreateTaskModal` and `CreateChannelModal`.
- Setup automatic polling using React Query (`refetchInterval`) to keep metrics live.

### Testing & Verification Checklist
- [x] Stats render properly and map accurately from backend calculations.
- [x] Timeline merges multiple data sources perfectly.
- [x] UI is fully responsive (Desktop/Tablet/Mobile).

---

## Phase 11: Engineering Excellence (Current)

**Objectives Completed:**
- Conducted a comprehensive audit of Security, Performance, Observability, and Test Coverage prior to production release.

### Backend Updates
- **Performance (N+1 Query Resolution):** Discovered and fixed a critical memory leak warning in Spring Data JPA paginated queries (`"firstResult/maxResults specified with collection fetch; applying in memory"`). Removed `JOIN FETCH` from collection relationships and replaced them with `@BatchSize(size = 50)` on `Task.assignees`, `Task.labels`, `Message.reactions`, `Message.mentions`, and `Message.attachments`.
- **Database Indexing:** Created Flyway Migration `V20__add_performance_indexes.sql` to apply highly targeted composite indexes mapping directly to high-throughput queries (e.g., `(channel_id, created_at)` for message pagination, `(workspace_id, status)` for task filtering).
- **Security:** Hardened `ChannelController` by removing untyped `Map<String, String>` payloads and replacing them with a strictly validated `@Valid` `InviteUserRequest` DTO.
- **Observability:** Integrated `spring-boot-starter-actuator` and `micrometer-registry-prometheus`. Configured `application.properties` to expose `/actuator/prometheus`, `/actuator/health`, and `/actuator/metrics` on a dedicated management port (8081).
- **Testing:** Authored comprehensive Mockito unit tests for `TaskService` core business logic, ensuring authorization and data mapping behaves strictly as expected.

### Artifact Generated
- `engineering_review_report.md` artifact finalized and delivered.

---

## Phase 12: Production Deployment & DevOps (Current)

**Objectives Completed:**
- Prepared the Relay platform for enterprise-grade production deployment by fully containerizing the stack and establishing robust CI/CD, observability, and infrastructure-as-code principles.

### DevOps & Infrastructure Setup
- **Dockerization:** Created multi-stage `Dockerfile`s for the Spring Boot backend and the React/Vite frontend. Engineered a comprehensive `docker-compose.prod.yml` orchestrating `backend`, `frontend`, `mysql`, `minio`, `nginx`, `prometheus`, and `grafana`.
- **Nginx Reverse Proxy:** Configured a global `nginx.conf` handling static asset caching, gzip compression, HTTP/WebSocket (STOMP) routing, and enforcing critical security headers (X-Frame-Options, XSS-Protection).
- **Environment Management:** Formalized external configuration through `.env.example`, `application-dev.yml`, and `application-prod.yml`.
- **CI/CD:** Established an automated GitHub Actions pipeline (`.github/workflows/ci.yml`) triggering on `main` pushes to run parallel testing across Node and Java before building final Docker artifacts.
- **Observability Stack:** Connected Spring Boot Actuator to Prometheus (`prometheus.yml`) allowing Grafana to scrape critical health, JVM, and application metrics on port 8081.
- **Logging Strategy:** Transformed root and application logs into structured JSON format in `application-prod.yml` appending distributed tracing vectors like `requestId` and `userId`.

### Artifact Generated
- `relay_production_operations_guide.md` (Contains architectural diagrams, backup strategies, pre-flight checklists, and troubleshooting runbooks).

---

## Phase 12.5: Quality Assurance, Bug Fixing & UI Polish (Current)

**Objectives Completed:**
- Conducted exhaustive End-to-End reviews of all features (Auth, Workspaces, Channels, Messaging, Files, Tasks, Notifications, Search, Dashboard).
- Evaluated Responsive Design, Accessibility (WCAG), Performance Metrics, and Global Error Handling mechanisms.

### Fixes & Polish Implemented
- **Global Error Handling:** Authored `ErrorBoundaryPage.tsx` and bound it to React Router (`index.tsx`) `errorElement` nodes to gracefully intercept component crashes and prevent "White Screen of Death" scenarios.
- **Verification:** Verified recent N+1 query fixes and new SQL indexes drastically lowered `Largest Contentful Paint` (LCP) and `Time to First Byte` (TTFB) during heavy Dashboard and Messaging loads.
- **Accessibility Check:** Validated that Radix UI primitives maintain proper `aria-` labels, Dialogs trap focus, and keyboard navigation is unimpeded.

### Artifacts Generated
- `qa_report.md`
- `bug_report.md`
- `ui_polish_report.md`
- `performance_report.md`
- `accessibility_report.md`

---

*Status: Ready for Version 1.0 Launch & Portfolio Documentation.*
