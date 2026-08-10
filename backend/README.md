# Relay API (Backend)

Standalone Maven Spring Boot project. Runs independently of the frontend — the web app communicates via REST (`/api/v1`) and WebSocket (`/ws`).

## Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK | 21 |
| Eclipse | Spring Tool Suite 4 (or Eclipse + Spring Tools) |
| Maven | 3.9+ (embedded in STS) |
| MySQL | 8.x (local installation) |

Docker is **not** required for development.

## Local MySQL setup

### 1. Create the database

```sql
CREATE DATABASE relay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Or run:

```bash
mysql -u root -p < scripts/init-local-database.sql
```

### 2. Configure credentials

Edit `src/main/resources/application.properties`:

| Property | Value |
|----------|-------|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/relay?...` (default) |
| `spring.datasource.username` | Your MySQL username |
| `spring.datasource.password` | Your MySQL password |

### 3. Schema migrations

Flyway is enabled. On startup, migrations in `src/main/resources/db/migration/` run automatically against the `relay` database. Hibernate is set to `validate` — it does not auto-create tables.

## Import into Eclipse STS

1. Open **Spring Tool Suite** (or Eclipse with Spring Tools installed).
2. **File → Import → Maven → Existing Maven Projects**
3. **Root Directory:** select the `backend` folder (the directory containing `pom.xml`).
4. Ensure `relay-api` is checked → **Finish**
5. Wait for Maven dependency download and project build to complete.

### Lombok

If Eclipse reports errors on `@Getter`, `@RequiredArgsConstructor`, etc.:

1. Download [lombok.jar](https://projectlombok.org/download)
2. Run `java -jar lombok.jar` and select your Eclipse installation
3. Restart Eclipse

### Java 21

**Window → Preferences → Java → Installed JREs** — ensure JDK 21 is selected.

## Run the application

1. Open `src/main/java/com/relay/RelayApplication.java`
2. Right-click → **Run As → Spring Boot App**

Or use the **Boot Dashboard** → select `relay-api` → **Start**.

### Verify

| URL | Purpose |
|-----|---------|
| http://localhost:8080/actuator/health | Spring Actuator health |
| http://localhost:8080/api/v1/health | API health |

### Port 8080 already in use

If startup fails with `Port 8080 was already in use`, a previous run is still active.

**Windows (PowerShell):**
```powershell
netstat -ano | findstr :8080
taskkill /PID <pid> /F
```

Or stop the existing **Spring Boot App** / **Boot Dashboard** instance in STS before starting again.

Alternatively, change the port in `application.properties`:
```properties
server.port=8081
```

### Eclipse compilation errors (red X on packages)

If packages like `common.dto`, `common.exception`, or `config.security` show errors:

1. **Project → Clean…** → select `relay-api`
2. **Right-click project → Maven → Update Project…** → OK
3. Ensure **JDK 21** is configured (Project Properties → Java Build Path)
4. For remaining Lombok errors on entity classes: install [Lombok for Eclipse](https://projectlombok.org/setup/eclipse) and restart STS

The `config`, `common.dto`, and `common.exception` packages use plain Java (no Lombok) so they compile without the Lombok plugin.

## Configuration reference

All local settings: `src/main/resources/application.properties`

| Property | Purpose |
|----------|---------|
| `spring.datasource.*` | Local MySQL connection |
| `spring.flyway.*` | Database migrations |
| `relay.cors.allowed-origins` | Frontend dev server (`http://localhost:5173`) |
| `relay.auth.frontend-url` | Password-reset email links |
| `spring.mail.*` | Optional SMTP for password-reset emails |

## Project structure

```
backend/
├── pom.xml
├── scripts/init-local-database.sql
└── src/
    ├── main/
    │   ├── java/com/relay/
    │   └── resources/
    │       ├── application.properties
    │       └── db/migration/
    └── test/
```

## Maven commands (optional)

```bash
mvn clean package
mvn test
java -jar target/relay-api-0.1.0-SNAPSHOT.jar
```

## Frontend

The React frontend is in `../frontend`. Run with `npm run dev` — it connects to `http://localhost:8080`.

## Production deployment

Optional Docker-based deployment: see [../docker/README.md](../docker/README.md).
