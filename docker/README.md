# Optional production deployment

Docker is **not used for local development**. Use your local MySQL installation and run the backend from Eclipse STS.

These files are for optional containerized production or staging deployments.

## Contents

| File | Purpose |
|------|---------|
| `docker-compose.yml` | MySQL, MinIO, Mailpit, and API services |
| `Dockerfile` | Backend JAR image |
| `application-docker.properties` | Spring `docker` profile in `backend/src/main/resources/` |

## Usage

From the repository root:

```bash
docker compose -f docker/docker-compose.yml up --build -d
```

Set environment variables in `docker/docker-compose.yml` or an `.env` file before deploying:

- `RELAY_JWT_SECRET` — strong secret (256+ bits)
- `MYSQL_ROOT_PASSWORD` / database credentials
- `RELAY_CORS_ORIGINS` — production frontend URL

## Local development

Do not use this stack for day-to-day development. Instead:

1. Create database `relay` on local MySQL
2. Configure `backend/src/main/resources/application.properties`
3. Run `RelayApplication` in Eclipse STS

See [../README.md](../README.md) and [../backend/README.md](../backend/README.md).
