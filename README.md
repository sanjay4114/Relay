# Relay

Enterprise collaboration platform — messaging, tasks, and docs in one workspace.

Backend and frontend are **independent projects**. They communicate only through REST APIs and WebSocket.

## Stack

| Layer | Technology | IDE |
|-------|------------|-----|
| Backend | Java 21, Spring Boot 3, MySQL 8, Flyway, JWT | Eclipse Spring Tool Suite |
| Frontend | React (Vite), TypeScript, Tailwind, shadcn/ui | Cursor / VS Code |
| Database | Local MySQL (`relay` on `localhost:3306`) | Your machine |

## Repository layout

```
relay/
├── backend/           # Standalone Maven Spring Boot project (Eclipse STS)
├── frontend/          # Standalone React Vite project (Cursor / VS Code)
├── docker/            # Optional production deployment (not required for dev)
└── README.md
```

## Quick start (local development)

### 1. MySQL database

Ensure **MySQL 8** is running locally, then create the database:

```sql
CREATE DATABASE relay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Or run the helper script:

```bash
mysql -u root -p < backend/scripts/init-local-database.sql
```

### 2. Configure database credentials

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/relay?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

Flyway runs migrations automatically when the backend starts.

### 3. Backend — Eclipse STS

1. **File → Import → Maven → Existing Maven Projects**
2. Select the `backend` folder
3. Open `RelayApplication.java` → **Run As → Spring Boot App**

See [backend/README.md](backend/README.md) for detailed STS setup.

API: http://localhost:8080

### 4. Frontend — Cursor / VS Code

```bash
cd frontend
npm install
npm run dev
```

App: http://localhost:5173

Configure API URLs in `frontend/.env`:

```
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_WS_URL=http://localhost:8080/ws
```

## Development workflow

| Task | Where |
|------|-------|
| REST API, auth, business logic | Eclipse STS → `backend/` |
| UI, routing, API client | Cursor / VS Code → `frontend/` |
| Database | Local MySQL (`relay`) |

No Docker is required for local development.

## Optional: production deployment with Docker

For production or containerized environments, see [docker/README.md](docker/README.md).

## Documentation

- [backend/README.md](backend/README.md) — Eclipse import, MySQL config, run instructions
- [docs/DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md) — MySQL schema
- [docs/SCAFFOLD.md](docs/SCAFFOLD.md) — Project structure reference

## License

Private — portfolio project.
