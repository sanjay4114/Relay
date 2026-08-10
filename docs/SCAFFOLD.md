# Relay Project Scaffold

## Repository layout

```
relay/
├── README.md
├── backend/                    # Standalone Maven Spring Boot (Eclipse STS)
│   ├── pom.xml
│   ├── README.md
│   ├── scripts/init-local-database.sql
│   └── src/
│       ├── main/resources/application.properties
│       └── main/resources/db/migration/
├── frontend/                   # Standalone React (Vite) — Cursor / VS Code
└── docker/                     # Optional production deployment (not for local dev)
    ├── docker-compose.yml
    ├── Dockerfile
    └── README.md
```

## How to run (local development)

| Project | IDE | Steps |
|---------|-----|-------|
| Database | Local MySQL | Create `relay` DB → set credentials in `application.properties` |
| Backend | Eclipse STS | Import `backend/` → Run `RelayApplication` as Spring Boot App |
| Frontend | Cursor / VS Code | `cd frontend && npm run dev` |

Flyway migrations run on backend startup. Docker is **not** required.

## Optional production

```bash
docker compose -f docker/docker-compose.yml up --build -d
```

See [docker/README.md](../docker/README.md).

## Database

See [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) for ER diagram and migration index.
